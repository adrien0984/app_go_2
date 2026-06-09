package com.appgo.game.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
