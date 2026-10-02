package com.example.novel.controller;

import com.example.novel.entity.Novel;
import com.example.novel.service.NovelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
                        Model model) {
        List<Novel> novels;
        if (keyword != null && !keyword.isBlank()) {
            novels = novelService.search(keyword, sort);
            model.addAttribute("keyword", keyword);
            model.addAttribute("activeCategory", null);
        } else {
            novels = novelService.listByCategory(category, sort);
            model.addAttribute("activeCategory", category);
        }

        model.addAttribute("novels", novels);
        model.addAttribute("categories", novelService.allCategories());
        model.addAttribute("sort", sort);
        return "home";
    }
}
