package com.talktrip.talktrip.global.redis;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis Sorted Set 기반 sliding window rate limiter.
 *
 * <p>score=요청 시각(ms), member=요청 UUID. {@code removeRangeByScore}로 윈도 밖 member를 제거한 뒤
 * 현재 요청을 추가하고 size로 허용 여부를 판단합니다.
 */
@Service
@RequiredArgsConstructor
public class RedisSlidingWindowRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisSlidingWindowRateLimiter.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * @return true=허용, false=제한 초과
     */
    public boolean tryConsume(String redisKey, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        stringRedisTemplate
                .opsForZSet()
                .removeRangeByScore(redisKey, 0, now - windowMs);

        stringRedisTemplate
                .opsForZSet()
                .add(redisKey, requestId, now);

        Long count = stringRedisTemplate
                .opsForZSet()
                .size(redisKey);

        stringRedisTemplate.expire(redisKey, Duration.ofMillis(windowMs));

        if (count != null && count > maxRequests) {
            stringRedisTemplate.opsForZSet().remove(redisKey, requestId);
            return false;
        }

        return true;
    }

    /**
     * Redis 장애 시 주문 API 가용성을 위해 fail-open.
     */
    public boolean tryConsumeSafely(String redisKey, int maxRequests, long windowMs) {
        try {
            return tryConsume(redisKey, maxRequests, windowMs);
        } catch (Exception e) {
            log.warn("[rate-limit] Redis 처리 실패 — 요청 허용(fail-open): key={}, err={}",
                    redisKey, e.getMessage());
            return true;
        }
    }
}
