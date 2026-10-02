package com.example.novel.controller;

import com.example.novel.entity.User;
import com.example.novel.service.BookshelfService;
import com.example.novel.service.CommentService;
import com.example.novel.service.CurrentUserService;
import com.example.novel.service.ReadingProgressService;
import com.example.novel.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 个人中心: 账号信息 / 使用统计 / 修改密码 / 设置昵称(均需登录)。
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final BookshelfService bookshelfService;
    private final CommentService commentService;
    private final ReadingProgressService progressService;
    private final CurrentUserService currentUserService;

    public ProfileController(UserService userService,
                             BookshelfService bookshelfService,
                             CommentService commentService,
                             ReadingProgressService progressService,
                             CurrentUserService currentUserService) {
        this.userService = userService;
        this.bookshelfService = bookshelfService;
        this.commentService = commentService;
        this.progressService = progressService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String view(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = currentUserService.require(principal);
        model.addAttribute("user", user);
        model.addAttribute("bookshelfCount", bookshelfService.countByUser(user.getId()));
        model.addAttribute("commentCount", commentService.countByUser(user.getId()));
        model.addAttribute("historyCount", progressService.countByUser(user.getId()));
        return "profile";
    }

    /** 修改密码: 校验原密码 + 二次确认。 */
    @PostMapping("/password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 @AuthenticationPrincipal UserDetails principal,
                                 RedirectAttributes ra) {
        User user = currentUserService.require(principal);
        try {
            userService.changePassword(user.getId(), oldPassword, newPassword, confirmPassword);
            ra.addFlashAttribute("passwordMessage", "密码修改成功");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("passwordError", ex.getMessage());
        }
        return "redirect:/profile";
    }

    /** 设置昵称: 留空表示清除,展示名回退用户名。 */
    @PostMapping("/nickname")
    public String updateNickname(@RequestParam(value = "nickname", required = false) String nickname,
                                 @AuthenticationPrincipal UserDetails principal,
                                 RedirectAttributes ra) {
        User user = currentUserService.require(principal);
        try {
            userService.updateNickname(user.getId(), nickname);
            ra.addFlashAttribute("nicknameMessage", "昵称已更新");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("nicknameError", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
