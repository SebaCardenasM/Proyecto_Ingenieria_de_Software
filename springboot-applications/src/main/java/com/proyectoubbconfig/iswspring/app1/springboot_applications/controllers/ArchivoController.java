package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.ArchivoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.ArchivoService;

@RestController
@RequestMapping("/api/estudiante/archivos")
public class ArchivoController {

    @Autowired
    private ArchivoService archivoService;

    @PostMapping("/subir")
    public ResponseEntity<?> subirDocumento(@RequestParam("archivo") MultipartFile archivo) {
        try {
            ArchivoPractica archivoGuardado = archivoService.subirArchivo(archivo);
            return ResponseEntity.status(HttpStatus.CREATED).body(archivoGuardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al subir el documento: " + e.getMessage());
        }
    }

    // Endpoint GET que faltaba para alimentar la tabla de tu frontend
    @GetMapping("/mis-documentos")
    public ResponseEntity<List<ArchivoPractica>> listarDocumentos() {
        // Asegúrate de tener este método implementado en tu ArchivoService
        List<ArchivoPractica> archivos = archivoService.obtenerMisArchivos(); 
        
        if (archivos == null || archivos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Devuelve código 204 como lo maneja tu JS
        }
        return ResponseEntity.ok(archivos);
    }
}