package com.library.controller;

import com.library.model.Book;
import com.library.payload.request.BookRequest;
import com.library.payload.response.MessageResponse;
import com.library.repository.BookRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    BookRepository bookRepository;

    @GetMapping("/public/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllBooksAdmin() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/public/search/title/{title}")
    public ResponseEntity<List<Book>> searchBooksByTitle(@PathVariable String title) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/public/search/author/{author}")
    public ResponseEntity<List<Book>> searchBooksByAuthor(@PathVariable String author) {
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/public/search/category/{category}")
    public ResponseEntity<List<Book>> searchBooksByCategory(@PathVariable String category) {
        List<Book> books = bookRepository.findByCategoryContainingIgnoreCase(category);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/public/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        List<Book> books = bookRepository.findAvailableBooks();
        return ResponseEntity.ok(books);
    }

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> addBook(@Valid @RequestBody BookRequest bookRequest) {
        if (bookRequest.getIsbn() != null && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: ISBN already exists!"));
        }

        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setAuthor(bookRequest.getAuthor());
        book.setIsbn(bookRequest.getIsbn());
        book.setPublicationDate(bookRequest.getPublicationDate());
        book.setPublisher(bookRequest.getPublisher());
        book.setCategory(bookRequest.getCategory());
        book.setDescription(bookRequest.getDescription());
        book.setTotalCopies(bookRequest.getTotalCopies());
        book.setAvailableCopies(bookRequest.getTotalCopies()); // Initially all copies are available
        book.setCoverImageUrl(bookRequest.getCoverImageUrl());
        book.setLanguage(bookRequest.getLanguage());
        book.setPages(bookRequest.getPages());

        bookRepository.save(book);
        return ResponseEntity.ok(new MessageResponse("Book added successfully!"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest bookRequest) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(bookRequest.getTitle());
                    book.setAuthor(bookRequest.getAuthor());

                    // Check if ISBN is being changed and if it already exists
                    if (bookRequest.getIsbn() != null && !bookRequest.getIsbn().equals(book.getIsbn())
                            && bookRepository.existsByIsbn(bookRequest.getIsbn())) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: ISBN already exists!"));
                    }

                    book.setIsbn(bookRequest.getIsbn());
                    book.setPublicationDate(bookRequest.getPublicationDate());
                    book.setPublisher(bookRequest.getPublisher());
                    book.setCategory(bookRequest.getCategory());
                    book.setDescription(bookRequest.getDescription());

                    // Update available copies based on the change in total copies
                    int oldTotal = book.getTotalCopies();
                    int newTotal = bookRequest.getTotalCopies();
                    int availableCopies = book.getAvailableCopies();

                    if (newTotal >= oldTotal) {
                        // If total copies increased, add the difference to available copies
                        availableCopies += (newTotal - oldTotal);
                    } else {
                        // If total copies decreased, reduce available copies but not below 0
                        availableCopies = Math.max(0, availableCopies - (oldTotal - newTotal));
                    }

                    book.setTotalCopies(newTotal);
                    book.setAvailableCopies(availableCopies);

                    book.setCoverImageUrl(bookRequest.getCoverImageUrl());
                    book.setLanguage(bookRequest.getLanguage());
                    book.setPages(bookRequest.getPages());

                    bookRepository.save(book);
                    return ResponseEntity.ok(new MessageResponse("Book updated successfully!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(book -> {
                    bookRepository.delete(book);
                    return ResponseEntity.ok(new MessageResponse("Book deleted successfully!"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
