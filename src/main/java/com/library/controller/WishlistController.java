package com.library.controller;

import com.library.payload.request.WishlistRequest;
import com.library.payload.response.MessageResponse;
import com.library.payload.response.WishlistResponse;
import com.library.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<List<WishlistResponse>> getUserWishlist(Authentication authentication) {
        List<WishlistResponse> wishlist = wishlistService.getUserWishlist(authentication);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<WishlistResponse> addToWishlist(
            Authentication authentication,
            @Valid @RequestBody WishlistRequest wishlistRequest) {
        WishlistResponse wishlist = wishlistService.addToWishlist(authentication, wishlistRequest);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long bookId) {
        wishlistService.removeFromWishlist(authentication, bookId);
        return ResponseEntity.ok(new MessageResponse("Book removed from wishlist successfully"));
    }

    @GetMapping("/check/{bookId}")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> isBookInWishlist(
            Authentication authentication,
            @PathVariable Long bookId) {
        boolean isInWishlist = wishlistService.isBookInWishlist(authentication, bookId);
        return ResponseEntity.ok(isInWishlist);
    }

    @PutMapping("/{bookId}/auto-borrow")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<WishlistResponse> updateAutoBorrow(
            Authentication authentication,
            @PathVariable Long bookId,
            @RequestParam boolean autoBorrow) {
        WishlistResponse wishlist = wishlistService.updateAutoBorrow(authentication, bookId, autoBorrow);
        return ResponseEntity.ok(wishlist);
    }
}
