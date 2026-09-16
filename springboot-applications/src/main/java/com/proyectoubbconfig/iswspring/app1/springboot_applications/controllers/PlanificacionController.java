package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PlanificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/planificaciones")
public class PlanificacionController {

    // Inyectamos el repositorio para poder guardar en la base de datos
    @Autowired
    private PlanificacionRepository planificacionRepository;

    // Ruta donde se guardarán físicamente los PDFs en tu computador/servidor
    private static final String UPLOAD_DIR = "uploads/planificaciones/";
    
    // Función para mostrar la página HTML del formulario
    @GetMapping("/subir")
    public String mostrarFormularioSubida() {
        return "subir_planificacion";
    }

    // Función que "Recibe" y "Almacena" la planificación
    @PostMapping("/subir")
    public String subirPlanificacion(@RequestParam("archivoPdf") MultipartFile archivoPdf,
                                     @RequestParam("practicaId") Long practicaId) {
        
        if (archivoPdf.isEmpty()) {
            return "redirect:/planificaciones/error"; // Manejo de error si no hay archivo
        }

        try {
            // 1. Crear la carpeta si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Generar un nombre único para el archivo y guardarlo físicamente
            String nombreArchivo = System.currentTimeMillis() + "_" + archivoPdf.getOriginalFilename();
            Path rutaFisica = uploadPath.resolve(nombreArchivo);
            Files.write(rutaFisica, archivoPdf.getBytes());

            // 3. Guardar el registro en la base de datos (Usando el modelo que validamos)
            Planificacion nuevaPlanificacion = new Planificacion();
            nuevaPlanificacion.setRutaPdf(rutaFisica.toString());
            
            // Aquí simulo la creación de la práctica para asociarla. 
            // Más adelante usaremos el PracticaRepository para buscarla por el practicaId.
            Practica practicaAsociada = new Practica();
            practicaAsociada.setId(practicaId);
            nuevaPlanificacion.setPractica(practicaAsociada);

            // Guardamos oficialmente en la base de datos MySQL
            planificacionRepository.save(nuevaPlanificacion);

            System.out.println("Planificación guardada exitosamente en: " + rutaFisica.toString());

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/planificaciones/error";
        }

        return "redirect:/planificaciones/exito";
    }
}