//package com.example.demo.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class CorsConfig {
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")  // Allow all endpoints
//                        .allowedOrigins("http://localhost:4200")  // Allow only requests from localhost:4200 (Angular frontend)
//                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allow specified HTTP methods
//                        .allowedHeaders("Authorization", "Content-Type", "*")  // Allow Authorization header for token, plus other headers
//                        .allowCredentials(true);  // Allow sending cookies or credentials like JWT token
//            }
//        };
//    }
//}
