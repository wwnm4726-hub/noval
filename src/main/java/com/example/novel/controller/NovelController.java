package com.example.novel.controller;

import com.example.novel.dto.CommentView;
import com.example.novel.entity.Chapter;
import com.example.novel.entity.Comment;
import com.example.novel.entity.Novel;
import com.example.novel.entity.ReadingProgress;
import com.example.novel.entity.User;
import com.example.novel.service.BookshelfService;
import com.example.novel.service.ChapterService;
import com.example.novel.service.CommentService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.NovelService;
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
    private final CurrentUserService currentUserService;

    public NovelController(NovelService novelService,
                           ChapterService chapterService,
                           CommentService commentService,
                           ReadingProgressService progressService,
                           BookshelfService bookshelfService,
                           CurrentUserService currentUserService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.commentService = commentService;
        this.progressService = progressService;
        this.bookshelfService = bookshelfService;
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
        if (user != null) {
            progressNo = progressService.getProgress(user.getId(), id)
                    .map(ReadingProgress::getChapterNo)
                    .orElse(null);
            inBookshelf = bookshelfService.existsInBookshelf(user.getId(), id);
        }

        List<Comment> comments = commentService.listByNovel(id);
        List<CommentView> commentViews = comments.stream()
                .map(c -> CommentView.of(c, user))
                .toList();

        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapters);
        model.addAttribute("chapterTotal", chapterTotal);
        model.addAttribute("progressNo", progressNo);
        model.addAttribute("inBookshelf", inBookshelf);
        model.addAttribute("comments", commentViews);
        model.addAttribute("commentCount", commentViews.size());
        return "novel-detail";
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
}
