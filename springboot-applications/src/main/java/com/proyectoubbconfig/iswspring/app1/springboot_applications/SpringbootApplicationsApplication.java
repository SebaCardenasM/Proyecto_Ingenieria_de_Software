package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
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
                // 1. Crear Usuario
                Usuario usuario = new Usuario();
                usuario.setRut("11.111.111-1");
                usuario.setNombre("Sebastián");
                usuario.setApellido("Cárdenas");
                usuario.setCorreo("sebastian.cardenas2301@alumnos.ubiobio.cl");
                usuario.setPassword(passwordEncoder.encode("seba67"));
                usuario.setRol("ROLE_ESTUDIANTE");

                // 2. Crear Estudiante
                Estudiante estudiante = new Estudiante();
                estudiante.setAnoIngreso(2022);
                estudiante.setPracticaActual(1);

                // 3. Establecer la relación bidireccional
                estudiante.setUsuario(usuario);
                usuario.setEstudiante(estudiante);

                // 4. Guardar Usuario (gracias a CascadeType.ALL se guarda también el Estudiante)
                usuarioRepository.save(usuario);
            }

            // 2. Crear Usuario Administrador (Coordinador de prácticas)
            
        };
    }
}