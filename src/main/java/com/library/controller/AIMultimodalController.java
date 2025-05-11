package com.library.controller;

import com.library.security.services.UserDetailsImpl;
import com.library.service.GeminiMultimodalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai/multimodal")
public class AIMultimodalController {

    @Autowired
    private GeminiMultimodalService geminiMultimodalService;

    /**
     * Analyze a book cover image and extract information
     *
     * @param coverImage The book cover image
     * @return Extracted information about the book
     */
    @PostMapping(value = "/analyze-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> analyzeBookCover(
            @RequestParam("image") MultipartFile coverImage) {
        
        Map<String, String> result = geminiMultimodalService.analyzeBookCover(coverImage);
        return ResponseEntity.ok(result);
    }

    /**
     * Generate personalized book recommendations based on a bookshelf image
     *
     * @param authentication The authenticated user
     * @param shelfImage Image of the user's bookshelf
     * @return Personalized book recommendations
     */
    @PostMapping(value = "/shelf-recommendations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getShelfRecommendations(
            Authentication authentication,
            @RequestParam("image") MultipartFile shelfImage) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String recommendations = geminiMultimodalService.generatePersonalizedRecommendations(
                userDetails.getId(), shelfImage);
        
        Map<String, String> response = new HashMap<>();
        response.put("recommendations", recommendations);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Identify a book from its cover image
     *
     * @param coverImage The book cover image
     * @return Information about the identified book
     */
    @PostMapping(value = "/identify-book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> identifyBook(
            @RequestParam("image") MultipartFile coverImage) {
        
        Map<String, Object> result = geminiMultimodalService.identifyBookFromCover(coverImage);
        return ResponseEntity.ok(result);
    }

    /**
     * General image and text analysis with Gemini
     *
     * @param prompt The text prompt
     * @param image The image file
     * @return AI-generated response
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> analyzeImageAndText(
            @RequestParam("prompt") String prompt,
            @RequestParam("image") MultipartFile image) {
        
        String response = geminiMultimodalService.generateMultimodalResponse(prompt, image);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        
        return ResponseEntity.ok(result);
    }
}
