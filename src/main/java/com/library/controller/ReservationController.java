package com.library.controller;

import com.library.model.*;
import com.library.payload.request.ReservationRequest;
import com.library.payload.response.MessageResponse;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<?> getReservationsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<Reservation> reservations = reservationRepository.findByUser(user);
                    return ResponseEntity.ok(reservations);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> getReservationsByBook(@PathVariable Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    List<Reservation> reservations = reservationRepository.findByBook(book);
                    return ResponseEntity.ok(reservations);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> getReservationsByStatus(@PathVariable String status) {
        try {
            ReservationStatus reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
            List<Reservation> reservations = reservationRepository.findByStatus(reservationStatus);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid status!"));
        }
    }

    @GetMapping("/expired")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<Reservation>> getExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(LocalDate.now());
        return ResponseEntity.ok(expiredReservations);
    }

    @PostMapping("/reserve")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> reserveBook(@Valid @RequestBody ReservationRequest reservationRequest, @RequestParam(required = false) Long userId) {
        // Check if book exists
        return bookRepository.findById(reservationRequest.getBookId())
                .map(book -> {
                    // Check if user exists
                    return userRepository.findById(userId)
                            .map(user -> {
                                // Check if user already has a pending reservation for this book
                                List<Reservation> pendingReservations = reservationRepository.findByUserAndStatus(user, ReservationStatus.PENDING);
                                for (Reservation res : pendingReservations) {
                                    if (res.getBook().getId().equals(book.getId())) {
                                        return ResponseEntity
                                                .badRequest()
                                                .body(new MessageResponse("Error: User already has a pending reservation for this book!"));
                                    }
                                }

                                // Create new reservation
                                Reservation reservation = new Reservation();
                                reservation.setUser(user);
                                reservation.setBook(book);
                                reservation.setReservationDate(LocalDate.now());
                                reservation.setExpiryDate(LocalDate.now().plusDays(3)); // Reservation expires in 3 days
                                reservation.setStatus(ReservationStatus.PENDING);

                                reservationRepository.save(reservation);
                                return ResponseEntity.ok(new MessageResponse("Book reserved successfully!"));
                            })
                            .orElseGet(() -> ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!")));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(new MessageResponse("Error: Book not found!")));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getStatus() != ReservationStatus.PENDING) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Can only cancel pending reservations!"));
                    }

                    reservation.setStatus(ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);
                    return ResponseEntity.ok(new MessageResponse("Reservation cancelled successfully!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/fulfill")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> fulfillReservation(@PathVariable Long id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if (reservation.getStatus() != ReservationStatus.PENDING) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Can only fulfill pending reservations!"));
                    }

                    Book book = reservation.getBook();
                    if (book.getAvailableCopies() <= 0) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: No copies available to fulfill reservation!"));
                    }

                    reservation.setStatus(ReservationStatus.FULFILLED);
                    reservationRepository.save(reservation);
                    return ResponseEntity.ok(new MessageResponse("Reservation fulfilled successfully!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
