package com.library.repository;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);

    List<Reservation> findByBook(Book book);

    List<Reservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.expiryDate < ?1 AND r.status = 'PENDING'")
    List<Reservation> findExpiredReservations(LocalDate currentDate);

    List<Reservation> findByExpiryDateBeforeAndStatus(LocalDate date, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.book.availableCopies > 0 AND r.status = 'PENDING'")
    List<Reservation> findPendingReservationsForAvailableBooks();

    List<Reservation> findByUserAndStatus(User user, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.book = ?1 AND r.status = 'PENDING' ORDER BY r.reservationDate ASC")
    List<Reservation> findPendingReservationsByBookOrderByDate(Book book);
}
