package com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByCorreo(String correo); // Spring Boot hace la consulta SQL por ti
}