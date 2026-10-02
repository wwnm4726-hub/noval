package com.example.novel.controller;

import com.example.novel.entity.Novel;
import com.example.novel.service.ChapterService;
import com.example.novel.service.NovelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 作者主页: 展示某作者的全部作品列表与作品数量。
 */
@Controller
public class AuthorController {

    private final NovelService novelService;
    private final ChapterService chapterService;

    public AuthorController(NovelService novelService, ChapterService chapterService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
    }

    @GetMapping("/authors/{name}")
    public String author(@PathVariable("name") String name, Model model) {
        List<Novel> novels = novelService.listByAuthor(name);
        model.addAttribute("author", name);
        model.addAttribute("novels", novels);
        model.addAttribute("novelCount", novels.size());
        model.addAttribute("chapterCounts", novels.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Novel::getId, n -> chapterService.countChapters(n.getId()))));
        return "author";
    }
}
