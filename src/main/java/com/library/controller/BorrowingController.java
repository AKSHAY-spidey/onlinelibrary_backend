package com.library.controller;

import com.library.model.*;
import com.library.payload.request.BorrowingRequest;
import com.library.payload.response.MessageResponse;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRepository;
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
@RequestMapping("/api/borrowings")
public class BorrowingController {
    @Autowired
    BorrowingRepository borrowingRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<Borrowing>> getAllBorrowings() {
        List<Borrowing> borrowings = borrowingRepository.findAll();
        return ResponseEntity.ok(borrowings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getBorrowingById(@PathVariable Long id) {
        return borrowingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<?> getBorrowingsByUser(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<Borrowing> borrowings = borrowingRepository.findByUser(user);
                    return ResponseEntity.ok(borrowings);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> getBorrowingsByBook(@PathVariable Long bookId) {
        return bookRepository.findById(bookId)
                .map(book -> {
                    List<Borrowing> borrowings = borrowingRepository.findByBook(book);
                    return ResponseEntity.ok(borrowings);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> getBorrowingsByStatus(@PathVariable String status) {
        try {
            BorrowingStatus borrowingStatus = BorrowingStatus.valueOf(status.toUpperCase());
            List<Borrowing> borrowings = borrowingRepository.findByStatus(borrowingStatus);
            return ResponseEntity.ok(borrowings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid status!"));
        }
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<Borrowing>> getOverdueBooks() {
        List<Borrowing> overdueBooks = borrowingRepository.findOverdueBooks(LocalDate.now());
        return ResponseEntity.ok(overdueBooks);
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> borrowBook(@Valid @RequestBody BorrowingRequest borrowingRequest) {
        // Check if book exists
        return bookRepository.findById(borrowingRequest.getBookId())
                .map(book -> {
                    // Check if user exists
                    return userRepository.findById(borrowingRequest.getUserId())
                            .map(user -> {
                                // Check if book is available
                                if (book.getAvailableCopies() <= 0) {
                                    return ResponseEntity
                                            .badRequest()
                                            .body(new MessageResponse("Error: Book is not available for borrowing!"));
                                }

                                // Check if user already has an active borrowing for this book
                                List<Borrowing> activeBorrowings = borrowingRepository.findActiveBorrowingsByUserAndBook(user, book);
                                if (!activeBorrowings.isEmpty()) {
                                    return ResponseEntity
                                            .badRequest()
                                            .body(new MessageResponse("Error: User already has an active borrowing for this book!"));
                                }

                                // Create new borrowing
                                Borrowing borrowing = new Borrowing();
                                borrowing.setUser(user);
                                borrowing.setBook(book);
                                borrowing.setBorrowDate(LocalDate.now());
                                borrowing.setDueDate(LocalDate.now().plusDays(borrowingRequest.getBorrowDays()));
                                borrowing.setStatus(BorrowingStatus.BORROWED);

                                // Update book available copies
                                book.setAvailableCopies(book.getAvailableCopies() - 1);
                                bookRepository.save(book);

                                borrowingRepository.save(borrowing);
                                return ResponseEntity.ok(new MessageResponse("Book borrowed successfully!"));
                            })
                            .orElse(ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!")));
                })
                .orElse(ResponseEntity.badRequest().body(new MessageResponse("Error: Book not found!")));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        return borrowingRepository.findById(id)
                .map(borrowing -> {
                    if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Book already returned!"));
                    }

                    borrowing.setReturnDate(LocalDate.now());
                    borrowing.setStatus(BorrowingStatus.RETURNED);

                    // Calculate fine if overdue
                    if (LocalDate.now().isAfter(borrowing.getDueDate())) {
                        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDate.now());
                        double fineAmount = daysOverdue * 10.0; // ₹10 per day
                        borrowing.setFineAmount(fineAmount);
                    }

                    // Update book available copies
                    Book book = borrowing.getBook();
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                    bookRepository.save(book);

                    borrowingRepository.save(borrowing);
                    return ResponseEntity.ok(new MessageResponse("Book returned successfully!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/extend")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> extendBorrowing(@PathVariable Long id) {
        return borrowingRepository.findById(id)
                .map(borrowing -> {
                    if (borrowing.getStatus() != BorrowingStatus.BORROWED) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Can only extend active borrowings!"));
                    }

                    if (borrowing.isExtended()) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Borrowing already extended once!"));
                    }

                    if (LocalDate.now().isAfter(borrowing.getDueDate())) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Cannot extend overdue borrowings!"));
                    }

                    // Extend due date by 7 days
                    borrowing.setDueDate(borrowing.getDueDate().plusDays(7));
                    borrowing.setExtended(true);

                    borrowingRepository.save(borrowing);
                    return ResponseEntity.ok(new MessageResponse("Borrowing extended successfully!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/lost")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> markBookAsLost(@PathVariable Long id) {
        return borrowingRepository.findById(id)
                .map(borrowing -> {
                    if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Cannot mark returned book as lost!"));
                    }

                    borrowing.setStatus(BorrowingStatus.LOST);

                    // Set a fixed fine for lost books (e.g., $25)
                    borrowing.setFineAmount(25.0);

                    borrowingRepository.save(borrowing);
                    return ResponseEntity.ok(new MessageResponse("Book marked as lost!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
