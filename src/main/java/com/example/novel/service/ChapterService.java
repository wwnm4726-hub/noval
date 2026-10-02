package com.example.novel.service;

import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.repository.ChapterRepository;
import com.example.novel.repository.NovelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 章节服务: 查询 + 管理端增删改(含章节号唯一校验)。
 */
@Service
@Transactional(readOnly = true)
public class ChapterService {

    private static final Logger log = LoggerFactory.getLogger(ChapterService.class);

    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;

    public ChapterService(ChapterRepository chapterRepository, NovelRepository novelRepository) {
        this.chapterRepository = chapterRepository;
        this.novelRepository = novelRepository;
    }

    public List<Chapter> listChaptersAsc(Long novelId) {
        return chapterRepository.findByNovelIdOrderByChapterNoAsc(novelId);
    }

    public List<Chapter> listChaptersDesc(Long novelId) {
        return chapterRepository.findByNovelIdOrderByChapterNoDesc(novelId);
    }

    public Optional<Chapter> findChapter(Long novelId, Integer chapterNo) {
        if (chapterNo == null) return Optional.empty();
        return chapterRepository.findByNovelIdAndChapterNo(novelId, chapterNo);
    }

    public long countChapters(Long novelId) {
        return chapterRepository.countByNovelId(novelId);
    }

    public Optional<Integer> previousChapterNo(Long novelId, Integer current) {
        return chapterRepository.findByNovelIdAndChapterNo(novelId, current)
                .flatMap(c -> chapterRepository.findByNovelIdAndChapterNo(novelId, current - 1))
                .map(Chapter::getChapterNo);
    }

    public Optional<Integer> nextChapterNo(Long novelId, Integer current) {
        return chapterRepository.findByNovelIdAndChapterNo(novelId, current)
                .flatMap(c -> chapterRepository.findByNovelIdAndChapterNo(novelId, current + 1))
                .map(Chapter::getChapterNo);
    }

    /**
     * 新增或更新章节。
     *
     * @param chapterId 为 null 表示新增,否则为更新的章节主键
     * @throws IllegalArgumentException 参数非法或同一本书内章节号重复
     */
    @Transactional
    public Chapter saveChapter(Long novelId, Long chapterId, Integer chapterNo,
                               String title, String content) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new IllegalArgumentException("小说不存在"));
        if (chapterNo == null || chapterNo < 1) {
            throw new IllegalArgumentException("章节号必须为不小于 1 的整数");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("章节标题不能为空");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("章节正文不能为空");
        }

        Optional<Chapter> existingWithNo = chapterRepository.findByNovelIdAndChapterNo(novelId, chapterNo);
        Chapter chapter;
        if (chapterId == null) {
            if (existingWithNo.isPresent()) {
                throw new IllegalArgumentException("第 " + chapterNo + " 章已存在,章节号不可重复");
            }
            chapter = new Chapter(title.trim(), content, chapterNo);
            chapter.setNovel(novel);
        } else {
            chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new IllegalArgumentException("章节不存在"));
            if (existingWithNo.isPresent() && !existingWithNo.get().getId().equals(chapterId)) {
                throw new IllegalArgumentException("第 " + chapterNo + " 章已存在,章节号不可重复");
            }
            chapter.setChapterNo(chapterNo);
            chapter.setTitle(title.trim());
            chapter.setContent(content);
        }
        // 章节新增/修改视为这本书的"最近更新",用于首页「最近更新」排序。
        novel.setUpdatedAt(LocalDateTime.now());
        Chapter saved = chapterRepository.save(chapter);
        log.info("保存章节 novelId={}, chapterId={}, no={}, title={}",
                novelId, saved.getId(), saved.getChapterNo(), saved.getTitle());
        return saved;
    }

    @Transactional
    public void deleteChapter(Long novelId, Integer chapterNo) {
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNo(novelId, chapterNo)
                .orElseThrow(() -> new IllegalArgumentException("章节不存在"));
        chapterRepository.delete(chapter);
        log.info("删除章节 novelId={}, no={}", novelId, chapterNo);
    }
}
