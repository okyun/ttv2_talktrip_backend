package com.talktrip.talktrip.domain.order.service;

import com.talktrip.talktrip.domain.order.entity.Order;
import com.talktrip.talktrip.domain.order.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 주문 완료 알림/이메일 처리를 위한 Redis Stream 생산자.
 *
 * - Stream Key: stream:order:notification
 * - Body 예시:
 *   type=ORDER_COMPLETED, orderCode=..., memberEmail=..., totalPrice=..., paymentKey=..., createdAt=...
 *
 * 실제 이메일/푸시 발송은 별도 워커(Consumer)가 이 스트림을 읽어서 처리하도록 한다.
 */
@Service
@RequiredArgsConstructor
public class OrderNotificationStreamService {

    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationStreamService.class);

    private static final String STREAM_KEY = "stream:order:notification";

    private final StringRedisTemplate stringRedisTemplate;

    public void enqueueOrderCompleted(Order order, Payment payment) {
        try {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("type", "ORDER_COMPLETED");
            fields.put("orderCode", order.getOrderCode());
            fields.put("memberEmail", order.getMember().getAccountEmail());
            fields.put("totalPrice", String.valueOf(order.getTotalPrice()));
            fields.put("paymentKey", payment.getPaymentKey());
            if (order.getCreatedAt() != null) {
                fields.put("createdAt", order.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
            }

            // XADD stream:order:notification * fields...
            stringRedisTemplate.opsForStream().add(STREAM_KEY, fields);
            logger.info("주문 완료 이벤트를 Redis Stream에 적재 완료: stream={}, orderCode={}, email={}",
                    STREAM_KEY, order.getOrderCode(), order.getMember().getAccountEmail());
        } catch (Exception e) {
            logger.warn("Redis Stream 주문 완료 이벤트 적재 실패: orderId={}, orderCode={}, error={}",
                    order.getId(), order.getOrderCode(), e.getMessage());
        }
    }
}

