package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.ArchivoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ArchivoPracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

@Service
public class ArchivoService {

    @Autowired
    private ArchivoPracticaRepository archivoRepository;

    // 👇 INYECTAMOS EL REPOSITORIO DE USUARIO 👇
    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String CARPETA_SUBIDAS = "uploads/practicas/";

    public List<ArchivoPractica> obtenerMisArchivos() {
        // Obtenemos el correo del usuario logueado
        String correoActual = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(correoActual);
        
        if (usuario != null) {
            // Buscamos los archivos usando el RUT real del usuario
            return archivoRepository.findByEstudianteRut(usuario.getRut());
        }
        return new ArrayList<>();
    }

    public ArchivoPractica subirArchivo(MultipartFile archivo) throws Exception {
        if (archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        // 👇 1. BUSCAMOS AL USUARIO LOGUEADO ACTUALMENTE 👇
        String correoActual = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(correoActual);

        if (usuario == null) {
            throw new Exception("No se pudo identificar al usuario autenticado.");
        }

        Path directorio = Paths.get(CARPETA_SUBIDAS);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
        Path rutaFinal = directorio.resolve(nombreUnico);

        Files.copy(archivo.getInputStream(), rutaFinal, StandardCopyOption.REPLACE_EXISTING);

        ArchivoPractica nuevoArchivo = new ArchivoPractica();
        nuevoArchivo.setNombreArchivo(nombreOriginal);
        nuevoArchivo.setRutaServidor(rutaFinal.toString());
        
        // 👇 2. ASIGNAMOS EL USUARIO AL ARCHIVO ANTES DE GUARDAR 👇
        nuevoArchivo.setEstudiante(usuario);

        return archivoRepository.save(nuevoArchivo);
    }
}