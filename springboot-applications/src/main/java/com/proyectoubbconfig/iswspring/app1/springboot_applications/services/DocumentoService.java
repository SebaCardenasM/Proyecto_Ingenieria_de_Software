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
        String correo = auth.getName();

        // Si es Coordinador, ve todos los documentos
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINADOR"))) {
            return documentoRepository.findAll();
        } 
        // Si es Profesor, ve los de sus alumnos
        else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"))) {
            return documentoRepository.findByProfesorCorreo(correo);
        } 
        // Si es Estudiante, solo ve los propios
        else {
            return documentoRepository.findByEstudianteCorreo(correo);
        }
    }

    public Documento subirArchivo(MultipartFile archivo, Integer numeroPractica, TipoDocumento tipoDocumento) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        String correoActual = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Usuario usuario = usuarioRepository.findByCorreo(correoActual)
            .orElseThrow(() -> new Exception("No se encontró el usuario con correo: " + correoActual));

        // Debido a la herencia, verificamos si el usuario recuperado es una instancia de Estudiante
        if (!(usuario instanceof Estudiante)) {
            throw new Exception("El usuario autenticado no es un Estudiante.");
        }

        Estudiante estudiante = (Estudiante) usuario;

        Practica practicaEncontrada = estudiante.getPracticas().stream()
            .filter(p -> p.getNumeroPractica() != null && p.getNumeroPractica().equals(numeroPractica))
            .findFirst()
            .orElseThrow(() -> new Exception("El estudiante no tiene asignada la Práctica " + numeroPractica));

        Path directorio = Paths.get(CARPETA_SUBIDAS);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
        Path rutaFinal = directorio.resolve(nombreUnico);

        Files.copy(archivo.getInputStream(), rutaFinal, StandardCopyOption.REPLACE_EXISTING);

        Documento nuevoDocumento = new Documento();
        nuevoDocumento.setNombreArchivo(nombreOriginal);
        // Normaliza a '/' independientemente del Sistema Operativo
        nuevoDocumento.setRutaServidor(rutaFinal.toString().replace("\\", "/"));
        nuevoDocumento.setTipoDocumento(tipoDocumento);
        nuevoDocumento.setPractica(practicaEncontrada);

        return documentoRepository.save(nuevoDocumento);
    }
}