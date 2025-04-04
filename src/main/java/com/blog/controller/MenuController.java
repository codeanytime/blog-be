package com.blog.controller;

import com.blog.dto.CategoryDTO;
import com.blog.dto.MenuItemDTO;
import com.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<MenuItemDTO>> getMenu() {
        List<MenuItemDTO> menuItems = new ArrayList<>();

        // Add Home as the first item
        menuItems.add(new MenuItemDTO(0L, "Home", "/", 0, "home"));

        // Get categories that should be displayed in menu
        List<CategoryDTO> categories = categoryService.getCategoriesForMenu();

        // Convert categories to menu items
        categories.forEach(category -> {
            menuItems.add(new MenuItemDTO(
                    category.getId(),
                    category.getName(),
                    "/category/" + category.getSlug(),
                    category.getMenuOrder(),
                    "category"
            ));
        });

        // Sort by menu order
        menuItems.sort(Comparator.comparingInt(MenuItemDTO::getOrder));

        return ResponseEntity.ok(menuItems);
    }
}