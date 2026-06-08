/**
 * TalkTrip — Redis 캐시 히트율이 높은 시나리오(동일 URL 반복).
 *
 * 실행 (back_end 디렉터리에서):
 *   k6 run scripts/k6-product-cache.js
 *
 * 다른 호스트/상품 ID:
 *   $env:K6_BASE="http://localhost:8081"; $env:K6_PRODUCT_ID="5"; k6 run scripts/k6-product-cache.js
 */
import http from "k6/http";
import { check, sleep } from "k6";

const BASE = __ENV.K6_BASE || "http://localhost:8080";
const PRODUCT_ID = __ENV.K6_PRODUCT_ID || "1";
const MEMBER_ID = __ENV.K6_MEMBER_ID || "1";

export const options = {
  vus: 5,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<3000"],
  },
};

export default function () {
  // 상품 상세 (캐시: product) — 쿼리 동일 시 히트
  const detailUrl = `${BASE}/api/products/${PRODUCT_ID}?page=0&size=3&sort=updatedAt,desc`;
  const r1 = http.get(detailUrl);
  check(r1, { "detail 200": (r) => r.status === 200 });

  // 상품 목록 (캐시: product)
  const listUrl = `${BASE}/api/products?countryName=${encodeURIComponent("전체")}&page=0&size=10&sort=updatedAt,desc`;
  const r2 = http.get(listUrl);
  check(r2, { "list 200": (r) => r.status === 200 });

  // 회원 공개 프로필 (캐시: user)
  const profileUrl = `${BASE}/api/member/profile/${MEMBER_ID}`;
  const r3 = http.get(profileUrl);
  check(r3, { "profile 200 or 404": (r) => r.status === 200 || r.status === 404 });

  sleep(0.3);
}
