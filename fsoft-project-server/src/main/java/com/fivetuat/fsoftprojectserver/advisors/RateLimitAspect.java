package com.fivetuat.fsoftprojectserver.advisors;

import com.fivetuat.fsoftprojectserver.annotations.RateLimit;
import com.fivetuat.fsoftprojectserver.dtos.response.RateLimiterResult;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.RateLimitKeyType;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RateLimiterService rateLimiterService;

    @Around("@annotation(com.fivetuat.fsoftprojectserver.annotations.RateLimit) || @within(com.fivetuat.fsoftprojectserver.annotations.RateLimit)")
    public Object rateLimitAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            rateLimit = joinPoint.getTarget().getClass().getAnnotation(RateLimit.class);
        }

        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        String bucketName = rateLimit.name().isEmpty() ? method.getName() : rateLimit.name();
        String identifier = extractIdentifier(request, rateLimit.keyType());
        String redisKey = "rate_limit:" + bucketName + ":" + identifier;

        RateLimiterResult result = rateLimiterService.checkLimit(redisKey, rateLimit.limit(), rateLimit.period());

        if (response != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetSeconds()));
        }

        if (!result.isAllowed()) {
            log.warn("Rate limit vượt ngưỡng cho API '{}' - Key: {}. Limit: {}/{}s",
                    bucketName, redisKey, rateLimit.limit(), rateLimit.period());
            throw new AppException(ErrorCode.TOO_MANY_REQUESTS);
        }

        return joinPoint.proceed();
    }

    private String extractIdentifier(HttpServletRequest request, RateLimitKeyType keyType) {
        if (keyType == RateLimitKeyType.GLOBAL) {
            return "global";
        }

        if (keyType == RateLimitKeyType.USER_OR_IP) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof Jwt jwt) {
                    return "user:" + jwt.getSubject();
                } else if (authentication.getName() != null) {
                    return "user:" + authentication.getName();
                }
            }
        }

        return "ip:" + getClientIp(request);
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
