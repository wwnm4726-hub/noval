package com.example.novel.controller;

import com.example.novel.dto.NovelSummary;
import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.entity.User;
import com.example.novel.service.AdminUserService;
import com.example.novel.service.ChapterService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.NovelService;
import com.example.novel.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 运营后台(页面): 小说 / 章节的增删改查 + 数据概览 + 用户治理。
 * 访问控制由 SecurityConfig 统一以 ROLE_ADMIN 拦截。
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final StatisticsService statisticsService;
    private final AdminUserService adminUserService;
    private final CurrentUserService currentUserService;

    public AdminController(NovelService novelService,
                           ChapterService chapterService,
                           StatisticsService statisticsService,
                           AdminUserService adminUserService,
                           CurrentUserService currentUserService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.statisticsService = statisticsService;
        this.adminUserService = adminUserService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String home() {
        return "redirect:/admin/novels";
    }

    // ---------------- 数据概览 ----------------

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", statisticsService.dashboard());
        return "admin/dashboard";
    }

    // ---------------- 用户治理 ----------------

    @GetMapping("/users")
    public String users(@AuthenticationPrincipal UserDetails principal, Model model) {
        User me = currentUserService.find(principal);
        model.addAttribute("users", adminUserService.listUsers());
        model.addAttribute("currentUserId", me == null ? null : me.getId());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam String role,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes ra) {
        User me = currentUserService.require(principal);
        try {
            adminUserService.changeRole(me.getId(), id, role);
            ra.addFlashAttribute("message", "已调整用户角色");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes ra) {
        User me = currentUserService.require(principal);
        try {
            adminUserService.deleteUser(me.getId(), id);
            ra.addFlashAttribute("message", "已删除用户及其互动数据");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ---------------- 小说 ----------------

    @GetMapping("/novels")
    public String novels(Model model) {
        List<NovelSummary> items = novelService.listAll("new").stream()
                .map(this::toSummary)
                .toList();
        model.addAttribute("novels", items);
        return "admin/novels";
    }

    @GetMapping("/novels/new")
    public String newNovelForm(Model model) {
        model.addAttribute("novel", null);
        model.addAttribute("mode", "create");
        return "admin/novel-form";
    }

    @PostMapping("/novels")
    public String createNovel(@RequestParam String title,
                              @RequestParam String author,
                              @RequestParam(required = false) String cover,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String tags,
                              @RequestParam(defaultValue = "连载中") String status,
                              RedirectAttributes ra) {
        try {
            Novel novel = new Novel(title, author, cover, description, category, status);
            novel.setTags(tags);
            Novel saved = novelService.saveNovel(novel);
            ra.addFlashAttribute("message", "已新增小说《" + saved.getTitle() + "》");
            return "redirect:/admin/novels";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/novels/new";
        }
    }

    @GetMapping("/novels/{id}/edit")
    public String editNovelForm(@PathVariable Long id, Model model) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));
        model.addAttribute("novel", novel);
        model.addAttribute("mode", "edit");
        return "admin/novel-form";
    }

    @PostMapping("/novels/{id}")
    public String updateNovel(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String author,
                              @RequestParam(required = false) String cover,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String category,
                              @RequestParam(required = false) String tags,
                              @RequestParam(defaultValue = "连载中") String status,
                              RedirectAttributes ra) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));
        novel.setTitle(title);
        novel.setAuthor(author);
        novel.setCover(cover);
        novel.setDescription(description);
        novel.setCategory(category);
        novel.setTags(tags);
        novel.setStatus(status);
        try {
            novelService.saveNovel(novel);
            ra.addFlashAttribute("message", "已更新小说《" + novel.getTitle() + "》");
            return "redirect:/admin/novels";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/novels/" + id + "/edit";
        }
    }

    @PostMapping("/novels/{id}/delete")
    public String deleteNovel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            novelService.deleteNovel(id);
            ra.addFlashAttribute("message", "已删除小说及其全部章节");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/novels";
    }

    // ---------------- 章节 ----------------

    @GetMapping("/novels/{id}/chapters")
    public String chapters(@PathVariable Long id, Model model) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));
        model.addAttribute("novel", novel);
        model.addAttribute("chapters", chapterService.listChaptersAsc(id));
        return "admin/chapters";
    }

    @GetMapping("/novels/{id}/chapters/new")
    public String newChapterForm(@PathVariable Long id, Model model) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));
        model.addAttribute("novel", novel);
        model.addAttribute("chapter", null);
        model.addAttribute("nextNo", (int) chapterService.countChapters(id) + 1);
        model.addAttribute("mode", "create");
        return "admin/chapter-form";
    }

    @PostMapping("/novels/{id}/chapters")
    public String createChapter(@PathVariable Long id,
                                @RequestParam Integer chapterNo,
                                @RequestParam String title,
                                @RequestParam String content,
                                RedirectAttributes ra) {
        try {
            chapterService.saveChapter(id, null, chapterNo, title, content);
            ra.addFlashAttribute("message", "已新增第 " + chapterNo + " 章");
            return "redirect:/admin/novels/" + id + "/chapters";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/novels/" + id + "/chapters/new";
        }
    }

    @GetMapping("/novels/{id}/chapters/{no}/edit")
    public String editChapterForm(@PathVariable Long id, @PathVariable Integer no, Model model) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));
        Chapter chapter = chapterService.findChapter(id, no)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "章节不存在"));
        model.addAttribute("novel", novel);
        model.addAttribute("chapter", chapter);
        model.addAttribute("mode", "edit");
        return "admin/chapter-form";
    }

    @PostMapping("/novels/{id}/chapters/{no}")
    public String updateChapter(@PathVariable Long id,
                                @PathVariable Integer no,
                                @RequestParam Integer chapterNo,
                                @RequestParam String title,
                                @RequestParam String content,
                                RedirectAttributes ra) {
        Chapter chapter = chapterService.findChapter(id, no)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "章节不存在"));
        try {
            chapterService.saveChapter(id, chapter.getId(), chapterNo, title, content);
            ra.addFlashAttribute("message", "已更新第 " + chapterNo + " 章");
            return "redirect:/admin/novels/" + id + "/chapters";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/novels/" + id + "/chapters/" + no + "/edit";
        }
    }

    @PostMapping("/novels/{id}/chapters/{no}/delete")
    public String deleteChapter(@PathVariable Long id, @PathVariable Integer no, RedirectAttributes ra) {
        try {
            chapterService.deleteChapter(id, no);
            ra.addFlashAttribute("message", "已删除第 " + no + " 章");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/novels/" + id + "/chapters";
    }

    private NovelSummary toSummary(Novel n) {
        return new NovelSummary(n.getId(), n.getTitle(), n.getAuthor(), n.getCover(),
                n.getCategory(), n.getStatus(), n.getViewCount(), chapterService.countChapters(n.getId()));
    }
}
