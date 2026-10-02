package com.example.novel.service;

import com.example.novel.entity.Novel;
import com.example.novel.repository.CommentRepository;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.ReadingProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 小说服务: 列表/搜索/详情 + 浏览量 + 管理端增删改。
 */
@Service
@Transactional(readOnly = true)
public class NovelService {

    private static final Logger log = LoggerFactory.getLogger(NovelService.class);

    /** 首页排序方式常量。 */
    public static final String SORT_HOT = "hot";

    private final NovelRepository novelRepository;
    private final CommentRepository commentRepository;
    private final ReadingProgressRepository progressRepository;

    public NovelService(NovelRepository novelRepository,
                        CommentRepository commentRepository,
                        ReadingProgressRepository progressRepository) {
        this.novelRepository = novelRepository;
        this.commentRepository = commentRepository;
        this.progressRepository = progressRepository;
    }

    public List<Novel> listAll(String sort) {
        if (SORT_HOT.equalsIgnoreCase(sort)) {
            return novelRepository.findAllByOrderByViewCountDesc();
        }
        return novelRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Novel> listByCategory(String category, String sort) {
        if (category == null || category.isBlank()) {
            return listAll(sort);
        }
        if (SORT_HOT.equalsIgnoreCase(sort)) {
            return novelRepository.findByCategoryOrderByViewCountDesc(category);
        }
        return novelRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    public List<String> allCategories() {
        return novelRepository.findAll().stream()
                .map(Novel::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Optional<Novel> findById(Long id) {
        return novelRepository.findById(id);
    }

    public List<Novel> search(String keyword, String sort) {
        if (keyword == null || keyword.isBlank()) {
            return listAll(sort);
        }
        if (SORT_HOT.equalsIgnoreCase(sort)) {
            return novelRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrderByViewCountDesc(keyword, keyword);
        }
        return novelRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
    }

    /** 浏览量 +1(详情页访问时调用)。 */
    @Transactional
    public void incrementViewCount(Long id) {
        int updated = novelRepository.incrementViewCount(id);
        if (updated > 0) {
            log.debug("小说 {} 浏览量 +1", id);
        }
    }

    @Transactional
    public Novel saveNovel(Novel novel) {
        validate(novel);
        Novel saved = novelRepository.save(novel);
        log.info("保存小说 id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    /**
     * 删除小说,并级联清理其评论、阅读进度;章节由 {@code Novel.chapters} 的 cascade 一并删除。
     */
    @Transactional
    public void deleteNovel(Long id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("小说不存在"));
        commentRepository.deleteByNovelId(id);
        progressRepository.deleteByNovelId(id);
        novelRepository.delete(novel);
        log.info("删除小说 id={}, title={} (含其评论/进度/章节)", id, novel.getTitle());
    }

    private void validate(Novel novel) {
        if (novel.getTitle() == null || novel.getTitle().isBlank()) {
            throw new IllegalArgumentException("书名不能为空");
        }
        if (novel.getAuthor() == null || novel.getAuthor().isBlank()) {
            throw new IllegalArgumentException("作者不能为空");
        }
    }
}
