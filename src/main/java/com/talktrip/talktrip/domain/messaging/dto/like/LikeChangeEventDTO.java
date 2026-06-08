package com.talktrip.talktrip.domain.messaging.dto.like;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 좋아요 영속화를 위한 Kafka 이벤트 (like-service DB 동기화).
 * JSON 직렬화 시 타입 헤더를 쓰지 않으므로 필드명은 변경하지 않습니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LikeChangeEventDTO {

    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_REMOVE = "REMOVE";

    private Long productId;
    private Long memberId;
    /** {@link #ACTION_ADD} 또는 {@link #ACTION_REMOVE} */
    private String action;
    private String eventId;
    private Instant occurredAt;

    public static LikeChangeEventDTO add(Long productId, Long memberId) {
        return LikeChangeEventDTO.builder()
                .productId(productId)
                .memberId(memberId)
                .action(ACTION_ADD)
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .build();
    }

    public static LikeChangeEventDTO remove(Long productId, Long memberId) {
        return LikeChangeEventDTO.builder()
                .productId(productId)
                .memberId(memberId)
                .action(ACTION_REMOVE)
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .build();
    }
}
