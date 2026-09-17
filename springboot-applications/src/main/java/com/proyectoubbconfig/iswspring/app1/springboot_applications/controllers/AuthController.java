package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login"; // Le dice a Spring que busque el archivo login.html
    }
}