package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Product;
import com.techstore.techstore_api.model.ProductCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCollectionRepository extends JpaRepository<ProductCollection, Long> {

    boolean existsByCollectionIdAndProductId(Long collectionId, Long productId);

    Optional<ProductCollection> findByCollectionIdAndProductId(Long collectionId, Long productId);

    List<ProductCollection> findByCollectionIdOrderBySortOrderAscIdAsc(Long collectionId);

    List<ProductCollection> findByProductId(Long productId);

    long countByCollectionId(Long collectionId);

    /**
     * Page des produits ACTIFS d'une collection, triés par position manuelle.
     * Utilisée par la vitrine publique : GET /api/v1/collections/{slug}/products
     */
    @Query("SELECT pc.product FROM ProductCollection pc " +
           "WHERE pc.collection.id = :collectionId AND pc.product.isActive = true " +
           "ORDER BY pc.sortOrder ASC, pc.id ASC")
    Page<Product> findActiveProductsByCollection(@Param("collectionId") Long collectionId, Pageable pageable);

    /**
     * Page de TOUS les produits d'une collection (admin, y compris inactifs).
     */
    @Query("SELECT pc.product FROM ProductCollection pc " +
           "WHERE pc.collection.id = :collectionId " +
           "ORDER BY pc.sortOrder ASC, pc.id ASC")
    Page<Product> findAllProductsByCollection(@Param("collectionId") Long collectionId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByCollectionIdAndProductId(Long collectionId, Long productId);

    @Modifying
    @Transactional
    void deleteByCollectionId(Long collectionId);

    @Modifying
    @Transactional
    void deleteByProductId(Long productId);
}
