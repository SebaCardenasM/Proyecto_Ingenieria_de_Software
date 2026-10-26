package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Colaborador;

import java.util.List;

@Repository
public interface PracticaRepository extends JpaRepository<Practica, Long> {

    // Buscar prácticas de un estudiante
    List<Practica> findByEstudiante(Estudiante estudiante);

    // Buscar prácticas asignadas a un Profesor / Tutor Supervisor
    List<Practica> findByProfesor(Profesor profesor);

    @Query("SELECT p FROM Practica p " +
       "LEFT JOIN FETCH p.profesor LEFT JOIN FETCH p.tutor LEFT JOIN FETCH p.centroPractica " +
       "WHERE p.estudiante.rut = :rut")
    List<Practica> findPortafolioByRut(@Param("rut") String rut);
}