package com.library.service;

import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    /**
     * Get personalized book recommendations for a user based on their loan history
     * 
     * @param user The user to get recommendations for
     * @param limit The maximum number of recommendations to return
     * @return A list of recommended books
     */
    public List<Book> getPersonalizedRecommendations(User user, int limit) {
        // Get the user's loan history
        List<Book> borrowedBooks = loanRepository.findByUser(user)
                .stream()
                .map(loan -> loan.getBook())
                .collect(Collectors.toList());
        
        if (borrowedBooks.isEmpty()) {
            // If the user has no loan history, return popular books
            return getPopularBooks(limit);
        }
        
        // Extract categories, authors, and keywords from borrowed books
        Set<String> categories = new HashSet<>();
        Set<String> authors = new HashSet<>();
        Set<String> keywords = new HashSet<>();
        
        for (Book book : borrowedBooks) {
            if (book.getCategory() != null) {
                categories.add(book.getCategory());
            }
            if (book.getAuthor() != null) {
                authors.add(book.getAuthor());
            }
            if (book.getTitle() != null) {
                Arrays.stream(book.getTitle().split("\\s+"))
                    .filter(word -> word.length() > 3)
                    .forEach(keywords::add);
            }
            if (book.getDescription() != null) {
                Arrays.stream(book.getDescription().split("\\s+"))
                    .filter(word -> word.length() > 3)
                    .forEach(keywords::add);
            }
        }
        
        // Find books with similar categories, authors, or keywords
        List<Book> allBooks = bookRepository.findAll();
        Map<Book, Integer> bookScores = new HashMap<>();
        
        for (Book book : allBooks) {
            // Skip books the user has already borrowed
            if (borrowedBooks.contains(book)) {
                continue;
            }
            
            int score = 0;
            
            // Score based on category match
            if (book.getCategory() != null && categories.contains(book.getCategory())) {
                score += 3;
            }
            
            // Score based on author match
            if (book.getAuthor() != null && authors.contains(book.getAuthor())) {
                score += 5;
            }
            
            // Score based on keyword matches in title and description
            if (book.getTitle() != null) {
                score += Arrays.stream(book.getTitle().split("\\s+"))
                    .filter(word -> keywords.contains(word))
                    .count();
            }
            if (book.getDescription() != null) {
                score += Arrays.stream(book.getDescription().split("\\s+"))
                    .filter(word -> keywords.contains(word))
                    .count() / 2; // Lower weight for description matches
            }
            
            // Only include books with a positive score
            if (score > 0) {
                bookScores.put(book, score);
            }
        }
        
        // Sort books by score (descending) and return the top 'limit' books
        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<Book, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Get similar books based on a given book
     * 
     * @param book The book to find similar books for
     * @param limit The maximum number of similar books to return
     * @return A list of similar books
     */
    public List<Book> getSimilarBooks(Book book, int limit) {
        if (book == null) {
            return Collections.emptyList();
        }
        
        // Extract category, author, and keywords from the book
        String category = book.getCategory();
        String author = book.getAuthor();
        Set<String> keywords = new HashSet<>();
        
        if (book.getTitle() != null) {
            Arrays.stream(book.getTitle().split("\\s+"))
                .filter(word -> word.length() > 3)
                .forEach(keywords::add);
        }
        if (book.getDescription() != null) {
            Arrays.stream(book.getDescription().split("\\s+"))
                .filter(word -> word.length() > 3)
                .forEach(keywords::add);
        }
        
        // Find similar books
        List<Book> allBooks = bookRepository.findAll();
        Map<Book, Integer> bookScores = new HashMap<>();
        
        for (Book otherBook : allBooks) {
            // Skip the same book
            if (otherBook.getId().equals(book.getId())) {
                continue;
            }
            
            int score = 0;
            
            // Score based on category match
            if (category != null && category.equals(otherBook.getCategory())) {
                score += 3;
            }
            
            // Score based on author match
            if (author != null && author.equals(otherBook.getAuthor())) {
                score += 5;
            }
            
            // Score based on keyword matches in title and description
            if (otherBook.getTitle() != null) {
                score += Arrays.stream(otherBook.getTitle().split("\\s+"))
                    .filter(word -> keywords.contains(word))
                    .count();
            }
            if (otherBook.getDescription() != null) {
                score += Arrays.stream(otherBook.getDescription().split("\\s+"))
                    .filter(word -> keywords.contains(word))
                    .count() / 2; // Lower weight for description matches
            }
            
            // Only include books with a positive score
            if (score > 0) {
                bookScores.put(otherBook, score);
            }
        }
        
        // Sort books by score (descending) and return the top 'limit' books
        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<Book, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Get popular books based on loan count
     * 
     * @param limit The maximum number of popular books to return
     * @return A list of popular books
     */
    public List<Book> getPopularBooks(int limit) {
        // Get all books
        List<Book> allBooks = bookRepository.findAll();
        
        // Count loans for each book
        Map<Book, Long> loanCounts = new HashMap<>();
        for (Book book : allBooks) {
            long loanCount = loanRepository.countByBook(book);
            loanCounts.put(book, loanCount);
        }
        
        // Sort books by loan count (descending) and return the top 'limit' books
        return loanCounts.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Get trending books (recently popular)
     * 
     * @param limit The maximum number of trending books to return
     * @return A list of trending books
     */
    public List<Book> getTrendingBooks(int limit) {
        // Get recent loans (last 30 days)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = cal.getTime();
        
        // Count recent loans for each book
        List<Book> allBooks = bookRepository.findAll();
        Map<Book, Long> recentLoanCounts = new HashMap<>();
        
        for (Book book : allBooks) {
            long recentLoanCount = loanRepository.countByBookAndLoanDateAfter(book, thirtyDaysAgo);
            recentLoanCounts.put(book, recentLoanCount);
        }
        
        // Sort books by recent loan count (descending) and return the top 'limit' books
        return recentLoanCounts.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
