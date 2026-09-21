package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    @Autowired
    private DocumentoService documentoService;

    @PostMapping("/subir")
    public ResponseEntity<?> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("numeroPractica") Integer numeroPractica,
            @RequestParam("tipoDocumento") TipoDocumento tipoDocumento) {
        try {
            Documento guardado = documentoService.subirArchivo(archivo, numeroPractica, tipoDocumento);
            return ResponseEntity.ok(guardado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mis-documentos")
    public ResponseEntity<List<Documento>> listarDocumentos() {
        List<Documento> documentos = documentoService.obtenerDocumentosSegunRol(); 
        
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(documentos);
    }
}