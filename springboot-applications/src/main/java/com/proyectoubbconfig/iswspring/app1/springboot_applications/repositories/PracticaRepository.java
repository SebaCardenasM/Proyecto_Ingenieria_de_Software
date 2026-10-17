package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import java.util.List;

public interface PracticaRepository extends JpaRepository<Practica, Long> {

    List<Practica> findByEstudiante(Estudiante estudiante);
}