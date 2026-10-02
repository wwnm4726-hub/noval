package com.example.novel.controller;

import com.example.novel.dto.CommentView;
import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.entity.User;
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
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class ChapterController {

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final ReadingProgressService progressService;
    private final CommentService commentService;
    private final CurrentUserService currentUserService;

    public ChapterController(NovelService novelService,
                             ChapterService chapterService,
                             ReadingProgressService progressService,
                             CommentService commentService,
                             CurrentUserService currentUserService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.progressService = progressService;
        this.commentService = commentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/novels/{novelId}/chapters/{chapterNo}")
    public String read(@PathVariable("novelId") Long novelId,
                       @PathVariable("chapterNo") Integer chapterNo,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        Novel novel = novelService.findById(novelId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));

        Chapter chapter = chapterService.findChapter(novelId, chapterNo)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                        "第 " + chapterNo + " 章不存在"));

        // 登录用户自动记录阅读进度
        User user = currentUserService.find(principal);
        if (user != null) {
            progressService.saveProgress(user.getId(), novelId, chapterNo);
        }

        Optional<Integer> prev = chapterService.previousChapterNo(novelId, chapterNo);
        Optional<Integer> next = chapterService.nextChapterNo(novelId, chapterNo);

        List<CommentView> comments = commentService.listViewsByChapter(chapter.getId(), user);

        model.addAttribute("novel", novel);
        model.addAttribute("chapter", chapter);
        model.addAttribute("prevNo", prev.orElse(null));
        model.addAttribute("nextNo", next.orElse(null));
        model.addAttribute("hasPrev", prev.isPresent());
        model.addAttribute("hasNext", next.isPresent());
        model.addAttribute("chapterTotal", chapterService.countChapters(novelId));
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", comments.size());
        return "chapter-read";
    }

    /** 阅读页发表章节评论(表单提交,需登录)。 */
    @PostMapping("/novels/{novelId}/chapters/{chapterNo}/comments")
    public String addChapterComment(@PathVariable("novelId") Long novelId,
                                    @PathVariable("chapterNo") Integer chapterNo,
                                    @RequestParam("content") String content,
                                    @AuthenticationPrincipal UserDetails principal,
                                    RedirectAttributes ra) {
        User user = currentUserService.require(principal);
        try {
            commentService.addChapterComment(user.getId(), novelId, chapterNo, content);
            ra.addFlashAttribute("commentMessage", "评论已发表");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("commentError", ex.getMessage());
        }
        return "redirect:/novels/" + novelId + "/chapters/" + chapterNo;
    }

    /** 删除章节评论(表单提交,仅作者本人或管理员)。 */
    @PostMapping("/novels/{novelId}/chapters/{chapterNo}/comments/{commentId}/delete")
    public String deleteChapterComment(@PathVariable("novelId") Long novelId,
                                       @PathVariable("chapterNo") Integer chapterNo,
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
        return "redirect:/novels/" + novelId + "/chapters/" + chapterNo;
    }
}
