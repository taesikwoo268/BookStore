package com.bookstore.aspect;

import com.bookstore.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Bỏ qua nếu không phải API search
        String uri = request.getRequestURI();
        if (!uri.contains("/search") && !uri.contains("/api/v1/books/search")) {
            return true;
        }

        // Lấy IP client
        String clientIp = getClientIp(request);
        log.debug("🔍 Rate limiting check for IP: {}, URI: {}", clientIp, uri);

        // Kiểm tra rate limit
        if (!rateLimiterService.isAllowed(clientIp)) {
            long remaining = rateLimiterService.getRemainingRequests(clientIp);
            long resetTime = rateLimiterService.getResetTime(clientIp);

            response.setStatus(429);
            response.setHeader("X-RateLimit-Limit", "30");
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
            response.setHeader("Retry-After", String.valueOf(resetTime));

            log.warn("🚫 Rate limit exceeded for IP: {}", clientIp);
            return false;
        }

        // Thêm headers vào response
        long remaining = rateLimiterService.getRemainingRequests(clientIp);
        response.setHeader("X-RateLimit-Limit", "30");
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimiterService.getResetTime(clientIp)));

        return true;
    }

    /**
     * Lấy IP client từ request (hỗ trợ proxy)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(X_FORWARDED_FOR);
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }

        return request.getRemoteAddr();
    }
}