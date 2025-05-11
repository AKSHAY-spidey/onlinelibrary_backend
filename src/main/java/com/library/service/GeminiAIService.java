package com.library.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.library.config.GeminiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAIService {

    @Autowired
    private GeminiConfig geminiConfig;

    @Autowired
    private RestTemplate geminiRestTemplate;

    @Value("${gemini.max-tokens:2048}")
    private int maxTokens;

    @Value("${gemini.temperature:0.7}")
    private double temperature;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate a response from Gemini AI based on the provided prompt
     *
     * @param prompt The user's prompt
     * @return The AI-generated response
     */
    public String generateResponse(String prompt) {
        // Log the prompt for debugging
        System.out.println("Sending prompt to Gemini: " + prompt.substring(0, Math.min(100, prompt.length())) + "...");
        try {
            // Prepare request body
            ObjectNode requestBody = objectMapper.createObjectNode();

            // Add contents array with user message
            ArrayNode contentsArray = requestBody.putArray("contents");
            ObjectNode content = contentsArray.addObject();

            // Add parts array with user text
            ArrayNode partsArray = content.putArray("parts");
            ObjectNode part = partsArray.addObject();
            part.put("text", prompt);

            // Add generation config
            ObjectNode generationConfig = requestBody.putObject("generationConfig");
            generationConfig.put("temperature", temperature);
            generationConfig.put("maxOutputTokens", maxTokens);
            generationConfig.put("topP", 0.95);
            generationConfig.put("topK", 40);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            // Create HTTP entity
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            // Make API call with retry logic
            String response = null;
            int maxRetries = 3;
            int retryCount = 0;
            Exception lastException = null;

            while (retryCount < maxRetries) {
                try {
                    response = geminiRestTemplate.postForObject(geminiConfig.getApiUrl(), entity, String.class);
                    break; // Success, exit the loop
                } catch (Exception e) {
                    lastException = e;
                    retryCount++;
                    System.err.println("Attempt " + retryCount + " failed: " + e.getMessage());

                    if (retryCount < maxRetries) {
                        try {
                            // Exponential backoff
                            Thread.sleep(1000 * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

            if (response == null) {
                // If API call fails, provide a fallback response based on the prompt
                return generateFallbackResponse(prompt);
            }

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

            return generateFallbackResponse(prompt);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "An error occurred while communicating with the AI service: " + e.getMessage();
            System.err.println(errorMessage);
            return generateFallbackResponse(prompt);
        }
    }

    /**
     * Generate a fallback response when the AI service is unavailable
     *
     * @param prompt The original prompt
     * @return A contextually appropriate fallback response
     */
    private String generateFallbackResponse(String prompt) {
        String promptLower = prompt.toLowerCase();

        // Check for library system related queries
        if (promptLower.contains("loan") || promptLower.contains("borrow") || promptLower.contains("return")) {
            return "Our library allows you to borrow books for up to 3 weeks. You can extend your loan period twice if no one else has reserved the book. To return a book, simply bring it back to the library or use the book drop box available 24/7 at the entrance.";
        }

        // Check for book recommendations
        if (promptLower.contains("recommend") || promptLower.contains("suggest") || promptLower.contains("what should i read")) {
            return "Here are some popular books in our library:\n\n1. 'The Midnight Library' by Matt Haig - A thought-provoking novel about the choices we make.\n2. 'Project Hail Mary' by Andy Weir - An exciting sci-fi adventure with a unique protagonist.\n3. 'Educated' by Tara Westover - A powerful memoir about self-invention.\n4. 'The Lincoln Highway' by Amor Towles - A captivating historical fiction journey.\n5. 'Klara and the Sun' by Kazuo Ishiguro - A moving exploration of what it means to be human.";
        }

        // Check for library hours/policies
        if (promptLower.contains("hour") || promptLower.contains("open") || promptLower.contains("close") || promptLower.contains("policy")) {
            return "Our library is open Monday through Friday from 9:00 AM to 8:00 PM, Saturday from 10:00 AM to 6:00 PM, and Sunday from 12:00 PM to 5:00 PM. We have a variety of services including computer access, printing, study rooms, and children's programs. For more specific information, please visit our website or speak with a librarian.";
        }

        // Check for account-related queries
        if (promptLower.contains("account") || promptLower.contains("login") || promptLower.contains("password") || promptLower.contains("sign up")) {
            return "You can manage your library account online through our website. This includes updating your personal information, changing your password, viewing your current loans, and placing holds on books. If you're having trouble accessing your account, please contact our support team for assistance.";
        }

        // Check for book search queries
        if (promptLower.contains("find") || promptLower.contains("search") || promptLower.contains("looking for")) {
            return "You can search for books in our catalog using the search bar at the top of the page. You can search by title, author, ISBN, or keywords. Advanced search options allow you to filter by genre, publication date, availability, and more. If you need help finding a specific book, our librarians are always happy to assist you.";
        }

        // Default response for greetings
        if (promptLower.contains("hello") || promptLower.contains("hi") || promptLower.contains("hey")) {
            return "Hello! I'm your library assistant. I can help you find books, answer questions about our services, assist with your account, and provide reading recommendations. How can I help you today?";
        }

        // Default response
        return "I'm here to help you with our library system. I can provide information about borrowing books, returning items, finding resources, and using our various services. Please let me know what specific information you're looking for, and I'll do my best to assist you.";
    }

    /**
     * Generate book recommendations based on user preferences and reading history
     *
     * @param userPreferences Map of user preferences (genres, authors, etc.)
     * @param readingHistory List of books the user has read
     * @return AI-generated book recommendations
     */
    public String generateBookRecommendations(Map<String, Object> userPreferences, List<String> readingHistory) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("As a library AI assistant, please recommend books based on the following user preferences and reading history:\n\n");

        // Add user preferences
        promptBuilder.append("User Preferences:\n");
        for (Map.Entry<String, Object> entry : userPreferences.entrySet()) {
            promptBuilder.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        // Add reading history
        promptBuilder.append("\nReading History:\n");
        for (String book : readingHistory) {
            promptBuilder.append("- ").append(book).append("\n");
        }

        promptBuilder.append("\nPlease recommend 5 books with title, author, and a brief reason why the user might enjoy each book. Format as a numbered list.");

        return generateResponse(promptBuilder.toString());
    }

    /**
     * Generate a summary of a book's content
     *
     * @param bookTitle The title of the book
     * @param bookAuthor The author of the book
     * @param bookContent A brief description or excerpt from the book
     * @return AI-generated summary of the book
     */
    public String generateBookSummary(String bookTitle, String bookAuthor, String bookContent) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Please provide a concise summary of the following book:\n\n");
        promptBuilder.append("Title: ").append(bookTitle).append("\n");
        promptBuilder.append("Author: ").append(bookAuthor).append("\n");
        promptBuilder.append("Content: ").append(bookContent).append("\n\n");
        promptBuilder.append("Please include the main themes, key characters, and plot points without spoilers. Keep the summary under 250 words.");

        return generateResponse(promptBuilder.toString());
    }

    /**
     * Analyze the reading level of a text
     *
     * @param text The text to analyze
     * @return AI-generated analysis of the reading level
     */
    public String analyzeReadingLevel(String text) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Please analyze the reading level of the following text and provide a detailed assessment:\n\n");
        promptBuilder.append(text).append("\n\n");
        promptBuilder.append("Include the following in your analysis:\n");
        promptBuilder.append("1. Approximate grade level (e.g., elementary, middle school, high school, college)\n");
        promptBuilder.append("2. Vocabulary complexity\n");
        promptBuilder.append("3. Sentence structure complexity\n");
        promptBuilder.append("4. Overall readability score\n");
        promptBuilder.append("5. Suggestions for readers who might enjoy this level of reading");

        return generateResponse(promptBuilder.toString());
    }

    /**
     * Generate a response to a user's question about a book or library services
     *
     * @param userQuestion The user's question
     * @param contextInfo Additional context information
     * @return AI-generated answer to the question
     */
    public String answerLibraryQuestion(String userQuestion, String contextInfo) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("As a knowledgeable library assistant, please answer the following question:\n\n");
        promptBuilder.append("Question: ").append(userQuestion).append("\n\n");

        if (contextInfo != null && !contextInfo.isEmpty()) {
            promptBuilder.append("Additional Context: ").append(contextInfo).append("\n\n");
        }

        promptBuilder.append("Provide a helpful, accurate, and concise response. If you don't know the answer, suggest where the user might find more information.");

        return generateResponse(promptBuilder.toString());
    }

    /**
     * Enhance a prompt with role-specific context and project knowledge
     *
     * @param userMessage The original user message
     * @param username The username of the user
     * @param roles The roles of the user
     * @return An enhanced prompt with role-specific context and project knowledge
     */
    public String enhancePromptWithRoleContext(String userMessage, String username, List<String> roles) {
        StringBuilder enhancedPrompt = new StringBuilder();
        enhancedPrompt.append("User query: ").append(userMessage).append("\n\n");
        enhancedPrompt.append("You are a helpful library assistant named 'LibraryGenius'. ");
        enhancedPrompt.append("You help users with finding books, answering questions about library services, ");
        enhancedPrompt.append("and providing information about literature and the Online Library Management System project. ");
        enhancedPrompt.append("Keep responses friendly, helpful, and concise. ");
        enhancedPrompt.append("If you don't know something, admit it and suggest alternatives. ");
        enhancedPrompt.append("The user's name is ").append(username).append(".\n\n");

        // Add library-specific context
        enhancedPrompt.append("Library context: This is an online library management system. ");
        enhancedPrompt.append("Users can borrow books, return them, and browse the catalog. ");
        enhancedPrompt.append("The library has various genres including fiction, non-fiction, science, history, etc. ");
        enhancedPrompt.append("The system includes user authentication, book management, loan processing, subscription management, and AI features. ");

        // Add project-specific context
        enhancedPrompt.append("\n\nProject Context: This Online Library Management System is built using: ");
        enhancedPrompt.append("\n- Backend: Spring Boot, Maven, and MySQL with microservices architecture");
        enhancedPrompt.append("\n- Frontend: React with Vite, Tailwind CSS, and React Spring for animations");
        enhancedPrompt.append("\n- Authentication: JWT-based with OAuth2 support for Google and GitHub");
        enhancedPrompt.append("\n- Database: MySQL with tables for books, loans, payments, subscriptions, users, and roles");
        enhancedPrompt.append("\n- Email: Brevo API for sending emails with templates");
        enhancedPrompt.append("\n- Payments: Razorpay integration for subscription payments");
        enhancedPrompt.append("\n- AI Features: Gemini API for chatbot, book recommendations, and semantic search");
        enhancedPrompt.append("\n- Subscription Plans: BASIC (₹199), STANDARD (₹299), and PREMIUM (₹399)");
        enhancedPrompt.append("\n- WebSockets: Using STOMP for real-time notifications");

        // Add role-specific context
        if (roles.contains("ROLE_ADMIN")) {
            enhancedPrompt.append("\n\nUser Role Context: This user is an ADMIN. ");
            enhancedPrompt.append("Admins can manage users, books, and subscriptions. ");
            enhancedPrompt.append("They can add/remove librarians, manage payment methods, and view system analytics. ");
            enhancedPrompt.append("When answering questions, provide admin-specific guidance when relevant. ");
            enhancedPrompt.append("For example, if they ask about managing users, explain the admin panel features. ");
            enhancedPrompt.append("If they ask about general topics not related to admin duties, answer normally.");
        } else if (roles.contains("ROLE_LIBRARIAN")) {
            enhancedPrompt.append("\n\nUser Role Context: This user is a LIBRARIAN. ");
            enhancedPrompt.append("Librarians can manage books, process loans, and help users. ");
            enhancedPrompt.append("They can add/update/remove books, approve/reject loan requests, and manage returns. ");
            enhancedPrompt.append("When answering questions, provide librarian-specific guidance when relevant. ");
            enhancedPrompt.append("For example, if they ask about managing books, explain the librarian features. ");
            enhancedPrompt.append("If they ask about general topics not related to librarian duties, answer normally.");
        } else if (roles.contains("ROLE_USER")) {
            enhancedPrompt.append("\n\nUser Role Context: This user is a regular LIBRARY USER. ");
            enhancedPrompt.append("Users can browse books, borrow books, return books, and manage their account. ");
            enhancedPrompt.append("They can also subscribe to premium plans for additional benefits. ");
            enhancedPrompt.append("When answering questions, focus on helping them use the library services effectively. ");
            enhancedPrompt.append("For example, if they ask about finding books, explain the search and filter features. ");
            enhancedPrompt.append("If they ask about general topics, answer normally.");
        }

        // Add project-specific questions handling
        enhancedPrompt.append("\n\nProject Questions: If the user asks about the Online Library Management System project, ");
        enhancedPrompt.append("provide detailed and accurate information about the project architecture, technologies, features, and implementation details. ");
        enhancedPrompt.append("For example, if they ask about how OAuth is implemented, explain the Spring Security OAuth2 configuration and the CustomOAuth2UserService. ");
        enhancedPrompt.append("If they ask about the subscription system, explain the subscription plans, payment processing with Razorpay, and subscription management features. ");
        enhancedPrompt.append("If they ask about the AI features, explain the Gemini API integration for chatbot, book recommendations, and semantic search. ");
        enhancedPrompt.append("Always provide technically accurate and helpful information about the project.");

        // Add instruction for general knowledge questions
        enhancedPrompt.append("\n\nGeneral Knowledge: If the user asks general knowledge questions not related to the library system or project, ");
        enhancedPrompt.append("answer them accurately and helpfully like a knowledgeable assistant would. ");
        enhancedPrompt.append("For example, if they ask about historical events, scientific concepts, or other general topics, ");
        enhancedPrompt.append("provide informative and educational responses. ");
        enhancedPrompt.append("Always maintain a helpful, friendly tone regardless of the question type.");

        return enhancedPrompt.toString();
    }

    /**
     * Generate personalized reading insights based on a user's reading history
     *
     * @param readingHistory List of books the user has read
     * @return AI-generated insights about reading patterns and preferences
     */
    public String generateReadingInsights(List<String> readingHistory) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Based on the following reading history, please provide insights about this reader's preferences, patterns, and potential interests:\n\n");

        promptBuilder.append("Reading History:\n");
        for (String book : readingHistory) {
            promptBuilder.append("- ").append(book).append("\n");
        }

        promptBuilder.append("\nPlease include:\n");
        promptBuilder.append("1. Preferred genres and themes\n");
        promptBuilder.append("2. Favorite authors or writing styles\n");
        promptBuilder.append("3. Reading patterns or trends\n");
        promptBuilder.append("4. Potential new interests to explore\n");
        promptBuilder.append("5. Personalized reading suggestions");

        return generateResponse(promptBuilder.toString());
    }

    /**
     * Answer a project-specific question about the Online Library Management System
     *
     * @param question The user's question about the project
     * @return AI-generated answer with project-specific details
     */
    public String answerProjectQuestion(String question) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("As a knowledgeable assistant familiar with the Online Library Management System project, please answer the following question:\n\n");
        promptBuilder.append("Question: ").append(question).append("\n\n");

        // Add project context
        promptBuilder.append("Project Context: This Online Library Management System is built using:\n");
        promptBuilder.append("- Backend: Spring Boot, Maven, and MySQL with microservices architecture\n");
        promptBuilder.append("- Frontend: React with Vite, Tailwind CSS, and React Spring for animations\n");
        promptBuilder.append("- Authentication: JWT-based with OAuth2 support for Google and GitHub\n");
        promptBuilder.append("- Database: MySQL with tables for books, loans, payments, subscriptions, users, and roles\n");
        promptBuilder.append("- Email: Brevo API for sending emails with templates\n");
        promptBuilder.append("- Payments: Razorpay integration for subscription payments\n");
        promptBuilder.append("- AI Features: Gemini API for chatbot, book recommendations, and semantic search\n");
        promptBuilder.append("- Subscription Plans: BASIC (₹199), STANDARD (₹299), and PREMIUM (₹399)\n");
        promptBuilder.append("- WebSockets: Using STOMP for real-time notifications\n\n");

        // Add implementation details
        promptBuilder.append("Implementation Details:\n");
        promptBuilder.append("1. Authentication: Uses Spring Security with JWT tokens. OAuth2 authentication is handled by CustomOAuth2UserService.\n");
        promptBuilder.append("2. Book Management: CRUD operations for books with image upload support.\n");
        promptBuilder.append("3. Loan Processing: Users can request loans, librarians can approve/reject them.\n");
        promptBuilder.append("4. Subscription Management: Users can subscribe to different plans with Razorpay payment integration.\n");
        promptBuilder.append("5. Email Notifications: Uses Brevo API to send welcome emails, subscription emails, etc.\n");
        promptBuilder.append("6. Real-time Notifications: Uses WebSockets with STOMP for real-time updates.\n");
        promptBuilder.append("7. AI Features: Uses Gemini API for chatbot, book recommendations, and semantic search.\n\n");

        promptBuilder.append("Provide a detailed, technically accurate, and helpful response that addresses the specific question about the project.");

        return generateResponse(promptBuilder.toString());
    }
}
