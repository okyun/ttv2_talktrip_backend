package com.talktrip.talktrip.global.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "talktrip.rate-limit.order")
public class OrderRateLimitProperties {

    /** 주문 API rate limit 사용 여부 */
    private boolean enabled = true;

    /** 슬라이딩 윈도우 크기 (ms) */
    private long windowMs = 10_000L;

    /** 윈도우 내 허용 요청 수 */
    private int maxRequests = 5;
}
