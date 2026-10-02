package com.example.novel.service;

import com.example.novel.dto.PageResult;
import com.example.novel.entity.Novel;
import com.example.novel.repository.BookshelfRepository;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.RatingRepository;
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
    public static final String SORT_UPDATE = "update";
    public static final String SORT_RATING = "rating";
    public static final String SORT_COLLECT = "collect";

    private final NovelRepository novelRepository;
    private final ReadingProgressRepository progressRepository;
    private final RatingRepository ratingRepository;
    private final BookshelfRepository bookshelfRepository;
    private final CommentService commentService;

    public NovelService(NovelRepository novelRepository,
                        ReadingProgressRepository progressRepository,
                        RatingRepository ratingRepository,
                        BookshelfRepository bookshelfRepository,
                        CommentService commentService) {
        this.novelRepository = novelRepository;
        this.progressRepository = progressRepository;
        this.ratingRepository = ratingRepository;
        this.bookshelfRepository = bookshelfRepository;
        this.commentService = commentService;
    }

    public List<Novel> listAll(String sort) {
        if (SORT_UPDATE.equalsIgnoreCase(sort)) {
            return novelRepository.findAllByOrderByUpdatedAtDesc();
        }
        if (SORT_RATING.equalsIgnoreCase(sort)) {
            return novelRepository.findSortedByRating(null, null);
        }
        if (SORT_COLLECT.equalsIgnoreCase(sort)) {
            return novelRepository.findSortedByCollect(null, null);
        }
        if (SORT_HOT.equalsIgnoreCase(sort)) {
            return novelRepository.findAllByOrderByViewCountDesc();
        }
        return novelRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Novel> listByCategory(String category, String sort) {
        if (category == null || category.isBlank()) {
            return listAll(sort);
        }
        if (SORT_UPDATE.equalsIgnoreCase(sort)) {
            return novelRepository.findByCategoryOrderByUpdatedAtDesc(category);
        }
        if (SORT_RATING.equalsIgnoreCase(sort)) {
            return novelRepository.findSortedByRating(category, null);
        }
        if (SORT_COLLECT.equalsIgnoreCase(sort)) {
            return novelRepository.findSortedByCollect(category, null);
        }
        if (SORT_HOT.equalsIgnoreCase(sort)) {
            return novelRepository.findByCategoryOrderByViewCountDesc(category);
        }
        return novelRepository.findByCategoryOrderByCreatedAtDesc(category);
    }

    /**
     * 分页后的列表(保留分类/关键词/排序/标签筛选),页码越界时自动收敛到有效范围。
     */
    public PageResult<Novel> page(String category, String keyword, String sort, String tag,
                                  int page, int size) {
        List<Novel> all = (keyword != null && !keyword.isBlank())
                ? search(keyword, sort)
                : listByCategory(category, sort);
        return PageResult.of(filterByTag(all, tag), page, size);
    }

    /** 全部标签(去重、排序),用于首页/搜索的标签筛选条。 */
    public List<String> allTags() {
        return novelRepository.findAll().stream()
                .flatMap(n -> n.getTagList().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** 某作者的全部作品,按上架时间倒序。 */
    public List<Novel> listByAuthor(String author) {
        if (author == null || author.isBlank()) {
            return List.of();
        }
        return novelRepository.findByAuthorOrderByCreatedAtDesc(author.trim());
    }

    private List<Novel> filterByTag(List<Novel> novels, String tag) {
        if (tag == null || tag.isBlank()) {
            return novels;
        }
        String t = tag.trim();
        return novels.stream().filter(n -> n.getTagList().contains(t)).toList();
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
        if (SORT_UPDATE.equalsIgnoreCase(sort)) {
            return novelRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrderByUpdatedAtDesc(keyword, keyword);
        }
        if (SORT_RATING.equalsIgnoreCase(sort)) {
            return novelRepository.findSortedByRating(null, keyword);
        }
        if (SORT_COLLECT.equalsIgnoreCase(sort)) {
            return novelRepository.findSortedByCollect(null, keyword);
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
        commentService.deleteByNovel(id);
        progressRepository.deleteByNovelId(id);
        ratingRepository.deleteByNovelId(id);
        bookshelfRepository.deleteByNovelId(id);
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
