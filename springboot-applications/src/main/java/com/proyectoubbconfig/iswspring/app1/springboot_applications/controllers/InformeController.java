package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.InformeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/informes")
public class InformeController {

    @Autowired
    private InformeRepository informeRepository;

    private static final String UPLOAD_DIR = "uploads/documentos/";

    @PostMapping("/subir")
    public String subirInforme(@RequestParam("archivo") MultipartFile archivo,
                               @RequestParam("practicaId") Long practicaId) {
        
        if (archivo.isEmpty()) {
            return "redirect:/informes/error";
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String nombreUnico = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path rutaFisica = uploadPath.resolve(nombreUnico);
            Files.write(rutaFisica, archivo.getBytes());

            // Instanciamos la subclase concreta
            Informe nuevoInforme = new Informe();
            nuevoInforme.setNombreArchivo(archivo.getOriginalFilename());
            nuevoInforme.setRutaServidor(rutaFisica.toString());
            
            Practica practica = new Practica();
            practica.setId(practicaId);
            nuevoInforme.setPractica(practica);

            informeRepository.save(nuevoInforme);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/informes/error";
        }

        return "redirect:/informes/exito";
    }
}