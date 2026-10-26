package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.DocumentoDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @PostMapping("/subir")
    @Transactional
    public ResponseEntity<?> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("numeroPractica") Integer numeroPractica,
            @RequestParam("tipoDocumento") TipoDocumento tipoDocumento) {
        try {
            Documento guardado = documentoService.subirArchivo(archivo, numeroPractica, tipoDocumento);
            return ResponseEntity.ok(DocumentoDTO.desde(guardado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mis-documentos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DocumentoDTO>> listarDocumentos() {
        return responder(documentoService.obtenerDocumentosSegunRol());
    }

    @GetMapping("/todos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DocumentoDTO>> listarTodosLosDocumentos() {
        return responder(documentoService.obtenerTodosLosDocumentos());
    }

    @GetMapping("/mis-estudiantes-documentos")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DocumentoDTO>> obtenerDocumentosMisEstudiantes() {
        return responder(documentoService.obtenerDocumentosSegunRol());
    }
    // Convierte a DTO y devuelve 204 si no hay nada (el front ya maneja el 204)
    private ResponseEntity<List<DocumentoDTO>> responder(List<Documento> documentos) {
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(documentos.stream().map(DocumentoDTO::desde).toList());
    }
}