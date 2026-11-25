package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByEstudiante(Estudiante estudiante);

    @Query("SELECT DISTINCT d FROM Documento d LEFT JOIN d.estudiante e LEFT JOIN d.practica p LEFT JOIN p.estudiante pe WHERE e.rut = :rut OR pe.rut = :rut")
    List<Documento> findByEstudianteRut(@Param("rut") String rut);

    @Query("SELECT d FROM Documento d WHERE d.practica.id = :practicaId")
    List<Documento> findByPracticaId(@Param("practicaId") Long practicaId);

    // Búsqueda exacta por ruta física (la usa el interceptor de /uploads/**)
    Optional<Documento> findFirstByRutaServidor(String rutaServidor);

    // Documentos que le corresponden a un académico (por RUT):
    // profesor de la práctica, tutor de la práctica, coordinador del estudiante,
    // o profesor de cualquier práctica del estudiante
    @Query("SELECT DISTINCT d FROM Documento d " +
        "LEFT JOIN d.practica p " +
        "LEFT JOIN p.profesor pp " +
        "LEFT JOIN p.tutor pt " +
        "LEFT JOIN p.estudiante pe " +
        "LEFT JOIN pe.practicas pep " +
        "LEFT JOIN pep.profesor pepp " +
        "LEFT JOIN pep.tutor pept " +
        "LEFT JOIN d.estudiante e " +
        "LEFT JOIN e.profesorCoordinador pc " +
        "LEFT JOIN e.practicas ep " +
        "LEFT JOIN ep.profesor epp " +
        "LEFT JOIN ep.tutor ept " +
        "WHERE pp.rut = :rut OR pt.rut = :rut OR pc.rut = :rut OR epp.rut = :rut OR pepp.rut = :rut OR pept.rut = :rut OR ept.rut = :rut")
    List<Documento> findByAcademicoRut(@Param("rut") String rut);

}