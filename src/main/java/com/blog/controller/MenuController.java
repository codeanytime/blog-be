package com.blog.controller;

import com.blog.dto.CategoryDTO;
import com.blog.dto.MenuItemDTO;
import com.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
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

        // Add About item
        menuItems.add(new MenuItemDTO(null, "About", "/about", 100, "page"));

        // Add Admin item for administrators only
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {

            // Check for admin role
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Add Profile for any authenticated user
            menuItems.add(new MenuItemDTO(null, "Profile", "/profile", 101, "auth"));

            // Add Admin Dashboard link for admins
            if (isAdmin) {
                menuItems.add(new MenuItemDTO(null, "Admin", "/admin", 102, "admin"));
            }

            // Add Logout for any authenticated user
            menuItems.add(new MenuItemDTO(null, "Logout", "/logout", 103, "auth"));
        } else {
            // Add Login/Register for unauthenticated users
            menuItems.add(new MenuItemDTO(null, "Login / Register", "/auth", 101, "auth"));
        }

        // Sort by menu order
        menuItems.sort(Comparator.comparing(item -> item.getOrder() != null ? item.getOrder() : 0));

        return ResponseEntity.ok(menuItems);
    }
}