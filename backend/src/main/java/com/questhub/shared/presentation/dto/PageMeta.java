package com.questhub.shared.presentation.dto;

public record PageMeta(long total, int page, int limit, int totalPages) {

    public static PageMeta of(long total, int page, int limit) {
        int totalPages = limit <= 0 ? 0 : (int) Math.ceil((double) total / limit);
        return new PageMeta(total, page, limit, totalPages);
    }
}