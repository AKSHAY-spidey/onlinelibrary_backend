package com.library.payload.request;

import jakarta.validation.constraints.NotNull;

public class WishlistRequest {
    @NotNull
    private Long bookId;
    
    private Boolean autoBorrow = false;

    public WishlistRequest() {
    }

    public WishlistRequest(Long bookId, Boolean autoBorrow) {
        this.bookId = bookId;
        this.autoBorrow = autoBorrow;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Boolean getAutoBorrow() {
        return autoBorrow;
    }

    public void setAutoBorrow(Boolean autoBorrow) {
        this.autoBorrow = autoBorrow;
    }
}
