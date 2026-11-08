package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findBySlug(String slug);

    Optional<Collection> findByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    // Public : uniquement les collections actives, triées (PROMO en premier via sortOrder)
    List<Collection> findByIsActiveTrueOrderBySortOrderAscIdAsc();

    // Admin : toutes les collections
    List<Collection> findAllByOrderBySortOrderAscIdAsc();

    long countByIsActiveTrue();

    /**
     * Comptage rapide du nombre de produits ACTIFS dans une collection.
     * Évite de charger toutes les liaisons côté Java.
     */
    @Query("SELECT COUNT(pc) FROM ProductCollection pc WHERE pc.collection.id = :collectionId AND pc.product.isActive = true")
    long countActiveProducts(@Param("collectionId") Long collectionId);
}
