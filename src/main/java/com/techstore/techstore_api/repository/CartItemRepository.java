package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Modifying
    @Transactional
    @Query(value = "UPDATE cart_items SET variant_id = NULL WHERE variant_id IN (SELECT id FROM product_variants WHERE product_id = :productId)", nativeQuery = true)
    void detachVariantByProductId(@Param("productId") Long productId);
}