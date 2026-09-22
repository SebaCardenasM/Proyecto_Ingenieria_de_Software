package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;

// Actualizado a String según el cambio de Luis, manteniendo tu método de búsqueda
public interface EstudianteRepository extends JpaRepository<Estudiante, String> {
    
    // Spring Boot leerá esto como: "Busca el Usuario asociado al Estudiante, y revisa su Correo"
    Estudiante findByUsuarioCorreo(String correo);
    
}