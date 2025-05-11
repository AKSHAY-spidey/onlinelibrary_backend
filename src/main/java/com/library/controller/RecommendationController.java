package com.library.controller;

import com.library.model.Book;
import com.library.payload.response.BookResponse;
import com.library.security.services.UserDetailsImpl;
import com.library.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * Get personalized book recommendations for the authenticated user
     *
     * @param authentication The authenticated user
     * @param limit The maximum number of recommendations to return (default: 10)
     * @return A list of recommended books
     */
    @GetMapping("/personalized")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<BookResponse>> getPersonalizedRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Book> recommendations = recommendationService.getPersonalizedRecommendations(userDetails.getId(), limit);
        
        List<BookResponse> bookResponses = recommendations.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }

    /**
     * Get similar books to a specific book
     *
     * @param bookId The ID of the book to get similar books for
     * @param limit The maximum number of similar books to return (default: 5)
     * @return A list of similar books
     */
    @GetMapping("/similar/{bookId}")
    public ResponseEntity<List<BookResponse>> getSimilarBooks(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<Book> similarBooks = recommendationService.getSimilarBooks(bookId, limit);
        
        List<BookResponse> bookResponses = similarBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }

    /**
     * Get trending books based on recent loan activity
     *
     * @param limit The maximum number of books to return (default: 10)
     * @return A list of trending books
     */
    @GetMapping("/trending")
    public ResponseEntity<List<BookResponse>> getTrendingBooks(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Book> trendingBooks = recommendationService.getTrendingBooks(limit);
        
        List<BookResponse> bookResponses = trendingBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }

    /**
     * Get popular books based on total loan count
     *
     * @param limit The maximum number of books to return (default: 10)
     * @return A list of popular books
     */
    @GetMapping("/popular")
    public ResponseEntity<List<BookResponse>> getPopularBooks(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Book> popularBooks = recommendationService.getPopularBooks(limit);
        
        List<BookResponse> bookResponses = popularBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }
}
