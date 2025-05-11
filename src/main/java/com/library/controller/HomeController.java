package com.library.controller;

import com.library.model.Book;
import com.library.payload.response.BookResponse;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private BookRepository bookRepository;

    /**
     * Get recently added books
     *
     * @param limit The maximum number of books to return (default: 10)
     * @return A list of recently added books
     */
    @GetMapping("/recent-books")
    public ResponseEntity<List<BookResponse>> getRecentBooks(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Book> recentBooks = bookRepository.findRecentlyAdded(PageRequest.of(0, limit));
        
        List<BookResponse> bookResponses = recentBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }

    /**
     * Get random books
     *
     * @param limit The maximum number of books to return (default: 10)
     * @return A list of random books
     */
    @GetMapping("/random-books")
    public ResponseEntity<List<BookResponse>> getRandomBooks(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Book> randomBooks = bookRepository.findRandomBooks(limit);
        
        List<BookResponse> bookResponses = randomBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }
}
