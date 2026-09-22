package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Coordinador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
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

    // Este Bean se ejecuta automáticamente cada vez que arranca la aplicación
    @Bean
    public CommandLineRunner crearUsuariosPrueba(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            
            // 1. Crear Usuario COORDINADOR de prueba
            if (usuarioRepository.findByCorreo("coordinador@ubb.cl").isEmpty()) {
                Coordinador coordinador = new Coordinador();
                coordinador.setRut("11.111.111-1");
                coordinador.setNombre("Prueba");
                coordinador.setApellido("Coordinador");
                coordinador.setCorreo("coordinador@ubb.cl");
                coordinador.setPassword(passwordEncoder.encode("admin"));

                usuarioRepository.save(coordinador);
            }

            // 2. Crear Usuario PROFESOR de prueba
            if (usuarioRepository.findByCorreo("profesor@ubb.cl").isEmpty()) {
                Profesor profesor = new Profesor();
                profesor.setRut("22.222.222-2");
                profesor.setNombre("Prueba");
                profesor.setApellido("Profesor");
                profesor.setCorreo("profesor@ubb.cl");
                profesor.setPassword(passwordEncoder.encode("profe"));

                usuarioRepository.save(profesor);
            }

            // 3. Crear Usuario ESTUDIANTE de prueba
            if (usuarioRepository.findByCorreo("estudiante@ubb.cl").isEmpty()) {
                Estudiante estudiante = new Estudiante();
                estudiante.setRut("33.333.333-3");
                estudiante.setNombre("Prueba");
                estudiante.setApellido("Estudiante");
                estudiante.setCorreo("estudiante@ubb.cl");
                estudiante.setPassword(passwordEncoder.encode("estudiante"));
                estudiante.setAnoIngreso(2023);
                estudiante.setPracticaActual(1);

                usuarioRepository.save(estudiante);
            }

        };
    }
}