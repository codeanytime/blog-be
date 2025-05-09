
package com.blog.config;

import com.blog.interceptor.UserAccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${cors.allowed-origins}")
    private String[] corsAllowedOrigins;

    @Autowired
    private UserAccessInterceptor userAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // This makes the interceptor apply to all request paths
        registry.addInterceptor(userAccessInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/api/test-connection");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // Use patterns instead of exact origins when using allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true) // Enable credentials for proper auth
                .maxAge(3600);
    }
}
