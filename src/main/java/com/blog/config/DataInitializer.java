package com.blog.config;

import com.blog.model.Category;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.repository.CategoryRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Starting data initialization...");

        // Count existing categories to determine if we should initialize data
        long categoryCount = categoryRepository.count();
        long postCount = postRepository.count();
        long userCount = userRepository.count();

        System.out.println("Current database state: " + categoryCount + " categories, " +
                postCount + " posts, " + userCount + " users");

        // Check if we should initialize users
        User adminUser;
        User regularUser;

        if (userCount == 0) {
            System.out.println("Initializing users...");
            // Create required admin user with specified credentials
            adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setName("Admin User");
            adminUser.setEmail("admin@example.com");
            adminUser.setPictureUrl("https://randomuser.me/api/portraits/men/1.jpg");
            adminUser.setRole("ADMIN");
            // Using the exact password hash for 'admin123456@'
            adminUser.setPassword("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG");
            adminUser = userRepository.save(adminUser);
            System.out.println("Created admin user with username: 'admin' and password: 'admin123456@'");

            // Create sample regular user
            regularUser = new User();
            regularUser.setUsername("user@example.com");
            regularUser.setName("Regular User");
            regularUser.setEmail("user@example.com");
            regularUser.setPictureUrl("https://randomuser.me/api/portraits/women/1.jpg");
            regularUser.setRole("USER");
            regularUser = userRepository.save(regularUser);
            System.out.println("Created regular user");
        } else {
            System.out.println("Skipping user initialization - users already exist");
            // Get existing users
            adminUser = userRepository.findByUsername("admin").orElse(null);
            regularUser = userRepository.findByUsername("user@example.com").orElse(null);
        }

        // Skip post and category creation if they already exist
        if (postCount == 0 && categoryCount == 0) {
            System.out.println("Creating categories and posts - first time initialization");
            // Create sample blog posts
            Post post1 = createPost(
                    adminUser,
                    "Getting Started with Spring Boot",
                    "spring-boot-intro",
                    "<h2>Introduction to Spring Boot</h2><p>Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can \"just run\".</p><p>We take an opinionated view of the Spring platform and third-party libraries so you can get started with minimum fuss. Most Spring Boot applications need minimal Spring configuration.</p><h3>Features</h3><ul><li>Create stand-alone Spring applications</li><li>Embed Tomcat, Jetty or Undertow directly (no need to deploy WAR files)</li><li>Provide opinionated 'starter' dependencies to simplify your build configuration</li><li>Automatically configure Spring and 3rd party libraries whenever possible</li><li>Provide production-ready features such as metrics, health checks, and externalized configuration</li><li>Absolutely no code generation and no requirement for XML configuration</li></ul>",
                    "https://springframework.guru/wp-content/uploads/2017/10/spring-boot-logo.png",
                    LocalDateTime.now().minusDays(10)
            );

            Post post2 = createPost(
                    adminUser,
                    "React Hooks: A Complete Guide",
                    "react-hooks-guide",
                    "<h2>Understanding React Hooks</h2><p>Hooks are a new addition in React 16.8. They let you use state and other React features without writing a class.</p><p>Hooks are functions that let you \"hook into\" React state and lifecycle features from function components. Hooks don't work inside classes — they let you use React without classes.</p><h3>Basic Hooks</h3><ul><li>useState - Returns a stateful value, and a function to update it.</li><li>useEffect - Perform side effects in function components.</li><li>useContext - Accept a context object and return the current context value.</li></ul><h3>Additional Hooks</h3><ul><li>useReducer - useState alternative for complex state logic.</li><li>useCallback - Returns a memoized callback.</li><li>useMemo - Returns a memoized value.</li><li>useRef - Returns a mutable ref object.</li></ul>",
                    "https://miro.medium.com/max/1200/1*-Ijet6kVJqGgul6adezDLQ.png",
                    LocalDateTime.now().minusDays(5)
            );

            Post post3 = createPost(
                    regularUser,
                    "The Power of AWS S3 for Storage",
                    "aws-s3-storage",
                    "<h2>Amazon S3: Simple Storage Service</h2><p>Amazon Simple Storage Service (Amazon S3) is an object storage service that offers industry-leading scalability, data availability, security, and performance.</p><p>This means customers of all sizes and industries can use it to store and protect any amount of data for a range of use cases, such as websites, mobile applications, backup and restore, archive, enterprise applications, IoT devices, and big data analytics.</p><h3>Key Features</h3><ul><li>Industry-leading performance, scalability, availability, and durability</li><li>Wide range of cost-effective storage classes</li><li>Unmatched security, compliance, and audit capabilities</li><li>Easily manage data and access controls</li><li>Query-in-place and process on-request</li><li>Most supported cloud storage service</li></ul>",
                    "https://media.amazonwebservices.com/blog/2017/s3_lifecycle_list.png",
                    LocalDateTime.now().minusDays(2)
            );

            Post post4 = createPost(
                    adminUser,
                    "Securing Your Spring Boot Application",
                    "spring-security-guide",
                    "<h2>Spring Security Fundamentals</h2><p>Spring Security is a powerful and highly customizable authentication and access-control framework. It is the de-facto standard for securing Spring-based applications.</p><p>Spring Security is a framework that focuses on providing both authentication and authorization to Java applications. Like all Spring projects, the real power of Spring Security is found in how easily it can be extended to meet custom requirements.</p><h3>Main Features</h3><ul><li>Comprehensive and extensible support for both Authentication and Authorization</li><li>Protection against attacks like session fixation, clickjacking, cross site request forgery, etc</li><li>Servlet API integration</li><li>Optional integration with Spring Web MVC</li><li>Support for CORS (Cross-Origin Resource Sharing)</li></ul><h3>Implementation Strategies</h3><p>For most applications, security requirements will be addressed by the following configurations:</p><ol><li>HTTP Basic authentication</li><li>Form-based authentication</li><li>JSON Web Token (JWT) authentication</li><li>OAuth 2.0 / OpenID Connect</li></ol>",
                    "https://miro.medium.com/max/792/1*AZks36mui8ODxTXJUhydow.png",
                    LocalDateTime.now().minusHours(12)
            );

            Post post5 = createPost(
                    regularUser,
                    "Google Authentication for Web Applications",
                    "google-auth-integration",
                    "<h2>Implementing Google Sign-In</h2><p>Google Sign-In is a secure authentication system that reduces the burden of login for your users, by enabling them to sign in with their Google Account—the same account they already use with Gmail, Play, and other Google services.</p><p>Google Sign-In is built on OAuth 2.0 and OpenID Connect, and provides users with seamless experiences across devices and platforms, and excellent security.</p><h3>Benefits of Google Sign-In</h3><ul><li>Reduced friction during sign-up/sign-in: Users don't need to create and remember a new username and password.</li><li>Security: Google accounts have industry-leading security including 2-step verification, suspicious login detection, and more.</li><li>Cross-platform: Works on Android, iOS, and web applications.</li><li>Control over shared information: Users can easily see and manage what information they share.</li></ul><h3>Implementation Steps</h3><ol><li>Create a project in the Google Developer Console</li><li>Configure the OAuth consent screen</li><li>Create OAuth 2.0 credentials</li><li>Implement the sign-in button and authentication flow</li><li>Verify the Google-signed ID token on your server</li></ol>",
                    "https://developers.google.com/identity/images/btn_google_signin_dark_normal_web.png",
                    LocalDateTime.now().minusHours(5)
            );

            System.out.println("Successfully created all posts individually");

            // Create main categories (level 1) matching the expected frontend categories
            Category backendCategory = createCategory("Backend Development", "backend", "Articles about server-side technologies and development", true, 1, null);
            Category devopsCategory = createCategory("DevOps", "devops", "Deployment, CI/CD, and infrastructure topics", true, 2, null);
            Category databaseCategory = createCategory("Database", "database", "SQL, NoSQL, and data management", true, 3, null);
            Category frontendCategory = createCategory("Frontend Development", "frontend", "Client-side technologies and frameworks", true, 4, null);

            // Save main categories
            backendCategory = categoryRepository.save(backendCategory);
            devopsCategory = categoryRepository.save(devopsCategory);
            databaseCategory = categoryRepository.save(databaseCategory);
            frontendCategory = categoryRepository.save(frontendCategory);

            // Create subcategories
            // Backend subcategories
            Category javaCategory = createCategory("Java", "java", "Java programming language and frameworks", true, 1, backendCategory);
            Category pythonCategory = createCategory("Python", "python", "Python programming and frameworks", true, 2, backendCategory);
            Category nodeJsCategory = createCategory("Node.js", "nodejs", "JavaScript on the server", true, 3, backendCategory);

            // DevOps subcategories
            Category dockerCategory = createCategory("Docker", "docker", "Containerization with Docker", true, 1, devopsCategory);
            Category kubernetesCategory = createCategory("Kubernetes", "kubernetes", "Container orchestration", true, 2, devopsCategory);
            Category cicdCategory = createCategory("CI/CD", "cicd", "Continuous Integration and Deployment", true, 3, devopsCategory);

            // Frontend subcategories
            Category reactCategory = createCategory("React", "react", "React library and ecosystem", true, 1, frontendCategory);

            // Save all subcategories
            javaCategory = categoryRepository.save(javaCategory);
            pythonCategory = categoryRepository.save(pythonCategory);
            nodeJsCategory = categoryRepository.save(nodeJsCategory);
            dockerCategory = categoryRepository.save(dockerCategory);
            kubernetesCategory = categoryRepository.save(kubernetesCategory);
            cicdCategory = categoryRepository.save(cicdCategory);
            reactCategory = categoryRepository.save(reactCategory);

            // Create category assignments for posts
            // First post: Spring Boot - Java as primary category, also in Backend
            Post springBootPost = postRepository.findById(1L).orElseThrow();
            springBootPost.addCategory(javaCategory);    // Primary category
            springBootPost.addCategory(backendCategory);

            // Second post: React Hooks - React as primary category, also in Frontend
            Post reactHooksPost = postRepository.findById(2L).orElseThrow();
            reactHooksPost.addCategory(reactCategory);   // Primary category
            reactHooksPost.addCategory(frontendCategory);

            // Third post: AWS S3 - Docker as primary category, also in DevOps
            Post awsS3Post = postRepository.findById(3L).orElseThrow();
            awsS3Post.addCategory(dockerCategory);      // Primary category
            awsS3Post.addCategory(devopsCategory);

            // Fourth post: Spring Security - Java as primary category, also in Backend
            Post springSecurity = postRepository.findById(4L).orElseThrow();
            springSecurity.addCategory(javaCategory);   // Primary category
            springSecurity.addCategory(backendCategory);

            // Fifth post: Google Auth - Frontend as primary category
            Post googleAuthPost = postRepository.findById(5L).orElseThrow();
            googleAuthPost.addCategory(frontendCategory);  // Primary category

            // Save all updated posts with categories
            System.out.println("Saving posts with categories...");
            postRepository.save(springBootPost);
            System.out.println("Saved Spring Boot post with categories: " + springBootPost.getCategories().size());

            postRepository.save(reactHooksPost);
            System.out.println("Saved React Hooks post with categories: " + reactHooksPost.getCategories().size());

            postRepository.save(awsS3Post);
            System.out.println("Saved AWS S3 post with categories: " + awsS3Post.getCategories().size());

            postRepository.save(springSecurity);
            System.out.println("Saved Spring Security post with categories: " + springSecurity.getCategories().size());

            postRepository.save(googleAuthPost);
            System.out.println("Saved Google Auth post with categories: " + googleAuthPost.getCategories().size());

            System.out.println("Sample data initialized successfully!");
        }
    }

    private Post createPost(User author, String title, String slug, String content, String coverImage, LocalDateTime createdAt) {
        try {
            Post post = new Post();
            post.setAuthor(author);
            post.setTitle(title);
            post.setSlug(slug);
            post.setContent(content);
            post.setCoverImage(coverImage);
            post.setCreatedAt(createdAt);
            post.setUpdatedAt(createdAt);
            post.setPublished(true);

            System.out.println("Creating post: " + title);
            Post saved = postRepository.save(post);
            System.out.println("Post saved successfully with ID: " + saved.getId());
            // Validate saved post by loading it back
            Post loaded = postRepository.findById(saved.getId()).orElse(null);
            if (loaded == null) {
                System.out.println("ERROR: Post was saved but cannot be loaded back! ID: " + saved.getId());
            } else {
                System.out.println("Validated post exists in database with ID: " + loaded.getId());
            }
            return saved;
        } catch (Exception e) {
            System.out.println("ERROR creating post: " + title);
            e.printStackTrace();
            throw e; // Re-throw to properly handle the exception
        }
    }

    private Category createCategory(String name, String slug, String description, boolean displayInMenu, Integer menuOrder, Category parent) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setDisplayInMenu(displayInMenu);
        category.setMenuOrder(menuOrder);
        category.setParent(parent);
        return category;
    }
}