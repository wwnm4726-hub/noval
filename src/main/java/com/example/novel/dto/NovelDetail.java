package com.example.novel.dto;

/** 小说详情(用于 REST 详情接口)。 */
public record NovelDetail(
        Long id,
        String title,
        String author,
        String cover,
        String description,
        String category,
        String status,
        Long viewCount,
        long chapterTotal) {
}
