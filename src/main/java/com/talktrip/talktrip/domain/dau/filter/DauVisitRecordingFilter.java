package com.talktrip.talktrip.domain.dau.filter;

import com.talktrip.talktrip.domain.dau.redis.DauBitmapService;
import com.talktrip.talktrip.domain.dau.redis.DauSetService;
import com.talktrip.talktrip.global.security.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 인증된 회원의 요청마다 당일 DAU를 비트맵·Set에 기록합니다. Redis 오류 시 요청은 그대로 진행합니다.
 */
@Component
@RequiredArgsConstructor
public class DauVisitRecordingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DauVisitRecordingFilter.class);

    private final DauBitmapService dauBitmapService;
    private final DauSetService dauSetService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!HttpMethod.OPTIONS.matches(request.getMethod()) && shouldRecordForUri(request.getRequestURI())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomMemberDetails details) {
                Long id = details.getId();
                try {
                    dauBitmapService.recordActiveToday(id);
                } catch (Exception e) {
                    log.warn("[DAU] bitmap 기록 실패 memberId={}: {}", id, e.getMessage());
                }
                try {
                    dauSetService.recordActiveToday(id);
                } catch (Exception e) {
                    log.warn("[DAU] set 기록 실패 memberId={}: {}", id, e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldRecordForUri(String uri) {
        if (uri == null) {
            return false;
        }
        return !(uri.startsWith("/api/actuator")
                || uri.startsWith("/actuator")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars"));
    }
}
