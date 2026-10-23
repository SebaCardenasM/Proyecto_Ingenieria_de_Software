package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
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

    // Buscar prácticas guiadas por un Colaborador del colegio
    List<Practica> findByColaborador(Colaborador colaborador);

    // Buscar prácticas realizadas en un Centro de Práctica específico
    List<Practica> findByCentroPracticaId(Long centroPracticaId);
}