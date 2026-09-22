package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PautaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pautas")
public class PautaController {

    @Autowired
    private PautaRepository pautaRepository;

    @PostMapping("/guardar")
    public String guardarPauta(@ModelAttribute Pauta pauta) {
        // Guardar la pauta (incluye el tipo de documento PAUTA, observaciones y puntaje)
        pautaRepository.save(pauta);
        return "redirect:/"; 
    }
}