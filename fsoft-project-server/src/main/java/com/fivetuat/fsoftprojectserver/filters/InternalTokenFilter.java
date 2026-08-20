package com.fivetuat.fsoftprojectserver.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class InternalTokenFilter extends OncePerRequestFilter {

    @Value("${fsoft-ai.backend-token}")
    private String backendToken;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // chỉ chạy filter này cho các API nội bộ
        String path = request.getServletPath();
        return !path.startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String internalToken = request.getHeader("X-Internal-Token");

        if (backendToken == null || !backendToken.equals(internalToken)) {
            log.warn("Unauthorized access to internal API. Expected token match failed.");
            
            response.setStatus(ErrorCode.AUTHENTICATION_FAILED.getHttpStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            
            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .status(ErrorCode.AUTHENTICATION_FAILED.getStatus())
                    .message("Unauthorized: Missing or invalid X-Internal-Token")
                    .build();
                    
            new ObjectMapper().writeValue(response.getWriter(), apiResponse);
            response.flushBuffer();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
