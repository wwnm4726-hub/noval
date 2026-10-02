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
 * - POST 发表: 需登录
 * - DELETE 删除: 需登录,仅作者本人或管理员
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

    @GetMapping("/novels/{novelId}/comments")
    public List<CommentView> list(@PathVariable("novelId") Long novelId,
                                  @AuthenticationPrincipal UserDetails principal) {
        User viewer = currentUserService.find(principal);
        return commentService.listByNovel(novelId).stream()
                .map(c -> CommentView.of(c, viewer))
                .toList();
    }

    @PostMapping("/novels/{novelId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentView add(@PathVariable("novelId") Long novelId,
                           @RequestBody CommentRequest request,
                           @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        Comment comment = commentService.add(user.getId(), novelId,
                request == null ? null : request.content());
        return CommentView.of(comment, user);
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
