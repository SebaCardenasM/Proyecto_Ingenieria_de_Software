package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.TwoFactorService;

@Controller
public class TwoFactorLoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TwoFactorService twoFactorService;

    @GetMapping("/login-2fa")
    public String mostrarLogin2FA(HttpSession session, Model model) {
        String rut = (String) session.getAttribute("2FA_PENDING_RUT");
        Boolean verificado = (Boolean) session.getAttribute("2FA_VERIFIED");

        if (Boolean.TRUE.equals(verificado)) {
            return "redirect:/";
        }

        if (rut == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                rut = auth.getName();
            } else {
                return "redirect:/login";
            }
        }

        Optional<Usuario> uOpt = usuarioRepository.findByRut(rut);
        if (uOpt.isEmpty()) {
            uOpt = usuarioRepository.findByRut(rut.replace(".", ""));
        }

        if (uOpt.isEmpty() || !uOpt.get().isTwoFactorEnabled()) {
            session.setAttribute("2FA_VERIFIED", true);
            session.removeAttribute("2FA_PENDING_RUT");
            return "redirect:/";
        }

        Usuario usuario = uOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("rut", usuario.getRut());
        model.addAttribute("correoOculto", ocultarCorreo(usuario.getCorreo()));

        return "login-2fa";
    }

    @PostMapping("/login-2fa/verificar")
    public String verificarCodigo(@RequestParam("codigo") String codigo,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        String rut = (String) session.getAttribute("2FA_PENDING_RUT");
        if (rut == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                rut = auth.getName();
            }
        }

        if (rut == null) {
            return "redirect:/login";
        }

        Optional<Usuario> uOpt = usuarioRepository.findByRut(rut);
        if (uOpt.isEmpty()) {
            uOpt = usuarioRepository.findByRut(rut.replace(".", ""));
        }

        if (uOpt.isEmpty()) {
            return "redirect:/login";
        }

        Usuario usuario = uOpt.get();
        boolean valido = twoFactorService.verificar(usuario, codigo);

        if (valido) {
            session.setAttribute("2FA_VERIFIED", true);
            session.removeAttribute("2FA_PENDING_RUT");
            return "redirect:/";
        } else {
            ra.addFlashAttribute("errorMensaje", "El código ingresado es incorrecto o ha expirado. Inténtalo nuevamente.");
            ra.addFlashAttribute("tituloError", "Código 2FA no válido");
            return "redirect:/login-2fa";
        }
    }

    @PostMapping("/login-2fa/enviar-correo")
    public String enviarCodigoAlCorreo(HttpSession session, RedirectAttributes ra) {
        String rut = (String) session.getAttribute("2FA_PENDING_RUT");
        if (rut == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) rut = auth.getName();
        }

        if (rut != null) {
            Optional<Usuario> uOpt = usuarioRepository.findByRut(rut);
            if (uOpt.isEmpty()) uOpt = usuarioRepository.findByRut(rut.replace(".", ""));
            if (uOpt.isPresent()) {
                twoFactorService.enviarCodigoCorreo(uOpt.get());
                ra.addFlashAttribute("mensajeExito", "Hemos enviado un código de 6 dígitos a tu correo: " + ocultarCorreo(uOpt.get().getCorreo()));
                ra.addFlashAttribute("tituloExito", "¡Código enviado!");
            }
        }
        return "redirect:/login-2fa";
    }

    private String ocultarCorreo(String correo) {
        if (correo == null || !correo.contains("@")) return "tu correo institucional";
        String[] partes = correo.split("@");
        String nombre = partes[0];
        String dominio = partes[1];
        if (nombre.length() <= 2) {
            return nombre.charAt(0) + "***@" + dominio;
        }
        return nombre.charAt(0) + "***" + nombre.charAt(nombre.length() - 1) + "@" + dominio;
    }
}
