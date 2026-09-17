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
import java.time.LocalDateTime;

@Controller
@RequestMapping("/planificaciones")
public class PlanificacionController {

    @Autowired
    private PlanificacionRepository planificacionRepository;

    private static final String UPLOAD_DIR = "uploads/documentos/"; // Carpeta más genérica

    @PostMapping("/subir")
    public String subirDocumento(@RequestParam("archivo") MultipartFile archivo,
                                 @RequestParam("practicaId") Long practicaId,
                                 @RequestParam("tipoDocumento") String tipoDocumento) {
        
        if (archivo.isEmpty()) return "redirect:/planificaciones/error";

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            // Generar nombre y guardar
            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path rutaFisica = uploadPath.resolve(nombreArchivo);
            Files.write(rutaFisica, archivo.getBytes());

            // Crear el registro con los nuevos atributos del modelo
            Planificacion nuevaPlanificacion = new Planificacion();
            nuevaPlanificacion.setRutaArchivo(rutaFisica.toString());
            nuevaPlanificacion.setTipoDocumento(tipoDocumento); // Ej: "Guía de aprendizaje"
            nuevaPlanificacion.setFechaSubida(LocalDateTime.now()); // Registra el momento exacto
            
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