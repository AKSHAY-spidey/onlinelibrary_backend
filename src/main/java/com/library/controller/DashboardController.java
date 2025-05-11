package com.library.controller;

import com.library.model.Book;
import com.library.model.ERole;
import com.library.model.Loan;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private LoanRepository loanRepository;
    
    /**
     * Get user dashboard statistics
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserDashboard(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Get user's active loans
        List<Loan> activeLoans = loanRepository.findByUserAndStatus(user, "APPROVED");
        
        // Get user's overdue loans
        List<Loan> overdueLoans = activeLoans.stream()
                .filter(loan -> loan.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        
        // Get user's loan history
        List<Loan> loanHistory = loanRepository.findLoanHistoryByUser(user);
        
        // Get available books count
        long availableBooksCount = bookRepository.findByAvailableCopiesGreaterThan(0).size();
        
        dashboardData.put("activeLoansCount", activeLoans.size());
        dashboardData.put("overdueLoansCount", overdueLoans.size());
        dashboardData.put("loanHistoryCount", loanHistory.size());
        dashboardData.put("availableBooksCount", availableBooksCount);
        
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get librarian dashboard statistics
     */
    @GetMapping("/librarian")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLibrarianDashboard() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Get all books
        List<Book> allBooks = bookRepository.findAll();
        
        // Get pending loans
        List<Loan> pendingLoans = loanRepository.findAll().stream()
                .filter(loan -> "PENDING".equals(loan.getStatus()))
                .collect(Collectors.toList());
        
        // Get pending returns
        List<Loan> pendingReturns = loanRepository.findAll().stream()
                .filter(loan -> "RETURN_PENDING".equals(loan.getStatus()))
                .collect(Collectors.toList());
        
        // Get overdue loans
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(LocalDate.now());
        
        dashboardData.put("totalBooks", allBooks.size());
        dashboardData.put("pendingLoansCount", pendingLoans.size());
        dashboardData.put("pendingReturnsCount", pendingReturns.size());
        dashboardData.put("overdueLoansCount", overdueLoans.size());
        
        // Calculate total available copies
        int totalAvailableCopies = allBooks.stream()
                .mapToInt(Book::getAvailableCopies)
                .sum();
        
        // Calculate total copies
        int totalCopies = allBooks.stream()
                .mapToInt(Book::getTotalCopies)
                .sum();
        
        dashboardData.put("totalAvailableCopies", totalAvailableCopies);
        dashboardData.put("totalCopies", totalCopies);
        
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // User statistics
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRolesName(ERole.ROLE_ADMIN);
        long librarianCount = userRepository.countByRolesName(ERole.ROLE_LIBRARIAN);
        long regularUserCount = userRepository.countByRolesName(ERole.ROLE_USER);
        
        // Book statistics
        long totalBooks = bookRepository.count();
        
        // Loan statistics
        long totalLoans = loanRepository.count();
        long activeLoans = loanRepository.findAll().stream()
                .filter(loan -> "APPROVED".equals(loan.getStatus()))
                .count();
        long completedLoans = loanRepository.findAll().stream()
                .filter(loan -> "RETURNED".equals(loan.getStatus()))
                .count();
        
        dashboardData.put("totalUsers", totalUsers);
        dashboardData.put("adminCount", adminCount);
        dashboardData.put("librarianCount", librarianCount);
        dashboardData.put("regularUserCount", regularUserCount);
        dashboardData.put("totalBooks", totalBooks);
        dashboardData.put("totalLoans", totalLoans);
        dashboardData.put("activeLoans", activeLoans);
        dashboardData.put("completedLoans", completedLoans);
        
        return ResponseEntity.ok(dashboardData);
    }
}
