package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    
    // Búsqueda por RUT (al ser la Clave Primaria @Id, también sirve findById)
    Optional<Usuario> findByRut(String rut);

    // Búsqueda por correo
    Optional<Usuario> findByCorreo(String correo);
}