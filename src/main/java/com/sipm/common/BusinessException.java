package com.sipm.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public BusinessException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public BusinessException(String message, HttpStatus status) {
        this(message, status, "BUSINESS_ERROR");
    }

    public BusinessException(String message, String code) {
        this(message, HttpStatus.BAD_REQUEST, code);
    }

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR");
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
        this.code = errorCode;
    }
} 