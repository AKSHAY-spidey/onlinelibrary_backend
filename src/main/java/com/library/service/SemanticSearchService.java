package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.library.config.GeminiConfig;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemanticSearchService {

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private RestTemplate geminiRestTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Value("${gemini.embedding.url}")
    private String embeddingApiUrl;

    @Value("${gemini.embedding.dimension:768}")
    private int embeddingDimension;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Cache for book embeddings to avoid recalculating them
    private final Map<Long, float[]> bookEmbeddingCache = new HashMap<>();

    /**
     * Generate embeddings for a text using Gemini's embedding model
     *
     * @param text The text to generate embeddings for
     * @return A float array of embeddings
     */
    public float[] generateEmbeddings(String text) {
        try {
            // Prepare request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("text", text);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            // Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            // Make API call
            String response = geminiRestTemplate.postForObject(embeddingApiUrl, entity, String.class);

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode embedding = responseJson.path("embedding").path("values");

            if (embedding.isArray()) {
                float[] embeddingValues = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    embeddingValues[i] = (float) embedding.get(i).asDouble();
                }
                return embeddingValues;
            }

            return new float[embeddingDimension]; // Return empty embedding if failed

        } catch (Exception e) {
            e.printStackTrace();
            return new float[embeddingDimension]; // Return empty embedding if failed
        }
    }

    /**
     * Calculate the cosine similarity between two embedding vectors
     *
     * @param embedding1 The first embedding vector
     * @param embedding2 The second embedding vector
     * @return The cosine similarity (between -1 and 1)
     */
    private double cosineSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }

        if (norm1 <= 0.0 || norm2 <= 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Generate or retrieve embeddings for a book
     *
     * @param book The book to generate embeddings for
     * @return The book's embedding vector
     */
    private float[] getBookEmbedding(Book book) {
        // Check if we have cached embeddings for this book
        if (bookEmbeddingCache.containsKey(book.getId())) {
            return bookEmbeddingCache.get(book.getId());
        }

        // Create a rich text representation of the book
        StringBuilder bookText = new StringBuilder();
        bookText.append("Title: ").append(book.getTitle()).append(". ");
        bookText.append("Author: ").append(book.getAuthor()).append(". ");

        if (book.getCategory() != null) {
            bookText.append("Category: ").append(book.getCategory()).append(". ");
        }

        if (book.getDescription() != null) {
            bookText.append("Description: ").append(book.getDescription()).append(". ");
        }

        if (book.getPublisher() != null) {
            bookText.append("Publisher: ").append(book.getPublisher()).append(". ");
        }

        if (book.getLanguage() != null) {
            bookText.append("Language: ").append(book.getLanguage()).append(". ");
        }

        // Generate embeddings
        float[] embedding = generateEmbeddings(bookText.toString());

        // Cache the embeddings
        bookEmbeddingCache.put(book.getId(), embedding);

        return embedding;
    }

    /**
     * Perform semantic search for books based on a natural language query
     *
     * @param query The natural language query
     * @param limit The maximum number of results to return
     * @return A list of books ranked by semantic similarity to the query
     */
    public List<Book> semanticSearch(String query, int limit) {
        // Generate embeddings for the query
        float[] queryEmbedding = generateEmbeddings(query);

        // Get all books
        List<Book> allBooks = bookRepository.findAll();

        // Calculate similarity scores
        List<Map.Entry<Book, Double>> scoredBooks = new ArrayList<>();

        for (Book book : allBooks) {
            float[] bookEmbedding = getBookEmbedding(book);
            double similarity = cosineSimilarity(queryEmbedding, bookEmbedding);
            scoredBooks.add(new AbstractMap.SimpleEntry<>(book, similarity));
        }

        // Sort by similarity score (descending)
        scoredBooks.sort(Map.Entry.<Book, Double>comparingByValue().reversed());

        // Return top results
        return scoredBooks.stream()
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Find books similar to a given book using semantic similarity
     *
     * @param bookId The ID of the reference book
     * @param limit The maximum number of similar books to return
     * @return A list of books similar to the reference book
     */
    public List<Book> findSimilarBooks(Long bookId, int limit) {
        // Get the reference book
        Book referenceBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Get embeddings for the reference book
        float[] referenceEmbedding = getBookEmbedding(referenceBook);

        // Get all books except the reference book
        List<Book> otherBooks = bookRepository.findAll().stream()
                .filter(book -> !book.getId().equals(bookId))
                .collect(Collectors.toList());

        // Calculate similarity scores
        List<Map.Entry<Book, Double>> scoredBooks = new ArrayList<>();

        for (Book book : otherBooks) {
            float[] bookEmbedding = getBookEmbedding(book);
            double similarity = cosineSimilarity(referenceEmbedding, bookEmbedding);
            scoredBooks.add(new AbstractMap.SimpleEntry<>(book, similarity));
        }

        // Sort by similarity score (descending)
        scoredBooks.sort(Map.Entry.<Book, Double>comparingByValue().reversed());

        // Return top results
        return scoredBooks.stream()
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Clear the embedding cache for a specific book
     * (useful when a book is updated)
     *
     * @param bookId The ID of the book to clear from cache
     */
    public void clearBookEmbeddingCache(Long bookId) {
        bookEmbeddingCache.remove(bookId);
    }

    /**
     * Clear the entire embedding cache
     */
    public void clearAllEmbeddingCache() {
        bookEmbeddingCache.clear();
    }
}
