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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/login").permitAll() // Todos pueden ver el login
                .anyRequest().authenticated() // Lo demás requiere inicio de sesión
            )
            .formLogin(form -> form
                .loginPage("/login") // Le decimos que usaremos nuestro propio HTML
                .defaultSuccessUrl("/") // A dónde ir si el login es exitoso
                .permitAll()
            )
            .logout(logout -> logout.permitAll());

        return http.build();
    }

    // Usaremos BCrypt para que las contraseñas se guarden encriptadas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
