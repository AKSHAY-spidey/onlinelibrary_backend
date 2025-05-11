package com.library.service;

import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.payload.request.ReservationRequest;
import com.library.payload.response.ReservationResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ReservationService {
    /**
     * Create a new reservation
     *
     * @param authentication The authenticated user
     * @param reservationRequest The reservation request
     * @return The created reservation
     */
    ReservationResponse createReservation(Authentication authentication, ReservationRequest reservationRequest);
    
    /**
     * Cancel a reservation
     *
     * @param authentication The authenticated user
     * @param reservationId The ID of the reservation to cancel
     * @return The cancelled reservation
     */
    ReservationResponse cancelReservation(Authentication authentication, Long reservationId);
    
    /**
     * Get all reservations for the authenticated user
     *
     * @param authentication The authenticated user
     * @return List of reservations
     */
    List<ReservationResponse> getUserReservations(Authentication authentication);
    
    /**
     * Get all reservations for a specific book
     *
     * @param bookId The ID of the book
     * @return List of reservations
     */
    List<ReservationResponse> getBookReservations(Long bookId);
    
    /**
     * Get all reservations with a specific status
     *
     * @param status The reservation status
     * @return List of reservations
     */
    List<ReservationResponse> getReservationsByStatus(ReservationStatus status);
    
    /**
     * Process pending reservations for available books
     * This will convert pending reservations to active reservations
     * and notify users
     */
    void processAvailableReservations();
    
    /**
     * Convert a reservation to a loan
     *
     * @param reservationId The ID of the reservation to convert
     * @return True if the conversion was successful, false otherwise
     */
    boolean convertReservationToLoan(Long reservationId);
}
