package com.example.novel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P4-1 只读 REST API 集成测试(/api/novels 系列)。
 */
@SpringBootTest
@AutoConfigureMockMvc
class NovelApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("GET /api/novels 返回种子小说列表,匿名可访问")
    void listAll() throws Exception {
        mvc.perform(get("/api/novels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].chapterCount").value(3));
    }

    @Test
    @DisplayName("sort=hot 按浏览量倒序")
    void hotSort() throws Exception {
        mvc.perform(get("/api/novels").param("sort", "hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].viewCount").value(213))
                .andExpect(jsonPath("$[1].viewCount").value(152))
                .andExpect(jsonPath("$[2].viewCount").value(97));
    }

    @Test
    @DisplayName("按分类筛选")
    void categoryFilter() throws Exception {
        mvc.perform(get("/api/novels").param("category", "玄幻"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("九天神帝"));
    }

    @Test
    @DisplayName("关键词搜索命中书名")
    void keywordSearch() throws Exception {
        mvc.perform(get("/api/novels").param("keyword", "都市"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/novels/{id} 详情")
    void detail() throws Exception {
        mvc.perform(get("/api/novels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("九天神帝"))
                .andExpect(jsonPath("$.chapterTotal").value(3));
    }

    @Test
    @DisplayName("GET /api/novels/{id} 不存在 -> 404 JSON")
    void detailNotFound() throws Exception {
        mvc.perform(get("/api/novels/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/novels/{id}/chapters 章节列表")
    void chapters() throws Exception {
        mvc.perform(get("/api/novels/1/chapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].chapterNo").value(1))
                .andExpect(jsonPath("$[2].chapterNo").value(3));
    }

    @Test
    @DisplayName("GET /api/novels/{id}/chapters/{no} 章节正文含导航")
    void chapterContent() throws Exception {
        mvc.perform(get("/api/novels/1/chapters/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chapterNo").value(2))
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.prevNo").value(1))
                .andExpect(jsonPath("$.nextNo").value(3));
    }

    @Test
    @DisplayName("GET /api/novels/{id}/chapters/{no} 不存在 -> 404 JSON")
    void chapterNotFound() throws Exception {
        mvc.perform(get("/api/novels/1/chapters/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
