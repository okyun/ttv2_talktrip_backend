package com.talktrip.talktrip.domain.dau.dto.response;

import java.time.LocalDate;

/** 같은 일자에 대해 비트맵 DAU와 Set DAU를 함께 표시 (비교용). */
public record DauDailyCompareResponse(
        LocalDate date,
        long bitmapUniqueVisitors,
        long setUniqueVisitors
) {
}
