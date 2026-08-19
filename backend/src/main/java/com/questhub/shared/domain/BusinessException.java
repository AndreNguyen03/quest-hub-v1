package com.questhub.shared.domain;

import java.util.List;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final List<FieldErrorItem> details;

    public BusinessException(String code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public BusinessException(String code, String message, HttpStatus status, List<FieldErrorItem> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public static BusinessException badRequest(String code, String message) {
        return new BusinessException(code, message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException unauthorized(String code, String message) {
        return new BusinessException(code, message, HttpStatus.UNAUTHORIZED);
    }

    public static BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String code, String message) {
        return new BusinessException(code, message, HttpStatus.CONFLICT);
    }

    public static BusinessException conflict(String code, String message, List<FieldErrorItem> details) {
        return new BusinessException(code, message, HttpStatus.CONFLICT, details);
    }

    public static BusinessException forbidden(String code, String message) {
        return new BusinessException(code, message, HttpStatus.FORBIDDEN);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<FieldErrorItem> getDetails() {
        return details;
    }
}