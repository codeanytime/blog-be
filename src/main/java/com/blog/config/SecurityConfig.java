package com.blog.config;

import com.blog.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                // Public endpoints - no authentication required
                .requestMatchers("/", "/api/test-connection", "/api/health", "/ping").permitAll() // Allow test and health endpoints
                .requestMatchers("/api/auth/**", "/login", "/oauth2/**").permitAll()
                .requestMatchers("/api/posts", "/api/posts/{id}").permitAll()
                .requestMatchers("/api/posts/public", "/api/posts/public/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/menu/**").permitAll()
                .requestMatchers("/api/images/**").permitAll()
                .requestMatchers("/api/public/**").permitAll() // Allow all public endpoints
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll() // Allow error page

                // Authenticated endpoints - require login but any role
                .requestMatchers("/api/s3/upload").authenticated()
                .requestMatchers("/api/profile/**").authenticated()

                // Admin-only endpoints
                .requestMatchers("/api/posts/create", "/api/posts/*/edit", "/api/posts/*/delete").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Any other endpoint requires authentication
                .anyRequest().authenticated();

        // Only configure OAuth2 login if client ID is provided
        // This is needed to make the application startup even without Google credentials
        String googleClientId = System.getenv("GOOGLE_CLIENT_ID");
        if (googleClientId != null && !googleClientId.isEmpty()) {
            http.oauth2Login()
                    .defaultSuccessUrl("/api/auth/oauth2/success")
                    .failureUrl("/api/auth/oauth2/failure");
        }

        // Add custom entry point handler for authentication failure
        http.exceptionHandling(handler -> {
            handler.authenticationEntryPoint((request, response, authException) -> {
                String skipAuthRedirect = request.getHeader("X-Skip-Auth-Redirect");
                if (skipAuthRedirect != null && skipAuthRedirect.equalsIgnoreCase("true")) {
                    // If header is present, return 401 instead of redirecting
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}\n");
                } else {
                    // Otherwise, proceed with normal OAuth redirect
                    response.sendRedirect("/oauth2/authorization/google");
                }
            });
        });

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Use allowedOriginPatterns instead of allowedOrigins when allowCredentials is true
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
