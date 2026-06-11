package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
// Dans ProductVariantRepository.java, ajoute :
Optional<ProductVariant> findBySkuVariant(String skuVariant);
    @Transactional
@Modifying
@Query("DELETE FROM ProductVariant v WHERE v.product.id = ?1")
void deleteByProductId(Long productId);

    // Compter les variantes en stock faible pour le dashboard
    long countByStockQtyLessThan(Integer threshold);

    java.util.List<ProductVariant> findByStockQtyLessThanOrderByStockQtyAsc(Integer threshold);
}