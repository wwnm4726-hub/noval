package com.example.novel.repository;

import com.example.novel.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 小说详情页评论: 针对整本小说(chapter 为空)的顶层评论(parent 为空),按时间倒序。 */
    List<Comment> findByNovelIdAndChapterIsNullAndParentIsNullOrderByCreatedAtDesc(Long novelId);

    /** 某章节的顶层评论,按时间正序。 */
    List<Comment> findByChapterIdAndParentIsNullOrderByCreatedAtAsc(Long chapterId);

    /** 某条顶层评论下的回复,按时间正序。 */
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByNovelId(Long novelId);

    long countByUserId(Long userId);

    /** 某用户发表的全部评论(含其回复),用于删除用户时清理。 */
    List<Comment> findByUserId(Long userId);

    Optional<Comment> findByIdAndUserId(Long id, Long userId);

    long deleteByNovelId(Long novelId);

    long deleteByChapterId(Long chapterId);

    long deleteByParentId(Long parentId);
}
