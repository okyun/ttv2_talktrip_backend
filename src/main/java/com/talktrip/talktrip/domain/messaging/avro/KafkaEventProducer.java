package com.talktrip.talktrip.domain.messaging.avro;

import com.talktrip.talktrip.domain.messaging.dto.like.LikeChangeEventDTO;
import com.talktrip.talktrip.domain.messaging.dto.order.OrderCreatedEventDTO;
import com.talktrip.talktrip.domain.messaging.dto.order.PaymentSuccessEventDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka 이벤트 발행자 (JSON 버전)
 *
 * Order와 Product 이벤트를 JSON 형식으로 Kafka에 발행합니다.
 * Spring Kafka의 JsonSerializer를 사용하여 DTO를 그대로 직렬화합니다.
 */
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventProducer.class);

    /**
     * JSON 직렬화를 사용하는 KafkaTemplate
     *
     * KafkaConfig.jsonKafkaTemplate Bean을 주입받습니다.
     */
    private final KafkaTemplate<String, Object> jsonKafkaTemplate;

    // 주문 생성 이벤트를 발행할 토픽
    @Value("${kafka.topics.order-created:order-created}")
    private String orderCreatedTopic;

    // 결제 성공 이벤트를 발행할 토픽
    @Value("${kafka.topics.payment-success:payment-success}")
    private String paymentSuccessTopic;

    @Value("${kafka.topics.like-change:like-change}")
    private String likeChangeTopic;

    // ========== Order Event Methods ==========

    /**
     * 주문 생성 이벤트 발행 (JSON 형식)
     *
     * OrderCreatedEventDTO를 그대로 JSON으로 직렬화하여 Kafka로 발행합니다.
     *
     * @param dto OrderCreatedEventDTO
     */
    public void publishOrderCreated(OrderCreatedEventDTO dto) {
        try {
            // [Kafka 전송 흐름]
            // - 이 send(...)는 "어떤 파일로" 넘어가는 게 아니라, Kafka 브로커의 토픽으로 메시지를 발행합니다.
            // - 전송 대상 토픽: ${kafka.topics.order-created:order-created}
            //   - 설정 위치: `tt/back_end/src/main/resources/application.yml` (kafka.topics.order-created)
            // - 이 토픽을 구독(@KafkaListener)하는 서비스/파일 예시:
            //   - `talktrip-order-purchases-service/.../messaging/consumer/OrderCreatedDebugConsumer.java`
            //     (groupId=debug-order-created-consumer / audit-order-created-consumer 등)
            // - 또한 Kafka Streams 집계가 이 토픽을 입력으로 사용할 수 있습니다(현재 집계는 `talktrip-stats-service`로 이관됨).
            // send(...) 는 비동기 메서드로, Kafka 브로커에 전송 요청만 하고 바로 Future(CompletableFuture 유사 객체)를 반환합니다.
            // whenComplete(...) 는 이 비동기 작업이 "성공 또는 실패로 끝난 이후"에 호출되는 콜백을 등록합니다.
            //  - result: 성공 시 RecordMetadata 를 포함 (어느 토픽/파티션/오프셋에 적혔는지)
            //  - ex: 실패 시 예외 정보 (null 이 아니면 전송 실패)
            jsonKafkaTemplate.send(orderCreatedTopic, String.valueOf(dto.getOrderId()), dto)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            // ✅ 전송 성공: 메타데이터를 로깅
                            logger.info("JSON 주문 생성 이벤트 발행 성공: orderId={}, orderCode={}, topic={}, partition={}, offset={}",
                                    dto.getOrderId(), dto.getOrderCode(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            // ❌ 전송 실패: 예외 정보를 함께 로깅 (추후 모니터링/알람 용도)
                            logger.error("JSON 주문 생성 이벤트 발행 실패: orderId={}, orderCode={}",
                                    dto.getOrderId(), dto.getOrderCode(), ex);
                        }
                    });
        } catch (Exception e) {
            logger.error("JSON 주문 생성 이벤트 발행 중 오류 발생: orderId={}, orderCode={}",
                    dto.getOrderId(), dto.getOrderCode(), e);
            throw new RuntimeException("JSON 주문 생성 이벤트 발행 실패", e);
        }
    }

    /**
     * 주문 생성 이벤트 발행 (JSON 형식) - 별칭 메서드
     *
     * @param dto OrderCreatedEventDTO
     */
    public void publishOrderEvent(OrderCreatedEventDTO dto) {
        publishOrderCreated(dto);
    }

    /**
     * 결제 성공 이벤트 발행 (JSON 형식)
     *
     * PaymentSuccessEventDTO를 JSON으로 직렬화하여 Kafka로 발행합니다.
     */
    public void publishPaymentSuccess(PaymentSuccessEventDTO dto) {
        try {
            // [Kafka 전송 흐름]
            // - 전송 대상 토픽: ${kafka.topics.payment-success:payment-success}
            //   - 설정 위치: `tt/back_end/src/main/resources/application.yml` (kafka.topics.payment-success)
            // - 이 토픽을 구독(@KafkaListener)하는 서비스/파일:
            //   - `talktrip-order-email-service/.../messaging/consumer/PaymentSuccessEmailConsumer.java`
            //   - `talktrip-order-purchases-service/.../messaging/consumer/OrderCreatedDebugConsumer.java`
            jsonKafkaTemplate.send(paymentSuccessTopic, String.valueOf(dto.getOrderId()), dto)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            logger.info("JSON 결제 성공 이벤트 발행 성공: orderId={}, orderCode={}, topic={}, partition={}, offset={}",
                                    dto.getOrderId(), dto.getOrderCode(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        } else {
                            logger.error("JSON 결제 성공 이벤트 발행 실패: orderId={}, orderCode={}",
                                    dto.getOrderId(), dto.getOrderCode(), ex);
                        }
                    });
        } catch (Exception e) {
            logger.error("JSON 결제 성공 이벤트 발행 중 오류 발생: orderId={}, orderCode={}",
                    dto.getOrderId(), dto.getOrderCode(), e);
        }
    }

    /**
     * 주문 이벤트 발행 (JSON 형식) - 직접 파라미터로 받기
     *
     * orderId, customerId, quantity, price를 직접 받아서 간단한 OrderCreatedEventDTO를 구성 후 발행합니다.
     *
     * @param orderId    주문 ID (UUID 문자열 등)
     * @param customerId 고객 ID (숫자 문자열일 경우 memberId로 사용 시도)
     * @param quantity   주문 수량
     * @param price      주문 가격 (BigDecimal)
     */
    public void publishOrderEvent(String orderId, String customerId, Integer quantity, BigDecimal price) {
        try {
            Long memberIdLong = null;
            if (customerId != null && !customerId.isEmpty()) {
                try {
                    memberIdLong = Long.parseLong(customerId);
                } catch (NumberFormatException e) {
                    logger.warn("customerId를 Long으로 변환할 수 없습니다. null로 설정: customerId={}", customerId);
                }
            }

            OrderCreatedEventDTO dto = OrderCreatedEventDTO.builder()
                    .orderId(0L) // 간단한 테스트용이므로 0L 사용 (실제 프로덕션에서는 별도 처리 필요)
                    .orderCode(orderId)
                    .memberId(memberIdLong)
                    .totalPrice(price != null ? price.intValue() : 0)
                    .orderStatus("PENDING")
                    .createdAt(LocalDateTime.now())
                    .items(List.of())
                    .build();

            publishOrderCreated(dto);
            logger.info("JSON 주문 이벤트 발행 성공 (직접 파라미터): orderId={}, customerId={}, quantity={}, price={}",
                    orderId, customerId, quantity, price);
        } catch (Exception e) {
            logger.error("JSON 주문 이벤트 발행 실패 (직접 파라미터): orderId={}, customerId={}",
                    orderId, customerId, e);
            throw new RuntimeException("JSON 주문 이벤트 발행 실패", e);
        }
    }

    /**
     * 좋아요 변경 이벤트 (write-behind 스케줄러가 배치로 호출) → like-service DB 동기화용.
     */
    public void publishLikeChange(LikeChangeEventDTO dto) {
        try {
            String partitionKey = dto.getMemberId() + ":" + dto.getProductId();
            jsonKafkaTemplate.send(likeChangeTopic, partitionKey, dto)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            logger.info("JSON 좋아요 이벤트 발행 성공: productId={}, memberId={}, action={}, topic={}, offset={}",
                                    dto.getProductId(), dto.getMemberId(), dto.getAction(),
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().offset());
                        } else {
                            logger.error("JSON 좋아요 이벤트 발행 실패: productId={}, memberId={}, action={}",
                                    dto.getProductId(), dto.getMemberId(), dto.getAction(), ex);
                        }
                    });
        } catch (Exception e) {
            logger.error("JSON 좋아요 이벤트 발행 중 오류: productId={}, memberId={}",
                    dto.getProductId(), dto.getMemberId(), e);
            throw new RuntimeException("JSON 좋아요 이벤트 발행 실패", e);
        }
    }
}


