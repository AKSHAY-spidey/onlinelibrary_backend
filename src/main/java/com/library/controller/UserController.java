package com.library.controller;

import com.library.model.User;
import com.library.payload.request.LoanRequest;
import com.library.payload.response.BookResponse;
import com.library.payload.response.LoanResponse;
import com.library.payload.response.MessageResponse;
import com.library.payload.response.UserResponse;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserProfile(Authentication authentication) {
        UserResponse userProfile = userService.getUserProfile(authentication);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserProfile(
            Authentication authentication,
            @RequestBody UserResponse userRequest) {
        UserResponse updatedProfile = userService.updateUserProfile(authentication, userRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/books/available")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<BookResponse>> getAvailableBooks() {
        List<BookResponse> books = userService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/search")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category) {
        List<BookResponse> books = userService.searchBooks(title, author, category);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/loans")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoanResponse> requestLoan(
            Authentication authentication,
            @RequestBody LoanRequest loanRequest) {
        LoanResponse loan = userService.requestLoan(authentication, loanRequest);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/loans")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<LoanResponse>> getUserLoans(Authentication authentication) {
        List<LoanResponse> loans = userService.getUserLoans(authentication);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/returns/{loanId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> returnBook(
            Authentication authentication,
            @PathVariable Long loanId) {
        userService.returnBook(authentication, loanId);
        return ResponseEntity.ok(new MessageResponse("Book return initiated successfully"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<LoanResponse>> getLoanHistory(Authentication authentication) {
        List<LoanResponse> loanHistory = userService.getLoanHistory(authentication);
        return ResponseEntity.ok(loanHistory);
    }
}
