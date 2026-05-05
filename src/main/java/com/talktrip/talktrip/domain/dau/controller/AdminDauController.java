package com.talktrip.talktrip.domain.dau.controller;

import com.talktrip.talktrip.domain.dau.dto.response.DauDailyCompareResponse;
import com.talktrip.talktrip.domain.dau.dto.response.DauDailyResponse;
import com.talktrip.talktrip.domain.dau.redis.DauBitmapService;
import com.talktrip.talktrip.domain.dau.redis.DauSetService;
import com.talktrip.talktrip.domain.member.enums.MemberRole;
import com.talktrip.talktrip.global.security.CustomMemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "DAU Admin", description = "관리자용 일별 활성 사용자 — 비트맵·Set 비교 조회")
@RestController
@RequestMapping("/api/admin/dau")
@RequiredArgsConstructor
public class AdminDauController {

    private final DauBitmapService dauBitmapService;
    private final DauSetService dauSetService;

    @Operation(summary = "특정 일 DAU (비트맵 vs Set)", description = "BITCOUNT / SCARD 각각. date 생략 시 서버 오늘(LocalDate.now).")
    @GetMapping("/daily")
    public ResponseEntity<?> daily(
            @AuthenticationPrincipal CustomMemberDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "ADMIN_ONLY"));
        }
        LocalDate d = date != null ? date : LocalDate.now();
        long bitmap = dauBitmapService.countFor(d);
        long set = dauSetService.countFor(d);
        return ResponseEntity.ok(new DauDailyCompareResponse(d, bitmap, set));
    }

    @Operation(summary = "기간별 DAU (비트맵 vs Set)", description = "시작·종료 포함, 최대 120일.")
    @GetMapping("/range")
    public ResponseEntity<?> range(
            @AuthenticationPrincipal CustomMemberDetails principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        if (!isAdmin(principal)) {
            return ResponseEntity.status(403).body(Map.of("error", "ADMIN_ONLY"));
        }
        try {
            List<DauDailyResponse> bitmapRows = dauBitmapService.countsBetween(start, end);
            List<DauDailyResponse> setRows = dauSetService.countsBetween(start, end);
            List<DauDailyCompareResponse> merged = new ArrayList<>(bitmapRows.size());
            for (int i = 0; i < bitmapRows.size(); i++) {
                DauDailyResponse b = bitmapRows.get(i);
                long setCount = setRows.get(i).uniqueVisitors();
                merged.add(new DauDailyCompareResponse(b.date(), b.uniqueVisitors(), setCount));
            }
            return ResponseEntity.ok(merged);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private static boolean isAdmin(CustomMemberDetails principal) {
        if (principal == null) {
            return false;
        }
        MemberRole role = principal.member().getMemberRole();
        return role == MemberRole.A || role == MemberRole.S;
    }
}
