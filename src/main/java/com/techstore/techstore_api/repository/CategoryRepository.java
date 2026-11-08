package com.techstore.techstore_api.repository;

import com.techstore.techstore_api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByIsActiveTrue();
    List<Category> findByParentIsNull(); // Pour récupérer uniquement les catégories parentes

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.parent = null WHERE c.parent.id = :parentId")
    void unlinkParent(@Param("parentId") Long parentId);

    @Transactional
    @Modifying
    @Query("UPDATE Category c SET c.parent = null")
    void unlinkAllParents();
}