package com.example.novel.dto;

/** 章节正文(含上下章导航信息)。 */
public record ChapterContent(
        Long id,
        Long novelId,
        String novelTitle,
        Integer chapterNo,
        String title,
        String content,
        Integer wordCount,
        Integer prevNo,
        Integer nextNo,
        long chapterTotal) {
}
