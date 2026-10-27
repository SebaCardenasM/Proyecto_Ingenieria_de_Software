package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // Públicas: estáticos, login y acceso por token del colaborador
                .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/login", "/evaluacion-colaborador/**").permitAll()

                // Cualquier usuario autenticado (el acceso fino a /uploads lo valida el interceptor)
                .requestMatchers("/seleccionar-rol", "/uploads/**").authenticated()

                // API de documentos: cada endpoint con su rol
                .requestMatchers(HttpMethod.POST, "/api/documentos/subir").hasRole("ESTUDIANTE")
                .requestMatchers("/api/documentos/mis-documentos")
                    .hasAnyRole("ESTUDIANTE", "PROFESOR_ASIGNATURA", "TUTOR", "COORDINADOR")
                .requestMatchers("/api/documentos/mis-estudiantes-documentos")
                    .hasAnyRole("PROFESOR_ASIGNATURA", "TUTOR", "COORDINADOR")
                .requestMatchers("/api/documentos/todos").hasRole("COORDINADOR")
                
                // Chatbot
                .requestMatchers(HttpMethod.POST, "/api/chat/preguntar").authenticated()

                // Cualquier otro endpoint de la API queda bloqueado (debe ir DESPUÉS de definir las rutas de /api/ permitidas)
                .requestMatchers("/api/**").denyAll() 

                // Paneles por rol (lo específico va antes que lo general)
                .requestMatchers("/estudiante/**").hasRole("ESTUDIANTE")
                .requestMatchers("/profesor/archivos").hasAnyRole("PROFESOR_ASIGNATURA", "TUTOR")
                .requestMatchers("/profesor/**").hasRole("PROFESOR_ASIGNATURA")
                .requestMatchers("/tutor/**").hasRole("TUTOR")

                // Coordinación
                .requestMatchers("/estudiantes/**", "/profesores/**", "/practicas/**",
                                "/gestor-archivos/**", "/portafolios/**", "/usuarios/**",
                                "/index").hasRole("COORDINADOR")

                // Esta línea SIEMPRE debe ser la última
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("rut")
                .passwordParameter("password")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}