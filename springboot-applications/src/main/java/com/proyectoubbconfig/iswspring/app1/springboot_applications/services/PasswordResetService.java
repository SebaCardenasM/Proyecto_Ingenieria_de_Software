package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.PasswordResetToken;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PasswordResetTokenRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int MINUTOS_VALIDEZ = 10;
    private static final int MAX_INTENTOS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@ubiobio.cl}")
    private String remitente;

    /** Mismo criterio que CustomUserDetailsService: RUT con o sin puntos. */
    private Optional<Usuario> buscar(String rut) {
        if (rut == null) return Optional.empty();
        String r = rut.trim();
        Optional<Usuario> u = usuarioRepository.findByRut(r);
        return u.isPresent() ? u : usuarioRepository.findByRut(r.replace(".", ""));
    }

    /**
     * Genera y envía el código. Devuelve el RUT canónico si el usuario existe, o null.
     * El controlador responde igual en ambos casos para no revelar qué RUT existen.
     */
    @Transactional
    public String solicitarCodigo(String rutIngresado) {
        Optional<Usuario> opt = buscar(rutIngresado);
        if (opt.isEmpty() || opt.get().getCorreo() == null || opt.get().getCorreo().isBlank()) {
            return null;
        }
        Usuario u = opt.get();
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));

        tokenRepository.deleteByRut(u.getRut());
        tokenRepository.save(new PasswordResetToken(
                u.getRut(), passwordEncoder.encode(codigo),
                LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ)));

        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(remitente);
            m.setTo(u.getCorreo());
            m.setSubject("Recuperación de contraseña - Prácticas UBB");
            m.setText("Hola " + u.getNombre() + ",\n\n"
                    + "Tu código de verificación es: " + codigo + "\n\n"
                    + "Vence en " + MINUTOS_VALIDEZ + " minutos. Si no lo solicitaste, ignora este correo.");
            mailSender.send(m);
        } catch (RuntimeException e) {
            log.error("No se pudo enviar el correo de recuperación a {}", u.getRut(), e);
        }
        return u.getRut();
    }

    public String correoEnmascarado(String rut) {
        return buscar(rut).map(Usuario::getCorreo).map(c -> {
            int at = c.indexOf('@');
            if (at < 2) return "***" + c.substring(Math.max(at, 0));
            return c.charAt(0) + "***" + c.substring(at - 1);
        }).orElse("tu correo registrado");
    }

    /** true si el código es correcto y vigente. Máx. 5 intentos por código. */
    @Transactional
    public boolean verificarCodigo(String rut, String codigo) {
        Optional<PasswordResetToken> opt = tokenRepository.findTopByRutOrderByIdDesc(rut);
        if (opt.isEmpty()) return false;
        PasswordResetToken t = opt.get();

        if (t.getExpira().isBefore(LocalDateTime.now()) || t.getIntentos() >= MAX_INTENTOS) {
            tokenRepository.deleteByRut(rut);
            return false;
        }
        if (codigo != null && passwordEncoder.matches(codigo.trim(), t.getCodigoHash())) {
            return true;
        }
        t.setIntentos(t.getIntentos() + 1);
        tokenRepository.save(t);
        return false;
    }

    @Transactional
    public void restablecer(String rut, String nuevaPassword) {
        Usuario u = usuarioRepository.findByRut(rut)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        u.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(u);
        tokenRepository.deleteByRut(rut); // el código es de un solo uso
    }
}
