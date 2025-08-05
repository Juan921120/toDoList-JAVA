package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.image-path:/uploads/images/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置图片访问路径
        // 访问路径：http://localhost:8080/images/2024/01/15/xxx.jpg
        // 实际路径：/uploads/images/2024/01/15/xxx.jpg
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);
    }
}