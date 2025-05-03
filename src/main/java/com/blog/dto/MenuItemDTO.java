package com.blog.dto;

public class MenuItemDTO {
    private Long id;
    private String label;
    private String url;
    private Integer order;
    private String type;

    public MenuItemDTO() {
    }

    public MenuItemDTO(Long id, String label, String url, Integer order, String type) {
        this.id = id;
        this.label = label;
        this.url = url;
        this.order = order;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}