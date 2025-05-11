package com.library.service;

import com.library.model.Loan;
import com.library.repository.LoanRepository;
import com.library.repository.ReservationRepository;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@EnableScheduling
public class ScheduledTasks {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * Check for overdue loans daily at midnight
     * This task finds all approved loans with due dates in the past
     * and marks them as overdue
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void checkOverdueLoans() {
        LocalDate today = LocalDate.now();
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(today);

        for (Loan loan : overdueLoans) {
            loan.setStatus("OVERDUE");

            // Calculate fine amount
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), today);
            double fineAmount = daysOverdue * 10.0; // ₹10 per day
            loan.setFineAmount(fineAmount);

            loanRepository.save(loan);
        }
    }

    /**
     * Process wishlist items hourly
     * This task checks for books that have become available and notifies users
     * or automatically borrows them based on user preferences
     */
    @Scheduled(cron = "0 0 * * * ?") // Run every hour
    @Transactional
    public void processWishlistItems() {
        wishlistService.processAvailableWishlistItems();
    }

    /**
     * Process reservations daily
     * This task checks for expired reservations and makes the books available again
     */
    @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
    @Transactional
    public void processReservations() {
        LocalDate today = LocalDate.now();
        List<Reservation> expiredReservations = reservationRepository.findByExpiryDateBeforeAndStatus(today, ReservationStatus.ACTIVE);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            // Make the book available again
            reservation.getBook().setAvailableCopies(reservation.getBook().getAvailableCopies() + 1);
        }
    }
}
