
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
                .requestMatchers("/", "/api/test-connection").permitAll() // Allow root and test endpoint
                .requestMatchers("/api/auth/**", "/login", "/oauth2/**").permitAll()
                .requestMatchers("/api/posts", "/api/posts/{id}").permitAll()
                .requestMatchers("/api/posts/public", "/api/posts/public/**").permitAll()
                .requestMatchers("/api/categories/**").permitAll()
                .requestMatchers("/api/menu/**").permitAll()
                .requestMatchers("/api/images/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll() // Allow error page

                // Authenticated endpoints - require login but any role
                .requestMatchers("/api/s3/upload").authenticated()

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

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow all origins to make development easier
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
