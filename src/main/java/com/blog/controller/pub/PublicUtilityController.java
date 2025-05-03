package com.blog.controller.pub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.model.Category;
import com.blog.model.Post;
import com.blog.repository.CategoryRepository;
import com.blog.repository.PostRepository;
import com.blog.service.PostService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/utility")
public class PublicUtilityController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostService postService;

    /**
     * Update the relationship between a post and a category
     *
     * @param postId The ID of the post
     * @param categoryId The ID of the category
     * @param operation The operation to perform: 'add', 'remove', or 'set-primary'
     * @return A response entity with the result of the operation
     */
    @GetMapping("/update-post-category/{postId}/{categoryId}/{operation}")
    public ResponseEntity<?> updatePostCategory(
            @PathVariable Long postId,
            @PathVariable Long categoryId,
            @PathVariable String operation) {

        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);

        if (postOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "error",
                            "message", "Post not found",
                            "postId", postId,
                            "categoryId", categoryId
                    )
            );
        }

        if (categoryOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "status", "error",
                            "message", "Category not found",
                            "postId", postId,
                            "categoryId", categoryId
                    )
            );
        }

        Post post = postOpt.get();
        Category category = categoryOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("postId", postId);
        response.put("categoryId", categoryId);

        switch (operation.toLowerCase()) {
            case "add":
                if (!post.getCategories().contains(category)) {
                    post.getCategories().add(category);
                    postRepository.save(post);
                    response.put("status", "success");
                    response.put("message", "Category added to post");
                } else {
                    response.put("status", "info");
                    response.put("message", "Category already associated with post");
                }
                break;

            case "remove":
                if (post.getCategories().contains(category)) {
                    post.getCategories().remove(category);

                    // If we're removing the primary category, clear it
                    if (post.getPrimaryCategory() != null && post.getPrimaryCategory().getId().equals(categoryId)) {
                        post.setPrimaryCategory(null);
                    }

                    postRepository.save(post);
                    response.put("status", "success");
                    response.put("message", "Category removed from post");
                } else {
                    response.put("status", "info");
                    response.put("message", "Category not associated with post");
                }
                break;

            case "set-primary":
                // Only set as primary if it's already a category of the post
                if (post.getCategories().contains(category)) {
                    post.setPrimaryCategory(category);
                    postRepository.save(post);
                    response.put("status", "success");
                    response.put("message", "Primary category updated");
                } else {
                    // Add the category first, then set as primary
                    post.getCategories().add(category);
                    post.setPrimaryCategory(category);
                    postRepository.save(post);
                    response.put("status", "success");
                    response.put("message", "Category added and set as primary");
                }
                break;

            default:
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "status", "error",
                                "message", "Invalid operation: " + operation + ". Must be 'add', 'remove', or 'set-primary'",
                                "postId", postId,
                                "categoryId", categoryId
                        )
                );
        }

        return ResponseEntity.ok(response);
    }
}
