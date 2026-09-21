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
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // 1. Buscar al usuario en la base de datos por su correo
        Usuario usuario = usuarioRepository.findByCorreo(correo != null ? correo.trim() : "");

        // 2. Si no existe, lanzar excepción
        if (usuario == null) {
            throw new UsernameNotFoundException("No existe usuario registrado con el correo: " + correo);
        }

        // 3. Obtener el nombre del enum Rol y agregar el prefijo ROLE_
        String rolNombre = "ROLE_" + usuario.getRol().name();

        // 4. Retornar el objeto UserDetails con el correo, password encriptado y rol
        return new User(
            usuario.getCorreo(),
            usuario.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(rolNombre))
        );
    }
}