package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PlanificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/planificaciones")
public class PlanificacionController {

    @Autowired
    private PlanificacionRepository planificacionRepository;

    private static final String UPLOAD_DIR = "uploads/documentos/";

    @PostMapping("/subir")
    public String subirDocumento(@RequestParam("archivo") MultipartFile archivo,
                                 @RequestParam("practicaId") Long practicaId) {
        
        if (archivo.isEmpty()) return "redirect:/planificaciones/error";

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String nombreUnico = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path rutaFisica = uploadPath.resolve(nombreUnico);
            Files.write(rutaFisica, archivo.getBytes());

            // Instanciamos la subclase concreta
            Planificacion nuevaPlanificacion = new Planificacion();
            nuevaPlanificacion.setNombreArchivo(archivo.getOriginalFilename());
            nuevaPlanificacion.setRutaServidor(rutaFisica.toString());
            
            Practica practica = new Practica();
            practica.setId(practicaId);
            nuevaPlanificacion.setPractica(practica);

            planificacionRepository.save(nuevaPlanificacion);

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/planificaciones/error";
        }

        return "redirect:/planificaciones/exito";
    }
}