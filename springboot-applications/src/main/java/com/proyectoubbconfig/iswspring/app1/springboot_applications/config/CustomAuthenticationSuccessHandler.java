package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String targetUrl = "/";

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ESTUDIANTE")) {
                targetUrl = "/estudiante/index";
                break;
            } else if (role.equals("ROLE_PROFESOR")) {
                targetUrl = "/profesor/index";
                break;
            } else if (role.equals("ROLE_COORDINADOR")) {
                targetUrl = "/index"; // o "/"
                break;
            }
        }

        response.sendRedirect(targetUrl);
    }
}