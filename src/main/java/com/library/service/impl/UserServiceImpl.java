package com.library.service.impl;

import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.payload.request.LoanRequest;
import com.library.payload.response.BookResponse;
import com.library.payload.response.LoanResponse;
import com.library.payload.response.UserResponse;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import com.library.security.services.UserDetailsImpl;
import com.library.service.UserService;
import com.library.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public UserResponse getUserProfile(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return new UserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(Authentication authentication, UserResponse userRequest) {
        User user = getUserFromAuthentication(authentication);

        // Update user details
        if (userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }

        if (userRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(userRequest.getPhoneNumber());
        }

        if (userRequest.getAddress() != null) {
            user.setAddress(userRequest.getAddress());
        }

        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser);
    }

    @Override
    public List<BookResponse> getAvailableBooks() {
        List<Book> books = bookRepository.findByAvailableCopiesGreaterThan(0);
        return books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> searchBooks(String title, String author, String category) {
        List<Book> books = bookRepository.searchBooks(title, author, category);
        return books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanResponse requestLoan(Authentication authentication, LoanRequest loanRequest) {
        User user = getUserFromAuthentication(authentication);

        // Check if user has reached maximum allowed loans (e.g., 5)
        Long activeLoansCount = loanRepository.countActiveLoans(user);
        if (activeLoansCount >= 5) {
            throw new IllegalStateException("You have reached the maximum number of allowed loans (5)");
        }

        Book book = bookRepository.findById(loanRequest.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + loanRequest.getBookId()));

        // Check if book is available
        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No available copies of this book");
        }

        // Create new loan
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now()); // Set current date as loan request date
        loan.setDueDate(loanRequest.getRequestedReturnDate());
        loan.setStatus("PENDING");
        loan.setFineAmount(0.0);

        Loan savedLoan = loanRepository.save(loan);

        // Send notification to librarians about the new loan request
        notificationService.sendRoleNotification(
            "ROLE_LIBRARIAN",
            "NEW_LOAN_REQUEST",
            "New loan request from " + user.getUsername() + " for " + book.getTitle(),
            savedLoan
        );

        return new LoanResponse(savedLoan);
    }

    @Override
    public List<LoanResponse> getUserLoans(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        List<Loan> loans = loanRepository.findByUserAndStatus(user, "APPROVED");
        return loans.stream()
                .map(LoanResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void returnBook(Authentication authentication, Long loanId) {
        User user = getUserFromAuthentication(authentication);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        // Verify the loan belongs to the user
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("This loan does not belong to you");
        }

        // Verify the loan is in an approved state
        if (!"APPROVED".equals(loan.getStatus()) && !"OVERDUE".equals(loan.getStatus())) {
            throw new IllegalStateException("Can only return books that are currently loaned");
        }

        // Mark the loan as pending return (librarian will process it)
        loan.setStatus("RETURN_PENDING");
        loanRepository.save(loan);

        // Send notification to librarians about the return request
        notificationService.sendRoleNotification(
            "ROLE_LIBRARIAN",
            "NEW_RETURN_REQUEST",
            "New return request from " + user.getUsername() + " for " + loan.getBook().getTitle(),
            loan
        );
    }

    @Override
    public List<LoanResponse> getLoanHistory(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        List<Loan> loans = loanRepository.findLoanHistoryByUser(user);
        return loans.stream()
                .map(LoanResponse::new)
                .collect(Collectors.toList());
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
