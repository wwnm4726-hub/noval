package com.example.novel.controller;

import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.entity.User;
import com.example.novel.service.ChapterService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.NovelService;
import com.example.novel.service.ReadingProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class ChapterController {

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final ReadingProgressService progressService;
    private final CurrentUserService currentUserService;

    public ChapterController(NovelService novelService,
                             ChapterService chapterService,
                             ReadingProgressService progressService,
                             CurrentUserService currentUserService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.progressService = progressService;
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

        model.addAttribute("novel", novel);
        model.addAttribute("chapter", chapter);
        model.addAttribute("prevNo", prev.orElse(null));
        model.addAttribute("nextNo", next.orElse(null));
        model.addAttribute("hasPrev", prev.isPresent());
        model.addAttribute("hasNext", next.isPresent());
        model.addAttribute("chapterTotal", chapterService.countChapters(novelId));
        return "chapter-read";
    }
}
