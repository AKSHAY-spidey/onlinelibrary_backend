package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "book_id"})
       })
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"wishlists", "loans", "password", "roles"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonBackReference
    private Book book;

    @NotNull
    @Column(name = "added_date")
    private LocalDateTime addedDate;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "auto_borrow")
    private Boolean autoBorrow = false;

    public Wishlist() {
    }

    public Wishlist(User user, Book book, LocalDateTime addedDate, Boolean autoBorrow) {
        this.user = user;
        this.book = book;
        this.addedDate = addedDate;
        this.autoBorrow = autoBorrow;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
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
}
