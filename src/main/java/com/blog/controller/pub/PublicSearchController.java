package com.blog.controller.pub;

import com.blog.dto.PostDTO;
import com.blog.model.Category;
import com.blog.repository.CategoryRepository;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/public/search")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class PublicSearchController {

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Search posts by text (in title/content) or by category
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PostDTO>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categorySlug) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<PostDTO> posts;

            // If categorySlug is provided, search by category
            if (categorySlug != null && !categorySlug.trim().isEmpty()) {
                Optional<Category> category = categoryRepository.findBySlug(categorySlug);
                if (category.isPresent()) {
                    // If query is also provided, search posts by query within this category
                    if (query != null && !query.trim().isEmpty()) {
                        posts = postService.searchPostsInCategory(query, category.get().getId(), pageable);
                    } else {
                        // Just get posts for this category
                        posts = postService.getPostsByCategory(category.get().getId(), pageable);
                    }
                } else {
                    // Category not found, return empty result
                    posts = Page.empty(pageable);
                }
            } else if (query != null && !query.trim().isEmpty()) {
                // Only query provided, search all posts
                posts = postService.searchPosts(query, pageable);
            } else {
                // Neither query nor category provided, return empty result
                posts = Page.empty(pageable);
            }


            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
