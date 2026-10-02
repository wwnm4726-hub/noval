package com.example.novel.service;

import com.example.novel.entity.ReadingProgress;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.ReadingProgressRepository;
import com.example.novel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 阅读进度服务: 记录与查询用户在各小说的阅读章节。
 */
@Service
@Transactional
public class ReadingProgressService {

    private static final Logger log = LoggerFactory.getLogger(ReadingProgressService.class);

    private final ReadingProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    public ReadingProgressService(ReadingProgressRepository progressRepository,
                                  UserRepository userRepository,
                                  NovelRepository novelRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
    }

    /**
     * 记录/更新阅读进度(幂等 upsert)。读到新章节时覆盖旧进度并刷新时间戳。
     */
    public void saveProgress(Long userId, Long novelId, Integer chapterNo) {
        if (userId == null || novelId == null || chapterNo == null) {
            return;
        }
        ReadingProgress progress = progressRepository.findByUserIdAndNovelId(userId, novelId)
                .orElseGet(() -> new ReadingProgress(
                        userRepository.getReferenceById(userId),
                        novelRepository.getReferenceById(novelId),
                        chapterNo));
        progress.setChapterNo(chapterNo);
        progress.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(progress);
        log.debug("保存阅读进度: userId={}, novelId={}, chapterNo={}", userId, novelId, chapterNo);
    }

    @Transactional(readOnly = true)
    public Optional<ReadingProgress> getProgress(Long userId, Long novelId) {
        if (userId == null || novelId == null) {
            return Optional.empty();
        }
        return progressRepository.findByUserIdAndNovelId(userId, novelId);
    }

    /** 阅读历史: 按最近阅读时间倒序。 */
    @Transactional(readOnly = true)
    public List<ReadingProgress> listHistory(Long userId) {
        return progressRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public void deleteByNovel(Long novelId) {
        long removed = progressRepository.deleteByNovelId(novelId);
        log.info("清理小说 {} 的阅读进度 {} 条", novelId, removed);
    }
}
