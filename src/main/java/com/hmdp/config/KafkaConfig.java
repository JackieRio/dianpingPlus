package com.hmdp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String SECKILL_ORDER_TOPIC = "seckill_order_topic";
    public static final String CACHE_UPDATE_TOPIC = "cache_update_topic";
    public static final String CACHE_CLEAN_TOPIC = "cache_clean_topic";

    // 创建Kafka主题
    @Bean
    public NewTopic seckillOrderTopic(){
        return TopicBuilder.name(SECKILL_ORDER_TOPIC)
                .partitions(6)  // 增加分区数提高并发
                .replicas(1)
                .config("retention.ms", "86400000") // 保留24小时
                .config("segment.bytes", "104857600") // 100MB段大小
                .build();
    }

    // 创建缓存更新主题
    @Bean
    public NewTopic cacheUpdateTopic(){
        return TopicBuilder.name(CACHE_UPDATE_TOPIC)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "43200000") // 12小时
                .build();
    }

    // 创建缓存清理主题
    @Bean
    public NewTopic cacheCleanTopic(){
        return TopicBuilder.name(CACHE_CLEAN_TOPIC)
                .partitions(2)
                .replicas(1)
                .config("retention.ms", "86400000") // 24小时
                .build();
    }
}