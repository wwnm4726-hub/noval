package com.example.novel.service;

import com.example.novel.dto.CommentView;
import com.example.novel.entity.Chapter;
import com.example.novel.entity.Comment;
import com.example.novel.entity.CommentLike;
import com.example.novel.entity.Novel;
import com.example.novel.entity.User;
import com.example.novel.repository.ChapterRepository;
import com.example.novel.repository.CommentLikeRepository;
import com.example.novel.repository.CommentRepository;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * 评论服务: 小说/章节评论的发表、查询、点赞、删除(仅本人或管理员)。
 * 支持一层回复(楼中楼)。
 */
@Service
@Transactional
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private static final int MAX_LENGTH = 500;

    private final CommentRepository commentRepository;
    private final CommentLikeRepository likeRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;

    public CommentService(CommentRepository commentRepository,
                          CommentLikeRepository likeRepository,
                          UserRepository userRepository,
                          NovelRepository novelRepository,
                          ChapterRepository chapterRepository) {
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
    }

    // ---------------- 查询 ----------------

    /** 小说详情页评论视图(顶层 + 其回复 + 点赞信息)。 */
    @Transactional(readOnly = true)
    public List<CommentView> listViewsByNovel(Long novelId, User viewer) {
        return commentRepository
                .findByNovelIdAndChapterIsNullAndParentIsNullOrderByCreatedAtDesc(novelId).stream()
                .map(c -> toView(c, viewer))
                .toList();
    }

    /** 某章节的评论视图。 */
    @Transactional(readOnly = true)
    public List<CommentView> listViewsByChapter(Long chapterId, User viewer) {
        return commentRepository.findByChapterIdAndParentIsNullOrderByCreatedAtAsc(chapterId).stream()
                .map(c -> toView(c, viewer))
                .toList();
    }

    @Transactional(readOnly = true)
    public long countByNovel(Long novelId) {
        return commentRepository.countByNovelId(novelId);
    }

    /** 按小说 + 章节号取章节主键,不存在则抛异常(读接口校验用)。 */
    @Transactional(readOnly = true)
    public Long chapterIdOrThrow(Long novelId, Integer chapterNo) {
        return chapterRepository.findByNovelIdAndChapterNo(novelId, chapterNo)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"))
                .getId();
    }

    /** 我发表的评论数。 */
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        return commentRepository.countByUserId(userId);
    }

    // ---------------- 发表 ----------------

    /** 发表针对整本小说的顶层评论(详情页)。 */
    public Comment add(Long userId, Long novelId, String content) {
        String trimmed = validateContent(content);
        Comment comment = new Comment(
                userRepository.getReferenceById(userId),
                novelRepository.getReferenceById(novelId),
                trimmed);
        Comment saved = commentRepository.save(comment);
        log.info("用户 {} 对小说 {} 发表评论 id={}", userId, novelId, saved.getId());
        return saved;
    }

    /** 回复某条顶层评论(仅一层)。 */
    public Comment addReply(Long userId, Long parentId, String content) {
        String trimmed = validateContent(content);
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("被回复的评论不存在"));
        if (parent.getParent() != null) {
            throw new IllegalArgumentException("仅支持对顶层评论进行回复(一层楼中楼)");
        }
        Comment reply = new Comment(userRepository.getReferenceById(userId), parent.getNovel(), trimmed);
        reply.setChapter(parent.getChapter());
        reply.setParent(parent);
        Comment saved = commentRepository.save(reply);
        log.info("用户 {} 回复评论 {} -> id={}", userId, parentId, saved.getId());
        return saved;
    }

    /** 发表针对某章节的评论(阅读页)。 */
    public Comment addChapterComment(Long userId, Long novelId, Integer chapterNo, String content) {
        String trimmed = validateContent(content);
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new IllegalArgumentException("小说不存在"));
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNo(novelId, chapterNo)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"));
        Comment comment = new Comment(userRepository.getReferenceById(userId), novel, trimmed);
        comment.setChapter(chapter);
        Comment saved = commentRepository.save(comment);
        log.info("用户 {} 对小说 {} 第 {} 章发表评论 id={}", userId, novelId, chapterNo, saved.getId());
        return saved;
    }

    // ---------------- 点赞 ----------------

    /** 点赞(幂等: 已赞则忽略)。 */
    public void like(Long userId, Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (likeRepository.findByUserIdAndCommentId(userId, commentId).isPresent()) {
            return;
        }
        likeRepository.save(new CommentLike(
                userRepository.getReferenceById(userId),
                commentRepository.getReferenceById(commentId)));
        log.info("用户 {} 点赞评论 {}", userId, commentId);
    }

    /** 取消点赞(幂等)。 */
    public void unlike(Long userId, Long commentId) {
        likeRepository.deleteByUserIdAndCommentId(userId, commentId);
        log.info("用户 {} 取消点赞评论 {}", userId, commentId);
    }

    // ---------------- 删除 ----------------

    /**
     * 删除评论: 仅作者本人或管理员可删;删除顶层评论时一并删除其回复与点赞。
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
            likeRepository.deleteByParentCommentId(commentId);
            commentRepository.deleteByParentId(commentId);
            likeRepository.deleteByCommentId(commentId);
            commentRepository.delete(comment);
            log.info("删除评论 id={} (operator={}, admin={})", commentId, operatorId, isAdmin);
            return true;
        }).orElse(false);
    }

    /** 删除某本小说全部评论及其点赞(删除小说时调用)。 */
    public void deleteByNovel(Long novelId) {
        likeRepository.deleteByNovelId(novelId);
        long removed = commentRepository.deleteByNovelId(novelId);
        log.info("清理小说 {} 的评论 {} 条", novelId, removed);
    }

    /** 删除某章节全部评论及其点赞(删除章节时调用)。 */
    public void deleteByChapter(Long chapterId) {
        likeRepository.deleteByChapterId(chapterId);
        long removed = commentRepository.deleteByChapterId(chapterId);
        log.info("清理章节 {} 的评论 {} 条", chapterId, removed);
    }

    /**
     * 删除某用户发表的全部评论及其相关点赞与回复(删除用户时调用)。
     * 先清理点赞,再删回复,最后删顶层评论,避免外键约束冲突。
     */
    public void deleteByUser(Long userId) {
        List<Comment> mine = commentRepository.findByUserId(userId);
        LinkedHashSet<Long> replyIds = new LinkedHashSet<>();
        LinkedHashSet<Long> topIds = new LinkedHashSet<>();
        for (Comment c : mine) {
            if (c.getParent() != null) {
                replyIds.add(c.getId());
            } else {
                topIds.add(c.getId());
            }
            // 该评论下他人/本人的回复也需一并清理
            for (Comment r : commentRepository.findByParentIdOrderByCreatedAtAsc(c.getId())) {
                replyIds.add(r.getId());
            }
        }
        // 该用户给出的全部点赞
        likeRepository.deleteByUserId(userId);
        // 待删评论上所有点赞
        for (Long id : replyIds) {
            likeRepository.deleteByCommentId(id);
        }
        for (Long id : topIds) {
            likeRepository.deleteByCommentId(id);
            commentRepository.deleteByParentId(id);
        }
        // 先删回复再删顶层,满足自关联外键顺序
        for (Long id : replyIds) {
            commentRepository.deleteById(id);
        }
        for (Long id : topIds) {
            commentRepository.deleteById(id);
        }
        log.info("清理用户 {} 的评论 {} 条", userId, mine.size());
    }

    // ---------------- 内部 ----------------

    private CommentView toView(Comment c, User viewer) {
        long likeCount = likeRepository.countByCommentId(c.getId());
        boolean liked = viewer != null
                && likeRepository.findByUserIdAndCommentId(viewer.getId(), c.getId()).isPresent();
        List<CommentView> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(c.getId()).stream()
                .map(r -> replyView(r, viewer))
                .toList();
        return CommentView.of(c, viewer, likeCount, liked, replies);
    }

    private CommentView replyView(Comment reply, User viewer) {
        long likeCount = likeRepository.countByCommentId(reply.getId());
        boolean liked = viewer != null
                && likeRepository.findByUserIdAndCommentId(viewer.getId(), reply.getId()).isPresent();
        return CommentView.of(reply, viewer, likeCount, liked, List.of());
    }

    private String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("评论不能超过 " + MAX_LENGTH + " 字");
        }
        return trimmed;
    }
}
