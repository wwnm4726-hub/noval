package com.example.novel.controller.api;

import com.example.novel.dto.CommentRequest;
import com.example.novel.dto.CommentView;
import com.example.novel.entity.Comment;
import com.example.novel.entity.User;
import com.example.novel.service.CommentService;
import com.example.novel.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 评论 REST API。
 * - GET 列表: 公开
 * - POST 发表/回复/点赞: 需登录
 * - DELETE 删除/取消点赞: 需登录,删除仅作者本人或管理员
 */
@RestController
@RequestMapping("/api")
public class ApiCommentController {

    private static final Logger log = LoggerFactory.getLogger(ApiCommentController.class);

    private final CommentService commentService;
    private final CurrentUserService currentUserService;

    public ApiCommentController(CommentService commentService,
                                CurrentUserService currentUserService) {
        this.commentService = commentService;
        this.currentUserService = currentUserService;
    }

    /** 小说详情页评论列表(顶层 + 回复 + 点赞信息)。 */
    @GetMapping("/novels/{novelId}/comments")
    public List<CommentView> list(@PathVariable("novelId") Long novelId,
                                  @AuthenticationPrincipal UserDetails principal) {
        User viewer = currentUserService.find(principal);
        return commentService.listViewsByNovel(novelId, viewer);
    }

    /** 某章节的评论列表。 */
    @GetMapping("/novels/{novelId}/chapters/{chapterNo}/comments")
    public List<CommentView> listChapter(@PathVariable("novelId") Long novelId,
                                         @PathVariable("chapterNo") Integer chapterNo,
                                         @AuthenticationPrincipal UserDetails principal) {
        User viewer = currentUserService.find(principal);
        Long chapterId = commentService.chapterIdOrThrow(novelId, chapterNo);
        return commentService.listViewsByChapter(chapterId, viewer);
    }

    /** 发表针对整本小说的顶层评论。 */
    @PostMapping("/novels/{novelId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentView add(@PathVariable("novelId") Long novelId,
                           @RequestBody CommentRequest request,
                           @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        Comment comment = commentService.add(user.getId(), novelId,
                request == null ? null : request.content());
        return CommentView.of(comment, user, 0L, false, List.of());
    }

    /** 发表针对某章节的评论。 */
    @PostMapping("/novels/{novelId}/chapters/{chapterNo}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentView addChapter(@PathVariable("novelId") Long novelId,
                                  @PathVariable("chapterNo") Integer chapterNo,
                                  @RequestBody CommentRequest request,
                                  @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        Comment comment = commentService.addChapterComment(user.getId(), novelId, chapterNo,
                request == null ? null : request.content());
        return CommentView.of(comment, user, 0L, false, List.of());
    }

    /** 回复某条顶层评论(一层楼中楼)。 */
    @PostMapping("/comments/{id}/reply")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentView reply(@PathVariable("id") Long id,
                             @RequestBody CommentRequest request,
                             @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        Comment reply = commentService.addReply(user.getId(), id,
                request == null ? null : request.content());
        return CommentView.of(reply, user, 0L, false, List.of());
    }

    /** 点赞评论(幂等)。 */
    @PostMapping("/comments/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@PathVariable("id") Long id,
                     @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        commentService.like(user.getId(), id);
        log.debug("用户 {} 点赞评论 {}", user.getUsername(), id);
    }

    /** 取消点赞(幂等)。 */
    @DeleteMapping("/comments/{id}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@PathVariable("id") Long id,
                       @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        commentService.unlike(user.getId(), id);
        log.debug("用户 {} 取消点赞评论 {}", user.getUsername(), id);
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id,
                       @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        boolean deleted = commentService.delete(id, user.getId(), user.getRole());
        if (!deleted) {
            throw new ResponseStatusException(NOT_FOUND, "评论不存在");
        }
        log.debug("评论 {} 已由 {} 删除", id, user.getUsername());
    }
}
