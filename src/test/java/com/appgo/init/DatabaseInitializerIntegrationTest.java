package com.appgo.init;

import com.appgo.auth.model.Utilisateur;
import com.appgo.auth.repository.UtilisateurRepository;
import com.appgo.game.model.Partie;
import com.appgo.game.repository.PartieRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseInitializerIntegrationTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PartieRepository partieRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DatabaseResetService resetService;

    @Test
    void testDatabaseInitializerCreatesUsers() {
        // Verify demo users exist
        assertTrue(utilisateurRepository.existsByUsername("demo1"));
        assertTrue(utilisateurRepository.existsByUsername("demo2"));
        assertTrue(utilisateurRepository.existsByUsername("demo3"));

        // Verify user count
        assertEquals(3, utilisateurRepository.count());
    }

    @Test
    void testDatabaseInitializerCreatesDemoGames() {
        // Verify demo games exist
        assertEquals(3, partieRepository.count());

        // Verify game sizes
        var games = partieRepository.findAll();
        var sizes = games.stream()
                .map(Partie::getBoardSize)
                .sorted()
                .toList();
        assertEquals(java.util.List.of(9, 13, 19), sizes);
    }

    @Test
    void testResetServiceIsIdempotent() {
        // First reset
        resetService.resetDatabase();
        long countAfterFirstReset = utilisateurRepository.count();
        assertEquals(3, countAfterFirstReset);

        // Second reset
        resetService.resetDatabase();
        long countAfterSecondReset = utilisateurRepository.count();

        // Should be same count (no duplicates)
        assertEquals(countAfterFirstReset, countAfterSecondReset);
    }

    @Test
    void testResetClearsAllData() {
        // Add extra user
        Utilisateur extraUser = new Utilisateur("extra", passwordEncoder.encode("pass"), "extra@test.com");
        utilisateurRepository.save(extraUser);

        long countBefore = utilisateurRepository.count();
        assertTrue(countBefore > 3);

        // Reset
        resetService.resetDatabase();

        // Should only have demo users
        assertEquals(3, utilisateurRepository.count());
        assertTrue(utilisateurRepository.existsByUsername("demo1"));
        assertTrue(utilisateurRepository.existsByUsername("demo2"));
        assertTrue(utilisateurRepository.existsByUsername("demo3"));
        assertFalse(utilisateurRepository.existsByUsername("extra"));
    }

    @Test
    void testResetGamesAfterReset() {
        // Reset once
        resetService.resetDatabase();
        long gamesAfterFirstReset = partieRepository.count();
        assertEquals(3, gamesAfterFirstReset);

        // Reset again
        resetService.resetDatabase();
        long gamesAfterSecondReset = partieRepository.count();
        assertEquals(3, gamesAfterSecondReset);
    }
}
