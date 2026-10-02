package com.example.novel.service;

import com.example.novel.entity.User;
import com.example.novel.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 当前登录用户解析: 供各控制器从 {@link UserDetails} 取回领域用户。
 */
@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 未登录返回 null。 */
    public User find(UserDetails principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getUsername()).orElse(null);
    }

    /** 未登录抛异常(用于受保护资源)。 */
    public User require(UserDetails principal) {
        User user = find(principal);
        if (user == null) {
            throw new IllegalStateException("未登录用户访问受保护资源");
        }
        return user;
    }
}
