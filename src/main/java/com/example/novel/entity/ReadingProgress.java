package com.example.novel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 阅读进度: 记录某用户在某本小说上读到的最新章节。
 * 每个 (user, novel) 组合唯一,读到新章节时更新到此实体。
 */
@Entity
@Table(name = "reading_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "novel_id"})
})
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(name = "chapter_no", nullable = false)
    private Integer chapterNo;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ReadingProgress() {}

    public ReadingProgress(User user, Novel novel, Integer chapterNo) {
        this.user = user;
        this.novel = novel;
        this.chapterNo = chapterNo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Novel getNovel() { return novel; }
    public void setNovel(Novel novel) { this.novel = novel; }
    public Integer getChapterNo() { return chapterNo; }
    public void setChapterNo(Integer chapterNo) { this.chapterNo = chapterNo; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
