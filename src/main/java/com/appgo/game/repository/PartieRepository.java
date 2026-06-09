package com.appgo.game.repository;

import com.appgo.game.model.Partie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour les parties de Go.
 */
@Repository
public interface PartieRepository extends JpaRepository<Partie, Long> {
}
