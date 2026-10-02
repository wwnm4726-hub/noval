package com.example.novel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P1-1 / P2-2 / P2-3 阅读进度与评论的端到端流程测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReaderFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("阅读章节后产生进度,详情页出现『继续阅读』,/api/me/progress 可查")
    void readingProgressFlow() throws Exception {
        // 以 test 用户阅读小说 1 第 2 章
        mvc.perform(get("/novels/1/chapters/2").with(user("test").roles("USER")))
                .andExpect(status().isOk());

        // 进度接口返回该进度
        mvc.perform(get("/api/me/progress").with(user("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"novelId\":1")))
                .andExpect(content().string(containsString("\"chapterNo\":2")));

        // 详情页出现『继续阅读』入口
        mvc.perform(get("/novels/1").with(user("test").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("继续阅读")));
    }

    @Test
    @DisplayName("发表评论 -> 列表可见 -> 删除自己的评论")
    void commentLifecycle() throws Exception {
        String response = mvc.perform(post("/api/novels/1/comments")
                        .with(user("test").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"集成测试评论\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.owner").value(true))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        long id = node.get("id").asLong();

        String listJson = mvc.perform(get("/api/novels/1/comments"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(listJson.contains("集成测试评论"), "评论列表应包含刚发表的评论");

        mvc.perform(delete("/api/comments/" + id).with(user("test").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("不能删除他人评论 -> 403")
    void cannotDeleteOthersComment() throws Exception {
        String response = mvc.perform(post("/api/novels/2/comments")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"管理员评论\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mvc.perform(delete("/api/comments/" + id).with(user("test").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 管理员本人可删
        mvc.perform(delete("/api/comments/" + id).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("空评论内容 -> 400 JSON")
    void blankCommentReturns400() throws Exception {
        mvc.perform(post("/api/novels/1/comments")
                        .with(user("test").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
