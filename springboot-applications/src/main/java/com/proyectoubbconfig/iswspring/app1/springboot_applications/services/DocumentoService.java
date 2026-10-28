package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
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

    @Autowired
    private com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository practicaRepository;

    private final String CARPETA_SUBIDAS = "uploads/practicas/";

    public List<Documento> obtenerDocumentosSegunRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rut = auth.getName(); // el username es el RUT (CustomUserDetailsService)

        boolean esCoordinador = tieneRol(auth, "ROLE_COORDINADOR");
        boolean esProfesorAsignatura = tieneRol(auth, "ROLE_PROFESOR_ASIGNATURA");
        boolean esTutor = tieneRol(auth, "ROLE_TUTOR");

        List<Documento> documentos;

        // Coordinador: ve todo
        if (esCoordinador) {
            documentos = documentoRepository.findAll();
        } 
        // Profesor de asignatura o tutor: ve lo de sus estudiantes
        else if (esProfesorAsignatura || esTutor) {
            documentos = documentoRepository.findByAcademicoRut(rut);
        } 
        // Estudiante: solo lo suyo (excluyendo pautas, ya que van en su propia pestaña)
        else {
            List<Documento> docsEstudiante = usuarioRepository.findById(rut)
                    .filter(u -> u instanceof Estudiante)
                    .map(u -> documentoRepository.findByEstudiante((Estudiante) u))
                    .orElse(List.of());

            return docsEstudiante.stream()
                    .filter(doc -> !(doc instanceof Pauta) && (doc.getTipoDocumento() == null || !doc.getTipoDocumento().name().equals("PAUTA")))
                    .collect(Collectors.toList());
        }

        // SI ES PROFESOR DE ASIGNATURA: Excluimos de forma estricta las PAUTAS de los tutores de su vista de gestor
        if (esProfesorAsignatura && !esTutor) {
            documentos = documentos.stream()
                    .filter(doc -> {
                        if (doc instanceof Pauta) {
                            return false;
                        }
                        if (doc.getTipoDocumento() == null) {
                            return true;
                        }
                        String tipo = doc.getTipoDocumento().toString().toUpperCase();
                        return !tipo.contains("PAUTA") && !tipo.contains("EVALUACION");
                    })
                    .collect(Collectors.toList());
        }

        return documentos;
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

    @Transactional
    public Documento subirPautaTutor(MultipartFile archivo, Long idPractica) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        // --- Validaciones de nombre y extensión ---
        String limpio = StringUtils.cleanPath(archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename());
        String nombreVisible = limpio.substring(limpio.lastIndexOf('/') + 1);
        if (nombreVisible.length() > 150 || nombreVisible.matches(".*[<>\"'`].*")) {
            throw new Exception("El nombre del archivo contiene caracteres no permitidos");
        }
        
        String extension = StringUtils.getFilenameExtension(nombreVisible);
        if (extension == null || !EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new Exception("Tipo de archivo no permitido. Formatos aceptados: " + EXTENSIONES_PERMITIDAS);
        }

        // --- Validaciones de Práctica y Permisos del Tutor ---
        Practica practica = practicaRepository.findById(idPractica)
                .orElseThrow(() -> new Exception("No se encontró la práctica con ID: " + idPractica));

        String rutTutorLogueado = SecurityContextHolder.getContext().getAuthentication().getName();
        if (practica.getTutor() == null || !practica.getTutor().getRut().equals(rutTutorLogueado)) {
            throw new Exception("Acceso denegado: No eres el tutor asignado a esta práctica.");
        }

        // --- Lógica del Límite: Máximo 4 Pautas ---
        long cantidadPautas = practica.getArchivos().stream()
                .filter(d -> d instanceof Pauta || (d.getTipoDocumento() != null && d.getTipoDocumento().name().equals("PAUTA")))
                .count();
                
        if (cantidadPautas >= 4) {
            throw new Exception("No puedes subir más archivos. Se ha alcanzado el límite de 4 pautas.");
        }

        // --- Guardado Físico ---
        Path directorio = Paths.get(CARPETA_SUBIDAS).toAbsolutePath().normalize();
        Files.createDirectories(directorio);

        String nombreFisico = UUID.randomUUID() + "." + extension.toLowerCase();
        Path rutaFinal = directorio.resolve(nombreFisico).normalize();
        
        try (var in = archivo.getInputStream()) {
            Files.copy(in, rutaFinal, StandardCopyOption.REPLACE_EXISTING);
        }

        // --- Registro en Base de Datos ---
        Pauta nuevaPauta = new Pauta();
        nuevaPauta.setNombreArchivo(nombreVisible);
        nuevaPauta.setRutaServidor(CARPETA_SUBIDAS + nombreFisico);
        nuevaPauta.setNumeroPractica(practica.getNumeroPractica());
        nuevaPauta.setPractica(practica);
        nuevaPauta.setEstudiante(practica.getEstudiante());
        nuevaPauta.setTipoDocumento(TipoDocumento.PAUTA); 
        nuevaPauta.setTipoEvaluador(TipoEvaluador.TUTOR_UNIVERSIDAD);

        Usuario usuarioLogueado = usuarioRepository.findById(rutTutorLogueado).orElse(null);
        if (usuarioLogueado instanceof Profesor) {
            nuevaPauta.setProfesorEvaluador((Profesor) usuarioLogueado);
        }

        return documentoRepository.save(nuevaPauta);
    }

    public List<Documento> obtenerTodosLosDocumentos() {
        return documentoRepository.findAll();
    }

  @Transactional(readOnly = true)
    public boolean puedeVerArchivo(String rutaRelativa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (tieneRol(auth, "ROLE_COORDINADOR")) return true;

        String rutUsuario = auth.getName();

        // 1. Si es Estudiante, permitimos ver si el documento está asociado a su perfil
        if (tieneRol(auth, "ROLE_ESTUDIANTE")) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(rutUsuario);
            if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuarioOpt.get();
                List<Documento> docsEstudiante = documentoRepository.findByEstudiante(estudiante);
                boolean esSuyo = docsEstudiante.stream()
                        .anyMatch(d -> d.getRutaServidor() != null && (rutaRelativa.contains(d.getRutaServidor()) || d.getRutaServidor().contains(rutaRelativa)));
                if (esSuyo) return true;
            }
        }

        // 2. Verificamos si el documento pertenece a una práctica supervisada por este tutor
        Optional<Documento> docEncontrado = documentoRepository.findAll().stream()
                .filter(d -> d.getRutaServidor() != null && (rutaRelativa.contains(d.getRutaServidor()) || d.getRutaServidor().contains(rutaRelativa)))
                .findFirst();

        if (docEncontrado.isPresent()) {
            Documento doc = docEncontrado.get();
            if (doc.getPractica() != null && doc.getPractica().getTutor() != null) {
                if (doc.getPractica().getTutor().getRut().equals(rutUsuario)) {
                    return true;
                }
            }
        }

        return obtenerDocumentosSegunRol().stream()
                .anyMatch(d -> d.getRutaServidor() != null && (rutaRelativa.contains(d.getRutaServidor()) || d.getRutaServidor().contains(rutaRelativa)));
    }

    @Transactional
    public void eliminarDocumento(Long idDocumento) throws Exception {
        Documento documento = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new Exception("No se encontró el documento con ID: " + idDocumento));

        // Validar permisos: el coordinador puede borrar todo, o el tutor si es de su práctica
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esCoordinador = tieneRol(auth, "ROLE_COORDINADOR");
        String rutUsuario = auth.getName();

        if (!esCoordinador) {
            if (documento.getPractica() == null || documento.getPractica().getTutor() == null ||
                !documento.getPractica().getTutor().getRut().equals(rutUsuario)) {
                throw new Exception("Acceso denegado: No tienes permisos para eliminar este documento.");
            }
        }

        // 1. Borrar el archivo físico del disco si existe
        try {
            if (documento.getRutaServidor() != null) {
                Path rutaFisica = Paths.get(documento.getRutaServidor()).toAbsolutePath().normalize();
                Files.deleteIfExists(rutaFisica);
            }
        } catch (Exception e) {
            // Si el archivo físico no se encuentra, continuamos para limpiar la BD
        }

        // 2. Eliminar el registro de la base de datos
        documentoRepository.delete(documento);
    }

    public List<Documento> obtenerDocumentosDeEstudiantesParaProfesor() {
        return documentoRepository.findAll().stream()
                .filter(doc -> doc.getTipoDocumento() == null || !doc.getTipoDocumento().name().equals("PAUTA"))
                .collect(Collectors.toList());
    }

    public List<Documento> obtenerDocumentosMisEstudiantes() {
        return documentoRepository.findAll().stream()
                .filter(doc -> doc.getTipoDocumento() == null || !doc.getTipoDocumento().name().equals("PAUTA"))
                .collect(Collectors.toList());
    }

    public List<Documento> obtenerPautasDeEstudiante() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rut = auth.getName();
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(rut);
        if (usuarioOpt.isPresent() && usuarioOpt.get() instanceof Estudiante) {
            Estudiante estudiante = (Estudiante) usuarioOpt.get();
            List<Documento> docs = documentoRepository.findByEstudiante(estudiante);
            return docs.stream()
                    .filter(doc -> doc instanceof Pauta || (doc.getTipoDocumento() != null && doc.getTipoDocumento().name().equals("PAUTA")))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

}