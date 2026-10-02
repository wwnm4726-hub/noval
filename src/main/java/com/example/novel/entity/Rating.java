package com.example.novel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 小说评分: 每个 (user, novel) 组合唯一,同一用户对同一本书可再次评分(覆盖)。
 * 分值范围 1 ~ 5。
 */
@Entity
@Table(name = "ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "novel_id"})
})
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Rating() {}

    public Rating(User user, Novel novel, Integer score) {
        this.user = user;
        this.novel = novel;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Novel getNovel() { return novel; }
    public void setNovel(Novel novel) { this.novel = novel; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
