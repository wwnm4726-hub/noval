package com.example.novel.service;

import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.repository.ChapterRepository;
import com.example.novel.repository.NovelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * P3-2 章节号唯一性校验单元测试。
 */
@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private NovelRepository novelRepository;

    @InjectMocks
    private ChapterService chapterService;

    @Test
    @DisplayName("新增章节时章节号重复 -> 抛 IllegalArgumentException")
    void createWithDuplicateChapterNoThrows() {
        when(novelRepository.findById(1L)).thenReturn(Optional.of(new Novel()));
        when(chapterRepository.findByNovelIdAndChapterNo(1L, 2))
                .thenReturn(Optional.of(new Chapter("已存在", "内容", 2)));

        assertThrows(IllegalArgumentException.class,
                () -> chapterService.saveChapter(1L, null, 2, "新标题", "新内容"));
    }

    @Test
    @DisplayName("章节号非法(<=0) -> 抛 IllegalArgumentException")
    void createWithInvalidChapterNoThrows() {
        when(novelRepository.findById(1L)).thenReturn(Optional.of(new Novel()));

        assertThrows(IllegalArgumentException.class,
                () -> chapterService.saveChapter(1L, null, 0, "标题", "内容"));
    }

    @Test
    @DisplayName("标题为空 -> 抛 IllegalArgumentException")
    void createWithBlankTitleThrows() {
        when(novelRepository.findById(1L)).thenReturn(Optional.of(new Novel()));

        assertThrows(IllegalArgumentException.class,
                () -> chapterService.saveChapter(1L, null, 1, "  ", "内容"));
    }

    @Test
    @DisplayName("小说不存在 -> 抛 IllegalArgumentException")
    void createWithMissingNovelThrows() {
        when(novelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> chapterService.saveChapter(99L, null, 1, "标题", "内容"));
    }
}
