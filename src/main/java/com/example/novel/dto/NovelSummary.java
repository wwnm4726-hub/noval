package com.example.novel.dto;

/** 小说列表项(用于首页 / REST 列表接口)。 */
public record NovelSummary(
        Long id,
        String title,
        String author,
        String cover,
        String category,
        String status,
        Long viewCount,
        long chapterCount) {
}
