package com.library.service;

import com.library.model.Wishlist;
import com.library.payload.request.WishlistRequest;
import com.library.payload.response.WishlistResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface WishlistService {
    /**
     * Add a book to the user's wishlist
     *
     * @param authentication The authenticated user
     * @param wishlistRequest The wishlist request containing the book ID and auto-borrow flag
     * @return The created wishlist item
     */
    WishlistResponse addToWishlist(Authentication authentication, WishlistRequest wishlistRequest);
    
    /**
     * Remove a book from the user's wishlist
     *
     * @param authentication The authenticated user
     * @param bookId The ID of the book to remove
     */
    void removeFromWishlist(Authentication authentication, Long bookId);
    
    /**
     * Get all items in the user's wishlist
     *
     * @param authentication The authenticated user
     * @return List of wishlist items
     */
    List<WishlistResponse> getUserWishlist(Authentication authentication);
    
    /**
     * Check if a book is in the user's wishlist
     *
     * @param authentication The authenticated user
     * @param bookId The ID of the book to check
     * @return True if the book is in the wishlist, false otherwise
     */
    boolean isBookInWishlist(Authentication authentication, Long bookId);
    
    /**
     * Update the auto-borrow setting for a wishlist item
     *
     * @param authentication The authenticated user
     * @param bookId The ID of the book
     * @param autoBorrow Whether to automatically borrow the book when available
     * @return The updated wishlist item
     */
    WishlistResponse updateAutoBorrow(Authentication authentication, Long bookId, boolean autoBorrow);
    
    /**
     * Process wishlist items with available books
     * This will send notifications and/or automatically borrow books
     */
    void processAvailableWishlistItems();
}
