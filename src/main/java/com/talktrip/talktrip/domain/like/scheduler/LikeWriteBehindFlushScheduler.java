package com.talktrip.talktrip.domain.like.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktrip.talktrip.domain.like.redis.LikeRedisKeys;
import com.talktrip.talktrip.domain.messaging.avro.KafkaEventProducer;
import com.talktrip.talktrip.domain.messaging.dto.like.LikeChangeEventDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis List에 쌓인 좋아요 이벤트를 배치로 꺼내, (productId, memberId) 기준으로 마지막 상태만 남겨
 * Kafka로 전달합니다. 큐에 동일 키가 여러 번 쌓여도 DB insert가 꼬이지 않게 합니다.
 */
@Component
@RequiredArgsConstructor
public class LikeWriteBehindFlushScheduler {

    private static final Logger log = LoggerFactory.getLogger(LikeWriteBehindFlushScheduler.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${talktrip.like.write-behind.batch-size:500}")
    private int batchSize;

    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> DEQUEUE_SCRIPT = new DefaultRedisScript<>();
    static {
        DEQUEUE_SCRIPT.setResultType(List.class);
        DEQUEUE_SCRIPT.setScriptText(
                "local key = KEYS[1]\n"
                        + "local n = tonumber(ARGV[1])\n"
                        + "local len = redis.call('LLEN', key)\n"
                        + "if len == 0 then return {} end\n"
                        + "local take = math.min(n, len)\n"
                        + "local elems = redis.call('LRANGE', key, 0, take - 1)\n"
                        + "redis.call('LTRIM', key, take, -1)\n"
                        + "return elems"
        );
    }

    @Scheduled(fixedDelayString = "${talktrip.like.write-behind.flush-interval-ms:2000}")
    public void flush() {
        try {
            @SuppressWarnings("unchecked")
            List<String> jsonRows = stringRedisTemplate.execute(
                    DEQUEUE_SCRIPT,
                    Collections.singletonList(LikeRedisKeys.WRITE_BEHIND_QUEUE),
                    String.valueOf(batchSize)
            );
            if (jsonRows == null || jsonRows.isEmpty()) {
                return;
            }
            Map<String, LikeChangeEventDTO> lastByPair = new LinkedHashMap<>();
            for (String row : jsonRows) {
                LikeChangeEventDTO dto = objectMapper.readValue(row, LikeChangeEventDTO.class);
                String k = dto.getProductId() + ":" + dto.getMemberId();
                lastByPair.put(k, dto);
            }
            for (LikeChangeEventDTO dto : lastByPair.values()) {
                kafkaEventProducer.publishLikeChange(dto);
            }
            log.debug("like write-behind flush: raw={}, deduped={}", jsonRows.size(), lastByPair.size());
        } catch (Exception e) {
            log.error("like write-behind flush 실패", e);
        }
    }
}
