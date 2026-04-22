package com.talktrip.talktrip.domain.product.service;

import com.talktrip.talktrip.domain.product.dto.response.ProductSearchPageCache;
import com.talktrip.talktrip.domain.product.dto.response.ProductSummaryResponse;
import com.talktrip.talktrip.domain.product.entity.Product;
import com.talktrip.talktrip.domain.product.repository.ProductRepository;
import com.talktrip.talktrip.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 제품 목록(검색/필터/정렬/페이지) 조회 결과를 Redis에 캐시합니다.
 *
 * <p>주의: {@code @Cacheable}은 동일 클래스 내부 호출(self-invocation)에서는 동작하지 않기 때문에,
 * 캐시 적용을 위해 별도 빈으로 분리했습니다.</p>
 */
@Service
@RequiredArgsConstructor
public class ProductSearchCacheService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Cacheable(
            cacheNames = "product",
            // 캐시 직렬화 포맷 변경 시 기존 엔트리와 충돌하지 않도록 키 버전 포함
            key = "'productSearch:v3:'"
                    + " + (#keyword == null ? '' : #keyword.trim())"
                    + " + ':' + (#countryName == null ? '' : #countryName)"
                    + " + ':' + #pageable.pageNumber"
                    + " + ':' + #pageable.pageSize"
                    + " + ':' + #pageable.sort.toString()",
            // "에러 응답" 자체는 예외로 끝나 캐싱되지 않지만,
            // 장애/초기화 시점 등에 발생할 수 있는 '빈 결과'는 오래 박제되지 않도록 캐시 제외(원하면 TTL로만 관리해도 됨).
            unless = "#result == null || #result.content() == null || #result.content().isEmpty()"
    )
    @Transactional(readOnly = true)
    public ProductSearchPageCache getBaseProductSearchPageCache(
            String keyword,
            String countryName,
            Pageable pageable
    ) {
        Page<Product> page = (keyword == null || keyword.isBlank())
                ? productRepository.findVisibleProducts(countryName, pageable)
                : productRepository.searchByKeywords(
                Arrays.stream(keyword.trim().split("\\s+"))
                        .filter(s -> !s.isBlank()).toList(),
                countryName,
                pageable
        );

        List<Product> products = page.getContent();
        if (products.isEmpty()) {
            return new ProductSearchPageCache(List.of(), page.getTotalElements());
        }

        List<Long> productIds = products.stream().map(Product::getId).toList();
        Map<Long, Double> avgStarMap = reviewRepository.fetchAvgStarsByProductIds(productIds);

        List<ProductSummaryResponse> content = products.stream()
                .map(p -> {
                    float avgStar = avgStarMap.getOrDefault(p.getId(), 0.0).floatValue();
                    return ProductSummaryResponse.from(p, avgStar, false);
                })
                .toList();

        return new ProductSearchPageCache(content, page.getTotalElements());
    }
}

