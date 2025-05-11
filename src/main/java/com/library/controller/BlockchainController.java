package com.library.controller;

import com.library.blockchain.Block;
import com.library.blockchain.Transaction;
import com.library.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;

    /**
     * Get the status of the blockchain
     * Accessible by admins only
     *
     * @return Blockchain status information
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBlockchainStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("blockCount", blockchainService.getBlockchain().getChain().size());
        status.put("pendingTransactions", blockchainService.getPendingTransactions().size());
        status.put("isValid", blockchainService.verifyBlockchain());
        status.put("difficulty", blockchainService.getBlockchain().getDifficulty());
        
        return ResponseEntity.ok(status);
    }

    /**
     * Get all blocks in the blockchain
     * Accessible by admins only
     *
     * @return List of blocks in the blockchain
     */
    @GetMapping("/blocks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Block>> getAllBlocks() {
        List<Block> blocks = blockchainService.getBlockchain().getChain();
        return ResponseEntity.ok(blocks);
    }

    /**
     * Get a specific block in the blockchain
     * Accessible by admins only
     *
     * @param index The index of the block
     * @return The block at the specified index
     */
    @GetMapping("/blocks/{index}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Block> getBlock(@PathVariable int index) {
        List<Block> chain = blockchainService.getBlockchain().getChain();
        
        if (index < 0 || index >= chain.size()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(chain.get(index));
    }

    /**
     * Get all pending transactions
     * Accessible by admins and librarians
     *
     * @return List of pending transactions
     */
    @GetMapping("/transactions/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<Transaction>> getPendingTransactions() {
        List<Transaction> pendingTransactions = blockchainService.getPendingTransactions();
        return ResponseEntity.ok(pendingTransactions);
    }

    /**
     * Get all transactions for a specific user
     * Accessible by the user themselves, librarians, and admins
     *
     * @param userId The ID of the user
     * @return List of transactions involving the user
     */
    @GetMapping("/transactions/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or (hasRole('USER') and #userId == authentication.principal.id)")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> userTransactions = blockchainService.getUserTransactions(userId);
        return ResponseEntity.ok(userTransactions);
    }

    /**
     * Get all transactions for a specific book
     * Accessible by librarians and admins
     *
     * @param bookId The ID of the book
     * @return List of transactions involving the book
     */
    @GetMapping("/transactions/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN')")
    public ResponseEntity<List<Transaction>> getBookTransactions(@PathVariable Long bookId) {
        List<Transaction> bookTransactions = blockchainService.getBookTransactions(bookId);
        return ResponseEntity.ok(bookTransactions);
    }

    /**
     * Get all transactions of a specific type
     * Accessible by admins only
     *
     * @param type The type of transaction
     * @return List of transactions of the specified type
     */
    @GetMapping("/transactions/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transaction>> getTransactionsByType(@PathVariable String type) {
        List<Transaction> typeTransactions = blockchainService.getTransactionsByType(type);
        return ResponseEntity.ok(typeTransactions);
    }

    /**
     * Manually trigger mining of pending transactions
     * Accessible by admins only
     *
     * @return Status message
     */
    @PostMapping("/mine")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> minePendingTransactions() {
        blockchainService.minePendingTransactions();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mining completed successfully");
        response.put("blockCount", String.valueOf(blockchainService.getBlockchain().getChain().size()));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verify the integrity of the blockchain
     * Accessible by admins only
     *
     * @return Verification result
     */
    @GetMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> verifyBlockchain() {
        boolean isValid = blockchainService.verifyBlockchain();
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        
        return ResponseEntity.ok(response);
    }
}
