package com.example.novel.dto;

import com.example.novel.entity.Novel;

import java.time.LocalDateTime;

/**
 * 书架条目视图模型: 小说 + 阅读进度 + 分组 + 最近阅读时间。
 */
public class BookshelfItem {

    private final Novel novel;
    private final Integer progressChapterNo;
    private final long chapterTotal;
    private final LocalDateTime lastReadAt;
    private final String groupName;

    public BookshelfItem(Novel novel, Integer progressChapterNo, long chapterTotal,
                         LocalDateTime lastReadAt, String groupName) {
        this.novel = novel;
        this.progressChapterNo = progressChapterNo;
        this.chapterTotal = chapterTotal;
        this.lastReadAt = lastReadAt;
        this.groupName = groupName;
    }

    public Novel getNovel() { return novel; }
    public Integer getProgressChapterNo() { return progressChapterNo; }
    public long getChapterTotal() { return chapterTotal; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public String getGroupName() { return groupName; }

    /** 是否已产生阅读进度。 */
    public boolean isStarted() { return progressChapterNo != null; }

    /** 阅读进度百分比(0 ~ 100),按当前章节 / 总章节估算。 */
    public int getProgressPercent() {
        if (progressChapterNo == null || chapterTotal <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(progressChapterNo * 100.0 / chapterTotal));
    }
}
