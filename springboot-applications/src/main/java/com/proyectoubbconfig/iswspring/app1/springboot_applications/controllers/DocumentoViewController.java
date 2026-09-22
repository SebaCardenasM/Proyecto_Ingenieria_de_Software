package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocumentoViewController {

    @GetMapping("/gestor-archivos")
    public String mostrarGestorArchivos() {
        return "gestor-archivos"; 
    }
}