package com.example.novel.dto;

import java.time.LocalDateTime;

/** 统一 API 错误响应体。 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {
}
