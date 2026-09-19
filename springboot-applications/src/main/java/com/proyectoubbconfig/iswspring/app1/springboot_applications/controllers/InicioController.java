package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // ¡Importante importar esto!
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio(Model model) {
        // "Empaquetamos" variables bajo un nombre clave
        model.addAttribute("titulo", "Plataforma de Prácticas UBB");
        model.addAttribute("mensajeBievenida", "¡El enlace con Thymeleaf funciona a la perfección!");
        model.addAttribute("usuarioActivo", "Limonagrio"); 
        
        return "index"; // Sigue apuntando a index.html
    }
}