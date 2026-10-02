package com.example.novel.repository;

import com.example.novel.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 按发表时间倒序列出某本小说的评论。 */
    List<Comment> findByNovelIdOrderByCreatedAtDesc(Long novelId);

    long countByNovelId(Long novelId);

    long countByUserId(Long userId);

    Optional<Comment> findByIdAndUserId(Long id, Long userId);

    long deleteByNovelId(Long novelId);
}
