package com.sipm.repository;

import com.sipm.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByReference(String reference);
    
    boolean existsByReference(String reference);

    @Query("SELECT a FROM Article a WHERE " +
           "LOWER(a.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.reference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.categorie) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Article> searchArticles(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.quantiteEnStock <= :seuil")
    Page<Article> findArticlesStockFaible(@Param("seuil") Integer seuil, Pageable pageable);
} 