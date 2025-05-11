package com.library.payload.response;

import com.library.model.Reservation;
import com.library.model.ReservationStatus;

import java.time.LocalDate;

public class ReservationResponse {
    private Long id;
    private BookResponse book;
    private UserResponse user;
    private LocalDate reservationDate;
    private LocalDate expiryDate;
    private ReservationStatus status;
    private Boolean autoConvert;
    private Boolean isAvailable;

    public ReservationResponse() {
    }

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.book = new BookResponse(reservation.getBook());
        this.user = new UserResponse(reservation.getUser());
        this.reservationDate = reservation.getReservationDate();
        this.expiryDate = reservation.getExpiryDate();
        this.status = reservation.getStatus();
        this.isAvailable = reservation.getBook().getAvailableCopies() > 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookResponse getBook() {
        return book;
    }

    public void setBook(BookResponse book) {
        this.book = book;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Boolean getAutoConvert() {
        return autoConvert;
    }

    public void setAutoConvert(Boolean autoConvert) {
        this.autoConvert = autoConvert;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }
}
