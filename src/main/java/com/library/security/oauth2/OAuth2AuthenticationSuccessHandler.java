package com.library.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.User;
import com.library.repository.UserRepository;
import com.library.security.jwt.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.redirectUri}")
    private String redirectUri;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Check if the user is blocked
        if (!userPrincipal.isEnabled()) {
            return UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "blocked")
                    .build().toUriString();
        }

        String token = jwtUtils.generateJwtToken(authentication);

        // Get user roles
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Get user details
        Optional<User> userOptional = userRepository.findByEmail(userPrincipal.getEmail());
        Long userId = userOptional.map(User::getId).orElse(null);
        String username = userOptional.map(User::getUsername).orElse(userPrincipal.getEmail());

        // Build the URL with all user information
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("id", userId)
                .queryParam("username", username)
                .queryParam("email", userPrincipal.getEmail());

        // Add roles as a comma-separated string
        if (!roles.isEmpty()) {
            String rolesStr = String.join(",", roles);
            uriBuilder.queryParam("roles", rolesStr);
        }

        return uriBuilder.build().toUriString();
    }
}
