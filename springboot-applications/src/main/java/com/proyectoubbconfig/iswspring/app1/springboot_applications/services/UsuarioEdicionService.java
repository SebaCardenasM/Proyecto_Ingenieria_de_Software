package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.UsuarioEdicionDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioEdicionService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Devuelve el mensaje de error o null si todo está bien
    public String validarDatosBase(String rut, UsuarioEdicionDTO dto) {
        if (dto.getNombre() == null || dto.getNombre().isBlank()
                || dto.getApellido() == null || dto.getApellido().isBlank()) {
            return "Error: El nombre y el apellido no pueden quedar vacíos.";
        }
        if (dto.getCorreo() == null || dto.getCorreo().isBlank()) {
            return "Error: El correo no puede quedar vacío.";
        }
        Optional<Usuario> otro = usuarioRepository.findByCorreo(dto.getCorreo().trim());
        if (otro.isPresent() && !otro.get().getRut().equals(rut)) {
            return "Error: El correo ingresado ya pertenece a otro usuario.";
        }
        return null;
    }

    // El RUT nunca se modifica (es la clave primaria)
    public void aplicarDatosBase(Usuario usuario, UsuarioEdicionDTO dto) {
        usuario.setNombre(dto.getNombre().trim());
        usuario.setApellido(dto.getApellido().trim());
        usuario.setCorreo(dto.getCorreo().trim());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }
}