# ttv2_talktrip_backend

## Product (공개 카탈로그 API)

`GET /api/products`, `GET /api/products/{id}`, `GET /api/products/aisearch` 는 **`talktrip-product-service`**(DB: `productDB`)로 이전했습니다.  
Redis 목록 캐시·검색 조합은 해당 모듈의 `com.talktrip.product.cache.ProductSearchCacheService` 를 참고하세요.

**프론트(Vite `5173`)**: `vite.config.js` 에서 `/api/products` 는 상품 MSA로 프록시됩니다. `tt/docker-compose.yml` 기준 호스트 포트는 **18086**(기본 프록시 대상). 로컬에서만 `bootRun`으로 **8082**에 띄우면 `PRODUCT_API_TARGET=http://127.0.0.1:8082` 로 덮어쓰세요.

이 모놀리스에는 **재고·리뷰·좋아요 등 상품 연동 엔티티**가 남아 있으며, `StockService`·`ReviewService`·`LikeService`가 `@CacheEvict(cacheNames="product", allEntries=true)` 로 캐시를 비울 때 **동일 `product` 캐시 이름**을 사용합니다(게이트웨이에서 상품 MSA와 같은 Redis를 쓰는 경우). **상품 CRUD 전용 `AdminProductService` 클래스는 현재 레포에 없습니다.**

- **캐시 설정(모놀리스)**: `src/main/java/.../global/config/CacheConfig.java`
- **무효화 트리거**: `StockService`, `ReviewService`, `LikeService`

### 캐시 ON/OFF (TPS 비교용)

- **캐시 ON(기본)**: 별도 설정 없이 실행
- **캐시 OFF**: 캐시 매니저를 끄고 실행

PowerShell 예시:

```powershell
# 캐시 OFF
$env:SPRING_CACHE_TYPE="none"
.\gradlew bootRun

# 캐시 ON (기본)
Remove-Item Env:SPRING_CACHE_TYPE -ErrorAction SilentlyContinue
.\gradlew bootRun
```

### 부하 테스트(k6)로 TPS 비교

캐시 히트율이 높은 시나리오(동일 URL 반복)는 아래 스크립트를 사용합니다.

- 스크립트: `scripts/k6-product-cache.js`

실행(예: back_end 디렉터리에서):

```powershell
k6 run .\scripts\k6-product-cache.js
```

결과에서 TPS(RPS)는 보통 `http_reqs/s`를 비교하면 됩니다.
