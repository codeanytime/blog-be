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

    /**
     * Search categories by name, slug, or description
     * @param query The search query
     * @return List of category DTOs that match the search criteria
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> searchCategories(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Convert query to lowercase for case-insensitive search
        String searchTerm = query.toLowerCase();

        // Get all categories
        List<Category> allCategories = categoryRepository.findAll();

        // Filter categories that match the search query
        return allCategories.stream()
                .filter(category ->
                        category.getName().toLowerCase().contains(searchTerm) ||
                                (category.getDescription() != null && category.getDescription().toLowerCase().contains(searchTerm)) ||
                                category.getSlug().toLowerCase().contains(searchTerm))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
    public List<CategoryDTO> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull(Sort.by(Sort.Direction.ASC, "menuOrder"))
                .stream()
                .map(this::convertToDTOWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        // Get all top-level categories
        List<Category> topLevelCategories = categoryRepository.findByParentIsNullOrderByMenuOrder();

        // Convert to DTOs with children populated
        return topLevelCategories.stream()
                .map(this::convertToDTOWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getMenuCategories() {
        // Find categories that should be displayed in the menu
        List<Category> menuCategories = categoryRepository.findCategoriesForMenu();

        // Convert to DTOs
        return menuCategories.stream()
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
    public CategoryDTO getCategoryByIdWithChildren(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTOWithChildren)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
    }

    @Transactional(readOnly = true)
    public CategoryDTO getCategoryBySlugWithChildren(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::convertToDTOWithChildren)
                .orElseThrow(() -> new RuntimeException("Category not found with slug: " + slug));
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        validateCategoryName(categoryDTO.getName(), null);

        Category category = new Category();
        updateCategoryFromDTO(category, categoryDTO);

        // Create slug from name
        category.setSlug(generateSlug(categoryDTO.getName()));

        // Handle parent-child relationship
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + categoryDTO.getParentId()));
            category.setParent(parent);
        }

        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        validateCategoryName(categoryDTO.getName(), id);
        updateCategoryFromDTO(category, categoryDTO);

        // Handle parent-child relationship
        if (categoryDTO.getParentId() != null) {
            // Check for circular dependency
            if (categoryDTO.getParentId().equals(id)) {
                throw new RuntimeException("A category cannot be its own parent");
            }

            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + categoryDTO.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return convertToDTO(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        // Move child categories to parent's level
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        for (Category child : category.getChildren()) {
            child.setParent(category.getParent());
            categoryRepository.save(child);
        }

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

        // Set parent ID if exists
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }

        return dto;
    }

    private CategoryDTO convertToDTOWithChildren(Category category) {
        CategoryDTO dto = convertToDTO(category);

        // Recursively convert children
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryDTO> childDTOs = category.getChildren().stream()
                    .map(this::convertToDTOWithChildren)
                    .collect(Collectors.toList());
            dto.setChildren(childDTOs);
        }

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