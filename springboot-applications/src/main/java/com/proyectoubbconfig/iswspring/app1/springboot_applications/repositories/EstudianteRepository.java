package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    
    // Spring Boot leerá esto como: "Busca el Usuario asociado al Estudiante, y revisa su Correo"
    Estudiante findByUsuarioCorreo(String correo);
    
}