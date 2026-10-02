package com.example.novel.controller.api;

import com.example.novel.dto.ProgressView;
import com.example.novel.entity.User;
import com.example.novel.service.ChapterService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.ReadingProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户相关 API(需登录)。
 */
@RestController
@RequestMapping("/api/me")
public class ApiMeController {

    private final ReadingProgressService progressService;
    private final ChapterService chapterService;
    private final CurrentUserService currentUserService;

    public ApiMeController(ReadingProgressService progressService,
                           ChapterService chapterService,
                           CurrentUserService currentUserService) {
        this.progressService = progressService;
        this.chapterService = chapterService;
        this.currentUserService = currentUserService;
    }

    /** GET /api/me/progress —— 我的阅读历史(按最近阅读时间倒序)。 */
    @GetMapping("/progress")
    public List<ProgressView> progress(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        return progressService.listHistory(user.getId()).stream()
                .map(p -> new ProgressView(
                        p.getNovel().getId(),
                        p.getNovel().getTitle(),
                        p.getNovel().getCover(),
                        p.getChapterNo(),
                        chapterService.countChapters(p.getNovel().getId()),
                        p.getUpdatedAt()))
                .toList();
    }
}
