package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Coordinador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
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
            
            // 1. Crear Usuario COORDINADOR de prueba
            if (usuarioRepository.findByCorreo("coordinador@ubb.cl") == null) {
                Usuario coordinadorUsuario = new Usuario();
                coordinadorUsuario.setRut("11.111.111-1");
                coordinadorUsuario.setNombre("Prueba");
                coordinadorUsuario.setApellido("Coordinador");
                coordinadorUsuario.setCorreo("coordinador@ubb.cl");
                coordinadorUsuario.setPassword(passwordEncoder.encode("admin"));
                coordinadorUsuario.setRol(Rol.COORDINADOR);

                Coordinador coordinador = new Coordinador();

                coordinador.setUsuario(coordinadorUsuario);
                coordinadorUsuario.setCoordinador(coordinador);

                usuarioRepository.save(coordinadorUsuario);
            }

            // 2. Crear Usuario PROFESOR de prueba
            if (usuarioRepository.findByCorreo("profesor@ubb.cl") == null) {
                Usuario profesorUsuario = new Usuario();
                profesorUsuario.setRut("22.222.222-2");
                profesorUsuario.setNombre("Prueba");
                profesorUsuario.setApellido("Profesor");
                profesorUsuario.setCorreo("profesor@ubb.cl");
                profesorUsuario.setPassword(passwordEncoder.encode("profe"));
                profesorUsuario.setRol(Rol.PROFESOR);

                Profesor profesor = new Profesor();
                profesor.setUsuario(profesorUsuario);
                profesorUsuario.setProfesor(profesor);

                usuarioRepository.save(profesorUsuario);
            }

            // 3. Crear Usuario ESTUDIANTE de prueba
            if (usuarioRepository.findByCorreo("estudiante@ubb.cl") == null) {
                Usuario estudianteUsuario = new Usuario();
                estudianteUsuario.setRut("33.333.333-3");
                estudianteUsuario.setNombre("Prueba");
                estudianteUsuario.setApellido("Estudiante");
                estudianteUsuario.setCorreo("estudiante@ubb.cl");
                estudianteUsuario.setPassword(passwordEncoder.encode("estudiante"));
                estudianteUsuario.setRol(Rol.ESTUDIANTE);

                Estudiante estudiante = new Estudiante();
                estudiante.setAnoIngreso(2023);
                estudiante.setPracticaActual(1);
                estudiante.setUsuario(estudianteUsuario);
                estudianteUsuario.setEstudiante(estudiante);

                usuarioRepository.save(estudianteUsuario);
            }

        };
    }
}