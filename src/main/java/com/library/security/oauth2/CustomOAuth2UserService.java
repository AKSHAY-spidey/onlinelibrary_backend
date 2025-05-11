package com.library.security.oauth2;

import com.library.exception.OAuth2AuthenticationProcessingException;
import com.library.model.AuthProvider;
import com.library.model.Role;
import com.library.model.ERole;
import com.library.model.User;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.service.BrevoEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BrevoEmailService emailService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // Handle GitHub's email retrieval
        if(!StringUtils.hasText(oAuth2UserInfo.getEmail()) && "github".equals(registrationId.toLowerCase())) {
            String email = getGitHubEmail(oAuth2UserRequest);
            if (StringUtils.hasText(email)) {
                // Update the OAuth2UserInfo with the retrieved email
                ((GithubOAuth2UserInfo) oAuth2UserInfo).setEmail(email);
            } else {
                throw new OAuth2AuthenticationProcessingException("Email not found from GitHub. Please make sure your email is public or grant email access.");
            }
        } else if(!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        // Use the repository method that eagerly fetches roles
        Optional<User> userOptional = userRepository.findByEmailWithRoles(oAuth2UserInfo.getEmail());
        User user;

        if(userOptional.isPresent()) {
            user = userOptional.get();

            if(!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
                throw new OAuth2AuthenticationProcessingException("You're signed up with " +
                    user.getProvider() + ". Please use your " + user.getProvider() + " account to login.");
            }

            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        // Get the provider (GOOGLE or GITHUB)
        AuthProvider provider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
        user.setProvider(provider);
        user.setProviderId(oAuth2UserInfo.getId());

        // Create a username from the email (e.g., john.doe@example.com -> john.doe)
        String email = oAuth2UserInfo.getEmail();
        String username = email;
        if (email.contains("@")) {
            username = email.substring(0, email.indexOf("@"));
        }

        // Check if username already exists, if so, use email as username
        if (userRepository.existsByUsername(username)) {
            username = email;
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(oAuth2UserInfo.getName());
        user.setActive(true); // Set active status for new users

        // Set default USER role
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);

        // Save the user
        User savedUser = userRepository.save(user);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(email, username);
            System.out.println("Welcome email sent to " + email + " for " + provider + " OAuth registration");
        } catch (Exception e) {
            // Log the error but don't throw it to allow the authentication to continue
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return savedUser;
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        // Update user information if needed
        if (oAuth2UserInfo.getName() != null && !oAuth2UserInfo.getName().isEmpty() && existingUser.getFirstName() == null) {
            existingUser.setFirstName(oAuth2UserInfo.getName());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Retrieves the primary email address from GitHub API
     * GitHub doesn't include email in the user info response if it's private
     * We need to make a separate API call to get the user's email
     */
    private String getGitHubEmail(OAuth2UserRequest oAuth2UserRequest) {
        try {
            // Set up headers with the access token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + oAuth2UserRequest.getAccessToken().getTokenValue());

            // Make request to GitHub API to get user's emails
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> emails = response.getBody();
            if (emails != null && !emails.isEmpty()) {
                // Find the primary email
                for (Map<String, Object> email : emails) {
                    Boolean primary = (Boolean) email.get("primary");
                    Boolean verified = (Boolean) email.get("verified");
                    if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                        return (String) email.get("email");
                    }
                }

                // If no primary email found, return the first verified email
                for (Map<String, Object> email : emails) {
                    Boolean verified = (Boolean) email.get("verified");
                    if (Boolean.TRUE.equals(verified)) {
                        return (String) email.get("email");
                    }
                }

                // If no verified email found, return the first email
                if (!emails.isEmpty()) {
                    return (String) emails.get(0).get("email");
                }
            }
        } catch (Exception e) {
            // Log the error but don't throw it
            System.err.println("Error retrieving GitHub email: " + e.getMessage());
        }

        return null;
    }
}
