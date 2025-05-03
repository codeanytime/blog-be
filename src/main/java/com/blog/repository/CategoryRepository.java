package com.blog.repository;

import com.blog.model.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.displayInMenu = true ORDER BY c.menuOrder ASC")
    List<Category> findCategoriesForMenu();

    List<Category> findByParentIsNull(Sort sort);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.menuOrder ASC")
    List<Category> findByParentIsNullOrderByMenuOrder();

    @Query(value = "SELECT id, name, slug, description, display_in_menu, menu_order, parent_id " +
            "FROM categories WHERE parent_id IS NULL ORDER BY menu_order ASC", nativeQuery = true)
    List<Object[]> findTopLevelCategoriesNative();

    @Query("SELECT c FROM Category c WHERE c.parent.id = ?1 ORDER BY c.menuOrder ASC")
    List<Category> findByParentIdOrderByMenuOrder(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.displayInMenu = true ORDER BY c.menuOrder ASC")
    List<Category> findTopLevelMenuCategories();
}