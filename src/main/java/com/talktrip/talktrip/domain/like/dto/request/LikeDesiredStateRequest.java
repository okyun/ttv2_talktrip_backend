package com.talktrip.talktrip.domain.like.dto.request;

/**
 * 좋아요 목표 상태(재시도·낙관적 UI에 적합). {@code liked}와 현재 Redis 투영이 같으면 서비스에서 no-op.
 */
public record LikeDesiredStateRequest(boolean liked) {}
