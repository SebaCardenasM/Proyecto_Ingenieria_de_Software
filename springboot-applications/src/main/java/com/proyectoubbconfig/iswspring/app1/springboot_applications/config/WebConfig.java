package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UploadAccessInterceptor uploadAccessInterceptor;

    @Autowired
    private TwoFactorInterceptor twoFactorInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(uploadAccessInterceptor).addPathPatterns("/uploads/**");
        registry.addInterceptor(twoFactorInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/login", "/login-2fa/**", "/logout");
    }
}