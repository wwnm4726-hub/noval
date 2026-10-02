package com.example.novel.service;

import com.example.novel.entity.Rating;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.RatingRepository;
import com.example.novel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 评分服务: 一人一评(可重复评分覆盖)、平均分与评分人数统计。
 */
@Service
@Transactional
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    /** 合法分值区间。 */
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 5;

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    public RatingService(RatingRepository ratingRepository,
                         UserRepository userRepository,
                         NovelRepository novelRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
    }

    /**
     * 打分: 同一用户对同一本书只保留一条,重复打分覆盖旧值。
     *
     * @throws IllegalArgumentException 分值不在 1 ~ 5
     */
    public void rate(Long userId, Long novelId, Integer score) {
        if (score == null || score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("评分必须为 " + MIN_SCORE + " ~ " + MAX_SCORE + " 星");
        }
        Rating rating = ratingRepository.findByUserIdAndNovelId(userId, novelId)
                .orElseGet(() -> new Rating(
                        userRepository.getReferenceById(userId),
                        novelRepository.getReferenceById(novelId),
                        score));
        rating.setScore(score);
        rating.setUpdatedAt(LocalDateTime.now());
        ratingRepository.save(rating);
        log.info("用户 {} 对小说 {} 评分 {}", userId, novelId, score);
    }

    @Transactional(readOnly = true)
    public Optional<Integer> userScore(Long userId, Long novelId) {
        if (userId == null || novelId == null) {
            return Optional.empty();
        }
        return ratingRepository.findByUserIdAndNovelId(userId, novelId).map(Rating::getScore);
    }

    /** 平均分(保留一位小数);无评分返回 0.0。 */
    @Transactional(readOnly = true)
    public double averageScore(Long novelId) {
        Double avg = ratingRepository.averageScoreByNovelId(novelId);
        if (avg == null) {
            return 0.0;
        }
        return Math.round(avg * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public long count(Long novelId) {
        return ratingRepository.countByNovelId(novelId);
    }

    public void deleteByNovel(Long novelId) {
        long removed = ratingRepository.deleteByNovelId(novelId);
        log.info("清理小说 {} 的评分 {} 条", novelId, removed);
    }
}
