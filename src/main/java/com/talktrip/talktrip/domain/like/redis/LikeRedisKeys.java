package com.talktrip.talktrip.domain.like.redis;

public final class LikeRedisKeys {

    private LikeRedisKeys() {
    }

    public static final String WRITE_BEHIND_QUEUE = "talktrip:like:wb:queue";

    static String hydratedMarker(Long memberId) {
        return "talktrip:like:hydrated:" + memberId;
    }

    static String memberZset(Long memberId) {
        return "talktrip:like:member:" + memberId + ":z";
    }
}
