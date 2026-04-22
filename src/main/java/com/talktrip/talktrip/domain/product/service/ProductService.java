package com.talktrip.talktrip.domain.product.service;

import com.talktrip.talktrip.domain.like.repository.LikeRepository;
import com.talktrip.talktrip.domain.product.dto.response.ProductDetailResponse;
import com.talktrip.talktrip.domain.product.dto.response.ProductSummaryResponse;
import com.talktrip.talktrip.domain.product.entity.Product;
import com.talktrip.talktrip.domain.product.entity.ProductOption;
import com.talktrip.talktrip.domain.product.repository.ProductRepository;
import com.talktrip.talktrip.domain.review.dto.response.ReviewResponse;
import com.talktrip.talktrip.domain.review.entity.Review;
import com.talktrip.talktrip.domain.review.repository.ReviewRepository;
import com.talktrip.talktrip.global.exception.ErrorCode;
import com.talktrip.talktrip.global.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final LikeRepository likeRepository;
    private final ProductSearchCacheService productSearchCacheService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    /**
     * 상품 목록(검색·국가·정렬·페이지).
     * 제품 목록은 조회가 잦으므로 Redis 캐시를 쓰되, Page 직렬화 문제를 피하려고
     * {@link com.talktrip.talktrip.domain.product.dto.response.ProductSearchPageCache}로 저장합니다.
     * 로그인 사용자의 "좋아요"는 사용자별 데이터이므로 캐시된 base 목록 위에 덧씌우는 방식으로 처리합니다.
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchProducts(
            String keyword,
            String countryName,
            Long memberId,
            Pageable pageable
    ) {
        Page<ProductSummaryResponse> basePage = productSearchCacheService
                .getBaseProductSearchPageCache(keyword, countryName, pageable)
                .toPage(pageable);

        if (memberId == null || basePage.isEmpty()) {
            return basePage;
        }

        List<Long> productIds = basePage.getContent().stream()
                .map(ProductSummaryResponse::productId)
                .toList();

        Set<Long> likedProductIds = likeRepository.findLikedProductIds(memberId, productIds);

        List<ProductSummaryResponse> withLikes = basePage.getContent().stream()
                .map(p -> new ProductSummaryResponse(
                        p.productId(),
                        p.productName(),
                        p.productDescription(),
                        p.thumbnailImageUrl(),
                        p.price(),
                        p.discountPrice(),
                        p.averageReviewStar(),
                        likedProductIds.contains(p.productId())
                ))
                .toList();

        return new PageImpl<>(withLikes, pageable, basePage.getTotalElements());
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(
            Long productId,
            Long memberId,
            Pageable pageable
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

        int futureStock = product.getProductOptions().stream()
                .mapToInt(ProductOption::getStock)
                .sum();

        if (futureStock == 0) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

        float avgStar = (float) reviewRepository.findByProductId(productId).stream()
                .mapToDouble(Review::getReviewStar)
                .average()
                .orElse(0.0);

        List<ReviewResponse> reviewResponses = reviewPage.stream()
                .map(review -> ReviewResponse.from(review, product))
                .toList();

        boolean isLiked = (memberId != null) &&
                likeRepository.existsByProductIdAndMemberId(productId, memberId);

        return ProductDetailResponse.from(product, avgStar, reviewResponses, isLiked);
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public List<ProductSummaryResponse> aiSearchProducts(
            String query,
            Long memberId
    ) {
        try {
            String fastApiUrl = fastApiBaseUrl + "/query";

            Map<String, String> requestBody = Map.of("query", query);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    fastApiUrl,
                    requestBody,
                    Map.class
            );

            if (response == null || !response.containsKey("product_ids")) {
                return List.of();
            }

            Object productIdsObj = response.get("product_ids");
            List<String> productIdStrings;

            if (productIdsObj instanceof List<?>) {
                productIdStrings = ((List<?>) productIdsObj).stream()
                        .map(Object::toString)
                        .toList();
            } else {
                return List.of();
            }

            if (productIdStrings.isEmpty()) {
                return List.of();
            }

            List<Long> productIds = productIdStrings.stream()
                    .map(Long::parseLong)
                    .toList();

            List<Product> products = productRepository.findAllById(productIds);

            Map<Long, Integer> idOrder = new HashMap<>();
            for (int i = 0; i < productIds.size(); i++) {
                idOrder.put(productIds.get(i), i);
            }

            return products.stream()
                    .sorted(Comparator.comparing(p -> idOrder.getOrDefault(p.getId(), Integer.MAX_VALUE)))
                    .map(product -> {
                        List<Review> allReviews = reviewRepository.findByProductId(product.getId());
                        float avgStar = (float) allReviews.stream()
                                .mapToDouble(Review::getReviewStar)
                                .average()
                                .orElse(0.0);

                        boolean liked = (memberId != null) &&
                                likeRepository.existsByProductIdAndMemberId(product.getId(), memberId);

                        return ProductSummaryResponse.from(product, avgStar, liked);
                    })
                    .toList();

        } catch (Exception e) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
