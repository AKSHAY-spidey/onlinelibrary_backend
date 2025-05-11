package com.library.service;

import com.library.payload.request.BookRequest;
import com.library.payload.response.BookResponse;
import com.library.payload.response.LoanResponse;

import java.util.List;

public interface LibrarianService {
    BookResponse addBook(BookRequest bookRequest);

    BookResponse updateBook(Long id, BookRequest bookRequest);

    void deleteBook(Long id);

    List<BookResponse> getAllBooks();

    List<LoanResponse> getAllLoans();

    void approveLoan(Long id);

    void rejectLoan(Long id);

    void processReturn(Long id);

    void modifyReturnDate(Long id, java.time.LocalDate newReturnDate);
}
