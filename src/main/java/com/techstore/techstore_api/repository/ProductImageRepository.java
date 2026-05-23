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
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    @Transactional
@Modifying
@Query("DELETE FROM ProductImage i WHERE i.product.id = ?1")
void deleteByProductId(Long productId);
}