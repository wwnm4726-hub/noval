package com.example.novel.repository;

import com.example.novel.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndNovelId(Long userId, Long novelId);

    long countByNovelId(Long novelId);

    /** 平均分;无评分时返回 null。 */
    @Query("select avg(r.score) from Rating r where r.novel.id = :novelId")
    Double averageScoreByNovelId(@Param("novelId") Long novelId);

    long deleteByNovelId(Long novelId);
}
