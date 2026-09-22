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
        
        http
            // Desactivamos la protección CSRF para peticiones POST/Fetch externas
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/css/**", 
                        "/js/**", 
                        "/images/**", 
                        "/login", 
                        "/registro", 
                        "/usuarios/**"
                    ).permitAll()
                    // Control de acceso por rol a cada sección
                    .requestMatchers("/estudiante/**").hasRole("ESTUDIANTE")
                    .requestMatchers("/profesor/**").hasRole("PROFESOR")
                    .requestMatchers("/index", "/").hasAnyRole("COORDINADOR", "PROFESOR", "ESTUDIANTE")
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                // 👇 Se cambia "correo" por "rut" para procesar el formulario con este parámetro
                .usernameParameter("rut") 
                .passwordParameter("password")
                // Redirección personalizada según el rol del usuario
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