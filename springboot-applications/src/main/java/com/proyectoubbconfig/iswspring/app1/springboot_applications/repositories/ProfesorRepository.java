package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, String> {

    // Busca todos los académicos que tienen un rol específico (ej: Rol.TUTOR o Rol.PROFESOR_ASIGNATURA)
    List<Profesor> findByRolesContaining(Rol rol);
}