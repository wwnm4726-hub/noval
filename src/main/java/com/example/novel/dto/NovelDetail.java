package com.example.novel.dto;

import java.util.List;

/** 小说详情(用于 REST 详情接口)。 */
public record NovelDetail(
        Long id,
        String title,
        String author,
        String cover,
        String description,
        String category,
        List<String> tags,
        String status,
        Long viewCount,
        long chapterTotal,
        Double ratingAvg,
        long ratingCount) {
}
