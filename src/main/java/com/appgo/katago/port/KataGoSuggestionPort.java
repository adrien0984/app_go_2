package com.appgo.katago.port;

/**
 * Contrat pour obtenir une suggestion de coup de KataGo.
 */
public interface KataGoSuggestionPort {

    /**
     * Obtient une suggestion de coup basée sur l'état du plateau.
     *
     * @param request Les paramètres de la requête (état du plateau, joueur courant, taille)
     * @return La suggestion avec le coup et la confiance
     * @throws KataGoException Si l'adaptateur échoue
     */
    SuggestionResponse getSuggestion(SuggestionRequest request) throws KataGoException;
}
