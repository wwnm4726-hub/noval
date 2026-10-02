package com.example.novel.repository;

import com.example.novel.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByNovelIdOrderByChapterNoAsc(Long novelId);

    List<Chapter> findByNovelIdOrderByChapterNoDesc(Long novelId);

    Optional<Chapter> findByNovelIdAndChapterNo(Long novelId, Integer chapterNo);

    long countByNovelId(Long novelId);

    boolean existsByNovelIdAndChapterNo(Long novelId, Integer chapterNo);

    long deleteByNovelId(Long novelId);
}