package com.appgo.init;

import com.appgo.auth.model.Utilisateur;
import com.appgo.auth.repository.UtilisateurRepository;
import com.appgo.game.model.Partie;
import com.appgo.game.repository.PartieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise les données de démonstration au démarrage de l'application.
 * Idempotent: peut être exécuté plusieurs fois sans créer de doublons.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PartieRepository partieRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UtilisateurRepository utilisateurRepository,
                              PartieRepository partieRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.partieRepository = partieRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("Initializing demo data...");

        initializeDemoUsers();
        initializeDemoGames();

        logger.info("Demo data initialization completed.");
    }

    private void initializeDemoUsers() {
        // Demo user (for testing with AuthProperties)
        if (!utilisateurRepository.existsByUsername("demo")) {
            Utilisateur demoUser = new Utilisateur(
                    "demo",
                    passwordEncoder.encode("demo-password"),
                    "demo@appgo.local"
            );
            utilisateurRepository.save(demoUser);
            logger.info("Created demo user: demo");
        }

        // Demo user 1
        if (!utilisateurRepository.existsByUsername("demo1")) {
            Utilisateur user1 = new Utilisateur(
                    "demo1",
                    passwordEncoder.encode("password1"),
                    "demo1@appgo.local"
            );
            utilisateurRepository.save(user1);
            logger.info("Created demo user: demo1");
        }

        // Demo user 2
        if (!utilisateurRepository.existsByUsername("demo2")) {
            Utilisateur user2 = new Utilisateur(
                    "demo2",
                    passwordEncoder.encode("password2"),
                    "demo2@appgo.local"
            );
            utilisateurRepository.save(user2);
            logger.info("Created demo user: demo2");
        }

        // Demo user 3
        if (!utilisateurRepository.existsByUsername("demo3")) {
            Utilisateur user3 = new Utilisateur(
                    "demo3",
                    passwordEncoder.encode("password3"),
                    "demo3@appgo.local"
            );
            utilisateurRepository.save(user3);
            logger.info("Created demo user: demo3");
        }
    }

    private void initializeDemoGames() {
        var users = utilisateurRepository.findAll();
        
        if (users.size() < 2) {
            logger.warn("Not enough users to create demo games");
            return;
        }

        Long user1Id = users.get(0).getId();
        Long user2Id = users.get(1).getId();
        Long user3Id = users.size() > 2 ? users.get(2).getId() : user1Id;

        // Demo game 1: En cours (19x19)
        if (partieRepository.count() == 0) {
            Partie game1 = new Partie(
                    user1Id,
                    user2Id,
                    19,
                    "{\"boardSize\": 19, \"state\": \"initial\"}",
                    "IN_PROGRESS",
                    user1Id
            );
            partieRepository.save(game1);
            logger.info("Created demo game 1: IN_PROGRESS (19x19)");

            // Demo game 2: En cours (13x13)
            Partie game2 = new Partie(
                    user2Id,
                    user3Id,
                    13,
                    "{\"boardSize\": 13, \"state\": \"initial\"}",
                    "IN_PROGRESS",
                    user2Id
            );
            partieRepository.save(game2);
            logger.info("Created demo game 2: IN_PROGRESS (13x13)");

            // Demo game 3: Terminée (9x9)
            Partie game3 = new Partie(
                    user1Id,
                    user3Id,
                    9,
                    "{\"boardSize\": 9, \"state\": \"finished\"}",
                    "FINISHED",
                    user1Id
            );
            partieRepository.save(game3);
            logger.info("Created demo game 3: FINISHED (9x9)");
        }
    }
}
