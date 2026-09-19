package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ArchivoViewController {

    // Cambiamos la ruta para que coincida con el enlace de tu index.html (/gestor-archivos)
    @GetMapping("/gestor-archivos")
    public String mostrarGestorArchivos() {
        return "gestor-archivos"; // Renderiza el archivo gestor-archivos.html desde templates/
    }
}