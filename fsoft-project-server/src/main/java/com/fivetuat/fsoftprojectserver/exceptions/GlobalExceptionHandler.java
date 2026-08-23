package com.fivetuat.fsoftprojectserver.exceptions;

import java.sql.SQLSyntaxErrorException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }

    @ExceptionHandler(SQLSyntaxErrorException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleSQLSyntaxErrorException(SQLSyntaxErrorException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "SQL syntax error: " + ex.getMessage(), request);
    }

    // Xử lý lỗi vi phạm ràng buộc dữ liệu (vd: Khóa ngoại, NOT NULL)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handlerErrorSql(DataIntegrityViolationException ex, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("path", request.getRequestURI());
        data.put("errors", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "SQL error: " + ex.getMessage(), data);
    }

    // Xử lý lỗi trùng lặp dữ liệu (Unique constraint / Primary key)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDuplicateKeyException(DuplicateKeyException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Data already exists", request);
    }

    // Xử lý lỗi không tìm thấy dữ liệu (khi dùng queryForObject)
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Data resource not found", request);
    }

    // Xử lý lỗi sai cú pháp SQL hoặc truy vấn hỏng
    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadSqlGrammarException(BadSqlGrammarException ex, HttpServletRequest request) {
        java.sql.SQLException sqlEx = ex.getSQLException();
        String sqlErrorMessage = sqlEx != null ? sqlEx.getMessage() : ex.getMessage();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database configuration or query error: " + sqlErrorMessage, request);
    }

    // xu ly loi validation @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("path", request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(
                        error -> errors.put(error.getField(), error.getDefaultMessage())
                );
        data.put("errors", errors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation error", data);
    }

    // xu ly loi form @ModelAttribute
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBindException(BindException ex, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("path", request.getRequestURI());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(
                        error -> errors.put(error.getField(), error.getDefaultMessage())
                );
        data.put("errors", errors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Form data error", data);
    }

    // xu ly loi path variable @PathVariable
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMethodArgumentTypeMismatchException(TypeMismatchException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Incorrect data type for the parameter: " + ex.getMessage(), request);
    }

    // xu ly loi khong co method trong url
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Http method not supported", request);
    }

    // xu ly loi khong co argument @RequestParam
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Miss argument require: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleException(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage(), request);
    }

    // Xử lý lỗi trả về từ fsoft-ai (hoặc các external API)
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Object> handleWebClientResponseException(WebClientResponseException ex) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object body = mapper.readValue(ex.getResponseBodyAsString(), Object.class);
            
            // Ẩn mã 401 (lỗi cấu hình token BE-fsoft-ai)
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "message", "Internal Server Error: AI service configuration error"));
            }
            
            // Trả về đúng HTTP status và body của fsoft-ai
            return ResponseEntity.status(ex.getStatusCode()).body(body);
        } catch (Exception e) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("path", request.getRequestURI());
        data.put("timestamp", LocalDateTime.now().toString());
        return buildErrorResponse(status, message, data);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(HttpStatus status, String message, T data) {
        ApiResponse<T> apiResponse = ApiResponse.<T>builder()
                .status(status.value())
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(status).body(apiResponse);
    }
}
