package com.appgo.init;

import com.appgo.auth.model.Utilisateur;
import com.appgo.auth.repository.UtilisateurRepository;
import com.appgo.game.repository.PartieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour réinitialiser les données de la base de données.
 * Supprime toutes les parties et les utilisateurs, puis reseed les données de démo.
 */
@Service
public class DatabaseResetService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseResetService.class);

    private final UtilisateurRepository utilisateurRepository;
    private final PartieRepository partieRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseResetService(UtilisateurRepository utilisateurRepository,
                                PartieRepository partieRepository,
                                PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.partieRepository = partieRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void resetDatabase() {
        logger.info("Resetting database...");

            // Supprimer toutes les parties d'abord (foreign keys)
        partieRepository.deleteAll();
        logger.info("Deleted all parties");

        // Supprimer tous les utilisateurs
        utilisateurRepository.deleteAll();
        logger.info("Deleted all users");

            // Flush pour s'assurer que les deletes sont effectués
            utilisateurRepository.flush();
            partieRepository.flush();

            // Reseed les données de démo
            seedDemoUsers();
            seedDemoGames();

            logger.info("Database reset completed successfully");
        }

    private void seedDemoUsers() {
        Utilisateur demoUser = new Utilisateur(
                "demo",
                passwordEncoder.encode("demo-password"),
                "demo@appgo.local"
        );
        utilisateurRepository.save(demoUser);
        logger.info("Seeded demo user: demo");

        Utilisateur user1 = new Utilisateur(
                "demo1",
                passwordEncoder.encode("password1"),
                "demo1@appgo.local"
        );
        utilisateurRepository.save(user1);
        logger.info("Seeded demo user: demo1");

        Utilisateur user2 = new Utilisateur(
                "demo2",
                passwordEncoder.encode("password2"),
                "demo2@appgo.local"
        );
        utilisateurRepository.save(user2);
        logger.info("Seeded demo user: demo2");

        Utilisateur user3 = new Utilisateur(
                "demo3",
                passwordEncoder.encode("password3"),
                "demo3@appgo.local"
        );
        utilisateurRepository.save(user3);
        logger.info("Seeded demo user: demo3");
    }

    private void seedDemoGames() {
        var users = utilisateurRepository.findAll();

        if (users.size() < 2) {
            logger.warn("Not enough users to seed demo games");
            return;
        }

        Long user1Id = users.get(0).getId();
        Long user2Id = users.get(1).getId();
        Long user3Id = users.size() > 2 ? users.get(2).getId() : user1Id;

        var game1 = new com.appgo.game.model.Partie(
                user1Id,
                user2Id,
                19,
                "{\"boardSize\": 19, \"state\": \"initial\"}",
                "IN_PROGRESS",
                user1Id
        );
        partieRepository.save(game1);
        logger.info("Seeded demo game 1: IN_PROGRESS (19x19)");

        var game2 = new com.appgo.game.model.Partie(
                user2Id,
                user3Id,
                13,
                "{\"boardSize\": 13, \"state\": \"initial\"}",
                "IN_PROGRESS",
                user2Id
        );
        partieRepository.save(game2);
        logger.info("Seeded demo game 2: IN_PROGRESS (13x13)");

        var game3 = new com.appgo.game.model.Partie(
                user1Id,
                user3Id,
                9,
                "{\"boardSize\": 9, \"state\": \"finished\"}",
                "FINISHED",
                user1Id
        );
        partieRepository.save(game3);
        logger.info("Seeded demo game 3: FINISHED (9x9)");
    }
}
