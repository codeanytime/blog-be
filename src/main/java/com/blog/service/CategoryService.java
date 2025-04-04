package com.blog.service;

import com.blog.dto.CategoryDTO;
import com.blog.model.Category;
import com.blog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "menuOrder"))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesForMenu() {
        return categoryRepository.findCategoriesForMenu()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        validateCategoryName(categoryDTO.getName(), null);

        Category category = new Category();
        updateCategoryFromDTO(category, categoryDTO);

        // Create slug from name
        category.setSlug(generateSlug(categoryDTO.getName()));

        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        validateCategoryName(categoryDTO.getName(), id);
        updateCategoryFromDTO(category, categoryDTO);

        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private void validateCategoryName(String name, Long excludeId) {
        categoryRepository.findByName(name).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new RuntimeException("Category with name '" + name + "' already exists");
            }
        });
    }

    private void updateCategoryFromDTO(Category category, CategoryDTO dto) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setDisplayInMenu(dto.isDisplayInMenu());
        category.setMenuOrder(dto.getMenuOrder());
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setDisplayInMenu(category.isDisplayInMenu());
        dto.setMenuOrder(category.getMenuOrder());
        return dto;
    }

    private String generateSlug(String name) {
        // Convert to lowercase and replace spaces with hyphens
        String slug = name.toLowerCase().replaceAll("\\s+", "-");
        // Remove special characters
        slug = slug.replaceAll("[^a-z0-9-]", "");

        // Check if slug exists and make it unique
        String baseSlug = slug;
        int count = 1;

        while (categoryRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + count;
            count++;
        }

        return slug;
    }
}