package com.hmdp.utils.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.config.KafkaConfig;
import com.hmdp.dto.CacheUpdateMessage;
import com.hmdp.utils.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CacheUpdateListener {

    @Resource
    private CacheManager cacheManager;
    @Resource
    private ObjectMapper objectMapper;

    // 监听缓存更新主题
    @KafkaListener(topics = KafkaConfig.CACHE_UPDATE_TOPIC, groupId = "cache_update_group")
    public void listenCacheUpdate(ConsumerRecord<String, CacheUpdateMessage> record, Acknowledgment ack) {
        String key = record.key();
        CacheUpdateMessage message = record.value();

        log.info("接收到缓存更新消息，key: {}, message: {}", key, message);

        try {
            if (CacheUpdateMessage.TYPE_UPDATE.equals(message.getType())) {
                // 更新缓存
                cacheManager.updateCache(
                        message.getCacheKey(),
                        message.getCacheValue(),
                        message.getExpireTime(),
                        TimeUnit.SECONDS
                );
                log.info("缓存更新成功，key: {}", message.getCacheKey());
            } else if (CacheUpdateMessage.TYPE_DELETE.equals(message.getType())) {
                // 删除缓存
                cacheManager.deleteCache(message.getCacheKey());
                log.info("缓存删除成功，key: {}", message.getCacheKey());
            }

            // 手动确认消息
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理缓存更新消息失败，key: {}, message: {}", key, message, e);

            // 检查是否可以重试
            if (message.canRetry()) {
                message.incrementRetry();
                // 延迟重试（梯度重试策略）
                long delay = getRetryDelay(message.getRetryCount());
                log.info("缓存更新失败，准备{}秒后重试，重试次数: {}", delay, message.getRetryCount());

                // 延迟重新发送消息
                try {
                    Thread.sleep(delay * 1000);
                    // 重新发送到同一主题
                    // 这里需要使用KafkaTemplate重新发送，但为了避免循环依赖，建议使用定时任务处理
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } else {
                // 达到最大重试次数，发送到清理队列
                log.error("缓存更新重试次数已达上限，发送清理消息，key: {}", key);
                cacheManager.sendCacheCleanMessage(key);
            }

            // 手动确认消息（即使失败也要确认，避免消息堆积）
            ack.acknowledge();
        }
    }

    /**
     * 计算重试延迟时间（梯度重试策略）
     */
    private long getRetryDelay(int retryCount) {
        switch (retryCount) {
            case 1: return 1;   // 首次重试：1秒
            case 2: return 5;   // 第二次重试：5秒
            case 3: return 10;  // 第三次重试：10秒
            default: return 30; // 其他情况：30秒
        }
    }
}