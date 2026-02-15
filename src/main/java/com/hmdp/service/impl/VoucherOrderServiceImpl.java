package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.config.KafkaConfig;
import com.hmdp.dto.Result;
import com.hmdp.dto.SeckillMessage;
import com.hmdp.dto.SeckillStockInfo;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.CacheManager;
import com.hmdp.utils.constants.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements IVoucherOrderService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private KafkaTemplate<String,Object> kafkaTemplate;
    @Resource
    private IVoucherService voucherService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private CacheManager cacheManager;

    // 定义引入Lua脚本
    private static final DefaultRedisScript<Long> ADVANCED_SECKILL_SCRIPT;
    static {
        ADVANCED_SECKILL_SCRIPT = new DefaultRedisScript<>();
        ADVANCED_SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill_advanced.lua"));
        ADVANCED_SECKILL_SCRIPT.setResultType(Long.class);
    }
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
    private volatile boolean isRunning = true;

    @PreDestroy
    public void stop() {
        isRunning = false;
    }

    @PostConstruct
    private void init() {
        // 启动异步处理线程
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
        }
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");

        // 执行异步lua脚本（包含时间检查）
        Long result = stringRedisTemplate.execute(
                ADVANCED_SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId));

        int r = result.intValue();
        // 判断结果
        switch (r) {
            case 1:
                return Result.fail("库存不足");
            case 2:
                return Result.fail("不能重复下单");
            case 3:
                return Result.fail("秒杀信息不存在");
            case 4:
                return Result.fail("秒杀尚未开始");
            case 5:
                return Result.fail("秒杀已经结束");
            default:
                // 发送消息到kafka
                SeckillMessage seckillMessage = new SeckillMessage(userId, voucherId, orderId);
                kafkaTemplate.send(KafkaConfig.SECKILL_ORDER_TOPIC, seckillMessage);
                // 返回订单id
                return Result.ok(orderId);
        }
    }

    @Override
    public void createVoucherOrder(SeckillMessage message) {
        Long userId = message.getUserId();
        Long voucherId = message.getVoucherId();
        Long orderId = message.getOrderId();

        // 创建锁对象（使用订单ID作为锁key，避免用户重复下单问题）
        RLock redisLock = redissonClient.getLock("lock:order:" + orderId);
        // 尝试获取锁（等待3秒，10秒后自动释放）
        boolean isLock = false;
        try {
            isLock = redisLock.tryLock(3, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("获取锁失败", e);
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        if (!isLock) {
            // 获取锁失败
            log.error("获取分布式锁失败，订单ID:{}", orderId);
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        try {
            // 再次检查数据库中是否已存在订单（防止重复消费）
            int count = query().eq("id", orderId).count();
            if (count > 0) {
                log.warn("订单已存在，订单ID:{}", orderId);
                return;
            }

            // 查询订单（检查用户是否已购买）
            count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                log.warn("用户已购买过该优惠券，用户ID:{}, 优惠券ID:{}", userId, voucherId);
                return;
            }

            // 扣减数据库库存
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    .gt("stock", 0)
                    .update();
            if (!success) {
                log.error("数据库库存不足，优惠券ID:{}", voucherId);
                throw new RuntimeException("库存不足");
            }

            // 创建订单
            VoucherOrder voucherOrder = new VoucherOrder();
            voucherOrder.setId(orderId);
            voucherOrder.setUserId(userId);
            voucherOrder.setVoucherId(voucherId);
            boolean saveResult = save(voucherOrder);
            if (!saveResult) {
                log.error("创建订单失败，订单ID:{}", orderId);
                throw new RuntimeException("订单创建失败");
            }
            // 异步更新缓存（发送消息到Kafka），避免阻塞主流程
            CompletableFuture.runAsync(() -> {
                try {
                    // 查询最新的库存信息
                    SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
                    if (seckillVoucher != null) {
                        // 更新库存缓存
                        SeckillStockInfo stockInfo = new SeckillStockInfo(
                                seckillVoucher.getStock(),
                                seckillVoucher.getBeginTime(),
                                seckillVoucher.getEndTime()
                        );
                        cacheManager.updateCacheWithMessage(
                                RedisConstants.SECKILL_STOCK_KEY + voucherId,
                                stockInfo,
                                30L, // 30秒TTL
                                TimeUnit.MINUTES
                        );

                        // 更新优惠券信息缓存
                        Voucher voucher = voucherService.getById(voucherId);
                        if (voucher != null) {
                            cacheManager.updateCacheWithMessage(
                                    "voucher:" + voucherId,
                                    voucher,
                                    60L, // 60秒TTL
                                    TimeUnit.MINUTES
                            );
                        }
                    }
                } catch (Exception e) {
                    log.error("异步更新缓存失败，voucherId:{}", voucherId, e);
                    // 发送缓存清理消息作为兜底
                    cacheManager.sendCacheCleanMessage(RedisConstants.SECKILL_STOCK_KEY + voucherId);
                    cacheManager.sendCacheCleanMessage("voucher:" + voucherId);
                }
            });

            log.info("秒杀订单创建成功，订单ID:{}", orderId);
        } finally {
            // 释放锁
            if (redisLock.isHeldByCurrentThread()) {
                redisLock.unlock();
            }
        }
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        voucherService.save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 保存秒杀库存信息到Redis中（包含完整信息）
        SeckillStockInfo stockInfo = new SeckillStockInfo(
                voucher.getStock(),
                voucher.getBeginTime(),
                voucher.getEndTime()
        );
        try {
            stringRedisTemplate.opsForValue().set(
                    RedisConstants.SECKILL_STOCK_KEY + voucher.getId(),
                    objectMapper.writeValueAsString(stockInfo)
            );
        } catch (JsonProcessingException e) {
            log.error("Redis存储秒杀信息失败", e);
            throw new RuntimeException("Redis存储秒杀信息失败");
        }
    }

}
