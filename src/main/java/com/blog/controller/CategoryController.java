package com.blog.controller;

import com.blog.dto.CategoryDTO;
import com.blog.dto.PostDTO;
import com.blog.service.CategoryService;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/menu")
    public ResponseEntity<List<CategoryDTO>> getCategoriesForMenu() {
        return ResponseEntity.ok(categoryService.getCategoriesForMenu());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return new ResponseEntity<>(categoryService.createCategory(categoryDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<Page<PostDTO>> getPostsByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostDTO> posts = postService.getPostsByCategory(id, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/slug/{slug}/posts")
    public ResponseEntity<Page<PostDTO>> getPostsByCategorySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // This request will call a custom query method in the repository that needs to be implemented
        Page<PostDTO> posts = postService.getPostsByCategory(
                categoryService.getCategoryBySlug(slug).getId(),
                pageable
        );

        return ResponseEntity.ok(posts);
    }

    @PostMapping("/sample")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryDTO>> createSampleCategories() {
        List<CategoryDTO> createdCategories = new ArrayList<>();

        // Create Programming category
        CategoryDTO programming = new CategoryDTO();
        programming.setName("Programming");
        programming.setDescription("Articles about software development and programming languages");
        programming.setDisplayInMenu(true);
        programming.setMenuOrder(1);
        createdCategories.add(categoryService.createCategory(programming));

        // Create Web Development category
        CategoryDTO webDev = new CategoryDTO();
        webDev.setName("Web Development");
        webDev.setDescription("Front-end and back-end web development topics");
        webDev.setDisplayInMenu(true);
        webDev.setMenuOrder(2);
        createdCategories.add(categoryService.createCategory(webDev));

        // Create Mobile Development category
        CategoryDTO mobileDev = new CategoryDTO();
        mobileDev.setName("Mobile Development");
        mobileDev.setDescription("iOS, Android, and cross-platform mobile app development");
        mobileDev.setDisplayInMenu(true);
        mobileDev.setMenuOrder(3);
        createdCategories.add(categoryService.createCategory(mobileDev));

        // Create Data Science category
        CategoryDTO dataScience = new CategoryDTO();
        dataScience.setName("Data Science");
        dataScience.setDescription("Machine learning, AI, data analysis, and visualization");
        dataScience.setDisplayInMenu(true);
        dataScience.setMenuOrder(4);
        createdCategories.add(categoryService.createCategory(dataScience));

        // Create Technology category
        CategoryDTO technology = new CategoryDTO();
        technology.setName("Technology");
        technology.setDescription("General technology news and trends");
        technology.setDisplayInMenu(true);
        technology.setMenuOrder(5);
        createdCategories.add(categoryService.createCategory(technology));

        return new ResponseEntity<>(createdCategories, HttpStatus.CREATED);
    }
}