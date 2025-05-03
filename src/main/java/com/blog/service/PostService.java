package com.blog.service;

import com.blog.dto.CategoryDTO;
import com.blog.dto.PostDTO;
import com.blog.dto.UserDTO;
import com.blog.model.Category;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.repository.CategoryRepository;
import com.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Post> posts;
        if (isAdmin) {
            posts = postRepository.findAll(pageable);
        } else {
            posts = postRepository.findAllByPublishedTrue(pageable);
        }

        return posts.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findAllByPublishedTrue(pageable);
    }

    @Transactional(readOnly = true)
    public List<Post> getFeaturedPosts() {
        // Return the most recently published posts as featured
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findAllByPublishedTrue(pageable);
        return posts.getContent();
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> searchPosts(String searchTerm, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Post> posts;
        if (isAdmin) {
            posts = postRepository.adminSearchByTitleOrContentOrTags(searchTerm, pageable);
        } else {
            posts = postRepository.searchByTitleOrContentOrTags(searchTerm, pageable);
        }

        return posts.map(this::convertToDTO);
    }

    /**
     * Search for posts within a specific category
     * @param searchTerm The search term to look for in title, content, or tags
     * @param categoryId The category ID to search within
     * @param pageable Pagination information
     * @return A page of post DTOs that match both the search term and category
     */
    @Transactional(readOnly = true)
    public Page<PostDTO> searchPostsInCategory(String searchTerm, Long categoryId, Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        // Search for posts that match both the search term and belong to the category
        Page<Post> posts;
        if (isAdmin) {
            // For admins, include unpublished posts
            posts = postRepository.searchPostsInCategory(searchTerm, category, pageable);
        } else {
            // For non-admins, only show published posts
            posts = postRepository.searchPublishedPostsInCategory(searchTerm, category, pageable);
        }

        return posts.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<Post> searchPosts(String searchTerm, boolean publishedOnly, Pageable pageable) {
        if (publishedOnly) {
            return postRepository.searchByTitleOrContentOrTags(searchTerm, pageable);
        } else {
            return postRepository.adminSearchByTitleOrContentOrTags(searchTerm, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getPostsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Post> posts;
        if (isAdmin) {
            posts = postRepository.findByCategoriesContaining(category, pageable);
        } else {
            posts = postRepository.findByCategoriesContainingAndPublishedTrue(category, pageable);
        }

        return posts.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public PostDTO getPostById(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));

        if (!post.isPublished() && !isAdmin) {
            throw new EntityNotFoundException("Post not found with id: " + id);
        }

        return convertToDTO(post);
    }

    @Transactional(readOnly = true)
    public PostDTO getPostBySlug(String slug) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with slug: " + slug));

        if (!post.isPublished() && !isAdmin) {
            throw new EntityNotFoundException("Post not found with slug: " + slug);
        }

        return convertToDTO(post);
    }

    @Transactional
    public PostDTO createPost(PostDTO postDTO) {
        Post post = new Post();
        updatePostFromDTO(post, postDTO);

        // Set current user as author
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUserByEmail(currentUserEmail);
        post.setAuthor(currentUser);

        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }

    @Transactional
    public PostDTO updatePost(Long id, PostDTO postDTO) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + id));

        updatePostFromDTO(existingPost, postDTO);
        Post updatedPost = postRepository.save(existingPost);

        return convertToDTO(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("Post not found with id: " + id);
        }

        postRepository.deleteById(id);
    }

    private void updatePostFromDTO(Post post, PostDTO postDTO) {
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setCoverImage(postDTO.getCoverImage());
        post.setTags(postDTO.getTags());
        post.setPublished(postDTO.isPublished());

        // Handle slug creation or update
        String slug = postDTO.getSlug();

        // If slug is not provided, generate from title
        if (slug == null || slug.isEmpty()) {
            // For existing posts, use the post ID to create a more deterministic slug
            if (post.getId() != null) {
                slug = generateSlug(postDTO.getTitle(), post.getId());
            } else {
                // For new posts, use timestamp-based uniqueness
                slug = generateSlug(postDTO.getTitle());
            }
        } else {
            // Check if the provided slug is already in use by another post
            if (post.getId() != null) { // This is an update of an existing post
                if (postRepository.findBySlugAndIdNot(slug, post.getId()).isPresent()) {
                    // If slug is used by another post, generate a new one with the ID
                    slug = generateSlug(postDTO.getTitle(), post.getId());
                }
            } else { // This is a new post
                if (postRepository.findBySlug(slug).isPresent()) {
                    // If slug is already used, generate a new one
                    slug = generateSlug(postDTO.getTitle());
                }
            }
        }

        // Set the slug on the post
        post.setSlug(slug);

        // Update categories
        if (postDTO.getCategories() != null && !postDTO.getCategories().isEmpty()) {
            // Clear existing categories
            post.getCategories().clear();

            // Add all new categories
            for (CategoryDTO categoryDTO : postDTO.getCategories()) {
                if (categoryDTO.getId() != null) {
                    Category category = categoryRepository.findById(categoryDTO.getId())
                            .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryDTO.getId()));
                    post.addCategory(category);
                }
            }
        }
    }

    /**
     * Generates a SEO-friendly unique slug from a title and optional post ID.
     *
     * @param title The title to convert to a slug
     * @param postId Optional post ID to include in the slug for existing posts
     * @return A SEO-friendly slug that is guaranteed to be unique
     */
    private String generateSlug(String title, Long postId) {
        if (title == null || title.isEmpty()) {
            return "";
        }

        // Convert to lowercase
        String baseSlug = title.toLowerCase();

        // Replace spaces with hyphens
        baseSlug = baseSlug.replaceAll("\\s+", "-");

        // Remove special characters and replace with hyphens
        baseSlug = baseSlug.replaceAll("[^a-z0-9-]", "-");

        // Replace multiple consecutive hyphens with a single one
        baseSlug = baseSlug.replaceAll("-+", "-");

        // Remove leading and trailing hyphens
        baseSlug = baseSlug.replaceAll("^-|-$", "");

        // If we have a post ID, include it in the slug to ensure uniqueness
        // This creates more predictable, deterministic slugs
        if (postId != null) {
            String uniqueSlug = baseSlug + "-" + postId;
            // Check if this slug is already taken by another post (safety check)
            if (postRepository.findBySlugAndIdNot(uniqueSlug, postId).isEmpty()) {
                return uniqueSlug;
            }
        }

        // Create a unique slug by adding a timestamp-based suffix if needed
        String uniqueSlug = baseSlug;
        int counter = 1;

        // Loop until we find a unique slug
        while (postRepository.findBySlug(uniqueSlug).isPresent()) {
            // For new posts without an ID, use a timestamp for uniqueness
            long timestamp = System.currentTimeMillis();
            // We'll use a shortened timestamp (last 6 digits) to keep the slug reasonable in length
            String shortTimestamp = String.valueOf(timestamp).substring(Math.max(0, String.valueOf(timestamp).length() - 6));
            uniqueSlug = baseSlug + "-" + shortTimestamp + (counter > 1 ? "-" + counter : "");
            counter++;
        }

        return uniqueSlug;
    }

    /**
     * Overloaded method for backward compatibility.
     */
    private String generateSlug(String title) {
        return generateSlug(title, null);
    }

    public PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setSlug(post.getSlug());
        dto.setCoverImage(post.getCoverImage());
        dto.setTags(post.getTags());
        dto.setPublished(post.isPublished());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());

        if (post.getAuthor() != null) {
            UserDTO authorDTO = new UserDTO();
            authorDTO.setId(post.getAuthor().getId());
            authorDTO.setName(post.getAuthor().getName());
            authorDTO.setEmail(post.getAuthor().getEmail());
            authorDTO.setPictureUrl(post.getAuthor().getPictureUrl());
            dto.setAuthor(authorDTO);
        }

        // Convert categories
        Set<CategoryDTO> categoryDTOs = new HashSet<>();
        if (post.getCategories() != null && !post.getCategories().isEmpty()) {
            for (Category category : post.getCategories()) {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setId(category.getId());
                categoryDTO.setName(category.getName());
                categoryDTO.setSlug(category.getSlug());
                categoryDTO.setDescription(category.getDescription());
                categoryDTO.setDisplayInMenu(category.isDisplayInMenu());
                categoryDTO.setMenuOrder(category.getMenuOrder());
                categoryDTOs.add(categoryDTO);
            }

            // Set primary category - first prefer top-level categories (parent_id is null)
            // Then fall back to any subcategory if none of the categories are top-level
            Category primaryCategory = post.getCategories().stream()
                    .filter(c -> c.getParent() == null) // first get top-level categories
                    .min((c1, c2) -> c1.getMenuOrder().compareTo(c2.getMenuOrder()))
                    .orElseGet(() -> post.getCategories().stream() // if no top-level categories, just use any category
                            .min((c1, c2) -> c1.getMenuOrder().compareTo(c2.getMenuOrder()))
                            .orElse(null));

            if (primaryCategory != null) {
                CategoryDTO primaryCategoryDTO = new CategoryDTO();
                primaryCategoryDTO.setId(primaryCategory.getId());
                primaryCategoryDTO.setName(primaryCategory.getName());
                primaryCategoryDTO.setSlug(primaryCategory.getSlug());
                primaryCategoryDTO.setDescription(primaryCategory.getDescription());
                primaryCategoryDTO.setDisplayInMenu(primaryCategory.isDisplayInMenu());
                primaryCategoryDTO.setMenuOrder(primaryCategory.getMenuOrder());
                dto.setPrimaryCategory(primaryCategoryDTO);
            }
        }

        dto.setCategories(categoryDTOs);
        return dto;
    }
}
