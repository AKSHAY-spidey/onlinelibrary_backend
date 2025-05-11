package com.library.controller;

import com.library.model.Book;
import com.library.payload.response.BookResponse;
import com.library.service.NLPSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/search/nlp")
public class NLPSearchController {

    @Autowired
    private NLPSearchService nlpSearchService;

    /**
     * Search for books using natural language processing
     *
     * @param query The natural language query
     * @param limit The maximum number of results to return (default: 10)
     * @return A list of books matching the query
     */
    @GetMapping
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Book> books = nlpSearchService.searchBooks(query, limit);
        
        List<BookResponse> bookResponses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }

    /**
     * Get search suggestions based on a partial query
     *
     * @param query The partial query
     * @param limit The maximum number of suggestions to return (default: 5)
     * @return A list of search suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<String> suggestions = nlpSearchService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(suggestions);
    }
}
