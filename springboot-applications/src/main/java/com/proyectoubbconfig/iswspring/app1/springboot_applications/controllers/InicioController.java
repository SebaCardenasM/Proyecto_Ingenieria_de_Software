package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    @GetMapping("/")
    public String inicio() {
        // Esto le dice a Spring que busque un archivo llamado "index.html" en la carpeta templates
        return "index";
    }
}