package com.library.repository;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.BorrowingStatus;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    List<Borrowing> findByUser(User user);
    
    List<Borrowing> findByBook(Book book);
    
    List<Borrowing> findByStatus(BorrowingStatus status);
    
    @Query("SELECT b FROM Borrowing b WHERE b.dueDate < ?1 AND b.status = 'BORROWED'")
    List<Borrowing> findOverdueBooks(LocalDate currentDate);
    
    List<Borrowing> findByUserAndStatus(User user, BorrowingStatus status);
    
    @Query("SELECT b FROM Borrowing b WHERE b.user = ?1 AND b.book = ?2 AND b.status = 'BORROWED'")
    List<Borrowing> findActiveBorrowingsByUserAndBook(User user, Book book);
}
