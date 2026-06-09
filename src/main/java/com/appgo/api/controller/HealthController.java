package com.appgo.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contrôleur pour les endpoints de santé et d'informations.
 * 
 * Endpoint key:
 * - GET /health - Check application health status (returns X-Correlation-Id header)
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    /**
     * Vérifie l'état de santé de l'application.
     * 
     * Cette opération ne consomme pas de ressources et est utilisée pour le monitoring.
     * Le correlation-id de la requête est retourné en header pour le suivi.
     * 
     * @return un objet JSON contenant le statut (UP) et un message
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Health check endpoint called");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Application is running");
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
