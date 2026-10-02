package com.example.novel.dto;

import com.example.novel.entity.Novel;

import java.time.LocalDateTime;

/**
 * 书架条目视图模型: 小说 + 阅读进度 + 最近阅读时间(用于进度徽标与排序)。
 */
public class BookshelfItem {

    private final Novel novel;
    private final Integer progressChapterNo;
    private final long chapterTotal;
    private final LocalDateTime lastReadAt;

    public BookshelfItem(Novel novel, Integer progressChapterNo, long chapterTotal, LocalDateTime lastReadAt) {
        this.novel = novel;
        this.progressChapterNo = progressChapterNo;
        this.chapterTotal = chapterTotal;
        this.lastReadAt = lastReadAt;
    }

    public Novel getNovel() { return novel; }
    public Integer getProgressChapterNo() { return progressChapterNo; }
    public long getChapterTotal() { return chapterTotal; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }

    /** 是否已产生阅读进度。 */
    public boolean isStarted() { return progressChapterNo != null; }
}
