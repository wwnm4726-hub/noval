package com.example.novel.dto;

import com.example.novel.entity.Comment;
import com.example.novel.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论展示模型。
 * - username 为展示名: 优先昵称,留空回退用户名。
 * - owner 表示当前浏览者是否可删除该评论(作者本人或管理员)。
 * - liked 表示当前浏览者是否已点赞;未登录恒为 false。
 * - replies 为该评论下的回复(一层,按时间正序);非顶层评论为空列表。
 */
public record CommentView(
        Long id,
        String username,
        String content,
        LocalDateTime createdAt,
        boolean owner,
        long likeCount,
        boolean liked,
        List<CommentView> replies) {

    public static CommentView of(Comment comment, User viewer, long likeCount, boolean liked,
                                 List<CommentView> replies) {
        boolean owner = viewer != null
                && (comment.getUser().getId().equals(viewer.getId()) || "ADMIN".equals(viewer.getRole()));
        return new CommentView(
                comment.getId(),
                comment.getUser().getDisplayName(),
                comment.getContent(),
                comment.getCreatedAt(),
                owner,
                likeCount,
                liked,
                replies);
    }
}
