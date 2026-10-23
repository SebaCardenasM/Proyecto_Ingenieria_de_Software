package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

        // CSRF queda activo por defecto (se eliminó csrf.disable())
        http
            .authorizeHttpRequests(auth -> auth
                    // Recursos estáticos y páginas públicas de registro/login
                    .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/login",
                        "/registro",
                        "/guardar",
                        "/evaluacion-colaborador/**" // Acceso por token al Colaborador del colegio
                    ).permitAll()

                    // Archivos subidos: solo usuarios autenticados
                    .requestMatchers("/uploads/**").authenticated()

                    // Selector de roles para usuarios con múltiples perfiles
                    .requestMatchers("/seleccionar-rol").authenticated()

                    // Control de acceso por rol a cada módulo
                    .requestMatchers("/estudiante/**").hasRole("ESTUDIANTE")
                    .requestMatchers("/profesor/**").hasRole("PROFESOR_ASIGNATURA")
                    .requestMatchers("/tutor/**").hasRole("TUTOR")

                    // Rutas exclusivas para la coordinación / administración
                    .requestMatchers(
                        "/estudiantes/**",
                        "/profesores/**",
                        "/practicas/**",
                        "/gestor-archivos/**",
                        "/portafolios/**",
                        "/usuarios/**",
                        "/index"
                    ).hasRole("COORDINADOR")

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