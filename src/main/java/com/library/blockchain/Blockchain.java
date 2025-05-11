package com.library.blockchain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a blockchain for tracking library transactions
 */
public class Blockchain {
    private List<Block> chain;
    private int difficulty;
    private List<Transaction> pendingTransactions;
    private String miningReward;

    /**
     * Create a new blockchain
     *
     * @param difficulty The mining difficulty (number of leading zeros required in block hash)
     * @param miningReward The reward for mining a block
     */
    public Blockchain(int difficulty, String miningReward) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        this.pendingTransactions = new ArrayList<>();
        this.miningReward = miningReward;
        
        // Create the genesis block
        createGenesisBlock();
    }

    /**
     * Create the genesis block (first block in the chain)
     */
    private void createGenesisBlock() {
        Block genesisBlock = new Block(0, LocalDateTime.now(), "0");
        genesisBlock.setHash(genesisBlock.calculateHash());
        chain.add(genesisBlock);
    }

    /**
     * Get the latest block in the chain
     *
     * @return The latest block
     */
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    /**
     * Add a new transaction to the pending transactions
     *
     * @param transaction The transaction to add
     */
    public void addTransaction(Transaction transaction) {
        // Validate transaction
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        // Add transaction to pending transactions
        pendingTransactions.add(transaction);
    }

    /**
     * Mine pending transactions and add a new block to the chain
     *
     * @param miningRewardAddress The address to receive the mining reward
     */
    public void minePendingTransactions(String miningRewardAddress) {
        // Create a new block with all pending transactions
        Block block = new Block(chain.size(), LocalDateTime.now(), getLatestBlock().getHash());
        
        // Add all pending transactions to the block
        for (Transaction transaction : pendingTransactions) {
            block.addTransaction(transaction);
        }
        
        // Mine the block
        System.out.println("Mining block...");
        block.mineBlock(difficulty);
        
        // Add the block to the chain
        System.out.println("Block successfully mined!");
        chain.add(block);
        
        // Reset pending transactions and add mining reward
        pendingTransactions = new ArrayList<>();
        Transaction rewardTransaction = new Transaction(
                "MINING_REWARD",
                0L,
                "SYSTEM",
                0L,
                "N/A",
                "Mining reward of " + miningReward
        );
        pendingTransactions.add(rewardTransaction);
    }

    /**
     * Check if the blockchain is valid
     *
     * @return True if the blockchain is valid
     */
    public boolean isChainValid() {
        // Iterate through the chain (skipping genesis block)
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);
            
            // Check if the current block's hash is valid
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Invalid hash for block " + i);
                return false;
            }
            
            // Check if the current block points to the correct previous block
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.out.println("Invalid previous hash for block " + i);
                return false;
            }
        }
        
        return true;
    }

    /**
     * Get all transactions for a specific user
     *
     * @param userId The ID of the user
     * @return A list of transactions involving the user
     */
    public List<Transaction> getTransactionsForUser(Long userId) {
        List<Transaction> userTransactions = new ArrayList<>();
        
        // Iterate through all blocks (except genesis block)
        for (int i = 1; i < chain.size(); i++) {
            Block block = chain.get(i);
            
            // Add transactions involving the user
            userTransactions.addAll(block.getTransactions().stream()
                    .filter(transaction -> transaction.getUserId().equals(userId))
                    .collect(Collectors.toList()));
        }
        
        return userTransactions;
    }

    /**
     * Get all transactions for a specific book
     *
     * @param bookId The ID of the book
     * @return A list of transactions involving the book
     */
    public List<Transaction> getTransactionsForBook(Long bookId) {
        List<Transaction> bookTransactions = new ArrayList<>();
        
        // Iterate through all blocks (except genesis block)
        for (int i = 1; i < chain.size(); i++) {
            Block block = chain.get(i);
            
            // Add transactions involving the book
            bookTransactions.addAll(block.getTransactions().stream()
                    .filter(transaction -> transaction.getBookId().equals(bookId))
                    .collect(Collectors.toList()));
        }
        
        return bookTransactions;
    }

    /**
     * Get all transactions of a specific type
     *
     * @param type The type of transaction
     * @return A list of transactions of the specified type
     */
    public List<Transaction> getTransactionsByType(String type) {
        List<Transaction> typeTransactions = new ArrayList<>();
        
        // Iterate through all blocks (except genesis block)
        for (int i = 1; i < chain.size(); i++) {
            Block block = chain.get(i);
            
            // Add transactions of the specified type
            typeTransactions.addAll(block.getTransactions().stream()
                    .filter(transaction -> transaction.getType().equals(type))
                    .collect(Collectors.toList()));
        }
        
        return typeTransactions;
    }

    // Getters and setters
    
    public List<Block> getChain() {
        return chain;
    }

    public void setChain(List<Block> chain) {
        this.chain = chain;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public List<Transaction> getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(List<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public String getMiningReward() {
        return miningReward;
    }

    public void setMiningReward(String miningReward) {
        this.miningReward = miningReward;
    }
}
