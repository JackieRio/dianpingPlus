package com.hmdp.utils.listener;

import com.hmdp.config.KafkaConfig;
import com.hmdp.dto.SeckillMessage;
import com.hmdp.service.IVoucherOrderService;
import com.sun.xml.internal.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillOrderListener {

    @Autowired
    private IVoucherOrderService voucherOrderService;

    @KafkaListener(topics = KafkaConfig.SECKILL_ORDER_TOPIC, groupId = "seckill_group")
    public void listenSeckillOrder(@Payload SeckillMessage message, Acknowledgment ack) {
        log.info("接收到秒杀订单信息:{}", message);
        try {
            voucherOrderService.createVoucherOrder(message);
            // 手动确认消息
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理秒杀订单失败:{}", message, e);
            // TODO 可以添加重试机制或死信队列处理
        }
    }
}
