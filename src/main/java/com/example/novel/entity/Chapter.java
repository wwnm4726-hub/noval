package com.example.novel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapters", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"novel_id", "chapter_no"})
})
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "chapter_no", nullable = false)
    private Integer chapterNo;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Chapter() {}

    public Chapter(String title, String content, Integer chapterNo) {
        this.title = title;
        this.content = content;
        this.chapterNo = chapterNo;
        this.wordCount = content == null ? 0 : content.length();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Novel getNovel() { return novel; }
    public void setNovel(Novel novel) { this.novel = novel; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.wordCount = content == null ? 0 : content.length();
    }
    public Integer getChapterNo() { return chapterNo; }
    public void setChapterNo(Integer chapterNo) { this.chapterNo = chapterNo; }
    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}