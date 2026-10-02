package com.example.novel.service;

import com.example.novel.entity.User;
import com.example.novel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

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
}