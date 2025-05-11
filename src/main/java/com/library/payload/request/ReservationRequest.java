package com.library.payload.request;

import jakarta.validation.constraints.NotNull;

public class ReservationRequest {
    @NotNull
    private Long bookId;

    private Boolean autoConvert = false;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Boolean getAutoConvert() {
        return autoConvert;
    }

    public void setAutoConvert(Boolean autoConvert) {
        this.autoConvert = autoConvert;
    }
}
