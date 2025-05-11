package com.library.service;

import com.library.blockchain.Blockchain;
import com.library.blockchain.Transaction;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Service
public class BlockchainService {

    private Blockchain blockchain;
    private static final int MINING_DIFFICULTY = 2; // Number of leading zeros required in block hash
    private static final String MINING_REWARD = "LIBRARY_TOKEN"; // Reward for mining a block
    private static final String MINING_REWARD_ADDRESS = "LIBRARY_SYSTEM"; // Address to receive mining rewards

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    /**
     * Initialize the blockchain
     */
    @PostConstruct
    public void init() {
        blockchain = new Blockchain(MINING_DIFFICULTY, MINING_REWARD);
        System.out.println("Blockchain initialized with difficulty " + MINING_DIFFICULTY);
    }

    /**
     * Record a loan transaction in the blockchain
     *
     * @param loan The loan to record
     */
    public void recordLoan(Loan loan) {
        User user = loan.getUser();
        Book book = loan.getBook();

        Transaction transaction = new Transaction(
                "LOAN",
                user.getId(),
                user.getUsername(),
                book.getId(),
                book.getTitle(),
                "Book loaned on " + loan.getLoanDate() + " with due date " + loan.getDueDate()
        );

        // In a real implementation, we would sign the transaction with the user's private key
        transaction.generateSignature("library_private_key");

        blockchain.addTransaction(transaction);
        System.out.println("Loan transaction added to pending transactions");
    }

    /**
     * Record a return transaction in the blockchain
     *
     * @param loan The loan to record the return for
     */
    public void recordReturn(Loan loan) {
        User user = loan.getUser();
        Book book = loan.getBook();

        Transaction transaction = new Transaction(
                "RETURN",
                user.getId(),
                user.getUsername(),
                book.getId(),
                book.getTitle(),
                "Book returned on " + loan.getReturnDate()
        );

        // In a real implementation, we would sign the transaction with the user's private key
        transaction.generateSignature("library_private_key");

        blockchain.addTransaction(transaction);
        System.out.println("Return transaction added to pending transactions");
    }

    /**
     * Record a fine payment transaction in the blockchain
     *
     * @param loan The loan to record the fine payment for
     * @param amount The amount of the fine
     */
    public void recordFinePayment(Loan loan, double amount) {
        User user = loan.getUser();
        Book book = loan.getBook();

        Transaction transaction = new Transaction(
                "FINE_PAYMENT",
                user.getId(),
                user.getUsername(),
                book.getId(),
                book.getTitle(),
                "Fine payment of $" + amount + " for overdue book"
        );

        // In a real implementation, we would sign the transaction with the user's private key
        transaction.generateSignature("library_private_key");

        blockchain.addTransaction(transaction);
        System.out.println("Fine payment transaction added to pending transactions");
    }

    /**
     * Mine pending transactions every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void minePendingTransactions() {
        if (blockchain.getPendingTransactions().isEmpty()) {
            System.out.println("No pending transactions to mine");
            return;
        }

        System.out.println("Mining pending transactions...");
        blockchain.minePendingTransactions(MINING_REWARD_ADDRESS);
        System.out.println("Blockchain now has " + blockchain.getChain().size() + " blocks");
    }

    /**
     * Get all transactions for a specific user
     *
     * @param userId The ID of the user
     * @return A list of transactions involving the user
     */
    public List<Transaction> getUserTransactions(Long userId) {
        return blockchain.getTransactionsForUser(userId);
    }

    /**
     * Get all transactions for a specific book
     *
     * @param bookId The ID of the book
     * @return A list of transactions involving the book
     */
    public List<Transaction> getBookTransactions(Long bookId) {
        return blockchain.getTransactionsForBook(bookId);
    }

    /**
     * Get all transactions of a specific type
     *
     * @param type The type of transaction
     * @return A list of transactions of the specified type
     */
    public List<Transaction> getTransactionsByType(String type) {
        return blockchain.getTransactionsByType(type);
    }

    /**
     * Get all pending transactions
     *
     * @return A list of pending transactions
     */
    public List<Transaction> getPendingTransactions() {
        return blockchain.getPendingTransactions();
    }

    /**
     * Verify the integrity of the blockchain
     *
     * @return True if the blockchain is valid
     */
    public boolean verifyBlockchain() {
        return blockchain.isChainValid();
    }

    /**
     * Get the entire blockchain
     *
     * @return The blockchain
     */
    public Blockchain getBlockchain() {
        return blockchain;
    }

    /**
     * Record a loan modification transaction in the blockchain
     *
     * @param loan The loan being modified
     * @param oldDueDate The original due date
     * @param newDueDate The new due date
     */
    public void recordLoanModification(Loan loan, java.time.LocalDate oldDueDate, java.time.LocalDate newDueDate) {
        User user = loan.getUser();
        Book book = loan.getBook();

        Transaction transaction = new Transaction(
                "LOAN_MODIFICATION",
                user.getId(),
                user.getUsername(),
                book.getId(),
                book.getTitle(),
                "Loan due date modified from " + oldDueDate + " to " + newDueDate
        );

        // Add the transaction to the pending transactions
        blockchain.addTransaction(transaction);

        // Mine a new block if there are enough pending transactions
        if (blockchain.getPendingTransactions().size() >= 3) {
            System.out.println("Mining a new block with loan modification transaction...");
            blockchain.minePendingTransactions(MINING_REWARD_ADDRESS);
        }
    }
}
