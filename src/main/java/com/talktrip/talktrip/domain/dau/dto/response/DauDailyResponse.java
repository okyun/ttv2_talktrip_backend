package com.talktrip.talktrip.domain.dau.dto.response;

import java.time.LocalDate;

/** 일별 비트맵 DAU 집계 결과 */
public record DauDailyResponse(LocalDate date, long uniqueVisitors) {
}
