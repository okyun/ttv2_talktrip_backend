package com.talktrip.talktrip.domain.product.service;

import com.talktrip.talktrip.domain.product.entity.ProductOption;
import com.talktrip.talktrip.domain.product.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

/**
 * 재고 관리 서비스
 * 재고 확인, 차감, 복원 및 변경 이력을 추적하고 관리
 *
 * <p>동일 옵션에 대한 동시 주문(멀티 인스턴스) 시 오버셀을 줄이기 위해
 * {@code checkStockAndDecrease} 에서 Redis 분산 락(Redisson)을 사용합니다.
 * 락은 호출부 트랜잭션(예: 주문 생성)이 커밋/롤백된 뒤 {@link TransactionSynchronization#afterCompletion} 에서 해제합니다.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    /** 옵션별 재고 직렬화 — 동일 ID에 대해 동시에 하나의 차감만 진행 */
    private static final String LOCK_PREFIX = "lock:stock:";
    private static final long LOCK_WAIT_MS = 5_000L;
    private static final long LOCK_LEASE_MS = 30_000L;

    private final ProductOptionRepository productOptionRepository;
    private final RedissonClient redissonClient;

    @Lazy
    @Autowired
    private StockService self;

    /**
     * 재고 확인 및 차감 (주문 생성 시 사용)
     * <p>분산 락 획득 후 DB 차감을 수행합니다. 프록시를 통한 {@link #decreaseStockTransactional} 호출로 트랜잭션 AOP가 적용됩니다.
     */
    public ProductOption checkStockAndDecrease(Long productOptionId, int quantity) {
        String lockName = LOCK_PREFIX + productOptionId;
        RLock lock = redissonClient.getLock(lockName);
        boolean locked;
        try {
            locked = lock.tryLock(LOCK_WAIT_MS, LOCK_LEASE_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("재고 처리 대기가 중단되었습니다.", e);
        }
        if (!locked) {
            throw new IllegalStateException("재고 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    unlockSafely(lock);
                }
            });
            return self.decreaseStockTransactional(productOptionId, quantity);
        }

        try {
            return self.decreaseStockTransactional(productOptionId, quantity);
        } finally {
            unlockSafely(lock);
        }
    }

    private void unlockSafely(RLock lock) {
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            logger.warn("재고 분산락 해제 실패: {}", e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "product", allEntries = true)
    public ProductOption decreaseStockTransactional(Long productOptionId, int quantity) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + productOptionId));

        if (productOption.getStock() < quantity) {
            throw new IllegalStateException("재고 부족: " + productOption.getOptionName() +
                    " (현재 재고: " + productOption.getStock() + ", 요청 수량: " + quantity + ")");
        }

        int beforeStock = productOption.getStock();
        productOption.setStock(beforeStock - quantity);

        recordStockChange(productOptionId, -quantity, "ORDER_CREATED");

        logger.info("재고 차감 완료: productOptionId={}, optionName={}, beforeStock={}, quantity={}, afterStock={}",
                productOptionId, productOption.getOptionName(), beforeStock, quantity, productOption.getStock());

        return productOption;
    }

    /**
     * 재고 복원 (주문 취소 시 사용)
     */
    @Transactional
    @CacheEvict(cacheNames = "product", allEntries = true)
    public void restoreStock(Long productOptionId, int quantity) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
                .orElse(null);

        if (productOption == null) {
            logger.warn("재고 복원 실패: 옵션을 찾을 수 없습니다. productOptionId={}, quantity={}",
                    productOptionId, quantity);
            return;
        }

        int beforeStock = productOption.getStock();
        productOption.addStock(quantity);

        recordStockChange(productOptionId, quantity, "ORDER_CANCELLED");

        logger.info("재고 복원 완료: productOptionId={}, optionName={}, beforeStock={}, quantity={}, afterStock={}",
                productOptionId, productOption.getOptionName(), beforeStock, quantity, productOption.getStock());
    }

    /**
     * 재고 확인만 수행 (재고 차감 없이 확인만)
     */
    @Transactional(readOnly = true)
    public boolean checkStock(Long productOptionId, int quantity) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + productOptionId));

        return productOption.getStock() >= quantity;
    }

    public void recordStockChange(Long productOptionId, int quantity, String reason) {
        try {
            logger.info("재고 변경 이력: productOptionId={}, quantity={}, reason={}",
                    productOptionId, quantity, reason);
        } catch (Exception e) {
            logger.error("재고 변경 이력 기록 실패: productOptionId={}, quantity={}, reason={}",
                    productOptionId, quantity, reason, e);
        }
    }
}
