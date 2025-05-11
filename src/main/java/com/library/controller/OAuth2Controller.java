package com.library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    @GetMapping("/urls")
    public ResponseEntity<Map<String, String>> getOAuthUrls() {
        Map<String, String> urls = new HashMap<>();
        urls.put("googleAuthUrl", "/oauth2/authorize/google");
        urls.put("githubAuthUrl", "/oauth2/authorize/github");

        return ResponseEntity.ok(urls);
    }
}
