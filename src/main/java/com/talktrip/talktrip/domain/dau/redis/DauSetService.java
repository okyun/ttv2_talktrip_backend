package com.talktrip.talktrip.domain.dau.redis;

import com.talktrip.talktrip.domain.dau.dto.response.DauDailyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 일별 활성 사용자(DAU)를 Redis <strong>Set</strong>으로 기록합니다.
 * {@code SADD}, 집계는 {@code SCARD}({@code size}).
 * <p>비트맵과 동일한 이벤트에 같이 쌓아 두 구조의 수치를 비교할 수 있습니다.
 */
@Service
@RequiredArgsConstructor
public class DauSetService {

    private static final int RETENTION_DAYS_AFTER_DAY_END = 120;
    private static final int MAX_DAYS_PER_RANGE_QUERY = 120;

    private final StringRedisTemplate stringRedisTemplate;

    public void recordActiveToday(long memberId) {
        recordActive(memberId, LocalDate.now());
    }

    public void recordActive(long memberId, LocalDate date) {
        String key = DauRedisKeys.dailySet(date);
        stringRedisTemplate.opsForSet().add(key, String.valueOf(memberId));
        scheduleRetentionIfAbsent(key, date);
    }

    public long countFor(LocalDate date) {
        String key = DauRedisKeys.dailySet(date);
        return Optional.ofNullable(stringRedisTemplate.opsForSet().size(key)).orElse(0L);
    }

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
}
