package com.blog.repository;

import com.blog.model.Category;
import com.blog.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByPublishedTrue(Pageable pageable);

    Page<Post> findByCategoriesContaining(Category category, Pageable pageable);

    Page<Post> findByCategoriesContainingAndPublishedTrue(Category category, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
            "(p.published = true) AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "EXISTS (SELECT t FROM p.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<Post> searchByTitleOrContentOrTags(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "EXISTS (SELECT t FROM p.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> adminSearchByTitleOrContentOrTags(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.slug = :categorySlug AND p.published = true")
    Page<Post> findPublishedPostsByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.categories c WHERE c.slug = :categorySlug")
    Page<Post> findAllPostsByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);
}
