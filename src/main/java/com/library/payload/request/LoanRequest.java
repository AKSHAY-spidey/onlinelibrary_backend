package com.library.payload.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LoanRequest {
    @NotNull
    private Long bookId;
    
    @NotNull
    private LocalDate requestedReturnDate;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public LocalDate getRequestedReturnDate() {
        return requestedReturnDate;
    }

    public void setRequestedReturnDate(LocalDate requestedReturnDate) {
        this.requestedReturnDate = requestedReturnDate;
    }
}
