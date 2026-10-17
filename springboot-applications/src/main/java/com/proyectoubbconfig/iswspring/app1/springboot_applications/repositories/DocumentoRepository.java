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

    // 1. Para Estudiantes (Ver solo sus propios documentos por correo)
    @Query("SELECT d FROM Documento d WHERE d.practica.estudiante.correo = :correo")
    List<Documento> findByEstudianteCorreo(@Param("correo") String correo);

    // 2. Para Profesores (Ver documentos de los alumnos que coordina)
    @Query("SELECT d FROM Documento d WHERE d.practica.estudiante.profesorCoordinador.correo = :correoProfesor")
    List<Documento> findByProfesorCorreo(@Param("correoProfesor") String correoProfesor);

    // 3. Filtros avanzados
    @Query("SELECT d FROM Documento d " +
           "JOIN d.practica p " +
           "JOIN p.estudiante e " +
           "WHERE (:busqueda IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "   OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
           "   OR e.rut LIKE CONCAT('%', :busqueda, '%')) " +
           "AND (:numPractica IS NULL OR p.numeroPractica = :numPractica) " +
           "AND (:tipo IS NULL OR d.tipoDocumento = :tipo)")
    List<Documento> buscarConFiltros(
        @Param("busqueda") String busqueda,
        @Param("numPractica") Integer numPractica,
        @Param("tipo") TipoDocumento tipo
    );
}