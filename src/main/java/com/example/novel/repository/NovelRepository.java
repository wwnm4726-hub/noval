package com.example.novel.repository;

import com.example.novel.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    List<Novel> findByCategory(String category);

    List<Novel> findByCategoryOrderByCreatedAtDesc(String category);

    List<Novel> findAllByOrderByCreatedAtDesc();

    List<Novel> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    // 热门排序: 按浏览量倒序
    List<Novel> findAllByOrderByViewCountDesc();

    List<Novel> findByCategoryOrderByViewCountDesc(String category);

    List<Novel> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrderByViewCountDesc(
            String title, String author);

    /** 浏览量原子自增,避免并发下的读改写丢失。 */
    @Modifying
    @Query("update Novel n set n.viewCount = n.viewCount + 1 where n.id = :id")
    int incrementViewCount(@Param("id") Long id);
}