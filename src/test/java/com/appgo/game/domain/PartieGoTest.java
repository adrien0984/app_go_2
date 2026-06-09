package com.appgo.game.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PartieGo Domain Rules Tests")
class PartieGoTest {

    @Test
    void refuseTaillePlateauInvalide() {
        assertThrows(IllegalArgumentException.class, () -> new PartieGo(10));
    }

    @Test
    void accepteTaillesPlateauStandards() {
        assertEquals(9, new PartieGo(9).getTaillePlateau());
        assertEquals(13, new PartieGo(13).getTaillePlateau());
        assertEquals(19, new PartieGo(19).getTaillePlateau());
    }

    @Test
    void refuseCoupHorsPlateauOuOccupe() {
        PartieGo partie = new PartieGo(9);
        partie.jouer(0, 0);

        assertThrows(IllegalArgumentException.class, () -> partie.jouer(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> partie.jouer(9, 9));
        assertThrows(IllegalArgumentException.class, () -> partie.jouer(0, 0));
    }

    @Test
    void captureUnePierreQuandGroupeAdverseSansLiberte() {
        PartieGo partie = new PartieGo(9);

        partie.jouer(0, 1); // N
        partie.jouer(1, 1); // B
        partie.jouer(1, 0); // N
        partie.passer();    // B
        partie.jouer(1, 2); // N
        partie.passer();    // B
        partie.jouer(2, 1); // N capture

        assertNull(partie.pierreA(1, 1));
        assertEquals(CouleurPierre.BLANC, partie.getJoueurCourant());
    }

    @Test
    void refuseSuicide() {
        PartieGo partie = new PartieGo(9);

        partie.jouer(0, 1); // N
        partie.passer();    // B
        partie.jouer(1, 0); // N
        partie.passer();    // B
        partie.jouer(1, 2); // N
        partie.passer();    // B
        partie.jouer(2, 1); // N

        assertFalse(partie.estCoupLegal(1, 1));
        assertThrows(IllegalArgumentException.class, () -> partie.jouer(1, 1));
    }

    @Test
    void termineApresDeuxPassesConsecutives() {
        PartieGo partie = new PartieGo(9);

        partie.passer();
        assertFalse(partie.estTerminee());
        assertEquals(CouleurPierre.BLANC, partie.getJoueurCourant());

        partie.passer();
        assertTrue(partie.estTerminee());
        assertEquals(2, partie.getPassesConsecutives());

        assertThrows(IllegalStateException.class, () -> partie.jouer(0, 0));
        assertThrows(IllegalStateException.class, partie::passer);
    }

    @Test
    @DisplayName("Game state alternates between black and white")
    void gameAlternatesPlayers() {
        PartieGo partie = new PartieGo(9);

        assertEquals(CouleurPierre.NOIR, partie.getJoueurCourant());
        partie.jouer(0, 0);

        assertEquals(CouleurPierre.BLANC, partie.getJoueurCourant());
        partie.jouer(1, 1);

        assertEquals(CouleurPierre.NOIR, partie.getJoueurCourant());
        partie.jouer(2, 2);

        assertEquals(CouleurPierre.BLANC, partie.getJoueurCourant());
    }

    @Test
    @DisplayName("Stone placement and retrieval")
    void stonePlacementAndRetrieval() {
        PartieGo partie = new PartieGo(9);

        partie.jouer(3, 3);
        partie.jouer(4, 4);
        partie.jouer(5, 5);

        assertEquals(CouleurPierre.NOIR, partie.pierreA(3, 3));
        assertEquals(CouleurPierre.BLANC, partie.pierreA(4, 4));
        assertEquals(CouleurPierre.NOIR, partie.pierreA(5, 5));
        assertNull(partie.pierreA(6, 6));
    }

    @Test
    @DisplayName("Pass moves work correctly")
    void passMoveWorks() {
        PartieGo partie = new PartieGo(9);

        partie.jouer(0, 0);
        assertEquals(CouleurPierre.BLANC, partie.getJoueurCourant());

        partie.passer();
        assertEquals(CouleurPierre.NOIR, partie.getJoueurCourant());
        assertEquals(1, partie.getPassesConsecutives());

        partie.jouer(1, 1);
        assertEquals(0, partie.getPassesConsecutives());
    }

    @Test
    @DisplayName("Cannot play after game is finished")
    void cannotPlayAfterGameFinished() {
        PartieGo partie = new PartieGo(9);

        partie.passer();
        partie.passer();

        assertTrue(partie.estTerminee());
        assertThrows(IllegalStateException.class, () -> partie.jouer(0, 0));
    }

    @Test
    @DisplayName("Consecutive passes end the game")
    void consecutivePassesEndGame() {
        PartieGo partie = new PartieGo(9);

        assertFalse(partie.estTerminee());

        partie.passer(); // Black passes
        assertFalse(partie.estTerminee());

        partie.passer(); // White passes -> game ends
        assertTrue(partie.estTerminee());
    }

    @Test
    @DisplayName("Pass after move resets pass counter")
    void passMoveResetsCounter() {
        PartieGo partie = new PartieGo(9);

        partie.passer();
        assertEquals(1, partie.getPassesConsecutives());

        partie.jouer(0, 0);
        assertEquals(0, partie.getPassesConsecutives());

        partie.passer();
        assertEquals(1, partie.getPassesConsecutives());
    }

    @Test
    @DisplayName("Capture creates empty intersections")
    void captureCreatesEmptyIntersections() {
        PartieGo partie = new PartieGo(9);

        partie.jouer(0, 1); // N
        partie.jouer(1, 1); // B
        partie.jouer(1, 0); // N
        partie.passer();    // B
        partie.jouer(1, 2); // N
        partie.passer();    // B
        partie.jouer(2, 1); // N capture

        // The captured position should now be empty
        assertNull(partie.pierreA(1, 1));
        // Adjacent empty positions should remain empty
        assertNull(partie.pierreA(0, 0));
        assertNull(partie.pierreA(3, 3));
    }
}

