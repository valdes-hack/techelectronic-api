package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySlug(String slug);

    // ✨ AJOUTE CETTE LIGNE POUR RÉPARER L'ERREUR ✨
    Optional<Product> findBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
    boolean existsBySlugAndIdNot(String slug, Long id);

    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.category = null WHERE p.category.id = :categoryId")
    void unlinkCategory(@Param("categoryId") Long categoryId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Product p WHERE p.category.id = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.category = null")
    void unlinkAllCategories();

    // Compter les produits en stock faible pour le dashboard
    long countByStockQtyLessThanAndIsActiveTrue(Integer threshold);
    
    java.util.List<Product> findByStockQtyLessThanAndIsActiveTrueOrderByStockQtyAsc(Integer threshold);

    /**
     * Recherche par mot-clé (nom, marque, description) : insensible à la casse, mots partiels acceptés.
     * Volontairement en JPQL/LIKE : ne dépend d'aucun index FULLTEXT à créer manuellement en base
     * (l'ancienne requête MATCH ... AGAINST renvoyait une erreur 500 sans cet index).
     */
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "CALL deduct_stock(:p_product_id, :p_variant_id, :p_quantity)", nativeQuery = true)
    void deductStock(
        @Param("p_product_id") Long productId, 
        @Param("p_variant_id") Long variantId, 
        @Param("p_quantity") Integer quantity
    );

    @Transactional
    @Modifying
    @Query(value = "CALL add_stock(:p_product_id, :p_variant_id, :p_quantity)", nativeQuery = true)
    void addStock(
        @Param("p_product_id") Long productId, 
        @Param("p_variant_id") Long variantId, 
        @Param("p_quantity") Integer quantity
    );
}
