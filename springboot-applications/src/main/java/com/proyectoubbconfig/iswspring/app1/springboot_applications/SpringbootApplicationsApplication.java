package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Coordinador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

import java.util.Optional;

@SpringBootApplication
public class SpringbootApplicationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplicationsApplication.class, args);
    }

    @Bean
    public CommandLineRunner crearUsuariosPrueba(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            
            // 1. COORDINADOR
            Optional<Usuario> coordOpt = usuarioRepository.findByCorreo("coordinador@ubb.cl");
            if (coordOpt.isEmpty()) {
                Coordinador coordinador = new Coordinador();
                coordinador.setRut("11.111.111-1");
                coordinador.setNombre("Prueba");
                coordinador.setApellido("Coordinador");
                coordinador.setCorreo("coordinador@ubb.cl");
                coordinador.setPassword(passwordEncoder.encode("admin"));
                coordinador.addRol(Rol.COORDINADOR);

                usuarioRepository.save(coordinador);
            } else {
                Usuario coord = coordOpt.get();
                if (coord.getRoles().isEmpty()) {
                    coord.addRol(Rol.COORDINADOR);
                    usuarioRepository.save(coord);
                }
            }

            // 2. PROFESOR DE ASIGNATURA Y TUTOR (Asignamos ambos roles de prueba)
            Optional<Usuario> profOpt = usuarioRepository.findByCorreo("profesor@ubb.cl");
            if (profOpt.isEmpty()) {
                Profesor profesor = new Profesor();
                profesor.setRut("22.222.222-2");
                profesor.setNombre("Prueba");
                profesor.setApellido("Profesor");
                profesor.setCorreo("profesor@ubb.cl");
                profesor.setPassword(passwordEncoder.encode("profe"));
                profesor.addRol(Rol.PROFESOR_ASIGNATURA);
                profesor.addRol(Rol.TUTOR); // Prueba de rol múltiple

                usuarioRepository.save(profesor);
            } else {
                Usuario prof = profOpt.get();
                if (prof.getRoles().isEmpty()) {
                    prof.addRol(Rol.PROFESOR_ASIGNATURA);
                    prof.addRol(Rol.TUTOR);
                    usuarioRepository.save(prof);
                }
            }

            // 3. ESTUDIANTE
            Optional<Usuario> estOpt = usuarioRepository.findByCorreo("estudiante@ubb.cl");
            if (estOpt.isEmpty()) {
                Estudiante estudiante = new Estudiante();
                estudiante.setRut("33.333.333-3");
                estudiante.setNombre("Prueba");
                estudiante.setApellido("Estudiante");
                estudiante.setCorreo("estudiante@ubb.cl");
                estudiante.setPassword(passwordEncoder.encode("estudiante"));
                estudiante.setAnoIngreso(2023);
                estudiante.setPracticaActual(1);
                estudiante.addRol(Rol.ESTUDIANTE);

                usuarioRepository.save(estudiante);
            } else {
                Usuario est = estOpt.get();
                if (est.getRoles().isEmpty()) {
                    est.addRol(Rol.ESTUDIANTE);
                    usuarioRepository.save(est);
                }
            }

        };
    }
}