package com.library.payload.response;

import com.library.model.Wishlist;

import java.time.LocalDateTime;

public class WishlistResponse {
    private Long id;
    private BookResponse book;
    private UserResponse user;
    private LocalDateTime addedDate;
    private Boolean notificationSent;
    private Boolean autoBorrow;
    private Boolean isAvailable;

    public WishlistResponse() {
    }

    public WishlistResponse(Wishlist wishlist) {
        this.id = wishlist.getId();
        this.book = new BookResponse(wishlist.getBook());
        this.user = new UserResponse(wishlist.getUser());
        this.addedDate = wishlist.getAddedDate();
        this.notificationSent = wishlist.getNotificationSent();
        this.autoBorrow = wishlist.getAutoBorrow();
        this.isAvailable = wishlist.getBook().getAvailableCopies() > 0;
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

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    public Boolean getNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(Boolean notificationSent) {
        this.notificationSent = notificationSent;
    }

    public Boolean getAutoBorrow() {
        return autoBorrow;
    }

    public void setAutoBorrow(Boolean autoBorrow) {
        this.autoBorrow = autoBorrow;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }
}
