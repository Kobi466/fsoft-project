package com.fivetuat.fsoftprojectserver.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    int status = 200;
    String message;
    T data;

    public static <T> ApiResponse<T> ok(T data, SuccessCode successCode) {
        return ApiResponse.<T>builder()
                .status(successCode.getStatus())
                .message(successCode.getMessage())
                .data(data)
                .build();

    }
    public static <T> ApiResponse<T> error(T data, ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .status(errorCode.getStatus())
                .message(errorCode.getMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .status(errorCode.getStatus())
                .message(errorCode.getMessage())
                .build();
    }
}
