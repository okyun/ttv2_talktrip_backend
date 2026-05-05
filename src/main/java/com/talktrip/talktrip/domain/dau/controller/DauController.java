package com.talktrip.talktrip.domain.dau.controller;

import com.talktrip.talktrip.domain.dau.redis.DauBitmapService;
import com.talktrip.talktrip.domain.dau.redis.DauSetService;
import com.talktrip.talktrip.global.security.CustomMemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * k6 등으로 비트맵·Set 저장 비용을 각각 측정할 수 있도록 엔드포인트를 분리합니다.
 * 일반 트래픽은 {@link com.talktrip.talktrip.domain.dau.filter.DauVisitRecordingFilter}에서 양쪽 모두 기록합니다.
 */
@Tag(name = "DAU", description = "일별 활성 사용자(방문) 기록 — 비트맵 / Set 분리")
@RestController
@RequestMapping("/api/me/dau")
@RequiredArgsConstructor
public class DauController {

    private final DauBitmapService dauBitmapService;
    private final DauSetService dauSetService;

    @Operation(summary = "당일 방문 기록 (비트맵만)", description = "오늘 날짜 DAU 비트맵만 갱신합니다.")
    @PostMapping("/visit/bitmap")
    public ResponseEntity<Void> recordVisitBitmap(@AuthenticationPrincipal CustomMemberDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        dauBitmapService.recordActiveToday(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "당일 방문 기록 (Set만)", description = "오늘 날짜 DAU Redis Set만 갱신합니다.")
    @PostMapping("/visit/set")
    public ResponseEntity<Void> recordVisitSet(@AuthenticationPrincipal CustomMemberDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        dauSetService.recordActiveToday(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
