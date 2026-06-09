package com.appgo.game.domain;

/**
 * Couleurs de pierres en Go.
 */
public enum CouleurPierre {
    NOIR,
    BLANC;

    public CouleurPierre adversaire() {
        return this == NOIR ? BLANC : NOIR;
    }
}
