package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String rut) throws UsernameNotFoundException {
        // Busca al usuario por su RUT directo ("11.111.111-1") o prueba limpiando puntos si no lo encuentra
        Usuario usuario = usuarioRepository.findByRut(rut)
                .orElseGet(() -> usuarioRepository.findByRut(rut.replace(".", ""))
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con RUT: " + rut)));

        // Convierte el Set<Rol> en una lista de GrantedAuthority con el prefijo "ROLE_"
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.name()))
                .collect(Collectors.toList());

        // Construye el UserDetails mapeando getPassword() y la lista completa de roles/autoridades
        return new User(
                usuario.getRut(),
                usuario.getPassword(), // Usa getPassword() definido en Usuario.java
                authorities
        );
    }
}