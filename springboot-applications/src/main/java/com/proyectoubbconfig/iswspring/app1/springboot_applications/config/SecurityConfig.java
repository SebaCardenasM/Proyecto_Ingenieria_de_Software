package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http
            // 👇 1. DESACTIVAMOS LA PROTECCIÓN CSRF PARA QUE FUNCIONE TU FETCH (POST)
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/css/**", 
                        "/js/**", 
                        "/images/**", 
                        "/login", 
                        "/registro", 
                        "/usuarios/**"
                        // 👇 2. ELIMINAMOS LAS RUTAS DEL GESTOR DE AQUÍ PARA OBLIGAR AL LOGIN
                    ).permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("correo") 
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
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