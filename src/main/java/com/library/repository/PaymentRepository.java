package com.library.repository;

import com.library.model.Loan;
import com.library.model.Payment;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);

    List<Payment> findByLoan(Loan loan);

    List<Payment> findByStatus(String status);

    List<Payment> findByUserAndStatus(User user, String status);

    List<Payment> findByVerified(Boolean verified);

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN ?1 AND ?2")
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.user = ?1 AND p.paymentDate BETWEEN ?2 AND ?3")
    List<Payment> findByUserAndPaymentDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double getTotalPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paymentDate BETWEEN ?1 AND ?2")
    Double getTotalPaymentsBetween(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByLoanAndStatus(Loan loan, String status);

    Payment findByTransactionId(String transactionId);

    List<Payment> findByPaymentMethod(String paymentMethod);
}
