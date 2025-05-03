package com.blog.dto;

import java.util.ArrayList;
import java.util.List;

public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private boolean displayInMenu;
    private Integer menuOrder;

    // Parent category reference (ID only to avoid circular references)
    private Long parentId;

    // Child categories
    private List<CategoryDTO> children = new ArrayList<>();

    public CategoryDTO() {
    }

    public CategoryDTO(Long id, String name, String slug, String description, boolean displayInMenu, Integer menuOrder, Long parentId, List<CategoryDTO> children) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.displayInMenu = displayInMenu;
        this.menuOrder = menuOrder;
        this.parentId = parentId;
        this.children = children;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisplayInMenu() {
        return displayInMenu;
    }

    public void setDisplayInMenu(boolean displayInMenu) {
        this.displayInMenu = displayInMenu;
    }

    public Integer getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<CategoryDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryDTO> children) {
        this.children = children;
    }
}