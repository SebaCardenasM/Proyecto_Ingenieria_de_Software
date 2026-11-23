package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TwoFactorInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // Rutas excluidas de la verificación intermedia de 2FA
        if (path.startsWith("/login-2fa") || path.startsWith("/logout") || path.equals("/login")
                || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Boolean verified = (Boolean) session.getAttribute("2FA_VERIFIED");
                String pendingRut = (String) session.getAttribute("2FA_PENDING_RUT");
                if (pendingRut != null && !Boolean.TRUE.equals(verified)) {
                    response.sendRedirect(contextPath + "/login-2fa");
                    return false;
                }
            }
        }

        return true;
    }
}
