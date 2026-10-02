package com.example.novel.dto;

import java.time.LocalDateTime;

/** 阅读进度(用于阅读历史页 / GET /api/me/progress)。 */
public record ProgressView(
        Long novelId,
        String novelTitle,
        String novelCover,
        Integer chapterNo,
        long chapterTotal,
        LocalDateTime updatedAt) {
}
