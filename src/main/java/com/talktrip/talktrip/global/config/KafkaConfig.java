package com.talktrip.talktrip.global.config;

import com.talktrip.talktrip.domain.messaging.dto.order.OrderEvent;
import com.talktrip.talktrip.domain.messaging.dto.product.ProductEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;
import java.util.HashMap;

/**
 * Kafka Consumer 및 Producer 설정
 * 
 * JSON 기반 Producer/Consumer Factory들을 제공합니다.
 * ErrorHandlingDeserializer를 사용하여 역직렬화 실패 시 애플리케이션 종료를 방지합니다.
 * 
 * @EnableKafka: Kafka Listener를 활성화하고 KafkaListenerEndpointRegistry를 자동 등록합니다.
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private static final int DEFAULT_TOPIC_PARTITIONS = 3;
    private static final short DEFAULT_REPLICATION_FACTOR = 1;

    // ----------- Kafka Admin: 토픽 자동 생성 (파티션 3개) -----------

    @Bean
    public org.springframework.kafka.core.KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new org.springframework.kafka.core.KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicProductClick(
            @Value("${kafka.topics.product-click:product-click}") String topicName) {
        return TopicBuilder.name(topicName).partitions(DEFAULT_TOPIC_PARTITIONS).replicas(DEFAULT_REPLICATION_FACTOR).build();
    }

    @Bean
    public NewTopic topicOrderCreated(
            @Value("${kafka.topics.order-created:order-created}") String topicName) {
        return TopicBuilder.name(topicName).partitions(DEFAULT_TOPIC_PARTITIONS).replicas(DEFAULT_REPLICATION_FACTOR).build();
    }

    @Bean
    public NewTopic topicProductClickStats(
            @Value("${kafka.topics.product-click-stats:product-click-stats}") String topicName) {
        return TopicBuilder.name(topicName).partitions(DEFAULT_TOPIC_PARTITIONS).replicas(DEFAULT_REPLICATION_FACTOR).build();
    }

    @Bean
    public NewTopic topicOrderPurchaseStats(
            @Value("${kafka.topics.order-purchase-stats:order-purchase-stats}") String topicName) {
        return TopicBuilder.name(topicName).partitions(DEFAULT_TOPIC_PARTITIONS).replicas(DEFAULT_REPLICATION_FACTOR).build();
    }

    @Bean
    public NewTopic topicPaymentSuccess(
            @Value("${kafka.topics.payment-success:payment-success}") String topicName) {
        return TopicBuilder.name(topicName).partitions(DEFAULT_TOPIC_PARTITIONS).replicas(DEFAULT_REPLICATION_FACTOR).build();
    }

    @Bean
    public NewTopic topicLikeChange(
            @Value("${kafka.topics.like-change:like-change}") String topicName) {
        return TopicBuilder.name(topicName).partitions(DEFAULT_TOPIC_PARTITIONS).replicas(DEFAULT_REPLICATION_FACTOR).build();
    }

    // ----------- Consumer Factory (Listener) --------------

    /**
     * OrderEvent 전용 Consumer Factory
     * 
     * ErrorHandlingDeserializer를 사용하여 역직렬화 실패 시 애플리케이션 종료를 방지합니다.
     * JsonDeserializer를 사용하여 OrderEvent 타입으로 자동 역직렬화합니다.
     * 메시지 포맷이 잘못되어도 consumer가 중단되지 않습니다.
     */
    @Bean
    public ConsumerFactory<String, OrderEvent> orderEventConsumerFactory() {
        // ErrorHandlingDeserializer - 역직렬화 실패 시 애플리케이션 종료 방지, 잘못된 JSON 방식 처리
        Map<String, Object> props = Map.ofEntries(
                Map.entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class),
                Map.entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class),
                Map.entry(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class),
                Map.entry(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class),
                // 메시지 포맷이 잘못되어도 consumer 중단하지 않는 형태
                Map.entry(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderEvent.class),
                Map.entry(JsonDeserializer.TRUSTED_PACKAGES, "*"), // 전체 path 허용
                Map.entry(JsonDeserializer.USE_TYPE_INFO_HEADERS, false)
        );
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * OrderEvent 전용 Kafka Listener Container Factory
     * 
     * @KafkaListener 어노테이션의 수신을 위한 Bean
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> orderEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory());
        return factory;
    }

    /**
     * ProductEvent 전용 Consumer Factory
     * 
     * ErrorHandlingDeserializer를 사용하여 역직렬화 실패 시 애플리케이션 종료를 방지합니다.
     * JsonDeserializer를 사용하여 ProductEvent 타입으로 자동 역직렬화합니다.
     * 메시지 포맷이 잘못되어도 consumer가 중단되지 않습니다.
     */
    @Bean
    public ConsumerFactory<String, ProductEvent> productEventConsumerFactory() {
        // ErrorHandlingDeserializer - 역직렬화 실패 시 애플리케이션 종료 방지, 잘못된 JSON 방식 처리
        Map<String, Object> props = Map.ofEntries(
                Map.entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class),
                Map.entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class),
                Map.entry(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class),
                Map.entry(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class),
                // 메시지 포맷이 잘못되어도 consumer 중단하지 않는 형태
                Map.entry(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductEvent.class),
                Map.entry(JsonDeserializer.TRUSTED_PACKAGES, "*"), // 전체 path 허용
                Map.entry(JsonDeserializer.USE_TYPE_INFO_HEADERS, false)
        );
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * ProductEvent 전용 Kafka Listener Container Factory
     * 
     * @KafkaListener 어노테이션의 수신을 위한 Bean
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductEvent> productEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productEventConsumerFactory());
        return factory;
    }

    /**
     * Generic Consumer Factory
     * 
     * OrderEvent가 아닌 다른 타입도 메시지로 받기 위한 메소드
     * USE_TYPE_INFO_HEADERS를 true로 설정하여 타입 정보를 헤더에서 읽어옵니다.
     */
    @Bean
    public ConsumerFactory<String, Object> genericConsumerFactory() {
        Map<String, Object> props = Map.ofEntries(
                Map.entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class),
                Map.entry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class),
                Map.entry(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class),
                Map.entry(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class),
                Map.entry(JsonDeserializer.TRUSTED_PACKAGES, "*"),
                Map.entry(JsonDeserializer.USE_TYPE_INFO_HEADERS, true)
        );
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Generic Kafka Listener Container Factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> genericKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(genericConsumerFactory());
        return factory;
    }

    // ----------- Producer Factory --------------

    /**
     * OrderEvent 전용 Producer Factory
     * 
     * JsonSerializer를 사용하여 OrderEvent를 JSON으로 직렬화합니다.
     * 타입 정보를 헤더에 추가하여 역직렬화 시 타입을 알 수 있도록 합니다.
     */
    @Bean
    public ProducerFactory<String, OrderEvent> orderEventProducerFactory() {
        // 직렬화, 역직렬화를 위한 타입 정보 헤더 추가
        Map<String, Object> props = Map.ofEntries(
                Map.entry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class),
                Map.entry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class),
                Map.entry(JsonSerializer.ADD_TYPE_INFO_HEADERS, true)
        );
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * OrderEvent 전용 Kafka Template
     */
    @Bean
    public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
        return new KafkaTemplate<>(orderEventProducerFactory());
    }

    /**
     * ProductEvent 전용 Producer Factory
     * 
     * JsonSerializer를 사용하여 ProductEvent를 JSON으로 직렬화합니다.
     * 타입 정보를 헤더에 추가하여 역직렬화 시 타입을 알 수 있도록 합니다.
     */
    @Bean
    public ProducerFactory<String, ProductEvent> productEventProducerFactory() {
        // 직렬화, 역직렬화를 위한 타입 정보 헤더 추가
        Map<String, Object> props = Map.ofEntries(
                Map.entry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class),
                Map.entry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class),
                Map.entry(JsonSerializer.ADD_TYPE_INFO_HEADERS, true)
        );
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * ProductEvent 전용 Kafka Template
     */
    @Bean
    public KafkaTemplate<String, ProductEvent> productEventKafkaTemplate() {
        return new KafkaTemplate<>(productEventProducerFactory());
    }

    /**
     * JSON Producer Factory (범용)
     * 
     * ProductClickEventDTO 등 JSON 형식의 DTO를 발행하기 위한 Factory입니다.
     * 타입 정보를 헤더에 추가하지 않아 다른 시스템과의 호환성이 좋습니다.
     */
    @Bean
    public ProducerFactory<String, Object> jsonProducerFactory() {
        Map<String, Object> props = Map.ofEntries(
                Map.entry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                Map.entry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class),
                Map.entry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class),
                Map.entry(JsonSerializer.ADD_TYPE_INFO_HEADERS, false) // 타입 정보 헤더 추가 안함 (호환성)
        );
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * JSON Kafka Template (범용)
     * 
     * ProductClickEventDTO 등 JSON 형식의 메시지를 발행하기 위한 Template입니다.
     */
    @Bean("jsonKafkaTemplate")
    public KafkaTemplate<String, Object> jsonKafkaTemplate() {
        return new KafkaTemplate<>(jsonProducerFactory());
    }
}
