package com.library.service.impl;

import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.model.Wishlist;
import com.library.payload.request.LoanRequest;
import com.library.payload.request.WishlistRequest;
import com.library.payload.response.WishlistResponse;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import com.library.repository.WishlistRepository;
import com.library.security.services.UserDetailsImpl;
import com.library.service.EmailService;
import com.library.service.NotificationService;
import com.library.service.UserService;
import com.library.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public WishlistResponse addToWishlist(Authentication authentication, WishlistRequest wishlistRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(wishlistRequest.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Check if book is already in wishlist
        if (wishlistRepository.existsByUserAndBook(user, book)) {
            // Update auto-borrow setting if it exists
            Wishlist existingWishlist = wishlistRepository.findByUserAndBook(user, book)
                    .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
            existingWishlist.setAutoBorrow(wishlistRequest.getAutoBorrow());
            return new WishlistResponse(wishlistRepository.save(existingWishlist));
        }

        // Create new wishlist item
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setBook(book);
        wishlist.setAddedDate(LocalDateTime.now());
        wishlist.setAutoBorrow(wishlistRequest.getAutoBorrow());
        wishlist.setNotificationSent(false);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                "WISHLIST_ADDED",
                "You have added \"" + book.getTitle() + "\" to your wishlist.",
                savedWishlist
        );

        return new WishlistResponse(savedWishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Authentication authentication, Long bookId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Check if book is in wishlist
        Wishlist wishlist = wishlistRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in wishlist"));

        wishlistRepository.delete(wishlist);

        // Send notification to user
        notificationService.sendUserNotification(
                user.getId(),
                "WISHLIST_REMOVED",
                "You have removed \"" + book.getTitle() + "\" from your wishlist.",
                null
        );
    }

    @Override
    public List<WishlistResponse> getUserWishlist(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Wishlist> wishlistItems = wishlistRepository.findByUser(user);
        return wishlistItems.stream()
                .map(WishlistResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isBookInWishlist(Authentication authentication, Long bookId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return wishlistRepository.findByBookIdAndUserId(bookId, userDetails.getId()).isPresent();
    }

    @Override
    @Transactional
    public WishlistResponse updateAutoBorrow(Authentication authentication, Long bookId, boolean autoBorrow) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Check if book is in wishlist
        Wishlist wishlist = wishlistRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in wishlist"));

        wishlist.setAutoBorrow(autoBorrow);
        Wishlist updatedWishlist = wishlistRepository.save(wishlist);

        // Send notification to user
        String message = autoBorrow
                ? "Auto-borrow enabled for \"" + book.getTitle() + "\". It will be automatically borrowed when available."
                : "Auto-borrow disabled for \"" + book.getTitle() + "\".";

        notificationService.sendUserNotification(
                user.getId(),
                "WISHLIST_UPDATED",
                message,
                updatedWishlist
        );

        return new WishlistResponse(updatedWishlist);
    }

    @Override
    @Transactional
    public void processAvailableWishlistItems() {
        // Get all wishlist items with available books that haven't been notified yet
        List<Wishlist> availableItems = wishlistRepository.findWishlistItemsWithAvailableBooks();

        for (Wishlist wishlist : availableItems) {
            User user = wishlist.getUser();
            Book book = wishlist.getBook();

            // Send notification to user
            notificationService.sendUserNotification(
                    user.getId(),
                    "BOOK_AVAILABLE",
                    "A book in your wishlist is now available: \"" + book.getTitle() + "\".",
                    wishlist
            );

            // Send email notification
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendWishlistAvailabilityEmail(
                        user.getEmail(),
                        user.getUsername(),
                        book.getTitle(),
                        book.getAuthor()
                );
            }

            // Mark as notification sent
            wishlist.setNotificationSent(true);
            wishlistRepository.save(wishlist);
        }

        // Process auto-borrow items
        List<Wishlist> autoBorrowItems = wishlistRepository.findWishlistItemsForAutoBorrow();

        for (Wishlist wishlist : autoBorrowItems) {
            User user = wishlist.getUser();
            Book book = wishlist.getBook();

            // Create a loan request
            LoanRequest loanRequest = new LoanRequest();
            loanRequest.setBookId(book.getId());
            loanRequest.setRequestedReturnDate(LocalDate.now().plusDays(14)); // 2 weeks loan period

            try {
                // Request the loan
                Authentication auth = new Authentication() {
                    @Override
                    public String getName() {
                        return user.getUsername();
                    }

                    @Override
                    public Object getPrincipal() {
                        return UserDetailsImpl.build(user);
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return true;
                    }

                    @Override
                    public void setAuthenticated(boolean isAuthenticated) {
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                        return null;
                    }
                };

                userService.requestLoan(auth, loanRequest);

                // Send notification to user
                notificationService.sendUserNotification(
                        user.getId(),
                        "AUTO_BORROW",
                        "A book in your wishlist has been automatically borrowed: \"" + book.getTitle() + "\".",
                        wishlist
                );

                // Send email notification
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    emailService.sendAutoBorrowEmail(
                            user.getEmail(),
                            user.getUsername(),
                            book.getTitle(),
                            book.getAuthor(),
                            loanRequest.getRequestedReturnDate()
                    );
                }

                // Remove from wishlist
                wishlistRepository.delete(wishlist);
            } catch (Exception e) {
                // If auto-borrow fails, just mark as notification sent
                wishlist.setNotificationSent(true);
                wishlistRepository.save(wishlist);
            }
        }
    }
}
