package com.example.novel.controller;

import com.example.novel.dto.CommentView;
import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.entity.ReadingProgress;
import com.example.novel.entity.User;
import com.example.novel.service.BookshelfService;
import com.example.novel.service.ChapterService;
import com.example.novel.service.CommentService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.NovelService;
import com.example.novel.service.RatingService;
import com.example.novel.service.ReadingProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class NovelController {

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final CommentService commentService;
    private final ReadingProgressService progressService;
    private final BookshelfService bookshelfService;
    private final RatingService ratingService;
    private final CurrentUserService currentUserService;

    public NovelController(NovelService novelService,
                           ChapterService chapterService,
                           CommentService commentService,
                           ReadingProgressService progressService,
                           BookshelfService bookshelfService,
                           RatingService ratingService,
                           CurrentUserService currentUserService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.commentService = commentService;
        this.progressService = progressService;
        this.bookshelfService = bookshelfService;
        this.ratingService = ratingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/novels/{id}")
    public String detail(@PathVariable("id") Long id,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));

        novelService.incrementViewCount(id);
        // incrementViewCount 走独立事务的批量更新,当前实体快照仍是旧值,这里同步一下用于展示
        novel.setViewCount(novel.getViewCount() + 1);

        List<Chapter> chapters = chapterService.listChaptersDesc(id);
        long chapterTotal = chapterService.countChapters(id);

        User user = currentUserService.find(principal);
        Integer progressNo = null;
        boolean inBookshelf = false;
        Integer userScore = null;
        if (user != null) {
            progressNo = progressService.getProgress(user.getId(), id)
                    .map(ReadingProgress::getChapterNo)
                    .orElse(null);
            inBookshelf = bookshelfService.existsInBookshelf(user.getId(), id);
            userScore = ratingService.userScore(user.getId(), id).orElse(null);
        }

        List<CommentView> commentViews = commentService.listViewsByNovel(id, user);

        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapters);
        model.addAttribute("chapterTotal", chapterTotal);
        model.addAttribute("progressNo", progressNo);
        model.addAttribute("inBookshelf", inBookshelf);
        model.addAttribute("bookshelfCount", bookshelfService.countByNovel(id));
        model.addAttribute("ratingAvg", ratingService.averageScore(id));
        model.addAttribute("ratingCount", ratingService.count(id));
        model.addAttribute("userScore", userScore);
        model.addAttribute("comments", commentViews);
        model.addAttribute("commentCount", commentViews.size());
        return "novel-detail";
    }

    /** 详情页打分(表单提交,需登录;一人一评,重复提交覆盖)。 */
    @PostMapping("/novels/{id}/rating")
    public String rate(@PathVariable("id") Long id,
                       @RequestParam("score") Integer score,
                       @AuthenticationPrincipal UserDetails principal,
                       RedirectAttributes ra) {
        User user = currentUserService.require(principal);
        try {
            ratingService.rate(user.getId(), id, score);
            ra.addFlashAttribute("ratingMessage", "评分成功");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("ratingError", ex.getMessage());
        }
        return "redirect:/novels/" + id;
    }

    /** 详情页发表评论(表单提交)。 */
    @PostMapping("/novels/{id}/comments")
    public String addComment(@PathVariable("id") Long id,
                             @RequestParam("content") String content,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes ra) {
        User user = currentUserService.require(principal);
        try {
            commentService.add(user.getId(), id, content);
            ra.addFlashAttribute("commentMessage", "评论已发表");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("commentError", ex.getMessage());
        }
        return "redirect:/novels/" + id;
    }

    /** 详情页删除评论(表单提交,仅作者本人或管理员)。 */
    @PostMapping("/novels/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("id") Long id,
                                @PathVariable("commentId") Long commentId,
                                @AuthenticationPrincipal UserDetails principal,
                                RedirectAttributes ra) {
        User user = currentUserService.require(principal);
        try {
            boolean deleted = commentService.delete(commentId, user.getId(), user.getRole());
            if (!deleted) {
                ra.addFlashAttribute("commentError", "评论不存在");
            }
        } catch (SecurityException ex) {
            ra.addFlashAttribute("commentError", ex.getMessage());
        }
        return "redirect:/novels/" + id;
    }

    /** 详情页回复某条顶层评论(表单提交,需登录)。 */
    @PostMapping("/novels/{id}/comments/{commentId}/reply")
    public String replyComment(@PathVariable("id") Long id,
                               @PathVariable("commentId") Long commentId,
                               @RequestParam("content") String content,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes ra) {
        User user = currentUserService.require(principal);
        try {
            commentService.addReply(user.getId(), commentId, content);
            ra.addFlashAttribute("commentMessage", "回复已发表");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("commentError", ex.getMessage());
        }
        return "redirect:/novels/" + id;
    }

    /** 点赞评论(表单提交,需登录;未登录跳登录页)。 */
    @PostMapping("/comments/{commentId}/like")
    public String likeComment(@PathVariable("commentId") Long commentId,
                              @RequestParam("novelId") Long novelId,
                              @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        commentService.like(user.getId(), commentId);
        return "redirect:/novels/" + novelId;
    }

    /** 取消点赞(表单提交,需登录)。 */
    @PostMapping("/comments/{commentId}/unlike")
    public String unlikeComment(@PathVariable("commentId") Long commentId,
                                @RequestParam("novelId") Long novelId,
                                @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        commentService.unlike(user.getId(), commentId);
        return "redirect:/novels/" + novelId;
    }
}
