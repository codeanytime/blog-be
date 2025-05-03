package com.blog.controller.pub;

import com.blog.dto.PostDTO;
import com.blog.model.Post;
import com.blog.repository.PostRepository;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController("publicPostsController") // Explicitly name the bean to avoid conflicts
@RequestMapping("/api/public/posts")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class PublicPostController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) Boolean published) {

        try {
            // Set default sorting to createdAt in descending order if not specified
            if (sortBy == null) sortBy = "createdAt";

            Sort sort = Sort.by(sortBy);
            if (sortDirection != null && sortDirection.equalsIgnoreCase("asc")) {
                sort = sort.ascending();
            } else {
                sort = sort.descending();
            }

            Pageable pageable = PageRequest.of(page, size, sort);

            // Use PostService instead to get DTOs (which avoids the Hibernate proxy issues)
            Page<PostDTO> postDTOs = postService.getAllPublishedPosts(pageable)
                    .map(post -> postService.convertToDTO(post));

            return ResponseEntity.ok(postDTOs);
        } catch (Exception e) {
            System.out.println("Error fetching posts: " + e.getMessage());
            e.printStackTrace();

            // Return an empty page if an error occurs
            Pageable pageable = PageRequest.of(page, size);
            Page<PostDTO> emptyPage = Page.empty(pageable);
            return ResponseEntity.ok(emptyPage);
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        try {
            PostDTO postDTO = postService.getPostById(id);
            return ResponseEntity.ok(postDTO);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/slug/{slug}")
    @Transactional(readOnly = true)
    public ResponseEntity<PostDTO> getPostBySlug(@PathVariable String slug) {
        try {
            PostDTO postDTO = postService.getPostBySlug(slug);
            return ResponseEntity.ok(postDTO);
        } catch (Exception e) {
            System.out.println("Error fetching post by slug " + slug + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{categoryId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PostDTO>> getPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostDTO> posts = postService.getPostsByCategory(categoryId, pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<PostDTO>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostDTO> posts = postService.searchPosts(query, pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/featured")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDTO>> getFeaturedPosts() {
        List<PostDTO> featuredPosts = postService.getFeaturedPosts().stream()
                .map(post -> postService.convertToDTO(post))
                .collect(Collectors.toList());
        return ResponseEntity.ok(featuredPosts);
    }

    @GetMapping("/recent")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostDTO>> getRecentPosts(
            @RequestParam(defaultValue = "5") int limit) {

        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<PostDTO> recentPosts = postService.getAllPublishedPosts(pageable)
                .getContent().stream()
                .map(post -> postService.convertToDTO(post))
                .collect(Collectors.toList());

        return ResponseEntity.ok(recentPosts);
    }
}