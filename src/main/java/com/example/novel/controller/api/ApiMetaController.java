package com.example.novel.controller.api;

import com.example.novel.service.NovelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开只读 REST API: 元数据(标签等)。无需登录。
 */
@RestController
@RequestMapping("/api")
public class ApiMetaController {

    private static final Logger log = LoggerFactory.getLogger(ApiMetaController.class);

    private final NovelService novelService;

    public ApiMetaController(NovelService novelService) {
        this.novelService = novelService;
    }

    /** GET /api/tags 全部标签列表(去重、排序)。 */
    @GetMapping("/tags")
    public List<String> tags() {
        List<String> tags = novelService.allTags();
        log.debug("API 标签列表: {}", tags);
        return tags;
    }
}
