package com.library.service;

import com.library.payload.request.LoanRequest;
import com.library.payload.response.BookResponse;
import com.library.payload.response.LoanResponse;
import com.library.payload.response.UserResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {
    UserResponse getUserProfile(Authentication authentication);
    
    UserResponse updateUserProfile(Authentication authentication, UserResponse userRequest);
    
    List<BookResponse> getAvailableBooks();
    
    List<BookResponse> searchBooks(String title, String author, String category);
    
    LoanResponse requestLoan(Authentication authentication, LoanRequest loanRequest);
    
    List<LoanResponse> getUserLoans(Authentication authentication);
    
    void returnBook(Authentication authentication, Long loanId);
    
    List<LoanResponse> getLoanHistory(Authentication authentication);
}
