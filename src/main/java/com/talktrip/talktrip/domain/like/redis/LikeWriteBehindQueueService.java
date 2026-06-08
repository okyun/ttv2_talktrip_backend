package com.talktrip.talktrip.domain.like.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktrip.talktrip.domain.messaging.dto.like.LikeChangeEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis List RPUSH 기반 write-behind 큐. 스케줄러가 배치로 꺼내 Kafka로 전달합니다.
 */
@Service
@RequiredArgsConstructor
public class LikeWriteBehindQueueService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void enqueue(LikeChangeEventDTO event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.opsForList().rightPush(LikeRedisKeys.WRITE_BEHIND_QUEUE, json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("like write-behind 직렬화 실패", e);
        }
    }
}
