package com.example.novel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookshelves", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "novel_id"})
})
public class Bookshelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    /** 自定义分组名(如「在看 / 想看 / 弃坑」);为空表示未分组。 */
    @Column(name = "group_name", length = 30)
    private String groupName;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    public Bookshelf() {}

    public Bookshelf(User user, Novel novel) {
        this.user = user;
        this.novel = novel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Novel getNovel() { return novel; }
    public void setNovel(Novel novel) { this.novel = novel; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
}