package com.appgo.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Route protégée de validation.
 */
@RestController
@RequestMapping("/api")
public class ProtectedController {

    @GetMapping("/protected")
    public Map<String, Object> protectedRoute(Authentication authentication) {
        return Map.of(
                "message", "Authenticated",
                "user", authentication.getName());
    }
}
