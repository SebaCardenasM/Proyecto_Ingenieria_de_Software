package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Profesor;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Rol;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ProfesorRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.TwoFactorService;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TwoFactorService twoFactorService;

    private static final Comparator<Practica> CRONOLOGICO = Comparator
            .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
            .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()));

    private Optional<Usuario> buscarUsuario(String rut) {
        if (rut == null) return Optional.empty();
        String r = rut.trim();
        Optional<Usuario> u = usuarioRepository.findByRut(r);
        return u.isPresent() ? u : usuarioRepository.findByRut(r.replace(".", ""));
    }

    /**
     * Visualización y edición del perfil propio del usuario logueado.
     */
    @GetMapping("/perfil")
    public String miPerfil(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Optional<Usuario> uOpt = buscarUsuario(authentication.getName());
        if (uOpt.isEmpty()) {
            return "redirect:/login";
        }

        Usuario usuario = uOpt.get();
        model.addAttribute("usuario", usuario);
        model.addAttribute("rolesUsuario", usuario.getRoles());

        boolean isEstudiante = usuario.hasRol(Rol.ESTUDIANTE);
        boolean isProfesor = usuario.hasRol(Rol.PROFESOR_ASIGNATURA) || usuario.hasRol(Rol.TUTOR);
        boolean isCoordinador = usuario.hasRol(Rol.COORDINADOR);

        model.addAttribute("isEstudiante", isEstudiante);
        model.addAttribute("isProfesor", isProfesor);
        model.addAttribute("isCoordinador", isCoordinador);

        if (isEstudiante) {
            Optional<Estudiante> estOpt = estudianteRepository.findById(usuario.getRut());
            Estudiante estudiante = estOpt.orElse(null);
            model.addAttribute("estudiante", estudiante);

            List<Practica> practicas = practicaRepository.findPortafolioByRut(usuario.getRut());
            practicas.sort(CRONOLOGICO);
            model.addAttribute("practicas", practicas);

            Practica practicaActual = practicas.stream()
                    .filter(p -> p.getNumeroPractica() != null && estudiante != null && p.getNumeroPractica().equals(estudiante.getPracticaActual()))
                    .findFirst()
                    .orElse(practicas.isEmpty() ? null : practicas.get(practicas.size() - 1));
            model.addAttribute("practicaActual", practicaActual);
        }

        if (isProfesor) {
            Optional<Profesor> profOpt = profesorRepository.findById(usuario.getRut());
            model.addAttribute("profesor", profOpt.orElse(null));

            List<Practica> practicasComoProfesor = practicaRepository.findByProfesor(profOpt.orElse(null));
            List<Practica> practicasComoTutor = practicaRepository.findByTutorRut(usuario.getRut());
            model.addAttribute("totalAlumnosProfesor", practicasComoProfesor != null ? practicasComoProfesor.size() : 0);
            model.addAttribute("totalAlumnosTutor", practicasComoTutor != null ? practicasComoTutor.size() : 0);
        }

        // Correo institucional ofuscado para visualización segura
        model.addAttribute("correoOculto", ocultarCorreo(usuario.getCorreo()));

        return "Perfil/perfil";
    }

    private String ocultarCorreo(String correo) {
        if (correo == null || !correo.contains("@")) return "tu correo institucional";
        String[] partes = correo.split("@");
        String nombre = partes[0];
        String dominio = partes[1];
        if (nombre.length() <= 2) {
            return nombre.charAt(0) + "***@" + dominio;
        }
        return nombre.charAt(0) + "***" + nombre.charAt(nombre.length() - 1) + "@" + dominio;
    }

    /**
     * Endpoint AJAX para enviar código al correo antes de cambiar contraseña.
     */
    @PostMapping("/perfil/enviar-codigo-password")
    @ResponseBody
    public Map<String, Object> enviarCodigoPassword(Authentication authentication) {
        if (authentication == null) return Map.of("ok", false, "mensaje", "No autenticado.");
        Optional<Usuario> uOpt = buscarUsuario(authentication.getName());
        if (uOpt.isEmpty()) return Map.of("ok", false, "mensaje", "Usuario no encontrado.");
        Usuario usuario = uOpt.get();
        boolean enviado = twoFactorService.enviarCodigoCorreo(usuario, "password");
        return Map.of("ok", enviado, "mensaje", "Se ha enviado un código de 6 dígitos a tu correo: " + ocultarCorreo(usuario.getCorreo()));
    }

    /**
     * Endpoint AJAX para enviar código al correo antes de activar 2FA.
     */
    @PostMapping("/perfil/enviar-codigo-2fa")
    @ResponseBody
    public Map<String, Object> enviarCodigo2FA(Authentication authentication) {
        if (authentication == null) return Map.of("ok", false, "mensaje", "No autenticado.");
        Optional<Usuario> uOpt = buscarUsuario(authentication.getName());
        if (uOpt.isEmpty()) return Map.of("ok", false, "mensaje", "Usuario no encontrado.");
        Usuario usuario = uOpt.get();
        boolean enviado = twoFactorService.enviarCodigoCorreo(usuario, "2fa");
        return Map.of("ok", enviado, "mensaje", "Se ha enviado un código de 6 dígitos a tu correo: " + ocultarCorreo(usuario.getCorreo()));
    }

    /**
     * Guarda los cambios permitidos en el perfil del usuario.
     */
    @PostMapping("/perfil/guardar")
    public String guardarPerfil(
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "correoSecundario", required = false) String correoSecundario,
            @RequestParam(value = "ciudad", required = false) String ciudad,
            @RequestParam(value = "biografia", required = false) String biografia,
            @RequestParam(value = "departamento", required = false) String departamento,
            @RequestParam(value = "gradoAcademico", required = false) String gradoAcademico,
            @RequestParam(value = "oficina", required = false) String oficina,
            @RequestParam(value = "horarioAtencion", required = false) String horarioAtencion,
            Authentication authentication,
            RedirectAttributes ra) {

        Optional<Usuario> uOpt = buscarUsuario(authentication.getName());
        if (uOpt.isEmpty()) return "redirect:/login";

        Usuario usuario = uOpt.get();
        usuario.setTelefono(telefono != null ? telefono.trim() : null);
        usuario.setCorreoSecundario(correoSecundario != null ? correoSecundario.trim() : null);
        usuario.setCiudad(ciudad != null ? ciudad.trim() : null);
        usuario.setBiografia(biografia != null ? biografia.trim() : null);

        if (usuario.hasRol(Rol.PROFESOR_ASIGNATURA) || usuario.hasRol(Rol.TUTOR)) {
            Optional<Profesor> profOpt = profesorRepository.findById(usuario.getRut());
            if (profOpt.isPresent()) {
                Profesor prof = profOpt.get();
                if (departamento != null && !departamento.isBlank()) prof.setDepartamento(departamento.trim());
                prof.setGradoAcademico(gradoAcademico != null ? gradoAcademico.trim() : null);
                prof.setOficina(oficina != null ? oficina.trim() : null);
                prof.setHorarioAtencion(horarioAtencion != null ? horarioAtencion.trim() : null);
                profesorRepository.save(prof);
            }
        }

        usuarioRepository.save(usuario);
        ra.addFlashAttribute("mensajeExito", "Los datos de tu perfil han sido actualizados exitosamente.");
        ra.addFlashAttribute("tituloExito", "¡Perfil guardado!");

        return "redirect:/perfil";
    }

    /**
     * Cambio de contraseña desde el perfil con confirmación de código por correo.
     */
    @PostMapping("/perfil/cambiar-password")
    public String cambiarPassword(
            @RequestParam("passwordActual") String passwordActual,
            @RequestParam("passwordNueva") String passwordNueva,
            @RequestParam("passwordConfirmar") String passwordConfirmar,
            @RequestParam("codigo") String codigo,
            Authentication authentication,
            RedirectAttributes ra) {

        Optional<Usuario> uOpt = buscarUsuario(authentication.getName());
        if (uOpt.isEmpty()) return "redirect:/login";

        Usuario usuario = uOpt.get();

        if (codigo == null || codigo.trim().length() != 6) {
            ra.addFlashAttribute("errorMensaje", "Debes ingresar el código de 6 dígitos enviado a tu correo institucional.");
            ra.addFlashAttribute("tituloError", "Código requerido");
            return "redirect:/perfil#seguridad";
        }

        boolean codigoValido = twoFactorService.verificar(usuario, codigo);
        if (!codigoValido) {
            ra.addFlashAttribute("errorMensaje", "El código de verificación ingresado es incorrecto o ha expirado. Por favor solicita uno nuevo.");
            ra.addFlashAttribute("tituloError", "Código no válido");
            return "redirect:/perfil#seguridad";
        }

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            ra.addFlashAttribute("errorMensaje", "La contraseña actual ingresada no es correcta.");
            ra.addFlashAttribute("tituloError", "Error de autenticación");
            return "redirect:/perfil#seguridad";
        }

        if (passwordNueva == null || passwordNueva.trim().length() < 8) {
            ra.addFlashAttribute("errorMensaje", "La nueva contraseña debe tener al menos 8 caracteres.");
            ra.addFlashAttribute("tituloError", "Contraseña demasiado corta");
            return "redirect:/perfil#seguridad";
        }

        if (!passwordNueva.equals(passwordConfirmar)) {
            ra.addFlashAttribute("errorMensaje", "La nueva contraseña y su confirmación no coinciden.");
            ra.addFlashAttribute("tituloError", "Discrepancia en contraseñas");
            return "redirect:/perfil#seguridad";
        }

        if (passwordEncoder.matches(passwordNueva, usuario.getPassword())) {
            ra.addFlashAttribute("errorMensaje", "La nueva contraseña debe ser distinta a la contraseña actual.");
            ra.addFlashAttribute("tituloError", "Contraseña idéntica");
            return "redirect:/perfil#seguridad";
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);

        ra.addFlashAttribute("mensajeExito", "Tu contraseña ha sido actualizada exitosamente.");
        ra.addFlashAttribute("tituloExito", "¡Contraseña actualizada!");
        return "redirect:/perfil#seguridad";
    }

    /**
     * Activa la autenticación en dos pasos (2FA) por correo verificando el código de 6 dígitos.
     */
    @PostMapping("/perfil/2fa/activar")
    public String activar2FA(
            @RequestParam("codigo") String codigo,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes ra) {

        Optional<Usuario> uOpt = buscarUsuario(authentication.getName());
        if (uOpt.isEmpty()) return "redirect:/login";

        Usuario usuario = uOpt.get();

        if (codigo == null || codigo.trim().length() != 6) {
            ra.addFlashAttribute("errorMensaje", "Debes ingresar el código de 6 dígitos enviado a tu correo institucional.");
            ra.addFlashAttribute("tituloError", "Código requerido");
            return "redirect:/perfil#seguridad";
        }

        boolean valido = twoFactorService.verificar(usuario, codigo);
        if (!valido) {
            ra.addFlashAttribute("errorMensaje", "El código de verificación de 6 dígitos ingresado es incorrecto o ha expirado. Por favor solicita uno nuevo.");
            ra.addFlashAttribute("tituloError", "Código no válido");
            return "redirect:/perfil#seguridad";
        }

        usuario.setTwoFactorEnabled(true);
        usuarioRepository.save(usuario);

        if (session != null) {
            session.setAttribute("2FA_VERIFIED", true);
            session.removeAttribute("2FA_PENDING_RUT");
        }

        ra.addFlashAttribute("mensajeExito", "¡La autenticación en dos pasos (2FA) por correo ha sido activada en tu cuenta!");
        ra.addFlashAttribute("tituloExito", "¡2FA Activado!");
        return "redirect:/perfil#seguridad";
    }

    /**
     * Desactiva la autenticación en dos pasos (2FA) confirmando con la contraseña.
     */
    @PostMapping("/perfil/2fa/desactivar")
    public String desactivar2FA(
            @RequestParam("password") String password,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes ra) {

        Optional<Usuario> uOpt = buscarUsuario(authentication.getName());
        if (uOpt.isEmpty()) return "redirect:/login";

        Usuario usuario = uOpt.get();

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            ra.addFlashAttribute("errorMensaje", "La contraseña ingresada para confirmar la desactivación no es correcta.");
            ra.addFlashAttribute("tituloError", "Contraseña incorrecta");
            return "redirect:/perfil#seguridad";
        }

        usuario.setTwoFactorEnabled(false);
        usuario.setTwoFactorSecret(null);
        usuarioRepository.save(usuario);

        session.setAttribute("2FA_VERIFIED", true);
        session.removeAttribute("2FA_PENDING_RUT");

        ra.addFlashAttribute("mensajeExito", "La autenticación en dos pasos (2FA) ha sido desactivada en tu cuenta.");
        ra.addFlashAttribute("tituloExito", "2FA Desactivado");
        return "redirect:/perfil#seguridad";
    }

    /**
     * Ver perfil de un estudiante (accesible para Coordinador, Profesor de Asignatura y Tutor).
     */
    @GetMapping("/perfil/estudiante/{rut}")
    public String verPerfilEstudiante(@PathVariable("rut") String rut, Model model, RedirectAttributes ra) {
        Optional<Usuario> uOpt = buscarUsuario(rut);
        if (uOpt.isEmpty() || !uOpt.get().hasRol(Rol.ESTUDIANTE)) {
            ra.addFlashAttribute("errorMensaje", "El estudiante solicitado no existe o no está registrado.");
            return "redirect:/estudiantes";
        }

        Usuario usuario = uOpt.get();
        Estudiante estudiante = estudianteRepository.findById(usuario.getRut()).orElse(null);

        List<Practica> practicas = practicaRepository.findPortafolioByRut(usuario.getRut());
        practicas.sort(CRONOLOGICO);

        Practica practicaActual = practicas.stream()
                .filter(p -> p.getNumeroPractica() != null && estudiante != null && p.getNumeroPractica().equals(estudiante.getPracticaActual()))
                .findFirst()
                .orElse(practicas.isEmpty() ? null : practicas.get(practicas.size() - 1));

        model.addAttribute("usuario", usuario);
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("practicas", practicas);
        model.addAttribute("practicaActual", practicaActual);

        return "Perfil/ver-estudiante";
    }

    /**
     * Ver perfil de un profesor o tutor (accesible para Coordinador y Profesores).
     */
    @GetMapping("/perfil/profesor/{rut}")
    public String verPerfilProfesor(@PathVariable("rut") String rut, Model model, RedirectAttributes ra) {
        Optional<Usuario> uOpt = buscarUsuario(rut);
        if (uOpt.isEmpty() || (!uOpt.get().hasRol(Rol.PROFESOR_ASIGNATURA) && !uOpt.get().hasRol(Rol.TUTOR) && !uOpt.get().hasRol(Rol.COORDINADOR))) {
            ra.addFlashAttribute("errorMensaje", "El profesor solicitado no existe o no está registrado.");
            return "redirect:/profesores";
        }

        Usuario usuario = uOpt.get();
        Profesor profesor = profesorRepository.findById(usuario.getRut()).orElse(null);

        List<Practica> practicasComoProfesor = practicaRepository.findByProfesor(profesor);
        List<Practica> practicasComoTutor = practicaRepository.findByTutorRut(usuario.getRut());

        model.addAttribute("usuario", usuario);
        model.addAttribute("profesor", profesor);
        model.addAttribute("practicasComoProfesor", practicasComoProfesor);
        model.addAttribute("practicasComoTutor", practicasComoTutor);

        return "Perfil/ver-profesor";
    }
}
