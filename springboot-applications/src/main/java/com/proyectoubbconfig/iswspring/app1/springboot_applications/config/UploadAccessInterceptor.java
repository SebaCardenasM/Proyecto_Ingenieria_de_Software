package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class UploadAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private DocumentoService documentoService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        response.setHeader("X-Content-Type-Options", "nosniff");

        String uri = request.getRequestURI().substring(request.getContextPath().length());
        String ruta = URLDecoder.decode(uri, StandardCharsets.UTF_8).replaceFirst("^/", "");

        if (documentoService.puedeVerArchivo(ruta)) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}