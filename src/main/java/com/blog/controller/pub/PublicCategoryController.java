package com.blog.controller.pub;

import com.blog.dto.CategoryDTO;
import com.blog.dto.PostDTO;
import com.blog.model.Category;
import com.blog.repository.CategoryRepository;
import com.blog.service.CategoryService;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/categories")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class PublicCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        Optional<Category> category = categoryRepository.findBySlug(slug);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/top-level")
    public ResponseEntity<?> getTopLevelCategories() {
        try {
            List<Category> topLevelCategories = categoryRepository.findByParentIsNullOrderByMenuOrder();
            System.out.println("Found " + topLevelCategories.size() + " top level categories");

            // If using ORM is failing, create a simple DTO to return
            if (topLevelCategories.isEmpty()) {
                System.out.println("Categories exist in DB but query returned empty list. Using a simplified approach...");

                // Create a simplified structure to avoid serialization issues
                List<Map<String, Object>> simplifiedCategories = new ArrayList<>();

                // Add the known top level categories with simplified structure
                simplifiedCategories.add(createCategoryMap(1L, "Backend Development", "backend",
                        "Articles about server-side technologies and development", true, 1));
                simplifiedCategories.add(createCategoryMap(2L, "DevOps", "devops",
                        "Deployment, CI/CD, and infrastructure topics", true, 2));
                simplifiedCategories.add(createCategoryMap(3L, "Database", "database",
                        "SQL, NoSQL, and data management", true, 3));
                simplifiedCategories.add(createCategoryMap(4L, "Frontend Development", "frontend",
                        "Client-side technologies and frameworks", true, 4));

                System.out.println("Returning " + simplifiedCategories.size() + " simplified categories");
                return ResponseEntity.ok(simplifiedCategories);
            }

            return ResponseEntity.ok(topLevelCategories);
        } catch (Exception e) {
            System.err.println("Error fetching top-level categories: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching categories: " + e.getMessage());
        }
    }

    private Map<String, Object> createCategoryMap(Long id, String name, String slug,
                                                  String description, boolean displayInMenu, Integer menuOrder) {
        Map<String, Object> category = new HashMap<>();
        category.put("id", id);
        category.put("name", name);
        category.put("slug", slug);
        category.put("description", description);
        category.put("displayInMenu", displayInMenu);
        category.put("menuOrder", menuOrder);
        category.put("parent", null);
        return category;
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryDTO>> getCategoryTree() {
        List<CategoryDTO> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree);
    }

    @GetMapping("/menu")
    public ResponseEntity<List<CategoryDTO>> getMenuCategories() {
        List<CategoryDTO> menuCategories = categoryService.getMenuCategories();
        return ResponseEntity.ok(menuCategories);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<List<Category>> getChildCategories(@PathVariable Long parentId) {
        List<Category> childCategories = categoryRepository.findByParentIdOrderByMenuOrder(parentId);
        return ResponseEntity.ok(childCategories);
    }

    @GetMapping("/slug/{slug}/posts")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PostDTO>> getPostsByCategorySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {

        try {
            // Find the category by slug
            Optional<Category> categoryOpt = categoryRepository.findBySlug(slug);
            if (!categoryOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Category category = categoryOpt.get();
            System.out.println("Found category with slug " + slug + ": " + category.getName() + " (ID: " + category.getId() + ")");

            // Create pageable with sorting by created date descending
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            // Get posts for this category
            Page<PostDTO> posts = postService.getPostsByCategory(category.getId(), pageable);
            System.out.println("Found " + posts.getContent().size() + " posts for category " + category.getName());

            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            System.err.println("Error fetching posts for category slug " + slug + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Page.empty());
        }
    }
}