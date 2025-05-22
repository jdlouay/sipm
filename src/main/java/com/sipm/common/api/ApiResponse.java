package com.sipm.common.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Opération réussie")
                .code("SUCCESS")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .code("SUCCESS")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code("ERROR")
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String code, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .data(data)
                .build();
    }
} 