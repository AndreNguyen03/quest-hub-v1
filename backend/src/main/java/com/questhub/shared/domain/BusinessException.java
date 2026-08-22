package com.questhub.shared.domain;

import java.util.List;

public class BusinessException extends RuntimeException {

    private final String code;
    private final ResponseStatus status;
    private final List<FieldErrorItem> details;

    public BusinessException(String code, String message, ResponseStatus status) {
        this(code, message, status, null);
    }

    public BusinessException(String code, String message, ResponseStatus status, List<FieldErrorItem> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public static BusinessException badRequest(String code, String message) {
        return new BusinessException(code, message, ResponseStatus.BAD_REQUEST);
    }

    public static BusinessException unauthorized(String code, String message) {
        return new BusinessException(code, message, ResponseStatus.UNAUTHORIZED);
    }

    public static BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, ResponseStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String code, String message) {
        return new BusinessException(code, message, ResponseStatus.CONFLICT);
    }

    public static BusinessException conflict(String code, String message, List<FieldErrorItem> details) {
        return new BusinessException(code, message, ResponseStatus.CONFLICT, details);
    }

    public static BusinessException forbidden(String code, String message) {
        return new BusinessException(code, message, ResponseStatus.FORBIDDEN);
    }

    public String getCode() {
        return code;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public List<FieldErrorItem> getDetails() {
        return details;
    }
}
