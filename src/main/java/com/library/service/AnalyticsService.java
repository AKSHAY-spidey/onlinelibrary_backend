package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get comprehensive library usage statistics
     *
     * @return A map containing various library usage statistics
     */
    public Map<String, Object> getLibraryStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long totalLoans = loanRepository.count();
        
        stats.put("totalBooks", totalBooks);
        stats.put("totalUsers", totalUsers);
        stats.put("totalLoans", totalLoans);
        
        // Book availability
        List<Book> allBooks = bookRepository.findAll();
        int totalCopies = allBooks.stream().mapToInt(Book::getTotalCopies).sum();
        int availableCopies = allBooks.stream().mapToInt(Book::getAvailableCopies).sum();
        double availabilityRate = totalCopies > 0 ? (double) availableCopies / totalCopies : 0;
        
        stats.put("totalCopies", totalCopies);
        stats.put("availableCopies", availableCopies);
        stats.put("availabilityRate", Math.round(availabilityRate * 100.0) / 100.0);
        
        // Loan statistics
        List<Loan> allLoans = loanRepository.findAll();
        long activeLoans = allLoans.stream()
                .filter(loan -> "APPROVED".equals(loan.getStatus()) || "OVERDUE".equals(loan.getStatus()))
                .count();
        long overdueLoans = allLoans.stream()
                .filter(loan -> "OVERDUE".equals(loan.getStatus()))
                .count();
        double overdueRate = activeLoans > 0 ? (double) overdueLoans / activeLoans : 0;
        
        stats.put("activeLoans", activeLoans);
        stats.put("overdueLoans", overdueLoans);
        stats.put("overdueRate", Math.round(overdueRate * 100.0) / 100.0);
        
        // Average loan duration (in days)
        double avgLoanDuration = allLoans.stream()
                .filter(loan -> loan.getLoanDate() != null && loan.getReturnDate() != null)
                .mapToLong(loan -> ChronoUnit.DAYS.between(loan.getLoanDate(), loan.getReturnDate()))
                .average()
                .orElse(0);
        
        stats.put("avgLoanDuration", Math.round(avgLoanDuration * 10.0) / 10.0);
        
        // Popular categories
        Map<String, Long> categoryStats = allLoans.stream()
                .map(loan -> loan.getBook().getCategory())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        
        stats.put("popularCategories", categoryStats);
        
        // User activity
        Map<Long, Long> userActivity = allLoans.stream()
                .collect(Collectors.groupingBy(loan -> loan.getUser().getId(), Collectors.counting()));
        
        double avgLoansPerUser = userActivity.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
        
        stats.put("avgLoansPerUser", Math.round(avgLoansPerUser * 10.0) / 10.0);
        
        // Most active users (top 5)
        List<Map<String, Object>> mostActiveUsers = userActivity.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    User user = userRepository.findById(entry.getKey()).orElse(null);
                    Map<String, Object> userMap = new HashMap<>();
                    if (user != null) {
                        userMap.put("id", user.getId());
                        userMap.put("username", user.getUsername());
                        userMap.put("loanCount", entry.getValue());
                    }
                    return userMap;
                })
                .collect(Collectors.toList());
        
        stats.put("mostActiveUsers", mostActiveUsers);
        
        // Time-based analytics
        Map<String, Long> weekdayStats = allLoans.stream()
                .filter(loan -> loan.getLoanDate() != null)
                .collect(Collectors.groupingBy(
                        loan -> loan.getLoanDate().getDayOfWeek().name(),
                        Collectors.counting()
                ));
        
        stats.put("weekdayStats", weekdayStats);
        
        // Monthly trends (last 6 months)
        Map<String, Long> monthlyTrends = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String monthKey = monthStart.getMonth().name() + " " + monthStart.getYear();
            
            long loansInMonth = allLoans.stream()
                    .filter(loan -> loan.getLoanDate() != null)
                    .filter(loan -> !loan.getLoanDate().isBefore(monthStart) && !loan.getLoanDate().isAfter(monthEnd))
                    .count();
            
            monthlyTrends.put(monthKey, loansInMonth);
        }
        
        stats.put("monthlyTrends", monthlyTrends);
        
        return stats;
    }

    /**
     * Get user activity analytics
     *
     * @param userId The ID of the user to get analytics for
     * @return A map containing user activity statistics
     */
    public Map<String, Object> getUserAnalytics(Long userId) {
        Map<String, Object> analytics = new HashMap<>();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Loan> userLoans = loanRepository.findByUser(user);
        
        // Basic stats
        analytics.put("totalLoans", userLoans.size());
        
        long activeLoans = userLoans.stream()
                .filter(loan -> "APPROVED".equals(loan.getStatus()) || "OVERDUE".equals(loan.getStatus()))
                .count();
        
        analytics.put("activeLoans", activeLoans);
        
        long overdueLoans = userLoans.stream()
                .filter(loan -> "OVERDUE".equals(loan.getStatus()))
                .count();
        
        analytics.put("overdueLoans", overdueLoans);
        
        // Category preferences
        Map<String, Long> categoryPreferences = userLoans.stream()
                .map(loan -> loan.getBook().getCategory())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        
        analytics.put("categoryPreferences", categoryPreferences);
        
        // Author preferences
        Map<String, Long> authorPreferences = userLoans.stream()
                .map(loan -> loan.getBook().getAuthor())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        
        analytics.put("authorPreferences", authorPreferences);
        
        // Loan history timeline
        List<Map<String, Object>> loanTimeline = userLoans.stream()
                .filter(loan -> loan.getLoanDate() != null)
                .sorted(Comparator.comparing(Loan::getLoanDate))
                .map(loan -> {
                    Map<String, Object> loanMap = new HashMap<>();
                    loanMap.put("id", loan.getId());
                    loanMap.put("bookTitle", loan.getBook().getTitle());
                    loanMap.put("loanDate", loan.getLoanDate().toString());
                    loanMap.put("dueDate", loan.getDueDate().toString());
                    loanMap.put("returnDate", loan.getReturnDate() != null ? loan.getReturnDate().toString() : null);
                    loanMap.put("status", loan.getStatus());
                    return loanMap;
                })
                .collect(Collectors.toList());
        
        analytics.put("loanTimeline", loanTimeline);
        
        // Reading velocity (books per month)
        if (!userLoans.isEmpty()) {
            Loan firstLoan = userLoans.stream()
                    .filter(loan -> loan.getLoanDate() != null)
                    .min(Comparator.comparing(Loan::getLoanDate))
                    .orElse(null);
            
            if (firstLoan != null) {
                LocalDate firstLoanDate = firstLoan.getLoanDate();
                LocalDate now = LocalDate.now();
                long monthsBetween = ChronoUnit.MONTHS.between(firstLoanDate, now) + 1; // Add 1 to include partial months
                
                double booksPerMonth = monthsBetween > 0 ? (double) userLoans.size() / monthsBetween : userLoans.size();
                analytics.put("booksPerMonth", Math.round(booksPerMonth * 100.0) / 100.0);
            }
        }
        
        // Return rate (percentage of books returned on time)
        long completedLoans = userLoans.stream()
                .filter(loan -> "RETURNED".equals(loan.getStatus()))
                .count();
        
        long lateReturns = userLoans.stream()
                .filter(loan -> "RETURNED".equals(loan.getStatus()) && loan.getReturnDate() != null && loan.getDueDate() != null)
                .filter(loan -> loan.getReturnDate().isAfter(loan.getDueDate()))
                .count();
        
        double onTimeReturnRate = completedLoans > 0 ? (double) (completedLoans - lateReturns) / completedLoans : 0;
        analytics.put("onTimeReturnRate", Math.round(onTimeReturnRate * 100.0) / 100.0);
        
        return analytics;
    }

    /**
     * Get book analytics
     *
     * @param bookId The ID of the book to get analytics for
     * @return A map containing book analytics
     */
    public Map<String, Object> getBookAnalytics(Long bookId) {
        Map<String, Object> analytics = new HashMap<>();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        List<Loan> bookLoans = loanRepository.findAll().stream()
                .filter(loan -> loan.getBook().getId().equals(bookId))
                .collect(Collectors.toList());
        
        // Basic stats
        analytics.put("totalLoans", bookLoans.size());
        analytics.put("availableCopies", book.getAvailableCopies());
        analytics.put("totalCopies", book.getTotalCopies());
        
        double availabilityRate = book.getTotalCopies() > 0 ? 
                (double) book.getAvailableCopies() / book.getTotalCopies() : 0;
        analytics.put("availabilityRate", Math.round(availabilityRate * 100.0) / 100.0);
        
        // Popularity metrics
        long totalBooks = bookRepository.count();
        List<Loan> allLoans = loanRepository.findAll();
        
        // Calculate popularity percentile
        if (totalBooks > 0 && !allLoans.isEmpty()) {
            // Count loans for each book
            Map<Long, Long> bookLoanCounts = allLoans.stream()
                    .collect(Collectors.groupingBy(
                            loan -> loan.getBook().getId(),
                            Collectors.counting()
                    ));
            
            // Sort books by loan count
            List<Long> sortedCounts = bookLoanCounts.values().stream()
                    .sorted()
                    .collect(Collectors.toList());
            
            // Find position of this book
            long thisBookLoans = bookLoans.size();
            int position = Collections.binarySearch(sortedCounts, thisBookLoans);
            if (position < 0) {
                position = -position - 1;
            }
            
            double percentile = (double) position / sortedCounts.size();
            analytics.put("popularityPercentile", Math.round(percentile * 100.0) / 100.0);
        }
        
        // Loan duration statistics
        OptionalDouble avgLoanDuration = bookLoans.stream()
                .filter(loan -> loan.getLoanDate() != null && loan.getReturnDate() != null)
                .mapToLong(loan -> ChronoUnit.DAYS.between(loan.getLoanDate(), loan.getReturnDate()))
                .average();
        
        analytics.put("avgLoanDuration", avgLoanDuration.isPresent() ? 
                Math.round(avgLoanDuration.getAsDouble() * 10.0) / 10.0 : 0);
        
        // Overdue rate
        long overdueLoans = bookLoans.stream()
                .filter(loan -> "OVERDUE".equals(loan.getStatus()) || 
                               ("RETURNED".equals(loan.getStatus()) && 
                                loan.getReturnDate() != null && 
                                loan.getDueDate() != null && 
                                loan.getReturnDate().isAfter(loan.getDueDate())))
                .count();
        
        double overdueRate = bookLoans.size() > 0 ? (double) overdueLoans / bookLoans.size() : 0;
        analytics.put("overdueRate", Math.round(overdueRate * 100.0) / 100.0);
        
        // Reader demographics (age groups, if available)
        // This would require additional user data like birth date
        
        // Loan history timeline
        List<Map<String, Object>> loanTimeline = bookLoans.stream()
                .filter(loan -> loan.getLoanDate() != null)
                .sorted(Comparator.comparing(Loan::getLoanDate))
                .map(loan -> {
                    Map<String, Object> loanMap = new HashMap<>();
                    loanMap.put("id", loan.getId());
                    loanMap.put("username", loan.getUser().getUsername());
                    loanMap.put("loanDate", loan.getLoanDate().toString());
                    loanMap.put("returnDate", loan.getReturnDate() != null ? loan.getReturnDate().toString() : null);
                    loanMap.put("status", loan.getStatus());
                    return loanMap;
                })
                .collect(Collectors.toList());
        
        analytics.put("loanTimeline", loanTimeline);
        
        // Monthly loan trend
        Map<String, Long> monthlyTrend = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String monthKey = monthStart.getMonth().name() + " " + monthStart.getYear();
            
            long loansInMonth = bookLoans.stream()
                    .filter(loan -> loan.getLoanDate() != null)
                    .filter(loan -> !loan.getLoanDate().isBefore(monthStart) && !loan.getLoanDate().isAfter(monthEnd))
                    .count();
            
            monthlyTrend.put(monthKey, loansInMonth);
        }
        
        analytics.put("monthlyTrend", monthlyTrend);
        
        return analytics;
    }

    /**
     * Get predictive analytics for library operations
     *
     * @return A map containing predictive analytics
     */
    public Map<String, Object> getPredictiveAnalytics() {
        Map<String, Object> predictions = new HashMap<>();
        
        // Predict loan volume for next month based on historical data
        List<Loan> allLoans = loanRepository.findAll();
        Map<String, Long> monthlyLoanCounts = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        
        // Collect monthly loan counts for the past 12 months
        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            String monthKey = monthStart.getMonth().name() + " " + monthStart.getYear();
            
            long loansInMonth = allLoans.stream()
                    .filter(loan -> loan.getLoanDate() != null)
                    .filter(loan -> !loan.getLoanDate().isBefore(monthStart) && !loan.getLoanDate().isAfter(monthEnd))
                    .count();
            
            monthlyLoanCounts.put(monthKey, loansInMonth);
        }
        
        // Simple moving average prediction for next month
        List<Long> recentCounts = new ArrayList<>(monthlyLoanCounts.values());
        if (recentCounts.size() >= 3) {
            long sum = recentCounts.subList(recentCounts.size() - 3, recentCounts.size()).stream()
                    .mapToLong(Long::longValue)
                    .sum();
            double predictedNextMonth = (double) sum / 3;
            
            LocalDate nextMonth = now.plusMonths(1);
            String nextMonthKey = nextMonth.getMonth().name() + " " + nextMonth.getYear();
            
            predictions.put("predictedLoanVolume", Math.round(predictedNextMonth));
            predictions.put("nextMonth", nextMonthKey);
        }
        
        // Predict which books will be in high demand
        List<Book> allBooks = bookRepository.findAll();
        
        // Calculate recent popularity trend for each book
        Map<Long, Double> bookPopularityTrend = new HashMap<>();
        
        for (Book book : allBooks) {
            // Get loans for this book in the last 3 months
            LocalDate threeMonthsAgo = now.minusMonths(3);
            
            List<Loan> recentBookLoans = allLoans.stream()
                    .filter(loan -> loan.getBook().getId().equals(book.getId()))
                    .filter(loan -> loan.getLoanDate() != null && !loan.getLoanDate().isBefore(threeMonthsAgo))
                    .collect(Collectors.toList());
            
            // Calculate monthly trend
            Map<Integer, Long> monthlyBookLoans = new HashMap<>();
            
            for (int i = 2; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                
                long loansInMonth = recentBookLoans.stream()
                        .filter(loan -> !loan.getLoanDate().isBefore(monthStart) && !loan.getLoanDate().isAfter(monthEnd))
                        .count();
                
                monthlyBookLoans.put(i, loansInMonth);
            }
            
            // Calculate trend (simple linear regression slope)
            if (monthlyBookLoans.size() == 3) {
                double x1 = 0, x2 = 1, x3 = 2;
                double y1 = monthlyBookLoans.get(2);
                double y2 = monthlyBookLoans.get(1);
                double y3 = monthlyBookLoans.get(0);
                
                double slope = ((y3 - y1) * (x3 - x1) + (y2 - y1) * (x2 - x1)) / 
                              ((x3 - x1) * (x3 - x1) + (x2 - x1) * (x2 - x1));
                
                bookPopularityTrend.put(book.getId(), slope);
            }
        }
        
        // Get top 5 books with positive trend
        List<Map<String, Object>> highDemandBooks = bookPopularityTrend.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // Only positive trends
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Book book = bookRepository.findById(entry.getKey()).orElse(null);
                    Map<String, Object> bookMap = new HashMap<>();
                    if (book != null) {
                        bookMap.put("id", book.getId());
                        bookMap.put("title", book.getTitle());
                        bookMap.put("author", book.getAuthor());
                        bookMap.put("availableCopies", book.getAvailableCopies());
                        bookMap.put("totalCopies", book.getTotalCopies());
                        bookMap.put("trendSlope", Math.round(entry.getValue() * 100.0) / 100.0);
                    }
                    return bookMap;
                })
                .collect(Collectors.toList());
        
        predictions.put("highDemandBooks", highDemandBooks);
        
        // Predict busy days of the week
        Map<DayOfWeek, Long> dayOfWeekCounts = allLoans.stream()
                .filter(loan -> loan.getLoanDate() != null)
                .collect(Collectors.groupingBy(
                        loan -> loan.getLoanDate().getDayOfWeek(),
                        Collectors.counting()
                ));
        
        // Find the busiest day
        Optional<Map.Entry<DayOfWeek, Long>> busiestDay = dayOfWeekCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());
        
        if (busiestDay.isPresent()) {
            predictions.put("busiestDay", busiestDay.get().getKey().name());
            predictions.put("busiestDayLoanCount", busiestDay.get().getValue());
        }
        
        return predictions;
    }
}
