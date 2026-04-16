package com.talktrip.talktrip.global.redis;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;

/**
 * back_end에서 주문/결제 완료 같은 사용자 알림을 멀티 인스턴스에서 WebSocket(STOMP)으로 fan-out 하기 위한
 * 최소 Redis Pub/Sub 브릿지입니다.
 *
 * - 발행: publishToUserAfterCommit(email, payload)
 * - 구독: Redis 채널 `orders:user:*`
 * - 전송: STOMP destination `/topic/orders.{email}`
 */
@Service
@Qualifier("redisSubscriber")
@RequiredArgsConstructor
public class RedisMessageBroker implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageBroker.class);

    private static final String USER_CHANNEL_PREFIX = "orders:user:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer container;
    private final SimpMessagingTemplate messagingTemplate;

    @PostConstruct
    public void subscribe() {
        container.addMessageListener(this, new PatternTopic(USER_CHANNEL_PREFIX + "*"));
        logger.info("[RedisMessageBroker] subscribed: {}*", USER_CHANNEL_PREFIX);
    }

    public void publishToUser(String userEmail, Object payload) {
        redisTemplate.convertAndSend(USER_CHANNEL_PREFIX + userEmail, payload);
    }

    public void publishToUserAfterCommit(String userEmail, Object payload) {
        runAfterCommit(() -> publishToUser(userEmail, payload));
    }

    private void runAfterCommit(Runnable r) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            r.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                r.run();
            }
        });
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        if (!channel.startsWith(USER_CHANNEL_PREFIX)) return;

        String userEmail = channel.substring(USER_CHANNEL_PREFIX.length());
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        messagingTemplate.convertAndSend("/topic/orders." + userEmail, body);
    }
}

