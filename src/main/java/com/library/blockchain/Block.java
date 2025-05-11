package com.library.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a block in the blockchain
 */
public class Block {
    private int index;
    private LocalDateTime timestamp;
    private String previousHash;
    private String hash;
    private List<Transaction> transactions;
    private int nonce;

    /**
     * Create a new block
     *
     * @param index The index of the block in the chain
     * @param timestamp The timestamp when the block was created
     * @param previousHash The hash of the previous block
     */
    public Block(int index, LocalDateTime timestamp, String previousHash) {
        this.index = index;
        this.timestamp = timestamp;
        this.previousHash = previousHash;
        this.transactions = new ArrayList<>();
        this.nonce = 0;
        this.hash = calculateHash();
    }

    /**
     * Calculate the hash of the block
     *
     * @return The hash of the block
     */
    public String calculateHash() {
        String dataToHash = index + timestamp.toString() + previousHash + transactions.toString() + nonce;
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mine the block with a specific difficulty
     *
     * @param difficulty The number of leading zeros required in the hash
     */
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        
        System.out.println("Block mined: " + hash);
    }

    /**
     * Add a transaction to the block
     *
     * @param transaction The transaction to add
     * @return True if the transaction was added successfully
     */
    public boolean addTransaction(Transaction transaction) {
        // Validate transaction
        if (transaction == null) return false;
        
        // Add transaction
        transactions.add(transaction);
        return true;
    }

    // Getters and setters
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }
}
