package com.library.payload.request;

import jakarta.validation.constraints.NotNull;

public class BorrowingRequest {
    @NotNull
    private Long bookId;

    @NotNull
    private Long userId;

    private Integer borrowDays = 14; // Default borrowing period

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getBorrowDays() {
        return borrowDays;
    }

    public void setBorrowDays(Integer borrowDays) {
        this.borrowDays = borrowDays;
    }
}
