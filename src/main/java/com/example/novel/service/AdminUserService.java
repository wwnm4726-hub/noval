package com.example.novel.service;

import com.example.novel.entity.User;
import com.example.novel.repository.BookshelfRepository;
import com.example.novel.repository.RatingRepository;
import com.example.novel.repository.ReadingProgressRepository;
import com.example.novel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台用户治理服务: 用户列表 / 角色调整 / 删除(含其互动数据清理)。
 * 约束: 不得删除或降级当前登录的自己,避免平台失去最后一个管理员。
 */
@Service
@Transactional
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";

    private final UserRepository userRepository;
    private final CommentService commentService;
    private final RatingRepository ratingRepository;
    private final BookshelfRepository bookshelfRepository;
    private final ReadingProgressRepository progressRepository;

    public AdminUserService(UserRepository userRepository,
                            CommentService commentService,
                            RatingRepository ratingRepository,
                            BookshelfRepository bookshelfRepository,
                            ReadingProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.ratingRepository = ratingRepository;
        this.bookshelfRepository = bookshelfRepository;
        this.progressRepository = progressRepository;
    }

    /** 全部注册用户,按注册(id)升序。 */
    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * 调整用户角色至 USER / ADMIN。
     *
     * @throws IllegalArgumentException 角色非法、用户不存在或试图修改当前登录自己
     */
    public void changeRole(Long operatorId, Long targetId, String role) {
        if (!ROLE_USER.equals(role) && !ROLE_ADMIN.equals(role)) {
            throw new IllegalArgumentException("角色只能为 USER 或 ADMIN");
        }
        if (operatorId.equals(targetId)) {
            throw new IllegalArgumentException("不能修改当前登录自己的角色");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        target.setRole(role);
        userRepository.save(target);
        log.info("管理员 {} 将用户 {} 的角色调整为 {}", operatorId, targetId, role);
    }

    /**
     * 删除用户并清理其评论/点赞/评分/书架/阅读进度。
     *
     * @throws IllegalArgumentException 用户不存在或试图删除当前登录自己
     */
    public void deleteUser(Long operatorId, Long targetId) {
        if (operatorId.equals(targetId)) {
            throw new IllegalArgumentException("不能删除当前登录的自己");
        }
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        commentService.deleteByUser(targetId);
        ratingRepository.deleteByUserId(targetId);
        bookshelfRepository.deleteByUserId(targetId);
        progressRepository.deleteByUserId(targetId);
        userRepository.delete(target);
        log.info("管理员 {} 删除用户 {} ({})", operatorId, targetId, target.getUsername());
    }
}
