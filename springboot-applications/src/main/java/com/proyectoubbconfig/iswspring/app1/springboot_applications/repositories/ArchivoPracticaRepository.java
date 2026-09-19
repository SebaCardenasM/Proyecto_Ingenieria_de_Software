package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.ArchivoPractica;

@Repository
public interface ArchivoPracticaRepository extends JpaRepository<ArchivoPractica, Long> {
    
    // Spring Data JPA crea la consulta SQL automáticamente:
    // SELECT * FROM archivos_practica WHERE estudiante_rut = ?
    List<ArchivoPractica> findByEstudianteRut(String rut);
}