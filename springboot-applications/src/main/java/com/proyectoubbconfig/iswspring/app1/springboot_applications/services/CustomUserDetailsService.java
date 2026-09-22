package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String rut) throws UsernameNotFoundException {
        // Busca al usuario por su RUT directo ("11.111.111-1") o prueba limpiando puntos si no lo encuentra
        Usuario usuario = usuarioRepository.findByRut(rut)
                .orElseGet(() -> usuarioRepository.findByRut(rut.replace(".", ""))
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con RUT: " + rut)));

        // Construye el UserDetails mapeando getPassword() y getRol()
        return new User(
                usuario.getRut(),
                usuario.getPassword(), // Usa getPassword() definido en Usuario.java
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}