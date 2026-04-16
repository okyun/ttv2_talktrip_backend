package com.talktrip.talktrip.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PG 결제 성공 콜백에서 Redis Stream으로 이벤트를 적재하는 Producer.
 *
 * - Stream Key : stream:payment:success
 * - 사용 시나리오:
 *   1) Toss 등 PG에서 결제 성공 응답을 받으면
 *   2) 이 Producer로 XADD 하고
 *   3) 별도 워커가 이 스트림을 읽어 processSuccessfulPayment 를 수행한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentSuccessStreamProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentSuccessStreamProducer.class);

    private static final String STREAM_KEY = "stream:payment:success";

    private final StringRedisTemplate stringRedisTemplate;

    public void enqueuePaymentSuccess(String orderCode, String paymentKey, JSONObject responseJson) {
        try {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("type", "PAYMENT_SUCCESS");
            fields.put("orderCode", orderCode);
            fields.put("paymentKey", paymentKey);
            fields.put("payload", responseJson.toJSONString());

            stringRedisTemplate.opsForStream().add(STREAM_KEY, fields);
            logger.info("결제 성공 이벤트를 Redis Stream에 적재 완료: stream={}, orderCode={}, paymentKey={}",
                    STREAM_KEY, orderCode, paymentKey);
        } catch (Exception e) {
            logger.error("결제 성공 이벤트를 Redis Stream에 적재 실패: orderCode={}, paymentKey={}, error={}",
                    orderCode, paymentKey, e.getMessage(), e);
            // PG 콜백에서 이 부분이 실패하면 운영상 큰 이슈이므로, 모니터링/알람 대상이 된다.
            throw new RuntimeException("결제 성공 이벤트 Stream 적재 실패", e);
        }
    }
}

