package com.example.novel.dto;

import com.example.novel.entity.Comment;
import com.example.novel.entity.User;

import java.time.LocalDateTime;

/**
 * 评论展示模型: owner 表示当前浏览者是否可删除该评论(作者本人或管理员)。
 * username 字段为展示名: 优先昵称,留空回退用户名。
 */
public record CommentView(
        Long id,
        String username,
        String content,
        LocalDateTime createdAt,
        boolean owner) {

    public static CommentView of(Comment comment, User viewer) {
        boolean owner = viewer != null
                && (comment.getUser().getId().equals(viewer.getId()) || "ADMIN".equals(viewer.getRole()));
        return new CommentView(
                comment.getId(),
                comment.getUser().getDisplayName(),
                comment.getContent(),
                comment.getCreatedAt(),
                owner);
    }
}
