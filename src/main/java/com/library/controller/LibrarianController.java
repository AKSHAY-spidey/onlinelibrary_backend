package com.library.controller;

import com.library.payload.request.BookRequest;
import com.library.payload.response.BookResponse;
import com.library.payload.response.MessageResponse;
import com.library.service.LibrarianService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/librarian")
@PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
public class LibrarianController {

    @Autowired
    private LibrarianService librarianService;

    @PostMapping("/books")
    public ResponseEntity<BookResponse> addBook(@RequestBody BookRequest bookRequest) {
        BookResponse newBook = librarianService.addBook(bookRequest);
        return ResponseEntity.ok(newBook);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @RequestBody BookRequest bookRequest) {
        BookResponse updatedBook = librarianService.updateBook(id, bookRequest);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<MessageResponse> deleteBook(@PathVariable Long id) {
        librarianService.deleteBook(id);
        return ResponseEntity.ok(new MessageResponse("Book deleted successfully"));
    }

    @GetMapping("/books/all")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        List<BookResponse> books = librarianService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/loans")
    public ResponseEntity<?> getAllLoans() {
        return ResponseEntity.ok(librarianService.getAllLoans());
    }

    @PutMapping("/loans/{id}/approve")
    public ResponseEntity<MessageResponse> approveLoan(@PathVariable Long id) {
        librarianService.approveLoan(id);
        return ResponseEntity.ok(new MessageResponse("Loan approved successfully"));
    }

    @PutMapping("/loans/{id}/reject")
    public ResponseEntity<MessageResponse> rejectLoan(@PathVariable Long id) {
        librarianService.rejectLoan(id);
        return ResponseEntity.ok(new MessageResponse("Loan rejected successfully"));
    }

    @PutMapping("/returns/{id}/process")
    public ResponseEntity<MessageResponse> processReturn(@PathVariable Long id) {
        librarianService.processReturn(id);
        return ResponseEntity.ok(new MessageResponse("Return processed successfully"));
    }

    @PutMapping("/loans/{id}/modify-return-date")
    public ResponseEntity<MessageResponse> modifyReturnDate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newReturnDate) {
        librarianService.modifyReturnDate(id, newReturnDate);
        return ResponseEntity.ok(new MessageResponse("Loan return date modified successfully"));
    }

    @PutMapping("/loans/{id}/return")
    public ResponseEntity<MessageResponse> returnLoan(@PathVariable Long id) {
        librarianService.processReturn(id);
        return ResponseEntity.ok(new MessageResponse("Return processed successfully"));
    }
}
