package com.library.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        // Get error details
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object exception = request.getAttribute("jakarta.servlet.error.exception");
        Object message = request.getAttribute("jakarta.servlet.error.message");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "An error occurred");
        errorDetails.put("message", message != null ? message : (exception != null ? exception.toString() : "Unknown error"));
        errorDetails.put("path", request.getRequestURI());
        
        return new ResponseEntity<>(errorDetails, HttpStatus.valueOf((Integer) errorDetails.get("status")));
    }
}
