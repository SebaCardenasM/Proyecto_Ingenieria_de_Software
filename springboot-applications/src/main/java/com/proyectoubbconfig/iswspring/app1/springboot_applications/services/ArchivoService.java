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
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ArchivoPracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

@Service
public class ArchivoService {

    @Autowired
    private ArchivoPracticaRepository archivoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String CARPETA_SUBIDAS = "uploads/practicas/";

    public List<ArchivoPractica> obtenerMisArchivos() {
        String correoActual = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(correoActual);

        if (usuario != null && usuario.getEstudiante() != null) {
            // Retorna los archivos buscando a través de la lista de prácticas del estudiante
            List<ArchivoPractica> todosMisArchivos = new ArrayList<>();
            for (Practica p : usuario.getEstudiante().getPracticas()) {
                todosMisArchivos.addAll(p.getArchivos()); // Requiere getArchivos() en Practica
            }
            return todosMisArchivos;
        }
        return new ArrayList<>();
    }

    public ArchivoPractica subirArchivo(MultipartFile archivo, Integer numeroPractica, TipoDocumento tipoDocumento) throws Exception {
        if (archivo == null || archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
        }

        // 1. Obtener Usuario y Estudiante autenticado
        String correoActual = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByCorreo(correoActual);

        if (usuario == null || usuario.getEstudiante() == null) {
            throw new Exception("No se encontró el estudiante autenticado.");
        }

        Estudiante estudiante = usuario.getEstudiante();

        // 2. Buscar la práctica correspondiente al estudiante por número
        Practica practicaEncontrada = estudiante.getPracticas().stream()
            .filter(p -> p.getNumeroPractica() != null && p.getNumeroPractica().equals(numeroPractica))
            .findFirst()
            .orElseThrow(() -> new Exception("El estudiante no tiene asignada la Práctica " + numeroPractica));
            
        // 3. Guardar archivo en disco
        Path directorio = Paths.get(CARPETA_SUBIDAS);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String nombreUnico = UUID.randomUUID().toString() + "_" + nombreOriginal;
        Path rutaFinal = directorio.resolve(nombreUnico);

        Files.copy(archivo.getInputStream(), rutaFinal, StandardCopyOption.REPLACE_EXISTING);

        // 4. Instanciar y asociar Practica y TipoDocumento
        ArchivoPractica nuevoArchivo = new ArchivoPractica();
        nuevoArchivo.setNombreArchivo(nombreOriginal);
        nuevoArchivo.setRutaServidor(rutaFinal.toString());
        nuevoArchivo.setTipoDocumento(tipoDocumento);
        nuevoArchivo.setPractica(practicaEncontrada);

        return archivoRepository.save(nuevoArchivo);
    }
}