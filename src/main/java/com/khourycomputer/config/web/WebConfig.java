package com.khourycomputer.config.web;

import com.khourycomputer.config.storage.ImageStorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImageStorageProperties properties;

    public WebConfig(
            ImageStorageProperties properties
    ) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {
        Path uploadRoot = Path.of(
                properties.getDirectory()
        )
                .toAbsolutePath()
                .normalize();

        String resourceLocation =
                uploadRoot.toUri().toString();

        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}