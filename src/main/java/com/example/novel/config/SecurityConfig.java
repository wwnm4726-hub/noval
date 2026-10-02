package com.example.novel.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 页面请求未登录 -> 跳登录页;接口请求 -> JSON 401
        LoginUrlAuthenticationEntryPoint loginEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");
        // 页面请求无权限 -> 403 错误页(error.html);接口请求 -> JSON 403
        AccessDeniedHandlerImpl errorPageDeniedHandler = new AccessDeniedHandlerImpl();

        http
                // 关闭 CSRF: 表单登录 + H2 控制台需要
                .csrf(AbstractHttpConfigurer::disable)
                // H2 控制台需要 frameOptions 同源
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        // 运营后台: 仅管理员
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/admin/**")).hasRole("ADMIN")
                        // 详情页评论写操作(表单): 需登录(未登录跳登录页)
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/novels/*/comments")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/novels/*/comments/*/delete")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/novels/*/comments/*/reply")).authenticated()
                        // 阅读页章节评论写操作(表单): 需登录
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/novels/*/chapters/*/comments")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/novels/*/chapters/*/comments/*/delete")).authenticated()
                        // 详情页评论点赞/取消(表单): 需登录
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/comments/*/like")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/comments/*/unlike")).authenticated()
                        // 详情页打分(表单): 需登录
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/novels/*/rating")).authenticated()
                        // 评论写操作(API): 需登录
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/novels/*/comments")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/novels/*/chapters/*/comments")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/comments/*/reply")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/comments/*/like")).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.DELETE, "/api/comments/**")).authenticated()
                        // 当前用户接口: 需登录
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/me/**")).authenticated()
                        // 公开只读 API
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**")).permitAll()
                        // 阅读历史 / 书架: 需登录
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/history"),
                                AntPathRequestMatcher.antMatcher("/history/**"),
                                AntPathRequestMatcher.antMatcher("/bookshelf"),
                                AntPathRequestMatcher.antMatcher("/bookshelf/**")
                        ).authenticated()
                        // 公共资源
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/"),
                                AntPathRequestMatcher.antMatcher("/home"),
                                AntPathRequestMatcher.antMatcher("/login"),
                                AntPathRequestMatcher.antMatcher("/register"),
                                AntPathRequestMatcher.antMatcher("/logout"),
                                AntPathRequestMatcher.antMatcher("/css/**"),
                                AntPathRequestMatcher.antMatcher("/js/**"),
                                AntPathRequestMatcher.antMatcher("/images/**"),
                                AntPathRequestMatcher.antMatcher("/webjars/**"),
                                AntPathRequestMatcher.antMatcher("/h2-console/**"),
                                AntPathRequestMatcher.antMatcher("/error"),
                                AntPathRequestMatcher.antMatcher("/novels/**"),
                                AntPathRequestMatcher.antMatcher("/novel/**"),
                                AntPathRequestMatcher.antMatcher("/chapters/**"),
                                AntPathRequestMatcher.antMatcher("/authors/**")
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        // 接口请求未登录/无权限时返回 JSON,而非重定向或错误页
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) ->
                                        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期"),
                                new AntPathRequestMatcher("/api/**"))
                        .defaultAccessDeniedHandlerFor(
                                (request, response, accessDeniedException) ->
                                        writeJson(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问该资源"),
                                new AntPathRequestMatcher("/api/**"))
                        // 兜底: 其余(页面)请求走登录跳转 / 错误页
                        .defaultAuthenticationEntryPointFor(loginEntryPoint, AnyRequestMatcher.INSTANCE)
                        .defaultAccessDeniedHandlerFor(errorPageDeniedHandler, AnyRequestMatcher.INSTANCE)
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String error = status == HttpServletResponse.SC_UNAUTHORIZED ? "Unauthorized" : "Forbidden";
        response.getWriter().write(
                "{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }
}
