package com.blog.controller.admin;

import com.blog.dto.CategoryDTO;
import com.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return new ResponseEntity<>(categoryService.createCategory(categoryDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sample-hierarchical")
    public ResponseEntity<List<CategoryDTO>> createSampleHierarchicalCategories() {
        List<CategoryDTO> createdCategories = new ArrayList<>();

        // Create top-level categories
        CategoryDTO programming = new CategoryDTO();
        programming.setName("Programming");
        programming.setDescription("Articles about software development and programming languages");
        programming.setDisplayInMenu(true);
        programming.setMenuOrder(1);
        CategoryDTO savedProgramming = categoryService.createCategory(programming);
        createdCategories.add(savedProgramming);

        CategoryDTO technology = new CategoryDTO();
        technology.setName("Technology");
        technology.setDescription("General technology news and trends");
        technology.setDisplayInMenu(true);
        technology.setMenuOrder(2);
        CategoryDTO savedTechnology = categoryService.createCategory(technology);
        createdCategories.add(savedTechnology);

        CategoryDTO health = new CategoryDTO();
        health.setName("Health");
        health.setDescription("Health, fitness, and wellness articles");
        health.setDisplayInMenu(true);
        health.setMenuOrder(3);
        CategoryDTO savedHealth = categoryService.createCategory(health);
        createdCategories.add(savedHealth);

        // Create sub-categories
        // Programming children
        CategoryDTO java = new CategoryDTO();
        java.setName("Java");
        java.setDescription("Java programming language articles");
        java.setDisplayInMenu(true);
        java.setMenuOrder(1);
        java.setParentId(savedProgramming.getId());
        createdCategories.add(categoryService.createCategory(java));

        CategoryDTO python = new CategoryDTO();
        python.setName("Python");
        python.setDescription("Python programming language articles");
        python.setDisplayInMenu(true);
        python.setMenuOrder(2);
        python.setParentId(savedProgramming.getId());
        createdCategories.add(categoryService.createCategory(python));

        CategoryDTO javascript = new CategoryDTO();
        javascript.setName("JavaScript");
        javascript.setDescription("JavaScript programming language articles");
        javascript.setDisplayInMenu(true);
        javascript.setMenuOrder(3);
        javascript.setParentId(savedProgramming.getId());
        createdCategories.add(categoryService.createCategory(javascript));

        // Technology children
        CategoryDTO ai = new CategoryDTO();
        ai.setName("Artificial Intelligence");
        ai.setDescription("AI and machine learning articles");
        ai.setDisplayInMenu(true);
        ai.setMenuOrder(1);
        ai.setParentId(savedTechnology.getId());
        createdCategories.add(categoryService.createCategory(ai));

        CategoryDTO hardware = new CategoryDTO();
        hardware.setName("Hardware");
        hardware.setDescription("Computer hardware reviews and news");
        hardware.setDisplayInMenu(true);
        hardware.setMenuOrder(2);
        hardware.setParentId(savedTechnology.getId());
        createdCategories.add(categoryService.createCategory(hardware));

        // Health children
        CategoryDTO nutrition = new CategoryDTO();
        nutrition.setName("Nutrition");
        nutrition.setDescription("Diet and nutrition articles");
        nutrition.setDisplayInMenu(true);
        nutrition.setMenuOrder(1);
        nutrition.setParentId(savedHealth.getId());
        createdCategories.add(categoryService.createCategory(nutrition));

        CategoryDTO fitness = new CategoryDTO();
        fitness.setName("Fitness");
        fitness.setDescription("Exercise and workout articles");
        fitness.setDisplayInMenu(true);
        fitness.setMenuOrder(2);
        fitness.setParentId(savedHealth.getId());
        createdCategories.add(categoryService.createCategory(fitness));

        return new ResponseEntity<>(createdCategories, HttpStatus.CREATED);
    }

    @PostMapping("/sample")
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