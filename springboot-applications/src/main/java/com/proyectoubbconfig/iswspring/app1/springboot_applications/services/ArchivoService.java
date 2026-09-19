package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.ArchivoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ArchivoPracticaRepository;

@Service
public class ArchivoService {

    @Autowired
    private ArchivoPracticaRepository archivoRepository;

    private final String CARPETA_SUBIDAS = "uploads/practicas/";

    public List<ArchivoPractica> obtenerMisArchivos() {
        String rutEstudiante = SecurityContextHolder.getContext().getAuthentication().getName();
        return archivoRepository.findByEstudianteRut(rutEstudiante);
        // ¡Se borró el return unreachable!
    }

    public ArchivoPractica subirArchivo(MultipartFile archivo) throws Exception {
        if (archivo.isEmpty()) {
            throw new Exception("El archivo está vacío");
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
        
        // Y aquí ya podemos retornar guardando en la BD real
        return archivoRepository.save(nuevoArchivo);
    }
}