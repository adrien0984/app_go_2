package com.appgo.init.controller;

import com.appgo.init.DatabaseResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller pour les opérations de réinitialisation de base de données.
 * Disponible uniquement en développement (propriété app.reset.enabled).
 */
@RestController
@RequestMapping("/api/admin/reset")
@ConditionalOnProperty(name = "app.reset.enabled", havingValue = "true", matchIfMissing = false)
public class ResetController {

    private static final Logger logger = LoggerFactory.getLogger(ResetController.class);

    private final DatabaseResetService resetService;

    public ResetController(DatabaseResetService resetService) {
        this.resetService = resetService;
    }

    /**
     * Réinitialise complètement la base de données (supprime tout et reseed les données de démo).
     */
    @PostMapping("/database")
    public ResponseEntity<String> resetDatabase() {
        logger.info("Reset database requested");
        resetService.resetDatabase();
        return ResponseEntity.ok("Database reset completed successfully");
    }
}
