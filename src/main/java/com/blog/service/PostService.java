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

        // Generate or update slug
        String slug = postDTO.getSlug();
        // If slug is not provided, generate from title
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(postDTO.getTitle());
        }
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
     * Generates a SEO-friendly slug from a title.
     *
     * @param title The title to convert to a slug
     * @return A SEO-friendly slug
     */
    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }

        // Convert to lowercase
        String slug = title.toLowerCase();

        // Replace spaces with hyphens
        slug = slug.replaceAll("\\s+", "-");

        // Remove special characters and replace with hyphens
        slug = slug.replaceAll("[^a-z0-9-]", "-");

        // Replace multiple consecutive hyphens with a single one
        slug = slug.replaceAll("-+", "-");

        // Remove leading and trailing hyphens
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }

    private PostDTO convertToDTO(Post post) {
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

            // Set primary category (first one by menu order)
            post.getCategories().stream()
                    .min((c1, c2) -> c1.getMenuOrder().compareTo(c2.getMenuOrder()))
                    .ifPresent(primaryCategory -> {
                        CategoryDTO primaryCategoryDTO = new CategoryDTO();
                        primaryCategoryDTO.setId(primaryCategory.getId());
                        primaryCategoryDTO.setName(primaryCategory.getName());
                        primaryCategoryDTO.setSlug(primaryCategory.getSlug());
                        primaryCategoryDTO.setDescription(primaryCategory.getDescription());
                        primaryCategoryDTO.setDisplayInMenu(primaryCategory.isDisplayInMenu());
                        primaryCategoryDTO.setMenuOrder(primaryCategory.getMenuOrder());
                        dto.setPrimaryCategory(primaryCategoryDTO);
                    });
        }

        dto.setCategories(categoryDTOs);
        return dto;
    }
}
