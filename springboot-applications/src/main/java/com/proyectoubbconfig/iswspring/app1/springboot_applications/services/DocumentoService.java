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

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String CARPETA_SUBIDAS = "uploads/practicas/";

    public List<Documento> obtenerDocumentosSegunRol() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String identificador = auth.getName();

        // Si es Coordinador, ve todos los documentos
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINADOR"))) {
            return documentoRepository.findAll();
        } 
        // Si es Profesor, ve los de sus alumnos
        else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"))) {
            return documentoRepository.findByProfesorCorreo(identificador);
        } 
        // Si es Estudiante, filtramos de manera segura por su entidad
        else {
            Usuario usuario = usuarioRepository.findById(identificador).orElse(null);
            if (usuario == null) {
                usuario = usuarioRepository.findByCorreo(identificador).orElse(null);
            }
            
            if (usuario instanceof Estudiante) {
                return documentoRepository.findByEstudiante((Estudiante) usuario);
            }
            
            return documentoRepository.findByEstudianteCorreo(identificador);
        }
    }

    public Documento subirArchivo(MultipartFile archivo, Integer numeroPractica, TipoDocumento tipoDocumento) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        String identificador = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Buscamos primero por ID (RUT) y como respaldo por correo electrónico
        Usuario usuario = usuarioRepository.findById(identificador).orElse(null);
        if (usuario == null) {
            usuario = usuarioRepository.findByCorreo(identificador)
                .orElseThrow(() -> new Exception("No se encontró el usuario con identificador: " + identificador));
        }

        if (!(usuario instanceof Estudiante)) {
            throw new Exception("El usuario autenticado no es un Estudiante.");
        }

        Estudiante estudiante = (Estudiante) usuario;

        // Búsqueda segura de la práctica del estudiante
        Practica practicaEncontrada = null;
        if (estudiante.getPracticas() != null) {
            practicaEncontrada = estudiante.getPracticas().stream()
                .filter(p -> p.getNumeroPractica() != null && p.getNumeroPractica().equals(numeroPractica))
                .findFirst()
                .orElse(null);
        }

        Path directorio = Paths.get(CARPETA_SUBIDAS);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
        Path rutaFinal = directorio.resolve(nombreUnico);

        Files.copy(archivo.getInputStream(), rutaFinal, StandardCopyOption.REPLACE_EXISTING);

        // Instanciamos la subclase concreta dependiendo del Enum TipoDocumento
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

        // Poblamos los atributos base heredados
        nuevoDocumento.setNombreArchivo(nombreOriginal);
        nuevoDocumento.setRutaServidor(rutaFinal.toString().replace("\\", "/"));
        nuevoDocumento.setNumeroPractica(numeroPractica);
        nuevoDocumento.setPractica(practicaEncontrada); 
        nuevoDocumento.setEstudiante(estudiante);

        return documentoRepository.save(nuevoDocumento);
    }
}