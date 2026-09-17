package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SpringbootApplicationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplicationsApplication.class, args);
    }

    // Este Bean se ejecuta automáticamente cada vez que arranca tu aplicación
    @Bean
    public CommandLineRunner crearUsuariosPrueba(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            
            // 1. Crear Usuario Estudiante (Tu cuenta)
            if (usuarioRepository.findByCorreo("sebastian.cardenas2301@alumnos.ubiobio.cl") == null) {
                Usuario estudiante = new Usuario();
                estudiante.setCorreo("sebastian.cardenas2301@alumnos.ubiobio.cl");
                estudiante.setPassword(passwordEncoder.encode("seba67")); // Se encripta la contraseña
                estudiante.setRol("ROLE_ESTUDIANTE"); 
                
                usuarioRepository.save(estudiante);
                System.out.println("✅ Usuario ESTUDIANTE creado con éxito en MySQL.");
            }

            // 2. Crear Usuario Administrador (Coordinador de prácticas)
            if (usuarioRepository.findByCorreo("coordinador@ubiobio.cl") == null) {
                Usuario admin = new Usuario();
                admin.setCorreo("coordinador@ubiobio.cl");
                admin.setPassword(passwordEncoder.encode("admin123")); // Contraseña genérica para el admin
                admin.setRol("ROLE_ADMIN"); 
                
                usuarioRepository.save(admin);
                System.out.println("✅ Usuario ADMINISTRADOR creado con éxito en MySQL.");
            }
        };
    }
}