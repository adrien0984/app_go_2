package com.appgo.game.domain;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Modele de partie de Go avec regles de base.
 */
public class PartieGo {

    private static final Set<Integer> TAILLES_AUTORISEES = Set.of(9, 13, 19);
    private static final int[][] DIRECTIONS = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
    };

    private final int taillePlateau;
    private CouleurPierre[][] plateau;
    private CouleurPierre joueurCourant;
    private int passesConsecutives;
    private boolean terminee;

    public PartieGo() {
        this(19);
    }

    public PartieGo(int taillePlateau) {
        if (!TAILLES_AUTORISEES.contains(taillePlateau)) {
            throw new IllegalArgumentException("Taille de plateau invalide. Tailles autorisees: 9, 13, 19");
        }

        this.taillePlateau = taillePlateau;
        this.plateau = new CouleurPierre[taillePlateau][taillePlateau];
        this.joueurCourant = CouleurPierre.NOIR;
        this.passesConsecutives = 0;
        this.terminee = false;
    }

    public int getTaillePlateau() {
        return taillePlateau;
    }

    public CouleurPierre getJoueurCourant() {
        return joueurCourant;
    }

    public boolean estTerminee() {
        return terminee;
    }

    public int getPassesConsecutives() {
        return passesConsecutives;
    }

    public CouleurPierre pierreA(int ligne, int colonne) {
        verifierCoordonnees(ligne, colonne);
        return plateau[ligne][colonne];
    }

    public boolean estCoupLegal(int ligne, int colonne) {
        if (terminee || !estCoordonneeValide(ligne, colonne) || plateau[ligne][colonne] != null) {
            return false;
        }

        CouleurPierre[][] simulation = copierPlateau();
        simulation[ligne][colonne] = joueurCourant;
        capturerGroupesAdjacentsSansLiberte(simulation, ligne, colonne, joueurCourant.adversaire());

        return groupeALiberte(simulation, ligne, colonne);
    }

    public void jouer(int ligne, int colonne) {
        verifierPartieActive();
        verifierCoordonnees(ligne, colonne);

        if (plateau[ligne][colonne] != null) {
            throw new IllegalArgumentException("Coup illegal: intersection deja occupee");
        }

        CouleurPierre[][] simulation = copierPlateau();
        simulation[ligne][colonne] = joueurCourant;
        capturerGroupesAdjacentsSansLiberte(simulation, ligne, colonne, joueurCourant.adversaire());

        if (!groupeALiberte(simulation, ligne, colonne)) {
            throw new IllegalArgumentException("Coup illegal: suicide");
        }

        this.plateau = simulation;
        this.passesConsecutives = 0;
        this.joueurCourant = joueurCourant.adversaire();
    }

    public void passer() {
        verifierPartieActive();

        this.passesConsecutives++;
        if (this.passesConsecutives >= 2) {
            this.terminee = true;
            return;
        }

        this.joueurCourant = joueurCourant.adversaire();
    }

    private void verifierPartieActive() {
        if (terminee) {
            throw new IllegalStateException("La partie est terminee");
        }
    }

    private void verifierCoordonnees(int ligne, int colonne) {
        if (!estCoordonneeValide(ligne, colonne)) {
            throw new IllegalArgumentException("Coordonnees hors plateau");
        }
    }

    private boolean estCoordonneeValide(int ligne, int colonne) {
        return ligne >= 0 && ligne < taillePlateau && colonne >= 0 && colonne < taillePlateau;
    }

    private CouleurPierre[][] copierPlateau() {
        CouleurPierre[][] copie = new CouleurPierre[taillePlateau][taillePlateau];
        for (int ligne = 0; ligne < taillePlateau; ligne++) {
            System.arraycopy(plateau[ligne], 0, copie[ligne], 0, taillePlateau);
        }
        return copie;
    }

    private void capturerGroupesAdjacentsSansLiberte(
            CouleurPierre[][] etatPlateau,
            int ligne,
            int colonne,
            CouleurPierre cible) {

        boolean[][] explores = new boolean[taillePlateau][taillePlateau];
        for (int[] direction : DIRECTIONS) {
            int voisinLigne = ligne + direction[0];
            int voisinColonne = colonne + direction[1];

            if (!estCoordonneeValide(voisinLigne, voisinColonne)) {
                continue;
            }

            if (etatPlateau[voisinLigne][voisinColonne] != cible || explores[voisinLigne][voisinColonne]) {
                continue;
            }

            Set<Coordonnee> groupe = collecterGroupe(etatPlateau, voisinLigne, voisinColonne, cible, explores);
            if (!groupeALiberte(etatPlateau, groupe)) {
                supprimerGroupe(etatPlateau, groupe);
            }
        }
    }

    private boolean groupeALiberte(CouleurPierre[][] etatPlateau, int ligne, int colonne) {
        CouleurPierre couleur = etatPlateau[ligne][colonne];
        Set<Coordonnee> groupe = collecterGroupe(etatPlateau, ligne, colonne, couleur, new boolean[taillePlateau][taillePlateau]);
        return groupeALiberte(etatPlateau, groupe);
    }

    private boolean groupeALiberte(CouleurPierre[][] etatPlateau, Set<Coordonnee> groupe) {
        for (Coordonnee coordonnee : groupe) {
            for (int[] direction : DIRECTIONS) {
                int voisinLigne = coordonnee.ligne() + direction[0];
                int voisinColonne = coordonnee.colonne() + direction[1];
                if (estCoordonneeValide(voisinLigne, voisinColonne) && etatPlateau[voisinLigne][voisinColonne] == null) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<Coordonnee> collecterGroupe(
            CouleurPierre[][] etatPlateau,
            int ligneDepart,
            int colonneDepart,
            CouleurPierre couleur,
            boolean[][] explores) {

        Set<Coordonnee> groupe = new HashSet<>();
        Deque<Coordonnee> pile = new ArrayDeque<>();
        pile.push(new Coordonnee(ligneDepart, colonneDepart));
        explores[ligneDepart][colonneDepart] = true;

        while (!pile.isEmpty()) {
            Coordonnee courante = pile.pop();
            groupe.add(courante);

            for (int[] direction : DIRECTIONS) {
                int voisinLigne = courante.ligne() + direction[0];
                int voisinColonne = courante.colonne() + direction[1];

                if (!estCoordonneeValide(voisinLigne, voisinColonne) || explores[voisinLigne][voisinColonne]) {
                    continue;
                }

                if (etatPlateau[voisinLigne][voisinColonne] == couleur) {
                    explores[voisinLigne][voisinColonne] = true;
                    pile.push(new Coordonnee(voisinLigne, voisinColonne));
                }
            }
        }

        return groupe;
    }

    private void supprimerGroupe(CouleurPierre[][] etatPlateau, Set<Coordonnee> groupe) {
        for (Coordonnee coordonnee : groupe) {
            etatPlateau[coordonnee.ligne()][coordonnee.colonne()] = null;
        }
    }

    private record Coordonnee(int ligne, int colonne) {
    }
}
