package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Permite acceder públicamente a los archivos de la carpeta física "uploads/" a través de la URL /uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}