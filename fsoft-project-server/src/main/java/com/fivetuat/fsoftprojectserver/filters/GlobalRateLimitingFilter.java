package com.fivetuat.fsoftprojectserver.filters;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivetuat.fsoftprojectserver.advisors.RateLimitAspect;
import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.RateLimiterResult;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.services.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalRateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    private static final long GLOBAL_IP_LIMIT = 120;
    private static final long GLOBAL_IP_PERIOD = 60;

//    cac endpoint pass
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                path.startsWith("/ws-");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = RateLimitAspect.getClientIp(request);
        String redisKey = "rate_limit:global_ip:" + clientIp;

        RateLimiterResult result = rateLimiterService.checkLimit(redisKey, GLOBAL_IP_LIMIT, GLOBAL_IP_PERIOD);

        response.setHeader("X-Global-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-Global-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-Global-RateLimit-Reset", String.valueOf(result.getResetSeconds()));

        if (!result.isAllowed()) {
            log.warn("Global IP Rate Limit bị vi phạm bởi IP: {} trên URL: {}", clientIp, request.getRequestURI());

            response.setStatus(ErrorCode.TOO_MANY_REQUESTS.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .status(ErrorCode.TOO_MANY_REQUESTS.getStatus())
                    .message(ErrorCode.TOO_MANY_REQUESTS.getMessage())
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            response.flushBuffer();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
