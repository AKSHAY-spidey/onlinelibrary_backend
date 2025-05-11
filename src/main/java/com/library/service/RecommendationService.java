package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get personalized book recommendations for a user based on their loan history
     * and similar users' preferences using collaborative filtering
     *
     * @param userId The ID of the user to get recommendations for
     * @param limit The maximum number of recommendations to return
     * @return A list of recommended books
     */
    public List<Book> getPersonalizedRecommendations(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all books the user has borrowed
        List<Loan> userLoans = loanRepository.findByUser(user);
        Set<Long> userBookIds = userLoans.stream()
                .map(loan -> loan.getBook().getId())
                .collect(Collectors.toSet());

        // If user has no loan history, return popular books
        if (userLoans.isEmpty()) {
            return getPopularBooks(limit);
        }

        // Get user's preferred categories based on loan history
        Map<String, Long> categoryFrequency = userLoans.stream()
                .map(loan -> loan.getBook().getCategory())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Get user's preferred authors
        Map<String, Long> authorFrequency = userLoans.stream()
                .map(loan -> loan.getBook().getAuthor())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Find similar users (users who borrowed at least one book that this user borrowed)
        List<User> similarUsers = findSimilarUsers(user, userBookIds);

        // Get books borrowed by similar users that this user hasn't borrowed yet
        Set<Book> recommendedBooks = new HashSet<>();
        
        for (User similarUser : similarUsers) {
            List<Loan> similarUserLoans = loanRepository.findByUser(similarUser);
            
            for (Loan loan : similarUserLoans) {
                Book book = loan.getBook();
                if (!userBookIds.contains(book.getId()) && book.getAvailableCopies() > 0) {
                    recommendedBooks.add(book);
                }
            }
        }

        // Sort recommendations by relevance score
        List<Book> sortedRecommendations = recommendedBooks.stream()
                .sorted((b1, b2) -> {
                    double score1 = calculateRelevanceScore(b1, categoryFrequency, authorFrequency);
                    double score2 = calculateRelevanceScore(b2, categoryFrequency, authorFrequency);
                    return Double.compare(score2, score1); // Higher score first
                })
                .limit(limit)
                .collect(Collectors.toList());

        // If we don't have enough recommendations, add some popular books
        if (sortedRecommendations.size() < limit) {
            List<Book> popularBooks = getPopularBooks(limit - sortedRecommendations.size()).stream()
                    .filter(book -> !userBookIds.contains(book.getId()) && 
                                   !sortedRecommendations.contains(book))
                    .collect(Collectors.toList());
            
            sortedRecommendations.addAll(popularBooks);
        }

        return sortedRecommendations;
    }

    /**
     * Get content-based recommendations based on a specific book
     *
     * @param bookId The ID of the book to get similar books for
     * @param limit The maximum number of recommendations to return
     * @return A list of similar books
     */
    public List<Book> getSimilarBooks(Long bookId, int limit) {
        Book targetBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        List<Book> allBooks = bookRepository.findAll();
        
        // Remove the target book from the list
        allBooks = allBooks.stream()
                .filter(book -> !book.getId().equals(bookId))
                .collect(Collectors.toList());

        // Calculate similarity scores
        Map<Book, Double> similarityScores = new HashMap<>();
        
        for (Book book : allBooks) {
            double score = calculateSimilarityScore(targetBook, book);
            similarityScores.put(book, score);
        }

        // Sort by similarity score and return top results
        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get trending books based on recent loan activity
     *
     * @param limit The maximum number of books to return
     * @return A list of trending books
     */
    public List<Book> getTrendingBooks(int limit) {
        // Get all loans from the last 30 days
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = calendar.getTime();

        List<Loan> recentLoans = loanRepository.findAll().stream()
                .filter(loan -> loan.getLoanDate() != null && 
                               loan.getLoanDate().isAfter(java.time.LocalDate.now().minusDays(30)))
                .collect(Collectors.toList());

        // Count loans per book
        Map<Book, Long> bookLoanCount = recentLoans.stream()
                .map(Loan::getBook)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Sort by loan count and return top results
        return bookLoanCount.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get popular books based on total loan count
     *
     * @param limit The maximum number of books to return
     * @return A list of popular books
     */
    public List<Book> getPopularBooks(int limit) {
        List<Book> allBooks = bookRepository.findAll();
        
        // Count loans per book
        Map<Book, Long> bookLoanCount = new HashMap<>();
        
        for (Book book : allBooks) {
            long loanCount = loanRepository.findAll().stream()
                    .filter(loan -> loan.getBook().getId().equals(book.getId()))
                    .count();
            
            bookLoanCount.put(book, loanCount);
        }

        // Sort by loan count and return top results
        return bookLoanCount.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Find users with similar reading preferences
     *
     * @param user The user to find similar users for
     * @param userBookIds The IDs of books the user has borrowed
     * @return A list of similar users
     */
    private List<User> findSimilarUsers(User user, Set<Long> userBookIds) {
        List<User> allUsers = userRepository.findAll();
        
        // Remove the target user
        allUsers = allUsers.stream()
                .filter(u -> !u.getId().equals(user.getId()))
                .collect(Collectors.toList());

        // Calculate similarity scores
        Map<User, Double> similarityScores = new HashMap<>();
        
        for (User otherUser : allUsers) {
            List<Loan> otherUserLoans = loanRepository.findByUser(otherUser);
            Set<Long> otherUserBookIds = otherUserLoans.stream()
                    .map(loan -> loan.getBook().getId())
                    .collect(Collectors.toSet());

            // Calculate Jaccard similarity (intersection over union)
            Set<Long> intersection = new HashSet<>(userBookIds);
            intersection.retainAll(otherUserBookIds);
            
            Set<Long> union = new HashSet<>(userBookIds);
            union.addAll(otherUserBookIds);
            
            double similarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
            similarityScores.put(otherUser, similarity);
        }

        // Sort by similarity score and return top 10 similar users
        return similarityScores.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // Only include users with some similarity
                .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calculate relevance score for a book based on user preferences
     *
     * @param book The book to calculate relevance for
     * @param categoryFrequency Map of category frequencies in user's loan history
     * @param authorFrequency Map of author frequencies in user's loan history
     * @return A relevance score
     */
    private double calculateRelevanceScore(Book book, Map<String, Long> categoryFrequency, Map<String, Long> authorFrequency) {
        double score = 0.0;
        
        // Category match
        if (book.getCategory() != null && categoryFrequency.containsKey(book.getCategory())) {
            score += categoryFrequency.get(book.getCategory()) * 2.0;
        }
        
        // Author match
        if (book.getAuthor() != null && authorFrequency.containsKey(book.getAuthor())) {
            score += authorFrequency.get(book.getAuthor()) * 3.0;
        }
        
        return score;
    }

    /**
     * Calculate similarity score between two books
     *
     * @param book1 The first book
     * @param book2 The second book
     * @return A similarity score between 0 and 1
     */
    private double calculateSimilarityScore(Book book1, Book book2) {
        double score = 0.0;
        
        // Category match
        if (book1.getCategory() != null && book2.getCategory() != null && 
            book1.getCategory().equals(book2.getCategory())) {
            score += 0.4;
        }
        
        // Author match
        if (book1.getAuthor() != null && book2.getAuthor() != null && 
            book1.getAuthor().equals(book2.getAuthor())) {
            score += 0.4;
        }
        
        // Publisher match
        if (book1.getPublisher() != null && book2.getPublisher() != null && 
            book1.getPublisher().equals(book2.getPublisher())) {
            score += 0.1;
        }
        
        // Language match
        if (book1.getLanguage() != null && book2.getLanguage() != null && 
            book1.getLanguage().equals(book2.getLanguage())) {
            score += 0.1;
        }
        
        return score;
    }
}
