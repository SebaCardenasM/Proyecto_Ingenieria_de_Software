package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.DocumentoDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.DocumentoService;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    @Autowired
    private DocumentoService documentoService;

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

    @PostMapping("/subir-tutor")
    @Transactional
    public ResponseEntity<?> subirPautaTutor(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("idPractica") Long idPractica) {
        try {
            Documento guardado = documentoService.subirPautaTutor(archivo, idPractica);
            return ResponseEntity.ok(DocumentoDTO.desde(guardado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/eliminar/{id}")
    @Transactional
    public ResponseEntity<?> eliminarDocumento(@PathVariable Long id) {
        try {
            documentoService.eliminarDocumento(id);
            return ResponseEntity.ok().body("Documento eliminado correctamente");
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
        List<Documento> documentos = documentoService.obtenerDocumentosSegunRol().stream()
                .filter(doc -> {
                    if (doc instanceof com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta) {
                        return false;
                    }
                    if (doc.getTipoDocumento() == null) {
                        return true;
                    }
                    String tipo = doc.getTipoDocumento().toString().toUpperCase();
                    return !tipo.contains("PAUTA") && !tipo.contains("EVALUACION");
                })
                .collect(Collectors.toList());
                
        return responder(documentos);
    }

    // Método auxiliar responder
    private ResponseEntity<List<DocumentoDTO>> responder(List<Documento> documentos) {
        if (documentos == null || documentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(documentos.stream().map(DocumentoDTO::desde).toList());
    }
}