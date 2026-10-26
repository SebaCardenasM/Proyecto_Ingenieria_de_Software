package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DocumentoRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import java.util.Set;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;


@Service    
public class DocumentoService {
    private static final Set<String> EXTENSIONES_PERMITIDAS =
        Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png");
        
    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String CARPETA_SUBIDAS = "uploads/practicas/";

    public List<Documento> obtenerDocumentosSegunRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rut = auth.getName(); // el username es el RUT (CustomUserDetailsService)

        boolean esCoordinador = tieneRol(auth, "ROLE_COORDINADOR");
        boolean esAcademico = tieneRol(auth, "ROLE_PROFESOR_ASIGNATURA")
                        || tieneRol(auth, "ROLE_TUTOR");

        // Coordinador: ve todo
        if (esCoordinador) {
            return documentoRepository.findAll();
        }

        // Profesor de asignatura o tutor: ve lo de sus estudiantes
        if (esAcademico) {
            return documentoRepository.findByAcademicoRut(rut);
        }

        // Estudiante: solo lo suyo
        return usuarioRepository.findById(rut)
                .filter(u -> u instanceof Estudiante)
                .map(u -> documentoRepository.findByEstudiante((Estudiante) u))
                .orElse(List.of());
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    public Documento subirArchivo(MultipartFile archivo, Integer numeroPractica, TipoDocumento tipoDocumento) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        // --- Nombre y extensión seguros ---
        String limpio = StringUtils.cleanPath(archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename());
        String nombreVisible = limpio.substring(limpio.lastIndexOf('/') + 1); // descarta cualquier ruta
        if (nombreVisible.length() > 150 || nombreVisible.matches(".*[<>\"'`].*")) {
            throw new Exception("El nombre del archivo contiene caracteres no permitidos");
        }
        if (nombreVisible.isBlank()) {
            throw new Exception("El nombre del archivo no es válido");
        }
        String extension = StringUtils.getFilenameExtension(nombreVisible);
        if (extension == null || !EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new Exception("Tipo de archivo no permitido. Formatos aceptados: " + EXTENSIONES_PERMITIDAS);
        }

        String identificador = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findById(identificador)
                .orElseThrow(() -> new Exception("No se encontró el usuario: " + identificador));

        if (!(usuario instanceof Estudiante)) {
            throw new Exception("El usuario autenticado no es un Estudiante.");
        }
        Estudiante estudiante = (Estudiante) usuario;

        Practica practicaEncontrada = null;
        if (estudiante.getPracticas() != null) {
            practicaEncontrada = estudiante.getPracticas().stream()
                    .filter(p -> numeroPractica.equals(p.getNumeroPractica()))
                    .findFirst()
                    .orElse(null);
        }

        // --- Ruta física controlada por nosotros ---
        Path directorio = Paths.get(CARPETA_SUBIDAS).toAbsolutePath().normalize();
        Files.createDirectories(directorio);

        String nombreFisico = UUID.randomUUID() + "." + extension.toLowerCase();
        Path rutaFinal = directorio.resolve(nombreFisico).normalize();
        if (!rutaFinal.startsWith(directorio)) {
            throw new Exception("Ruta de archivo no válida");
        }
        try (var in = archivo.getInputStream()) {
            Files.copy(in, rutaFinal, StandardCopyOption.REPLACE_EXISTING);
        }

        Documento nuevoDocumento;
        if (tipoDocumento == TipoDocumento.PLANIFICACION) {
            nuevoDocumento = new Planificacion();
        } else if (tipoDocumento == TipoDocumento.INFORME_FINAL) {
            nuevoDocumento = new Informe();
        } else if (tipoDocumento == TipoDocumento.PAUTA) {
            nuevoDocumento = new Pauta();
        } else {
            throw new Exception("Tipo de documento no soportado: " + tipoDocumento);
        }

        nuevoDocumento.setNombreArchivo(nombreVisible);                 // lo que ve el usuario
        nuevoDocumento.setRutaServidor(CARPETA_SUBIDAS + nombreFisico); // lo que se sirve por /uploads/**
        nuevoDocumento.setNumeroPractica(numeroPractica);
        nuevoDocumento.setPractica(practicaEncontrada);
        nuevoDocumento.setEstudiante(estudiante);

        return documentoRepository.save(nuevoDocumento);
    }
    // Mantén tu obtenerDocumentosSegunRol() tal cual está...

    public List<Documento> obtenerTodosLosDocumentos() {
        return documentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean puedeVerArchivo(String rutaRelativa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (tieneRol(auth, "ROLE_COORDINADOR")) return true;

        return obtenerDocumentosSegunRol().stream()
                .anyMatch(d -> rutaRelativa.equals(d.getRutaServidor()));
    }
}