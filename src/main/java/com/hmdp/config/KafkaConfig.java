package com.hmdp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

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

    // Kafka消费者配置
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    // Kafka消费者工厂
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "seckill_group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonDeserializer");
        props.put("spring.kafka.consumer.properties.spring.json.trusted.packages", "com.hmdp.dto");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}