package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.library.config.GeminiConfig;
import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiMultimodalService {

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private RestTemplate geminiRestTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${gemini.multimodal.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent}")
    private String multimodalApiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate a response from Gemini AI based on text and image inputs
     *
     * @param prompt The text prompt
     * @param imageFile The image file
     * @return The AI-generated response
     */
    public String generateMultimodalResponse(String prompt, MultipartFile imageFile) {
        try {
            // Prepare request body
            ObjectNode requestBody = objectMapper.createObjectNode();

            // Add contents array with user message
            ArrayNode contentsArray = requestBody.putArray("contents");
            ObjectNode content = contentsArray.addObject();

            // Add parts array with text and image
            ArrayNode partsArray = content.putArray("parts");

            // Add text part
            ObjectNode textPart = partsArray.addObject();
            textPart.put("text", prompt);

            // Add image part if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                ObjectNode imagePart = partsArray.addObject();
                ObjectNode inlineData = imagePart.putObject("inlineData");

                // Convert image to base64
                String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
                inlineData.put("data", base64Image);
                inlineData.put("mimeType", imageFile.getContentType());
            }

            // Add generation config
            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("temperature", 0.4);
            generationConfig.put("maxOutputTokens", 2048);
            generationConfig.put("topP", 0.95);
            generationConfig.put("topK", 40);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            // Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            // Make API call
            String response = geminiRestTemplate.postForObject(multimodalApiUrl, entity, String.class);

            // Parse response
            JsonNode responseJson = objectMapper.readTree(response);
            JsonNode candidates = responseJson.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content1 = candidates.get(0).path("content");
                JsonNode parts = content1.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            return "Sorry, I couldn't generate a response for this image.";

        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while processing the image: " + e.getMessage();
        }
    }

    /**
     * Analyze a book cover image and extract information
     *
     * @param imageFile The book cover image
     * @return Extracted information about the book
     */
    public Map<String, String> analyzeBookCover(MultipartFile imageFile) {
        String prompt = "You are a book cover analyzer. Extract the following information from this book cover image:\n" +
                "1. Book title\n" +
                "2. Author name\n" +
                "3. Publisher (if visible)\n" +
                "4. Genre/category based on the cover design\n" +
                "5. Brief description of the cover art\n\n" +
                "Format your response as a JSON object with these fields. If you can't determine a field, use null for its value.";

        String response = generateMultimodalResponse(prompt, imageFile);

        // Parse the JSON response
        Map<String, String> result = new HashMap<>();
        try {
            // Check if response is already in JSON format
            if (response.trim().startsWith("{") && response.trim().endsWith("}")) {
                JsonNode jsonNode = objectMapper.readTree(response);
                result.put("title", jsonNode.path("title").asText(null));
                result.put("author", jsonNode.path("author").asText(null));
                result.put("publisher", jsonNode.path("publisher").asText(null));
                result.put("genre", jsonNode.path("genre").asText(null));
                result.put("coverDescription", jsonNode.path("coverDescription").asText(null));
            } else {
                // Try to extract information from text
                result.put("rawResponse", response);

                // Extract title
                if (response.contains("Title:")) {
                    String[] parts = response.split("Title:");
                    if (parts.length > 1) {
                        String title = parts[1].split("\\n")[0].trim();
                        result.put("title", title);
                    }
                }

                // Extract author
                if (response.contains("Author:")) {
                    String[] parts = response.split("Author:");
                    if (parts.length > 1) {
                        String author = parts[1].split("\\n")[0].trim();
                        result.put("author", author);
                    }
                }

                // Extract genre
                if (response.contains("Genre:")) {
                    String[] parts = response.split("Genre:");
                    if (parts.length > 1) {
                        String genre = parts[1].split("\\n")[0].trim();
                        result.put("genre", genre);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", "Failed to parse response: " + e.getMessage());
        }

        return result;
    }

    /**
     * Generate a personalized book recommendation based on a user's profile and an image of their bookshelf
     *
     * @param userId The ID of the user
     * @param shelfImage Image of the user's bookshelf
     * @return Personalized book recommendations
     */
    public String generatePersonalizedRecommendations(Long userId, MultipartFile shelfImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are a personalized book recommendation system. ");
        promptBuilder.append("This is an image of the user's bookshelf. ");
        promptBuilder.append("The user's name is ").append(user.getUsername()).append(". ");

        if (user.getFirstName() != null && user.getLastName() != null) {
            promptBuilder.append("Their full name is ").append(user.getFirstName())
                    .append(" ").append(user.getLastName()).append(". ");
        }

        promptBuilder.append("\nBased on the books visible in this image and considering modern literary trends, ");
        promptBuilder.append("recommend 5 books that this person might enjoy. ");
        promptBuilder.append("For each recommendation, provide:\n");
        promptBuilder.append("1. Title and author\n");
        promptBuilder.append("2. A brief description\n");
        promptBuilder.append("3. Why you think they would enjoy it based on what you see in their collection\n");
        promptBuilder.append("\nBe specific about which visible books informed your recommendations.");

        return generateMultimodalResponse(promptBuilder.toString(), shelfImage);
    }

    /**
     * Identify a book from its cover image
     *
     * @param coverImage The book cover image
     * @return Information about the identified book
     */
    public Map<String, Object> identifyBookFromCover(MultipartFile coverImage) {
        String prompt = "You are a book identification system. Look at this book cover image and identify the book. " +
                "Provide the title, author, and if possible, the ISBN. " +
                "If you recognize this as a specific edition, mention that as well. " +
                "Format your response as a JSON object with fields: title, author, isbn (if visible), edition (if identifiable).";

        String response = generateMultimodalResponse(prompt, coverImage);
        Map<String, Object> result = new HashMap<>();

        try {
            // Try to parse as JSON
            if (response.trim().startsWith("{") && response.trim().endsWith("}")) {
                JsonNode jsonNode = objectMapper.readTree(response);

                String title = jsonNode.path("title").asText(null);
                String author = jsonNode.path("author").asText(null);

                result.put("title", title);
                result.put("author", author);
                result.put("isbn", jsonNode.path("isbn").asText(null));
                result.put("edition", jsonNode.path("edition").asText(null));

                // Try to find the book in our database
                if (title != null && author != null) {
                    Book matchedBook = bookRepository.findByTitleAndAuthor(title, author);
                    if (matchedBook != null) {
                        result.put("bookId", matchedBook.getId());
                        result.put("inDatabase", true);
                        result.put("availableCopies", matchedBook.getAvailableCopies());
                    } else {
                        result.put("inDatabase", false);
                    }
                }
            } else {
                result.put("rawResponse", response);
                result.put("inDatabase", false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", "Failed to parse response: " + e.getMessage());
        }

        return result;
    }
}
