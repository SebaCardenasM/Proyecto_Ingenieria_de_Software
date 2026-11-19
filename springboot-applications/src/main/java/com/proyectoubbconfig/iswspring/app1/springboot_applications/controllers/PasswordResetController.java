package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PasswordResetService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/recuperar")
public class PasswordResetController {

    private static final String S_RUT = "resetRut";   // RUT en proceso (paso 2)
    private static final String S_OK = "resetOk";     // RUT con código verificado (paso 3)

    @Autowired
    private PasswordResetService service;

    // ---------- Paso 1: pedir RUT ----------
    @GetMapping
    public String paso1() {
        return "recuperar";
    }

    @PostMapping
    public String enviarCodigo(@RequestParam String rut, HttpSession session) {
        String rutReal = service.solicitarCodigo(rut);
        session.removeAttribute(S_OK);
        // Misma respuesta exista o no el RUT (evita enumerar usuarios)
        session.setAttribute(S_RUT, rutReal != null ? rutReal : "__inexistente__" + rut);
        return "redirect:/recuperar/verificar";
    }

    // ---------- Paso 2: código ----------
    @GetMapping("/verificar")
    public String paso2(HttpSession session, Model model) {
        String rut = (String) session.getAttribute(S_RUT);
        if (rut == null) return "redirect:/recuperar";
        model.addAttribute("correo", rut.startsWith("__") ? "tu correo registrado" : service.correoEnmascarado(rut));
        return "recuperar-codigo";
    }

    @PostMapping("/verificar")
    public String verificar(@RequestParam String codigo, HttpSession session, Model model) {
        String rut = (String) session.getAttribute(S_RUT);
        if (rut == null) return "redirect:/recuperar";

        if (!rut.startsWith("__") && service.verificarCodigo(rut, codigo)) {
            session.setAttribute(S_OK, rut);
            return "redirect:/recuperar/nueva";
        }
        model.addAttribute("error", "Código incorrecto o vencido.");
        model.addAttribute("correo", rut.startsWith("__") ? "tu correo registrado" : service.correoEnmascarado(rut));
        return "recuperar-codigo";
    }

    // ---------- Paso 3: nueva contraseña ----------
    @GetMapping("/nueva")
    public String paso3(HttpSession session) {
        return session.getAttribute(S_OK) == null ? "redirect:/recuperar" : "recuperar-nueva";
    }

    @PostMapping("/nueva")
    public String restablecer(@RequestParam String password, @RequestParam String confirmar,
                              HttpSession session, Model model) {
        String rut = (String) session.getAttribute(S_OK);
        if (rut == null) return "redirect:/recuperar";

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "recuperar-nueva";
        }
        if (!password.equals(confirmar)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "recuperar-nueva";
        }
        service.restablecer(rut, password);
        session.removeAttribute(S_RUT);
        session.removeAttribute(S_OK);
        return "redirect:/login?restablecida";
    }
}
