package com.example.novel.service;

import com.example.novel.dto.DashboardStats;
import com.example.novel.repository.ChapterRepository;
import com.example.novel.repository.CommentRepository;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.RatingRepository;
import com.example.novel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 运营统计服务: 汇总后台数据概览所需的核心指标。
 */
@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;

    public StatisticsService(NovelRepository novelRepository,
                             ChapterRepository chapterRepository,
                             UserRepository userRepository,
                             CommentRepository commentRepository,
                             RatingRepository ratingRepository) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
    }

    /** 汇总后台数据概览指标(小说/章节/用户/评论/评分)。 */
    public DashboardStats dashboard() {
        return new DashboardStats(
                novelRepository.count(),
                chapterRepository.count(),
                userRepository.count(),
                commentRepository.count(),
                ratingRepository.count());
    }
}
