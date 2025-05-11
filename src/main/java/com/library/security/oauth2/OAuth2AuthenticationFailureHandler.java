package com.library.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // Get a simplified error message without newlines or special characters
        String errorMessage = "Authentication failed";
        if (exception.getMessage() != null) {
            // Clean up the error message to avoid issues with redirects
            errorMessage = exception.getMessage()
                    .replaceAll("[\r\n]", " ")
                    .replaceAll("\s+", " ")
                    .trim();

            // Limit the length to avoid issues with very long error messages
            if (errorMessage.length() > 100) {
                errorMessage = errorMessage.substring(0, 100) + "...";
            }
        }

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", errorMessage)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
