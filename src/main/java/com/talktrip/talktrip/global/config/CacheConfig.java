package com.talktrip.talktrip.global.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 캐시 설정 클래스
 * 
 * 주요 기능:
 * 1. Redis를 캐시 저장소로 사용
 * 2. JSON 직렬화 설정
 * 3. 캐시 만료 시간 설정
 * 4. LocalDateTime 직렬화 지원
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {




    /**
     * Redis 캐시 매니저 설정
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @param objectMapper JSON 직렬화용 ObjectMapper
     * @return 설정된 CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // Cache 직렬화는 역직렬화 시점에 반환 타입 정보를 잃기 쉬우므로(LinkedHashMap으로 복원),
        // Redis 캐시 전용 ObjectMapper에 타입 메타데이터를 포함시켜 저장합니다.
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.talktrip.talktrip")
                        .allowIfSubType("java.util")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // JSON 직렬화 설정
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        
        // Redis 캐시 기본 설정
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 기본 만료 시간: 30분
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues(); // null 값 캐싱 비활성화
        
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("user", createCacheConfig(Duration.ofMinutes(60), redisObjectMapper)) // 사용자 정보: 1시간
                .withCacheConfiguration("product", createCacheConfig(Duration.ofMinutes(15), redisObjectMapper)) // 상품 정보: 15분
                .withCacheConfiguration("chat", createCacheConfig(Duration.ofMinutes(5), redisObjectMapper)) // 채팅 정보: 5분
                .withCacheConfiguration("order", createCacheConfig(Duration.ofMinutes(10), redisObjectMapper)) // 주문 정보: 10분
                .build();
    }

    /**
     * 특정 캐시 설정 생성
     *
     * @param ttl 캐시 만료 시간
     * @param objectMapper {@link JavaTimeModule} 등 애플리케이션과 동일한 직렬화 규칙 사용
     * @return RedisCacheConfiguration
     */
    private RedisCacheConfiguration createCacheConfig(Duration ttl, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues(); // NULL 값에는 캐싱하지 않음.
    }
}
