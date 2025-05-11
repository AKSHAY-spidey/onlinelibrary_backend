package com.library.controller;

import com.library.model.Book;
import com.library.payload.response.BookResponse;
import com.library.service.SemanticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/search/semantic")
public class SemanticSearchController {

    @Autowired
    private SemanticSearchService semanticSearchService;

    /**
     * Perform semantic search for books based on a natural language query
     *
     * @param query The natural language query
     * @param limit The maximum number of results to return (default: 10)
     * @return A list of books ranked by semantic similarity to the query
     */
    @GetMapping
    public ResponseEntity<List<BookResponse>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Book> books = semanticSearchService.semanticSearch(query, limit);
        
        List<BookResponse> bookResponses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }

    /**
     * Find books similar to a given book using semantic similarity
     *
     * @param bookId The ID of the reference book
     * @param limit The maximum number of similar books to return (default: 5)
     * @return A list of books similar to the reference book
     */
    @GetMapping("/similar/{bookId}")
    public ResponseEntity<List<BookResponse>> findSimilarBooks(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<Book> similarBooks = semanticSearchService.findSimilarBooks(bookId, limit);
        
        List<BookResponse> bookResponses = similarBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }
}
