package com.talktrip.talktrip.domain.like.redis;

import com.talktrip.talktrip.domain.like.entity.Like;
import com.talktrip.talktrip.domain.like.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 좋아요 읽기 모델(redis ZSET). 토글 시 즉시 반영되며, 최초 1회 RDB에서 적재합니다.
 * <p>마커 값: {@code "0"} = 마지막 DB 동기화 시 좋아요 없음(빈 ZSET이면 DB 재조회 안 함),
 * {@code "1"} = 동기화 시 DB에 행이 있었음(ZSET이 비면 RDB에서 다시 채움 — 레거시 "1"만 있던 경우 포함).
 */
@Service
@RequiredArgsConstructor
public class LikeRedisProjectionService {

    private static final String HYDRATED_EMPTY = "0";
    private static final String HYDRATED_NONEMPTY = "1";

    private final StringRedisTemplate stringRedisTemplate;
    private final LikeRepository likeRepository;

    public void ensureHydrated(Long memberId) {
        String markerKey = LikeRedisKeys.hydratedMarker(memberId);
        String zkey = LikeRedisKeys.memberZset(memberId);
        String markerVal = stringRedisTemplate.opsForValue().get(markerKey);
        long zsize = Optional.ofNullable(stringRedisTemplate.opsForZSet().size(zkey)).orElse(0L);

        if (HYDRATED_EMPTY.equals(markerVal) && zsize == 0) {
            return;
        }
        if (HYDRATED_EMPTY.equals(markerVal) && zsize > 0) {
            return;
        }
        if (HYDRATED_NONEMPTY.equals(markerVal) && zsize > 0) {
            return;
        }

        List<Like> likes = likeRepository.findAllByMember_IdWithProduct(memberId);
        for (Like like : likes) {
            double scoreMillis = like.getUpdatedAt() != null
                    ? like.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis();
            stringRedisTemplate.opsForZSet().add(zkey, String.valueOf(like.getProduct().getId()), scoreMillis);
        }
        stringRedisTemplate.opsForValue().set(markerKey, likes.isEmpty() ? HYDRATED_EMPTY : HYDRATED_NONEMPTY);
    }

    public boolean isLiked(Long memberId, Long productId) {
        ensureHydrated(memberId);
        String key = LikeRedisKeys.memberZset(memberId);
        return stringRedisTemplate.opsForZSet().score(key, String.valueOf(productId)) != null;
    }

    /**
     * 목록에 포함된 상품 ID 중 좋아요한 것만 반환 (검색 결과 하트 표시용).
     */
    public Set<Long> findLikedAmong(Long memberId, List<Long> productIds) {
        if (memberId == null || productIds == null || productIds.isEmpty()) {
            return Set.of();
        }
        ensureHydrated(memberId);
        String key = LikeRedisKeys.memberZset(memberId);
        Set<Long> liked = new LinkedHashSet<>();
        for (Long pid : productIds) {
            if (pid != null && stringRedisTemplate.opsForZSet().score(key, String.valueOf(pid)) != null) {
                liked.add(pid);
            }
        }
        return liked;
    }

    public void addLike(Long memberId, Long productId) {
        ensureHydrated(memberId);
        String key = LikeRedisKeys.memberZset(memberId);
        stringRedisTemplate.opsForZSet().add(key, String.valueOf(productId), (double) System.currentTimeMillis());
    }

    public void removeLike(Long memberId, Long productId) {
        ensureHydrated(memberId);
        String key = LikeRedisKeys.memberZset(memberId);
        stringRedisTemplate.opsForZSet().remove(key, String.valueOf(productId));
    }

    public Page<Long> findLikedProductIds(Long memberId, Pageable pageable) {
        ensureHydrated(memberId);
        String key = LikeRedisKeys.memberZset(memberId);
        long total = Optional.ofNullable(stringRedisTemplate.opsForZSet().size(key)).orElse(0L);
        if (total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        int start = (int) pageable.getOffset();
        int end = start + pageable.getPageSize() - 1;
        Set<String> range = stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
        if (range == null || range.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }
        List<Long> ids = range.stream().map(Long::valueOf).collect(Collectors.toList());
        return new PageImpl<>(ids, pageable, total);
    }
}
