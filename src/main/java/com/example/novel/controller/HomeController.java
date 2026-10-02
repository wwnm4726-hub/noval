package com.example.novel.controller;

import com.example.novel.dto.PageResult;
import com.example.novel.entity.Novel;
import com.example.novel.service.NovelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final NovelService novelService;

    public HomeController(NovelService novelService) {
        this.novelService = novelService;
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "category", required = false) String category,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "sort", required = false, defaultValue = "new") String sort,
                        @RequestParam(value = "tag", required = false) String tag,
                        @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                        @RequestParam(value = "size", required = false, defaultValue = "12") int size,
                        Model model) {
        PageResult<Novel> result = novelService.page(category, keyword, sort, tag, page, size);
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("keyword", keyword);
            model.addAttribute("activeCategory", null);
        } else {
            model.addAttribute("activeCategory", category);
        }

        model.addAttribute("novels", result.items());
        model.addAttribute("total", result.total());
        model.addAttribute("page", result.page());
        model.addAttribute("size", result.size());
        model.addAttribute("totalPages", result.totalPages());
        model.addAttribute("hasPrev", result.hasPrev());
        model.addAttribute("hasNext", result.hasNext());
        model.addAttribute("categories", novelService.allCategories());
        model.addAttribute("tags", novelService.allTags());
        model.addAttribute("activeTag", tag);
        model.addAttribute("sort", sort);
        return "home";
    }
}
