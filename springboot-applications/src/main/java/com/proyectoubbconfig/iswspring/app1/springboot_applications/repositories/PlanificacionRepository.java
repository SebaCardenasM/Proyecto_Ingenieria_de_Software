package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;

@Repository
public interface PlanificacionRepository extends JpaRepository<Planificacion, Long> {

    // Planificaciones que le corresponde revisar a un profesor UBB (sin borradores):
    // las de las prácticas donde es el profesor asignado; si la práctica no tiene profesor,
    // las de los estudiantes que coordina.
    @Query("SELECT p FROM Planificacion p " +
           "LEFT JOIN p.practica pr " +
           "LEFT JOIN pr.profesor pp " +
           "LEFT JOIN p.estudiante e " +
           "LEFT JOIN e.profesorCoordinador pc " +
           "WHERE (pp.rut = :rut OR (pp IS NULL AND pc.rut = :rut)) " +
           "AND (p.estado IS NULL OR p.estado <> com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoDocumento.BORRADOR) " +
           "ORDER BY p.fechaSubida DESC")
    List<Planificacion> findParaRevisar(@Param("rut") String rut);

    @Query("SELECT COUNT(p) FROM Planificacion p " +
           "LEFT JOIN p.practica pr " +
           "LEFT JOIN pr.profesor pp " +
           "LEFT JOIN p.estudiante e " +
           "LEFT JOIN e.profesorCoordinador pc " +
           "WHERE (pp.rut = :rut OR (pp IS NULL AND pc.rut = :rut)) " +
           "AND (p.estado IS NULL OR p.estado <> com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoDocumento.BORRADOR) " +
           "AND (p.revisada IS NULL OR p.revisada = false)")
    long contarPendientes(@Param("rut") String rut);
}