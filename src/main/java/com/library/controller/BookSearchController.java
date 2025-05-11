package com.library.controller;

import com.library.model.Book;
import com.library.payload.response.BookResponse;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books/search")
public class BookSearchController {

    @Autowired
    private BookRepository bookRepository;

    /**
     * Advanced search endpoint that allows searching by multiple criteria
     */
    @GetMapping
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String publisher) {
        
        // Use the searchBooks method from the repository
        List<Book> books = bookRepository.searchBooks(title, author, category);
        
        // Additional filtering for ISBN and publisher if provided
        if (isbn != null && !isbn.isEmpty()) {
            books = books.stream()
                    .filter(book -> book.getIsbn() != null && book.getIsbn().contains(isbn))
                    .collect(Collectors.toList());
        }
        
        if (publisher != null && !publisher.isEmpty()) {
            books = books.stream()
                    .filter(book -> book.getPublisher() != null && 
                            book.getPublisher().toLowerCase().contains(publisher.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        // Convert to BookResponse objects
        List<BookResponse> bookResponses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }
    
    /**
     * Get books by category
     */
    @GetMapping("/category")
    public ResponseEntity<List<BookResponse>> getBooksByCategory(@RequestParam String category) {
        List<Book> books = bookRepository.findAll().stream()
                .filter(book -> book.getCategory() != null && 
                        book.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        
        List<BookResponse> bookResponses = books.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bookResponses);
    }
    
    /**
     * Get all available categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = bookRepository.findAll().stream()
                .map(Book::getCategory)
                .filter(category -> category != null && !category.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categories);
    }
}
