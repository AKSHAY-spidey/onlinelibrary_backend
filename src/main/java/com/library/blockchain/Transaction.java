package com.library.blockchain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a transaction in the blockchain
 */
public class Transaction {
    private String transactionId;
    private String type; // LOAN, RETURN, FINE_PAYMENT, etc.
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private LocalDateTime timestamp;
    private String details;
    private String signature; // Digital signature for verification

    /**
     * Create a new transaction
     *
     * @param type The type of transaction
     * @param userId The ID of the user involved in the transaction
     * @param username The username of the user
     * @param bookId The ID of the book involved in the transaction
     * @param bookTitle The title of the book
     * @param details Additional details about the transaction
     */
    public Transaction(String type, Long userId, String username, Long bookId, String bookTitle, String details) {
        this.transactionId = UUID.randomUUID().toString();
        this.type = type;
        this.userId = userId;
        this.username = username;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

    /**
     * Generate a digital signature for the transaction
     *
     * @param privateKey The private key to sign the transaction with
     */
    public void generateSignature(String privateKey) {
        // In a real implementation, this would use asymmetric cryptography
        // For simplicity, we'll just concatenate the data with the private key and hash it
        String data = userId + bookId + timestamp.toString() + type + details;
        this.signature = data + privateKey;
    }

    /**
     * Verify the digital signature of the transaction
     *
     * @param publicKey The public key to verify the signature with
     * @return True if the signature is valid
     */
    public boolean verifySignature(String publicKey) {
        // In a real implementation, this would use asymmetric cryptography
        // For simplicity, we'll just check if the signature ends with the public key
        String data = userId + bookId + timestamp.toString() + type + details;
        return signature != null && signature.equals(data + publicKey);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", type='" + type + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", bookId=" + bookId +
                ", bookTitle='" + bookTitle + '\'' +
                ", timestamp=" + timestamp +
                ", details='" + details + '\'' +
                '}';
    }

    // Getters and setters
    
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
