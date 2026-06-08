package com.talktrip.talktrip.global.redis;

public final class OrderRateLimitRedisKeys {

    private static final String PREFIX = "rate_limit:order:member:";

    private OrderRateLimitRedisKeys() {
    }

    /** 회원·상품별 주문 생성 rate limit key */
    public static String orderCreate(Long memberId, Long productId) {
        return PREFIX + memberId + ":product:" + productId;
    }
}
