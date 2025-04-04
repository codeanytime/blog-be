package com.blog.repository;

import com.blog.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author thanhch
 * <p>
 * Date: 04/04/2025
 * <p>
 * interface: CategoryRepository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.displayInMenu = true ORDER BY c.menuOrder ASC")
    List<Category> findCategoriesForMenu();
}