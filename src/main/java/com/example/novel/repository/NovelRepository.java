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

    // 最近更新排序: 按 Novel.updatedAt 倒序。
    List<Novel> findAllByOrderByUpdatedAtDesc();

    List<Novel> findByCategoryOrderByUpdatedAtDesc(String category);

    List<Novel> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrderByUpdatedAtDesc(
            String title, String author);

    // 评分 / 收藏人数排序: 需聚合子查询,故用 @Query。category / keyword 为 null 表示不过滤。
    @Query("select n from Novel n " +
            "where (:category is null or n.category = :category) " +
            "and (:keyword is null or lower(n.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(n.author) like lower(concat('%', :keyword, '%'))) " +
            "order by (select coalesce(avg(r.score), 0.0) from Rating r where r.novel = n) desc, n.createdAt desc")
    List<Novel> findSortedByRating(@Param("category") String category, @Param("keyword") String keyword);

    @Query("select n from Novel n " +
            "where (:category is null or n.category = :category) " +
            "and (:keyword is null or lower(n.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(n.author) like lower(concat('%', :keyword, '%'))) " +
            "order by (select count(b) from Bookshelf b where b.novel = n) desc, n.createdAt desc")
    List<Novel> findSortedByCollect(@Param("category") String category, @Param("keyword") String keyword);

    /** 浏览量原子自增,避免并发下的读改写丢失。 */
    @Modifying
    @Query("update Novel n set n.viewCount = n.viewCount + 1 where n.id = :id")
    int incrementViewCount(@Param("id") Long id);
}