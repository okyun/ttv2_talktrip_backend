package com.talktrip.talktrip.domain.like.service;

import com.talktrip.talktrip.domain.like.redis.LikeRedisProjectionService;
import com.talktrip.talktrip.domain.like.redis.LikeWriteBehindQueueService;
import com.talktrip.talktrip.domain.member.repository.MemberRepository;
import com.talktrip.talktrip.domain.messaging.dto.like.LikeChangeEventDTO;
import com.talktrip.talktrip.domain.product.dto.response.ProductSummaryResponse;
import com.talktrip.talktrip.domain.product.entity.Product;
import com.talktrip.talktrip.domain.product.repository.ProductRepository;
import com.talktrip.talktrip.domain.review.entity.Review;
import com.talktrip.talktrip.global.exception.ErrorCode;
import com.talktrip.talktrip.global.exception.MemberException;
import com.talktrip.talktrip.global.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRedisProjectionService likeRedisProjectionService;
    private final LikeWriteBehindQueueService likeWriteBehindQueueService;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    /**
     * Redis ZSET에 즉시 반영하고, RPUSH 큐에 적재합니다. DB 반영은 스케줄러 → Kafka → like-service 가 담당합니다.
     */
    @CacheEvict(cacheNames = "product", allEntries = true)
    public void toggleLike(Long productId, Long memberId) {
        validateProductAndMember(productId, memberId);
        boolean currentlyLiked = likeRedisProjectionService.isLiked(memberId, productId);
        applyDesiredLikeState(productId, memberId, !currentlyLiked);
    }

    /**
     * 목표 상태로 맞춤(멱등). 이미 {@code liked}와 같으면 Redis·write-behind 큐를 건드리지 않습니다.
     */
    @CacheEvict(cacheNames = "product", allEntries = true)
    public void setLikeDesiredState(Long productId, Long memberId, boolean liked) {
        validateProductAndMember(productId, memberId);
        applyDesiredLikeState(productId, memberId, liked);
    }

    private void validateProductAndMember(Long productId, Long memberId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));
    }

    private void applyDesiredLikeState(Long productId, Long memberId, boolean liked) {
        if (likeRedisProjectionService.isLiked(memberId, productId) == liked) {
            return;
        }
        if (liked) {
            likeRedisProjectionService.addLike(memberId, productId);
            likeWriteBehindQueueService.enqueue(LikeChangeEventDTO.add(productId, memberId));
        } else {
            likeRedisProjectionService.removeLike(memberId, productId);
            likeWriteBehindQueueService.enqueue(LikeChangeEventDTO.remove(productId, memberId));
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getLikedProducts(Long memberId, Pageable pageable) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.USER_NOT_FOUND));

        Page<Long> idPage = likeRedisProjectionService.findLikedProductIds(memberId, pageable);
        if (idPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, idPage.getTotalElements());
        }

        List<Long> orderedIds = idPage.getContent();
        List<Product> loaded = productRepository.findAllById(orderedIds);
        Map<Long, Product> byId = loaded.stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<ProductSummaryResponse> content = orderedIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(product -> {
                    float avgStar = (float) product.getReviews().stream()
                            .mapToDouble(Review::getReviewStar)
                            .average()
                            .orElse(0.0);
                    return ProductSummaryResponse.from(product, avgStar, true);
                })
                .toList();

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }
}
