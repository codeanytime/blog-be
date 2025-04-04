package com.blog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String coverImage;

    private List<String> tags = new ArrayList<>();

    private boolean published = true;

    private UserDTO author;

    private Set<CategoryDTO> categories = new HashSet<>();

    // For backward compatibility and convenience
    private CategoryDTO primaryCategory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
