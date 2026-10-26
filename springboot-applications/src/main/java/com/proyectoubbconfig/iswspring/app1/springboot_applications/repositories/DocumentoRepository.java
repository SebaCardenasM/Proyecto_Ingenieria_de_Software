package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoDocumento;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    // Método para buscar documentos pasando la entidad Estudiante directamente
    List<Documento> findByEstudiante(Estudiante estudiante);


    // 2. Para Profesores (Ver documentos de los alumnos que coordina)
    // Documentos que le corresponden a un académico (por RUT):
    // profesor de la práctica, tutor de la práctica o coordinador del estudiante
    @Query("SELECT DISTINCT d FROM Documento d " +
        "LEFT JOIN d.practica p " +
        "LEFT JOIN p.profesor pp " +
        "LEFT JOIN p.tutor pt " +
        "LEFT JOIN d.estudiante e " +
        "LEFT JOIN e.profesorCoordinador pc " +
        "WHERE pp.rut = :rut OR pt.rut = :rut OR pc.rut = :rut")
    List<Documento> findByAcademicoRut(@Param("rut") String rut);

}