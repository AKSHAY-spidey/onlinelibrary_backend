package com.library.security;

import com.library.exception.UserBlockedException;
import com.library.security.services.UserDetailsImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            // First, retrieve the user details
            String username = authentication.getName();
            UserDetails userDetails = getUserDetailsService().loadUserByUsername(username);

            // Check if the user is blocked before even attempting password validation
            if (!userDetails.isEnabled()) {
                throw new UserBlockedException("Your account has been blocked. Please contact the administrator.");
            }

            // Now proceed with normal authentication
            Authentication result = super.authenticate(authentication);
            return result;
        } catch (UserBlockedException e) {
            // Re-throw user blocked exceptions
            throw e;
        } catch (Exception e) {
            // Let other exceptions be handled by the parent
            throw e;
        }
    }
}
