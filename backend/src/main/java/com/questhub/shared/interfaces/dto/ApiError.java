package com.questhub.shared.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.questhub.shared.domain.FieldErrorItem;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, List<FieldErrorItem> details) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }

    public static ApiError of(String code, String message, List<FieldErrorItem> details) {
        return new ApiError(code, message, details);
    }
}