package com.example.novel.repository;

import com.example.novel.entity.Bookshelf;
import com.example.novel.entity.Novel;
import com.example.novel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {

    List<Bookshelf> findByUser(User user);

    List<Bookshelf> findByUserId(Long userId);

    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    long deleteByUserIdAndNovelId(Long userId, Long novelId);

    boolean existsByUserAndNovel(User user, Novel novel);
}