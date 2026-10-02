package com.example.novel.repository;

import com.example.novel.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {

    Optional<ReadingProgress> findByUserIdAndNovelId(Long userId, Long novelId);

    /** 按最近阅读时间倒序列出某用户的全部进度(用于阅读历史)。 */
    List<ReadingProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);

    long deleteByNovelId(Long novelId);
}
