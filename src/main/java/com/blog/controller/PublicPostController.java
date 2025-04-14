package com.blog.controller;

import com.blog.dto.PostDTO;
import com.blog.model.Post;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts/public")
public class PublicPostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        // Only get published posts
        Page<Post> posts = postService.getAllPublishedPosts(pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        try {
            PostDTO postDTO = postService.getPostById(id);
            if (postDTO != null && postDTO.isPublished()) {
                // Convert DTO to entity for now
                // This is a temporary solution for compatibility
                Post post = new Post();
                post.setId(postDTO.getId());
                post.setTitle(postDTO.getTitle());
                post.setContent(postDTO.getContent());
                post.setCoverImage(postDTO.getCoverImage());
                post.setTags(postDTO.getTags());
                post.setPublished(postDTO.isPublished());
                post.setCreatedAt(postDTO.getCreatedAt());
                post.setUpdatedAt(postDTO.getUpdatedAt());
                return ResponseEntity.ok(post);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Post>> getFeaturedPosts() {
        List<Post> featuredPosts = postService.getFeaturedPosts();
        return ResponseEntity.ok(featuredPosts);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Post>> getRecentPosts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postService.getAllPublishedPosts(pageable);
        return ResponseEntity.ok(posts.getContent());
    }
}
