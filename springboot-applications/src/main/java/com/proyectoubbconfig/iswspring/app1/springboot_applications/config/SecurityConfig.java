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
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                // Públicas: estáticos, login, recuperación de contraseña y acceso por token del colaborador
                .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/login", "/recuperar/**", "/evaluacion-colaborador/**").permitAll()

                // Cualquier usuario autenticado (el acceso fino a /uploads lo valida el interceptor)
                .requestMatchers("/seleccionar-rol", "/uploads/**").authenticated()

                // API de documentos: cada endpoint con su rol
                .requestMatchers(HttpMethod.POST, "/api/documentos/subir", "/api/documentos/subir-tutor").hasAnyRole("ESTUDIANTE", "TUTOR", "PROFESOR_ASIGNATURA")

                // Paso 3 de la cadena: solo el estudiante envía sus borradores al gestor
                .requestMatchers(HttpMethod.POST, "/api/documentos/enviar/**").hasRole("ESTUDIANTE")

                // PERMITE ELIMINAR
                .requestMatchers(HttpMethod.POST, "/api/documentos/eliminar/**").hasAnyRole("ESTUDIANTE", "TUTOR", "PROFESOR_ASIGNATURA", "COORDINADOR")

                .requestMatchers("/api/documentos/mis-documentos", "/api/documentos/pautas-estudiante")
                    .hasAnyRole("ESTUDIANTE", "PROFESOR_ASIGNATURA", "TUTOR", "COORDINADOR")
                .requestMatchers("/api/documentos/mis-estudiantes-documentos")
                    .hasAnyRole("PROFESOR_ASIGNATURA", "TUTOR", "COORDINADOR")
                .requestMatchers("/api/documentos/todos").hasRole("COORDINADOR")

                // Evaluar informes: profesor de asignatura o coordinador (el servicio valida además que le corresponda el estudiante)
                .requestMatchers(HttpMethod.POST, "/api/documentos/evaluar-informe/**").hasAnyRole("PROFESOR_ASIGNATURA", "COORDINADOR")

                // Chatbot
                .requestMatchers(HttpMethod.POST, "/api/chat/preguntar").authenticated()
                // Paso 2 de la cadena: revisión de borradores (solo estudiantes)
                .requestMatchers(HttpMethod.POST, "/api/chat/revisar/**").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.GET, "/api/chat/historial").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/chat/resumen/**").hasAnyRole("COORDINADOR", "PROFESOR_ASIGNATURA")

                // Cualquier otro endpoint de la API queda bloqueado (debe ir DESPUÉS de definir las rutas de /api/ permitidas)
                .requestMatchers("/api/**").denyAll()

                // Evaluación ponderada por componentes (Coordinador y Profesor de Asignatura)
                .requestMatchers("/evaluacion/**").hasAnyRole("COORDINADOR", "PROFESOR_ASIGNATURA")

                // Paneles por rol (lo específico va antes que lo general)
                .requestMatchers("/estudiante/**").hasRole("ESTUDIANTE")
                .requestMatchers("/profesor/archivos").hasAnyRole("PROFESOR_ASIGNATURA", "TUTOR")
                .requestMatchers("/profesor/analiticas-ia").hasAnyRole("PROFESOR_ASIGNATURA", "TUTOR", "COORDINADOR")
                .requestMatchers("/profesor/**").hasRole("PROFESOR_ASIGNATURA")
                .requestMatchers("/tutor/**").hasRole("TUTOR")

                // Coordinación (un solo bloque)
                .requestMatchers("/estudiantes/**", "/profesores/**", "/colaboradores/**",
                                "/practicas/**", "/centros-practica/**",
                                "/gestor-archivos/**", "/portafolios/**", "/usuarios/**",
                                "/monitoreo-chat/**", "/index").hasRole("COORDINADOR")

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