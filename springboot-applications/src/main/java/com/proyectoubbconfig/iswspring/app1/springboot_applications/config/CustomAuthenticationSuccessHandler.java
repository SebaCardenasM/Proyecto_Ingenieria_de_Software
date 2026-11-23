package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("Login exitoso para {} con roles {}", authentication.getName(), authentication.getAuthorities());

        String rut = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioRepository.findByRut(rut);
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByRut(rut.replace(".", ""));
        }

        // Si el usuario tiene 2FA activado, requiere el segundo factor antes de acceder a la plataforma
        if (usuarioOpt.isPresent() && usuarioOpt.get().isTwoFactorEnabled()) {
            log.info("Usuario {} requiere segundo factor 2FA", rut);
            request.getSession().setAttribute("2FA_PENDING_RUT", usuarioOpt.get().getRut());
            request.getSession().setAttribute("2FA_VERIFIED", false);
            response.sendRedirect(request.getContextPath() + "/login-2fa");
            return;
        }

        // Sin 2FA: acceso directo
        request.getSession().setAttribute("2FA_VERIFIED", true);
        request.getSession().removeAttribute("2FA_PENDING_RUT");
        response.sendRedirect(request.getContextPath() + "/");
    }
}