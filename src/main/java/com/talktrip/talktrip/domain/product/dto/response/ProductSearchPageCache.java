package com.talktrip.talktrip.domain.product.dto.response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Redis 캐시 직렬화용 — {@link org.springframework.data.domain.Page} 대신 저장합니다.
 * 페이지·정렬은 캐시 키(@Cacheable)와 요청 {@link Pageable}이 일치할 때만 사용합니다.
 */
public record ProductSearchPageCache(
        List<ProductSummaryResponse> content,
        long totalElements
) {
    public Page<ProductSummaryResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
