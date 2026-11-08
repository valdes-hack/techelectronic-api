package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Use LIMIT via JPQL to avoid NonUniqueResultException when duplicate carts exist
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId ORDER BY c.id ASC")
    List<Cart> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Cart c WHERE c.sessionId = :sessionId ORDER BY c.id ASC")
    List<Cart> findAllBySessionId(@Param("sessionId") String sessionId);

    default Optional<Cart> findByUserId(Long userId) {
        List<Cart> carts = findAllByUserId(userId);
        return carts.isEmpty() ? Optional.empty() : Optional.of(carts.get(0));
    }

    default Optional<Cart> findBySessionId(String sessionId) {
        List<Cart> carts = findAllBySessionId(sessionId);
        return carts.isEmpty() ? Optional.empty() : Optional.of(carts.get(0));
    }
}