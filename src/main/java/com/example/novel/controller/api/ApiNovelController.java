package com.example.novel.controller.api;

import com.example.novel.dto.ChapterContent;
import com.example.novel.dto.ChapterSummary;
import com.example.novel.dto.NovelDetail;
import com.example.novel.dto.NovelSummary;
import com.example.novel.dto.RatingSummary;
import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.service.ChapterService;
import com.example.novel.service.NovelService;
import com.example.novel.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 只读公开 REST API: 小说列表 / 详情 / 章节列表 / 章节正文。
 * 支持关键词搜索与分类筛选。全部端点无需登录。
 */
@RestController
@RequestMapping("/api/novels")
public class ApiNovelController {

    private static final Logger log = LoggerFactory.getLogger(ApiNovelController.class);

    private final NovelService novelService;
    private final ChapterService chapterService;
    private final RatingService ratingService;

    public ApiNovelController(NovelService novelService,
                              ChapterService chapterService,
                              RatingService ratingService) {
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.ratingService = ratingService;
    }

    /** GET /api/novels?category=&keyword=&tag=&sort=new|hot&page=&size= */
    @GetMapping
    public List<NovelSummary> list(@RequestParam(value = "category", required = false) String category,
                                   @RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "tag", required = false) String tag,
                                   @RequestParam(value = "sort", required = false, defaultValue = "new") String sort,
                                   @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                   @RequestParam(value = "size", required = false, defaultValue = "12") int size) {
        List<Novel> novels = novelService.page(category, keyword, sort, tag, page, size).items();
        log.debug("API 小说列表: category={}, keyword={}, tag={}, sort={}, page={}, size={}, 返回={}",
                category, keyword, tag, sort, page, size, novels.size());
        return novels.stream().map(this::toSummary).toList();
    }

    /** GET /api/novels/{id} 详情(含标签与评分摘要)。 */
    @GetMapping("/{id}")
    public NovelDetail detail(@PathVariable("id") Long id) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));
        long ratingCount = ratingService.count(id);
        Double avg = ratingCount > 0 ? ratingService.averageScore(id) : null;
        return new NovelDetail(
                novel.getId(), novel.getTitle(), novel.getAuthor(), novel.getCover(),
                novel.getDescription(), novel.getCategory(), novel.getTagList(), novel.getStatus(),
                novel.getViewCount(), chapterService.countChapters(id), avg, ratingCount);
    }

    /** GET /api/novels/{id}/rating 评分摘要(平均分 + 评分人数)。 */
    @GetMapping("/{id}/rating")
    public RatingSummary rating(@PathVariable("id") Long id) {
        if (novelService.findById(id).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "小说不存在");
        }
        long count = ratingService.count(id);
        Double avg = count > 0 ? ratingService.averageScore(id) : null;
        log.debug("API 评分摘要: novelId={}, avg={}, count={}", id, avg, count);
        return new RatingSummary(id, avg, count);
    }

    /** GET /api/novels/{id}/chapters */
    @GetMapping("/{id}/chapters")
    public List<ChapterSummary> chapters(@PathVariable("id") Long id) {
        if (novelService.findById(id).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "小说不存在");
        }
        return chapterService.listChaptersAsc(id).stream()
                .map(c -> new ChapterSummary(c.getId(), c.getChapterNo(), c.getTitle(), c.getWordCount()))
                .toList();
    }

    /** GET /api/novels/{id}/chapters/{no} */
    @GetMapping("/{id}/chapters/{no}")
    public ChapterContent chapter(@PathVariable("id") Long id, @PathVariable("no") Integer no) {
        Novel novel = novelService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "小说不存在"));
        Chapter chapter = chapterService.findChapter(id, no)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "第 " + no + " 章不存在"));

        Optional<Integer> prev = chapterService.previousChapterNo(id, no);
        Optional<Integer> next = chapterService.nextChapterNo(id, no);
        return new ChapterContent(
                chapter.getId(), novel.getId(), novel.getTitle(), chapter.getChapterNo(),
                chapter.getTitle(), chapter.getContent(), chapter.getWordCount(),
                prev.orElse(null), next.orElse(null), chapterService.countChapters(id));
    }

    private NovelSummary toSummary(Novel n) {
        return new NovelSummary(n.getId(), n.getTitle(), n.getAuthor(), n.getCover(),
                n.getCategory(), n.getStatus(), n.getViewCount(), chapterService.countChapters(n.getId()));
    }
}
