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
import java.time.LocalDateTime;

@Controller
@RequestMapping("/informes")
public class InformeController {

    @Autowired
    private InformeRepository informeRepository;

    // Usamos la misma carpeta centralizada para el portafolio del estudiante
    private static final String UPLOAD_DIR = "uploads/documentos/";

    @PostMapping("/subir")
    public String subirInforme(@RequestParam("archivo") MultipartFile archivo,
                               @RequestParam("practicaId") Long practicaId,
                               @RequestParam("tipoDocumento") String tipoDocumento) {
        
        if (archivo.isEmpty()) {
            return "redirect:/informes/error";
        }

        try {
            // 1. Verificar/Crear directorio
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 2. Generar nombre único y guardar físicamente
            String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            Path rutaFisica = uploadPath.resolve(nombreArchivo);
            Files.write(rutaFisica, archivo.getBytes());

            // 3. Crear el registro en la base de datos
            Informe nuevoInforme = new Informe();
            nuevoInforme.setRutaArchivo(rutaFisica.toString());
            nuevoInforme.setTipoDocumento(tipoDocumento); // Ej: "Informe Reflexivo", "Anexo"
            nuevoInforme.setFechaSubida(LocalDateTime.now()); // Sello de tiempo automático
            
            // Asociar a la práctica correspondiente
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