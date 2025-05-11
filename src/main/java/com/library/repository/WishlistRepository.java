package com.library.repository;

import com.library.model.Book;
import com.library.model.User;
import com.library.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUser(User user);
    
    List<Wishlist> findByBook(Book book);
    
    Optional<Wishlist> findByUserAndBook(User user, Book book);
    
    boolean existsByUserAndBook(User user, Book book);
    
    @Query("SELECT w FROM Wishlist w WHERE w.book.id = ?1 AND w.user.id = ?2")
    Optional<Wishlist> findByBookIdAndUserId(Long bookId, Long userId);
    
    @Query("SELECT w FROM Wishlist w WHERE w.book.availableCopies > 0 AND w.notificationSent = false")
    List<Wishlist> findWishlistItemsWithAvailableBooks();
    
    @Query("SELECT w FROM Wishlist w WHERE w.book.availableCopies > 0 AND w.autoBorrow = true AND w.notificationSent = false")
    List<Wishlist> findWishlistItemsForAutoBorrow();
    
    void deleteByUserAndBook(User user, Book book);
}
