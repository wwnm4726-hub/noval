package com.example.novel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P3-3 / P4-2 鉴权与错误响应测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("匿名访问后台页面 -> 跳转登录(3xx)")
    void adminPageAnonymousRedirect() throws Exception {
        mvc.perform(get("/admin/novels"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("普通用户访问后台页面 -> 403")
    void adminPageForbiddenForUser() throws Exception {
        mvc.perform(get("/admin/novels").with(user("test").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("管理员访问后台页面 -> 200")
    void adminPageAllowedForAdmin() throws Exception {
        mvc.perform(get("/admin/novels").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("匿名访问书架/历史 -> 跳转登录")
    void protectedPagesRedirect() throws Exception {
        mvc.perform(get("/bookshelf")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/history")).andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("匿名发表评论(API) -> 401 JSON")
    void anonymousCommentPostReturns401Json() throws Exception {
        mvc.perform(post("/api/novels/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"anonymous\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("匿名访问 /api/me/progress -> 401 JSON")
    void anonymousMeProgressReturns401Json() throws Exception {
        mvc.perform(get("/api/me/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("公开读接口匿名可访问 -> 200")
    void publicReadApiAnonymous() throws Exception {
        mvc.perform(get("/api/novels")).andExpect(status().isOk());
    }
}
