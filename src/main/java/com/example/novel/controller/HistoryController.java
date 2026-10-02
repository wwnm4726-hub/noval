package com.example.novel.controller;

import com.example.novel.dto.ProgressView;
import com.example.novel.entity.User;
import com.example.novel.service.ChapterService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.ReadingProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HistoryController {

    private final ReadingProgressService progressService;
    private final ChapterService chapterService;
    private final CurrentUserService currentUserService;

    public HistoryController(ReadingProgressService progressService,
                             ChapterService chapterService,
                             CurrentUserService currentUserService) {
        this.progressService = progressService;
        this.chapterService = chapterService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = currentUserService.require(principal);
        List<ProgressView> items = progressService.listHistory(user.getId()).stream()
                .map(p -> new ProgressView(
                        p.getNovel().getId(),
                        p.getNovel().getTitle(),
                        p.getNovel().getCover(),
                        p.getChapterNo(),
                        chapterService.countChapters(p.getNovel().getId()),
                        p.getUpdatedAt()))
                .toList();

        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("items", items);
        return "history";
    }
}
