package com.example.novel.controller;

import com.example.novel.dto.BookshelfItem;
import com.example.novel.entity.Bookshelf;
import com.example.novel.entity.ReadingProgress;
import com.example.novel.entity.User;
import com.example.novel.service.BookshelfService;
import com.example.novel.service.ChapterService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.ReadingProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class BookshelfController {

    private final BookshelfService bookshelfService;
    private final ChapterService chapterService;
    private final ReadingProgressService progressService;
    private final CurrentUserService currentUserService;

    public BookshelfController(BookshelfService bookshelfService,
                               ChapterService chapterService,
                               ReadingProgressService progressService,
                               CurrentUserService currentUserService) {
        this.bookshelfService = bookshelfService;
        this.chapterService = chapterService;
        this.progressService = progressService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/bookshelf")
    public String view(@AuthenticationPrincipal UserDetails principal,
                       Model model) {
        User user = currentUserService.require(principal);

        Map<Long, ReadingProgress> progressByNovel = progressService.listHistory(user.getId()).stream()
                .collect(Collectors.toMap(p -> p.getNovel().getId(), Function.identity(), (a, b) -> a));

        List<BookshelfItem> items = bookshelfService.listEntries(user.getId()).stream()
                .map(entry -> toItem(entry, progressByNovel))
                .sorted(Comparator.comparing(
                        BookshelfItem::getLastReadAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("items", items);
        model.addAttribute("bookshelfCount", items.size());
        return "bookshelf";
    }

    @PostMapping("/bookshelf/add")
    public String add(@RequestParam("novelId") Long novelId,
                      @AuthenticationPrincipal UserDetails principal,
                      RedirectAttributes redirectAttributes) {
        User user = currentUserService.require(principal);
        try {
            bookshelfService.addToBookshelf(user.getId(), novelId);
            redirectAttributes.addFlashAttribute("bookshelfAdded", true);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("bookshelfError", ex.getMessage());
        }
        return "redirect:/novels/" + novelId;
    }

    @PostMapping("/bookshelf/remove")
    public String remove(@RequestParam("novelId") Long novelId,
                         @AuthenticationPrincipal UserDetails principal) {
        User user = currentUserService.require(principal);
        bookshelfService.removeFromBookshelf(user.getId(), novelId);
        return "redirect:/bookshelf";
    }

    private BookshelfItem toItem(Bookshelf entry, Map<Long, ReadingProgress> progressByNovel) {
        Long novelId = entry.getNovel().getId();
        ReadingProgress progress = progressByNovel.get(novelId);
        long total = chapterService.countChapters(novelId);
        return new BookshelfItem(
                entry.getNovel(),
                progress == null ? null : progress.getChapterNo(),
                total,
                progress == null ? null : progress.getUpdatedAt());
    }
}
