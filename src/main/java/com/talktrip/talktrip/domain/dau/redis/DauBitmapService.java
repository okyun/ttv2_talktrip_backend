package com.talktrip.talktrip.domain.dau.redis;

import com.talktrip.talktrip.domain.dau.dto.response.DauDailyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 일별 활성 사용자(DAU)를 Redis String 비트맵으로 기록합니다.
 * {@code SETBIT key memberId 1}, 집계는 {@code BITCOUNT}.
 * <p>Redis 비트 오프셋은 0 이상 {@code 2^32 - 1} 이하여야 하므로 회원 ID도 그 범위여야 합니다.
 */
@Service
@RequiredArgsConstructor
public class DauBitmapService {

    /** 비트맵 키 보존 기간 (일). 만료 스케줄링 비용을 줄이려 첫 기록 시 한 번만 설정합니다. */
    private static final int RETENTION_DAYS_AFTER_DAY_END = 120;

    private static final long MAX_BIT_OFFSET = 0xFFFFFFFFL;

    /** 관리자 구간 조회 시 Redis BITCOUNT 호출 상한 */
    private static final int MAX_DAYS_PER_RANGE_QUERY = 120;

    private final StringRedisTemplate stringRedisTemplate;

    public void recordActiveToday(long memberId) {
        recordActive(memberId, LocalDate.now());
    }

    public void recordActive(long memberId, LocalDate date) {
        requireValidMemberOffset(memberId);
        String key = DauRedisKeys.dailyBitmap(date);
        stringRedisTemplate.opsForValue().setBit(key, memberId, true);
        scheduleRetentionIfAbsent(key, date);
    }

    public boolean wasActive(long memberId, LocalDate date) {
        requireValidMemberOffset(memberId);
        String key = DauRedisKeys.dailyBitmap(date);
        Boolean bit = stringRedisTemplate.opsForValue().getBit(key, memberId);
        return Boolean.TRUE.equals(bit);
    }

    /** 해당 일의 서로 다른 활성 회원 수(비트맵 카운트). */
    public long countFor(LocalDate date) {
        String key = DauRedisKeys.dailyBitmap(date);
        return bitCount(key);
    }

    /**
     * 시작일~종료일(포함) 각 날짜의 DAU. 최대 {@value #MAX_DAYS_PER_RANGE_QUERY}일까지.
     */
    public List<DauDailyResponse> countsBetween(LocalDate startInclusive, LocalDate endInclusive) {
        if (startInclusive == null || endInclusive == null) {
            throw new IllegalArgumentException("start and end are required");
        }
        if (startInclusive.isAfter(endInclusive)) {
            throw new IllegalArgumentException("start must be on or before end");
        }
        long spanDays = ChronoUnit.DAYS.between(startInclusive, endInclusive);
        if (spanDays > MAX_DAYS_PER_RANGE_QUERY) {
            throw new IllegalArgumentException("range must not exceed " + MAX_DAYS_PER_RANGE_QUERY + " days");
        }
        List<DauDailyResponse> list = new ArrayList<>((int) spanDays + 1);
        for (LocalDate d = startInclusive; !d.isAfter(endInclusive); d = d.plusDays(1)) {
            list.add(new DauDailyResponse(d, countFor(d)));
        }
        return list;
    }

    private long bitCount(String key) {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        Long n = stringRedisTemplate.execute((RedisCallback<Long>) connection ->
                connection.stringCommands().bitCount(raw));
        return n != null ? n : 0L;
    }

    /** 해당 일 데이터가 끝난 뒤 {@link #RETENTION_DAYS_AFTER_DAY_END}일 보관 후 삭제. */
    private void scheduleRetentionIfAbsent(String key, LocalDate day) {
        Long ttl = stringRedisTemplate.getExpire(key);
        if (ttl != null && ttl > 0) {
            return;
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant expireAt = day.plusDays(1 + RETENTION_DAYS_AFTER_DAY_END)
                .atStartOfDay(zone)
                .toInstant();
        stringRedisTemplate.expireAt(key, expireAt);
    }

    private static void requireValidMemberOffset(long memberId) {
        if (memberId < 0 || memberId > MAX_BIT_OFFSET) {
            throw new IllegalArgumentException("memberId must be in [0, 2^32-1] for Redis bitmap offset");
        }
    }
}
