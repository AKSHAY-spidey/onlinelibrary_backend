package com.library.controller;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.security.services.UserDetailsImpl;
import com.library.service.GeminiAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai")
public class AIChatbotController {

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    /**
     * Chat with the AI assistant
     *
     * @param request The chat request containing the user's message
     * @param authentication The authenticated user
     * @return The AI's response
     */
    @PostMapping("/chat")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userMessage = request.get("message");

        // Get user roles
        List<String> userRoles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());

        // Use the enhanced prompt with role-specific context
        String enhancedPrompt = geminiAIService.enhancePromptWithRoleContext(
                userMessage,
                userDetails.getUsername(),
                userRoles
        );

        try {
            // Generate response
            String aiResponse = geminiAIService.generateResponse(enhancedPrompt);

            Map<String, String> response = new HashMap<>();
            response.put("response", aiResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("response", "I'm sorry, I'm having trouble connecting to my knowledge base right now. Please try again later or contact library support if this issue persists.");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get personalized book recommendations
     *
     * @param authentication The authenticated user
     * @return AI-generated book recommendations
     */
    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getRecommendations(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get user's loan history
        List<Loan> userLoans = loanRepository.findByUserId(userDetails.getId());

        // Extract book information from loans
        List<String> readingHistory = userLoans.stream()
                .map(loan -> {
                    Book book = loan.getBook();
                    return book.getTitle() + " by " + book.getAuthor();
                })
                .collect(Collectors.toList());

        // If user has no reading history, provide some defaults
        if (readingHistory.isEmpty()) {
            readingHistory = Arrays.asList(
                "The Great Gatsby by F. Scott Fitzgerald",
                "To Kill a Mockingbird by Harper Lee",
                "1984 by George Orwell"
            );
        }

        // Prepare user preferences
        Map<String, Object> userPreferences = new HashMap<>();

        // Extract preferred genres based on reading history
        Map<String, Long> genreCounts = userLoans.stream()
                .map(loan -> loan.getBook().getCategory())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        category -> category,
                        Collectors.counting()
                ));

        // Get top 3 genres
        List<String> topGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        userPreferences.put("Preferred Genres", String.join(", ", topGenres));
        userPreferences.put("Reading Level", "Adult"); // Default to adult

        try {
            // Generate recommendations
            String recommendations = geminiAIService.generateBookRecommendations(userPreferences, readingHistory);

            Map<String, String> response = new HashMap<>();
            response.put("recommendations", recommendations);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate recommendations: " + e.getMessage());
            errorResponse.put("recommendations", "Here are some classic book recommendations:\n\n1. To Kill a Mockingbird by Harper Lee\n2. 1984 by George Orwell\n3. The Great Gatsby by F. Scott Fitzgerald");

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get a summary of a book
     *
     * @param bookId The ID of the book to summarize
     * @return AI-generated book summary
     */
    @GetMapping("/book-summary/{bookId}")
    public ResponseEntity<Map<String, String>> getBookSummary(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        String summary = geminiAIService.generateBookSummary(
                book.getTitle(),
                book.getAuthor(),
                book.getDescription() != null ? book.getDescription() : "No description available."
        );

        Map<String, String> response = new HashMap<>();
        response.put("summary", summary);

        return ResponseEntity.ok(response);
    }

    /**
     * Analyze the reading level of a book
     *
     * @param bookId The ID of the book to analyze
     * @return AI-generated reading level analysis
     */
    @GetMapping("/reading-level/{bookId}")
    public ResponseEntity<Map<String, String>> analyzeReadingLevel(@PathVariable Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        String textToAnalyze = book.getDescription() != null ? book.getDescription() :
                "This is a book titled " + book.getTitle() + " by " + book.getAuthor() + ".";

        String analysis = geminiAIService.analyzeReadingLevel(textToAnalyze);

        Map<String, String> response = new HashMap<>();
        response.put("readingLevelAnalysis", analysis);

        return ResponseEntity.ok(response);
    }

    /**
     * Get personalized reading insights
     *
     * @param authentication The authenticated user
     * @return AI-generated reading insights
     */
    @GetMapping("/reading-insights")
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getReadingInsights(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get user's loan history
        List<Loan> userLoans = loanRepository.findByUserId(userDetails.getId());

        // Extract book information from loans
        List<String> readingHistory = userLoans.stream()
                .map(loan -> {
                    Book book = loan.getBook();
                    return book.getTitle() + " by " + book.getAuthor() +
                           (book.getCategory() != null ? " (" + book.getCategory() + ")" : "");
                })
                .collect(Collectors.toList());

        // If user has no reading history, provide some defaults
        if (readingHistory.isEmpty()) {
            readingHistory = Arrays.asList(
                "The Great Gatsby by F. Scott Fitzgerald (Fiction)",
                "To Kill a Mockingbird by Harper Lee (Fiction)",
                "A Brief History of Time by Stephen Hawking (Science)"
            );
        }

        // Generate insights
        String insights = geminiAIService.generateReadingInsights(readingHistory);

        Map<String, String> response = new HashMap<>();
        response.put("readingInsights", insights);

        return ResponseEntity.ok(response);
    }

    /**
     * Answer a library-related question
     *
     * @param request The question request
     * @return AI-generated answer
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String context = request.get("context"); // Optional context

        String answer = geminiAIService.answerLibraryQuestion(question, context);

        Map<String, String> response = new HashMap<>();
        response.put("answer", answer);

        return ResponseEntity.ok(response);
    }

    /**
     * Answer a project-specific question about the Online Library Management System
     *
     * @param request The question request
     * @return AI-generated answer with project-specific details
     */
    @PostMapping("/project")
    public ResponseEntity<Map<String, String>> answerProjectQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");

        try {
            String answer = geminiAIService.answerProjectQuestion(question);

            Map<String, String> response = new HashMap<>();
            response.put("answer", answer);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("answer", "I'm sorry, I'm having trouble accessing project information right now. Please try again later.");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.ok(errorResponse);
        }
    }
}
