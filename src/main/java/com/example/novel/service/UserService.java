package com.example.novel.service;

import com.example.novel.entity.User;
import com.example.novel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NICKNAME_LENGTH = 50;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String password, String confirmPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        String u = username.trim();
        if (u.length() < 3 || u.length() > 50) {
            throw new IllegalArgumentException("用户名长度必须在 3 ~ 50 个字符之间");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 个字符");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        if (userRepository.existsByUsername(u)) {
            throw new IllegalArgumentException("用户名已存在,请换一个");
        }

        User user = new User(u, passwordEncoder.encode(password), "USER");
        return userRepository.save(user);
    }

    /**
     * 修改密码: 校验原密码,新密码需二次确认且满足最小长度。
     *
     * @throws IllegalArgumentException 原密码错误或新密码不合法
     */
    public void changePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("新密码至少 " + MIN_PASSWORD_LENGTH + " 个字符");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("用户 {} 修改密码成功", userId);
    }

    /**
     * 设置昵称: 去空格;留空表示清除(展示名回退用户名)。
     *
     * @throws IllegalArgumentException 昵称超长
     */
    public void updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String trimmed = nickname == null ? "" : nickname.trim();
        if (trimmed.length() > MAX_NICKNAME_LENGTH) {
            throw new IllegalArgumentException("昵称不能超过 " + MAX_NICKNAME_LENGTH + " 个字符");
        }
        user.setNickname(trimmed.isEmpty() ? null : trimmed);
        userRepository.save(user);
        log.info("用户 {} 更新昵称 -> {}", userId, user.getDisplayName());
    }
}