package com.talktrip.talktrip.domain.dau.redis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DauRedisKeys {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    private DauRedisKeys() {
    }

    /** 일별 활성 사용자 비트맵. 오프셋 = 회원 ID. */
    static String dailyBitmap(LocalDate date) {
        return "talktrip:dau:bitmap:" + date.format(DAY);
    }

    /** 일별 활성 사용자 Set. 멤버 = 회원 ID 문자열. */
    static String dailySet(LocalDate date) {
        return "talktrip:dau:set:" + date.format(DAY);
    }
}
