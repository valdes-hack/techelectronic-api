package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);
    List<ProductImage> findByProductIdIn(List<Long> productIds);

    // Méthodes ordonnées par ID (garanti 100% compatible SQL et MySQL)
    List<ProductImage> findByProductIdOrderByIdAsc(Long productId);
    List<ProductImage> findByProductIdInOrderByIdAsc(List<Long> productIds);

    @Transactional
    @Modifying
    @Query("DELETE FROM ProductImage i WHERE i.product.id = ?1")
    void deleteByProductId(Long productId);
}