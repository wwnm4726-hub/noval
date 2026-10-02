package com.example.novel.service;

import com.example.novel.entity.Bookshelf;
import com.example.novel.entity.Novel;
import com.example.novel.entity.User;
import com.example.novel.repository.BookshelfRepository;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    public BookshelfService(BookshelfRepository bookshelfRepository,
                            UserRepository userRepository,
                            NovelRepository novelRepository) {
        this.bookshelfRepository = bookshelfRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
    }

    @Transactional(readOnly = true)
    public List<Novel> listBookshelf(Long userId) {
        return bookshelfRepository.findByUserId(userId).stream()
                .map(Bookshelf::getNovel)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Bookshelf> listEntries(Long userId) {
        return bookshelfRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsInBookshelf(Long userId, Long novelId) {
        return bookshelfRepository.existsByUserIdAndNovelId(userId, novelId);
    }

    /** 追更人数: 某小说的书架收藏数。 */
    @Transactional(readOnly = true)
    public long countByNovel(Long novelId) {
        return bookshelfRepository.countByNovelId(novelId);
    }

    /** 我在书架中的藏书数。 */
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        return bookshelfRepository.countByUserId(userId);
    }

    public void addToBookshelf(Long userId, Long novelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new IllegalArgumentException("小说不存在"));

        if (bookshelfRepository.existsByUserIdAndNovelId(userId, novelId)) {
            return; // 幂等: 不重复添加
        }
        bookshelfRepository.save(new Bookshelf(user, novel));
    }

    public void removeFromBookshelf(Long userId, Long novelId) {
        bookshelfRepository.deleteByUserIdAndNovelId(userId, novelId);
    }
}