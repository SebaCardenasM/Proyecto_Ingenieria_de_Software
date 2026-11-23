package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Usuario;

@Service
public class TwoFactorService {

    private static final Logger log = LoggerFactory.getLogger(TwoFactorService.class);
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TIME_STEP_SECONDS = 30;

    // Almacenamiento temporal en memoria para códigos de verificación enviados por correo (RUT -> InfoCodigo)
    private record CodigoTemporal(String codigo, Instant expira) {}
    private final Map<String, CodigoTemporal> codigosCorreo = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@ubiobio.cl}")
    private String remitente;

    /**
     * Genera un secreto aleatorio en Base32 (20 bytes = 160 bits = 32 caracteres Base32).
     */
    public String generateSecret() {
        byte[] buffer = new byte[20];
        RANDOM.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    /**
     * Genera la URI estándar otpauth para escanear en Google Authenticator / Authy.
     */
    public String getQrCodeUri(String rut, String secret) {
        String etiqueta = URLEncoder.encode("PracticasUBB:" + rut, StandardCharsets.UTF_8);
        String emisor = URLEncoder.encode("Universidad del Bío-Bío", StandardCharsets.UTF_8);
        return String.format("otpauth://totp/%s?secret=%s&issuer=%s&digits=6&period=30", etiqueta, secret, emisor);
    }

    /**
     * Genera la URL pública para renderizar el código QR como imagen.
     */
    public String getQrImageUrl(String rut, String secret) {
        String uri = getQrCodeUri(rut, secret);
        return "https://api.qrserver.com/v1/create-qr-code/?size=220x220&data="
                + URLEncoder.encode(uri, StandardCharsets.UTF_8);
    }

    /**
     * Valida un código TOTP de 6 dígitos con ventana de tiempo (actual ± 1 paso de 30s).
     */
    public boolean verifyTotp(String secret, String code) {
        if (secret == null || code == null) return false;
        String sanitized = code.trim().replaceAll("\\s+", "");
        if (sanitized.length() != 6 || !sanitized.matches("\\d{6}")) return false;

        byte[] keyBytes = decodeBase32(secret);
        if (keyBytes == null || keyBytes.length == 0) return false;

        long currentWindow = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;

        // Tolerancia de ±1 ventana (30 segundos antes, actual y 30 segundos después)
        for (long i = -1; i <= 1; i++) {
            String expected = generateTotpCode(keyBytes, currentWindow + i);
            if (sanitized.equals(expected)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Envía un código de 6 dígitos al correo institucional del usuario como alternativa o para confirmación.
     */
    public boolean enviarCodigoCorreo(Usuario usuario) {
        return enviarCodigoCorreo(usuario, "verificacion");
    }

    /**
     * Envía un código de 6 dígitos con asunto y cuerpo según el motivo (password, 2fa, verificacion).
     */
    public boolean enviarCodigoCorreo(Usuario usuario, String motivo) {
        if (usuario == null || usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            return false;
        }

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        Instant expira = Instant.now().plusSeconds(600); // Válido por 10 minutos
        codigosCorreo.put(usuario.getRut(), new CodigoTemporal(codigo, expira));

        log.info("Código de seguridad generado para {} [motivo: {}]: {}", usuario.getRut(), motivo, codigo);

        if (mailSender != null) {
            try {
                String asunto;
                String mensaje;

                if ("password".equalsIgnoreCase(motivo)) {
                    asunto = "Código para cambio de contraseña - Prácticas UBB";
                    mensaje = "Hola " + usuario.getNombre() + ",\n\n"
                            + "Tu código para confirmar el cambio de tu contraseña institucional es:\n\n"
                            + "   " + codigo + "\n\n"
                            + "Este código es válido por 10 minutos. Si tú no solicitaste este cambio, ignora este mensaje o contacta a soporte.\n\n"
                            + "Plataforma de Prácticas - Universidad del Bío-Bío";
                } else if ("2fa".equalsIgnoreCase(motivo)) {
                    asunto = "Código para activar autenticación de 2 pasos - Prácticas UBB";
                    mensaje = "Hola " + usuario.getNombre() + ",\n\n"
                            + "Tu código para activar la verificación en dos pasos en tu cuenta es:\n\n"
                            + "   " + codigo + "\n\n"
                            + "Este código es válido por 10 minutos.\n\n"
                            + "Plataforma de Prácticas - Universidad del Bío-Bío";
                } else {
                    asunto = "Código de verificación - Prácticas UBB";
                    mensaje = "Hola " + usuario.getNombre() + ",\n\n"
                            + "Tu código de verificación de seguridad es:\n\n"
                            + "   " + codigo + "\n\n"
                            + "Este código es válido por 10 minutos.\n\n"
                            + "Plataforma de Prácticas - Universidad del Bío-Bío";
                }

                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(remitente);
                msg.setTo(usuario.getCorreo());
                msg.setSubject(asunto);
                msg.setText(mensaje);
                mailSender.send(msg);
                return true;
            } catch (Exception ex) {
                log.warn("No se pudo enviar el correo a {}: {}", usuario.getCorreo(), ex.getMessage());
                // Permite continuar si es en entorno local/desarrollo
                return true;
            }
        }
        return true;
    }

    /**
     * Verifica el código 2FA ingresado (sea de Authenticator App o de correo).
     */
    public boolean verificar(Usuario usuario, String codigoIngresado) {
        if (usuario == null || codigoIngresado == null) return false;
        String codigo = codigoIngresado.trim().replaceAll("\\s+", "");

        // 1. Probar con la aplicación TOTP (Google Authenticator / Authy)
        if (usuario.getTwoFactorSecret() != null && verifyTotp(usuario.getTwoFactorSecret(), codigo)) {
            codigosCorreo.remove(usuario.getRut());
            return true;
        }

        // 2. Probar con el código temporal enviado al correo
        CodigoTemporal temporal = codigosCorreo.get(usuario.getRut());
        if (temporal != null) {
            if (Instant.now().isBefore(temporal.expira()) && temporal.codigo().equals(codigo)) {
                codigosCorreo.remove(usuario.getRut());
                return true;
            } else if (Instant.now().isAfter(temporal.expira())) {
                codigosCorreo.remove(usuario.getRut());
            }
        }

        return false;
    }

    // ------------------------------------------------------------- Métodos RFC 6238
    private String generateTotpCode(byte[] keyBytes, long timeStep) {
        try {
            byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyBytes, "RAW"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % 1_000_000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            log.error("Error al calcular HMAC-SHA1 para TOTP: {}", e.getMessage());
            return "";
        }
    }

    // ------------------------------------------------------------- Base32 Helper
    private String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                sb.append(BASE32_CHARS.charAt(index));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            sb.append(BASE32_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private byte[] decodeBase32(String base32) {
        String clean = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int numBytes = clean.length() * 5 / 8;
        byte[] result = new byte[numBytes];
        int buffer = 0;
        int bitsLeft = 0;
        int byteIndex = 0;

        for (char c : clean.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                if (byteIndex < result.length) {
                    result[byteIndex++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                }
                bitsLeft -= 8;
            }
        }
        return result;
    }
}
