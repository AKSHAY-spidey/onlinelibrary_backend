package com.library.repository;

import com.library.model.Loan;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.library.model.Book;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);

    List<Loan> findByUserAndStatus(User user, String status);

    @Query("SELECT l FROM Loan l WHERE l.user = :user ORDER BY l.loanDate DESC")
    List<Loan> findLoanHistoryByUser(@Param("user") User user);

    @Query("SELECT l FROM Loan l WHERE l.status = 'APPROVED' AND l.dueDate < :today AND l.returnDate IS NULL")
    List<Loan> findOverdueLoans(@Param("today") LocalDate today);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = :user AND l.status IN ('APPROVED', 'PENDING')")
    Long countActiveLoans(@Param("user") User user);

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId ORDER BY l.loanDate DESC")
    List<Loan> findByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId ORDER BY l.loanDate DESC")
    List<Loan> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT l FROM Loan l WHERE l.status = :status ORDER BY l.loanDate DESC")
    List<Loan> findByStatus(@Param("status") String status);

    // Count loans for a specific book
    Long countByBook(Book book);

    // Count loans for a specific book after a certain date
    Long countByBookAndLoanDateAfter(Book book, Date date);

    // Find loans for a specific book
    List<Loan> findByBook(Book book);

    // Find loans for a specific book and status
    List<Loan> findByBookAndStatus(Book book, String status);
}
