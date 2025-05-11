package com.library.security.oauth2;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {

    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return ((Integer) attributes.get("id")).toString();
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    private String email;

    @Override
    public String getEmail() {
        // Return the manually set email if available, otherwise get from attributes
        if (email != null) {
            return email;
        }
        return (String) attributes.get("email");
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
