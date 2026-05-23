package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByIsActiveTrue();
    List<Category> findByParentIsNull(); // Pour récupérer uniquement les catégories parentes
}