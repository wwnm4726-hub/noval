package com.example.novel.service;

import com.example.novel.entity.Comment;
import com.example.novel.repository.CommentRepository;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论服务: 发表 / 查询 / 删除(仅本人或管理员)。
 */
@Service
@Transactional
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private static final int MAX_LENGTH = 500;

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          NovelRepository novelRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
    }

    @Transactional(readOnly = true)
    public List<Comment> listByNovel(Long novelId) {
        return commentRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
    }

    @Transactional(readOnly = true)
    public long countByNovel(Long novelId) {
        return commentRepository.countByNovelId(novelId);
    }

    /** 我发表的评论数。 */
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        return commentRepository.countByUserId(userId);
    }

    public Comment add(Long userId, Long novelId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("评论不能超过 " + MAX_LENGTH + " 字");
        }
        Comment comment = new Comment(
                userRepository.getReferenceById(userId),
                novelRepository.getReferenceById(novelId),
                trimmed);
        Comment saved = commentRepository.save(comment);
        log.info("用户 {} 对小说 {} 发表评论 id={}", userId, novelId, saved.getId());
        return saved;
    }

    /**
     * 删除评论: 仅作者本人或管理员可删。
     *
     * @param operatorRole 操作者角色(用于放行 ADMIN)
     * @return 是否删除成功
     */
    public boolean delete(Long commentId, Long operatorId, String operatorRole) {
        return commentRepository.findById(commentId).map(comment -> {
            boolean isOwner = comment.getUser().getId().equals(operatorId);
            boolean isAdmin = "ADMIN".equals(operatorRole);
            if (!isOwner && !isAdmin) {
                throw new SecurityException("只能删除自己的评论");
            }
            commentRepository.delete(comment);
            log.info("删除评论 id={} (operator={}, admin={})", commentId, operatorId, isAdmin);
            return true;
        }).orElse(false);
    }

    public void deleteByNovel(Long novelId) {
        long removed = commentRepository.deleteByNovelId(novelId);
        log.info("清理小说 {} 的评论 {} 条", novelId, removed);
    }
}
