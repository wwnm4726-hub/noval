package com.example.novel.repository;

import com.example.novel.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    long deleteByUserIdAndCommentId(Long userId, Long commentId);

    long deleteByCommentId(Long commentId);

    /** 清理某用户给出的全部点赞(删除用户时使用)。 */
    long deleteByUserId(Long userId);

    /** 清理某条评论下所有回复的点赞(删除顶层评论时使用)。 */
    @Modifying
    @Query("delete from CommentLike cl where cl.comment.parent.id = :parentId")
    int deleteByParentCommentId(@Param("parentId") Long parentId);

    /** 清理某本小说所有评论的点赞(删除小说时使用)。 */
    @Modifying
    @Query("delete from CommentLike cl where cl.comment.novel.id = :novelId")
    int deleteByNovelId(@Param("novelId") Long novelId);

    /** 清理某章节所有评论的点赞(删除章节时使用)。 */
    @Modifying
    @Query("delete from CommentLike cl where cl.comment.chapter.id = :chapterId")
    int deleteByChapterId(@Param("chapterId") Long chapterId);
}
