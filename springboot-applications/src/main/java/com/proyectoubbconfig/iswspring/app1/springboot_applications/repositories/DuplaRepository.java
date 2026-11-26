package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Dupla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuplaRepository extends JpaRepository<Dupla, Long> {

    @Query("SELECT d FROM Dupla d WHERE d.profesor.rut = :rutProfesor AND d.numeroPractica = :num ORDER BY d.fechaCreacion DESC")
    List<Dupla> findByProfesorRutAndNumeroPractica(@Param("rutProfesor") String rutProfesor, @Param("num") Integer numeroPractica);

    @Query("SELECT d FROM Dupla d WHERE d.numeroPractica = :num ORDER BY d.fechaCreacion DESC")
    List<Dupla> findByNumeroPractica(@Param("num") Integer numeroPractica);

    @Query("SELECT d FROM Dupla d WHERE d.profesor.rut = :rutProfesor ORDER BY d.numeroPractica ASC, d.fechaCreacion DESC")
    List<Dupla> findByProfesorRut(@Param("rutProfesor") String rutProfesor);

    @Query("SELECT d FROM Dupla d WHERE (d.estudiante1.rut = :rut OR d.estudiante2.rut = :rut) AND d.numeroPractica = :num")
    Optional<Dupla> findByEstudianteRutAndNumeroPractica(@Param("rut") String rut, @Param("num") Integer numeroPractica);

    @Query("SELECT d FROM Dupla d WHERE d.practica1.id = :practicaId OR d.practica2.id = :practicaId")
    Optional<Dupla> findByPracticaId(@Param("practicaId") Long practicaId);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Dupla d WHERE (d.estudiante1.rut = :rut OR d.estudiante2.rut = :rut) AND d.numeroPractica = :num")
    boolean existsByEstudianteRutAndNumeroPractica(@Param("rut") String rut, @Param("num") Integer numeroPractica);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Dupla d WHERE d.practica1.id = :practicaId OR d.practica2.id = :practicaId")
    boolean existsByPracticaId(@Param("practicaId") Long practicaId);
}
