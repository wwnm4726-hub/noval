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

    long countByUserId(Long userId);

    long countByNovelId(Long novelId);

    long deleteByNovelId(Long novelId);

    /** 清理某用户的全部书架记录(删除用户时使用)。 */
    long deleteByUserId(Long userId);
}