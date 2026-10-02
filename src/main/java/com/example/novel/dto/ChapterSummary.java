package com.example.novel.dto;

/** 章节列表项(不含正文)。 */
public record ChapterSummary(
        Long id,
        Integer chapterNo,
        String title,
        Integer wordCount) {
}
