package com.library.service;

import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NLPSearchService {

    @Autowired
    private BookRepository bookRepository;

    // Common English stop words
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "the", "and", "or", "but", "about", "above", "after", "along", "amid", "among",
            "as", "at", "by", "for", "from", "in", "into", "like", "minus", "near", "of", "off", "on",
            "onto", "out", "over", "past", "per", "plus", "since", "till", "to", "under", "until", "up",
            "via", "vs", "with", "that", "can", "cannot", "could", "may", "might", "must",
            "need", "ought", "shall", "should", "will", "would", "have", "had", "has", "having", "be",
            "is", "am", "are", "was", "were", "being", "been", "get", "gets", "got", "gotten",
            "getting", "i", "you", "he", "she", "it", "we", "they", "who", "which", "this", "that"
    ));

    /**
     * Search for books using natural language processing techniques
     *
     * @param query The natural language query
     * @param limit The maximum number of results to return
     * @return A list of books matching the query
     */
    public List<Book> searchBooks(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Preprocess the query
        List<String> queryTokens = preprocessText(query);
        
        // Extract potential entities (authors, titles, categories)
        Map<String, List<String>> entities = extractEntities(query);
        
        // Get all books
        List<Book> allBooks = bookRepository.findAll();
        
        // Score each book based on the query
        Map<Book, Double> bookScores = new HashMap<>();
        
        for (Book book : allBooks) {
            double score = calculateRelevanceScore(book, queryTokens, entities);
            if (score > 0) {
                bookScores.put(book, score);
            }
        }
        
        // Sort books by score and return top results
        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Preprocess text by tokenizing, removing stop words, and stemming
     *
     * @param text The text to preprocess
     * @return A list of preprocessed tokens
     */
    private List<String> preprocessText(String text) {
        // Convert to lowercase
        String lowercaseText = text.toLowerCase();
        
        // Remove punctuation
        String noPunctuation = lowercaseText.replaceAll("[^a-zA-Z0-9\\s]", " ");
        
        // Tokenize
        String[] tokens = noPunctuation.split("\\s+");
        
        // Remove stop words and apply simple stemming
        return Arrays.stream(tokens)
                .filter(token -> !token.isEmpty() && !STOP_WORDS.contains(token))
                .map(this::simpleStemming)
                .collect(Collectors.toList());
    }

    /**
     * Apply simple stemming to a word
     *
     * @param word The word to stem
     * @return The stemmed word
     */
    private String simpleStemming(String word) {
        // Very simple stemming rules
        if (word.endsWith("ing")) {
            return word.substring(0, word.length() - 3);
        } else if (word.endsWith("ed")) {
            return word.substring(0, word.length() - 2);
        } else if (word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        } else {
            return word;
        }
    }

    /**
     * Extract potential entities from the query
     *
     * @param query The query to extract entities from
     * @return A map of entity types to lists of potential entities
     */
    private Map<String, List<String>> extractEntities(String query) {
        Map<String, List<String>> entities = new HashMap<>();
        entities.put("author", new ArrayList<>());
        entities.put("title", new ArrayList<>());
        entities.put("category", new ArrayList<>());
        
        // Extract potential author names (capitalized words)
        Pattern authorPattern = Pattern.compile("\\b[A-Z][a-z]+ [A-Z][a-z]+\\b");
        java.util.regex.Matcher authorMatcher = authorPattern.matcher(query);
        while (authorMatcher.find()) {
            entities.get("author").add(authorMatcher.group().toLowerCase());
        }
        
        // Extract potential book titles (phrases in quotes)
        Pattern titlePattern = Pattern.compile("\"([^\"]*)\"");
        java.util.regex.Matcher titleMatcher = titlePattern.matcher(query);
        while (titleMatcher.find()) {
            entities.get("title").add(titleMatcher.group(1).toLowerCase());
        }
        
        // Extract potential categories (common category keywords)
        String[] commonCategories = {"fiction", "non-fiction", "science", "history", "biography", 
                                    "fantasy", "romance", "thriller", "mystery", "horror", 
                                    "poetry", "drama", "comedy", "adventure", "children"};
        
        String lowercaseQuery = query.toLowerCase();
        for (String category : commonCategories) {
            if (lowercaseQuery.contains(category)) {
                entities.get("category").add(category);
            }
        }
        
        return entities;
    }

    /**
     * Calculate the relevance score of a book for a query
     *
     * @param book The book to score
     * @param queryTokens The preprocessed query tokens
     * @param entities The extracted entities from the query
     * @return A relevance score
     */
    private double calculateRelevanceScore(Book book, List<String> queryTokens, Map<String, List<String>> entities) {
        double score = 0.0;
        
        // Preprocess book fields
        List<String> titleTokens = book.getTitle() != null ? preprocessText(book.getTitle()) : Collections.emptyList();
        List<String> authorTokens = book.getAuthor() != null ? preprocessText(book.getAuthor()) : Collections.emptyList();
        List<String> descriptionTokens = book.getDescription() != null ? preprocessText(book.getDescription()) : Collections.emptyList();
        List<String> categoryTokens = book.getCategory() != null ? preprocessText(book.getCategory()) : Collections.emptyList();
        
        // Score based on token matches
        for (String token : queryTokens) {
            // Title matches (highest weight)
            if (titleTokens.contains(token)) {
                score += 5.0;
            }
            
            // Author matches
            if (authorTokens.contains(token)) {
                score += 4.0;
            }
            
            // Category matches
            if (categoryTokens.contains(token)) {
                score += 3.0;
            }
            
            // Description matches
            if (descriptionTokens.contains(token)) {
                score += 1.0;
            }
        }
        
        // Score based on entity matches
        
        // Author entity matches
        for (String author : entities.get("author")) {
            if (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(author)) {
                score += 10.0;
            }
        }
        
        // Title entity matches
        for (String title : entities.get("title")) {
            if (book.getTitle() != null && book.getTitle().toLowerCase().contains(title)) {
                score += 10.0;
            }
        }
        
        // Category entity matches
        for (String category : entities.get("category")) {
            if (book.getCategory() != null && book.getCategory().toLowerCase().contains(category)) {
                score += 8.0;
            }
        }
        
        return score;
    }

    /**
     * Get search suggestions based on a partial query
     *
     * @param partialQuery The partial query
     * @param limit The maximum number of suggestions to return
     * @return A list of search suggestions
     */
    public List<String> getSearchSuggestions(String partialQuery, int limit) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String lowercaseQuery = partialQuery.toLowerCase().trim();
        List<String> suggestions = new ArrayList<>();
        
        // Get all books
        List<Book> allBooks = bookRepository.findAll();
        
        // Add title suggestions
        for (Book book : allBooks) {
            if (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowercaseQuery)) {
                suggestions.add("\"" + book.getTitle() + "\"");
            }
        }
        
        // Add author suggestions
        for (Book book : allBooks) {
            if (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lowercaseQuery)) {
                suggestions.add("by " + book.getAuthor());
            }
        }
        
        // Add category suggestions
        for (Book book : allBooks) {
            if (book.getCategory() != null && book.getCategory().toLowerCase().contains(lowercaseQuery)) {
                suggestions.add("category: " + book.getCategory());
            }
        }
        
        // Remove duplicates and limit results
        return suggestions.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
}
