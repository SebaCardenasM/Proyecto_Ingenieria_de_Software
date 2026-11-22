package com.proyectoubbconfig.iswspring.app1.springboot_applications.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Único lugar donde se normalizan y validan RUT.
 * Formato canónico para guardar en la BD: con puntos ("12.345.678-9"), igual que Usuario.
 */
public final class RutUtil {

    private static final Pattern FORMATO = Pattern.compile("^(\\d{7,8})-?([\\dK])$");

    private RutUtil() {}

    /** Acepta con o sin puntos/guión/espacios. Devuelve "12345678-9" o null si no tiene forma de RUT. */
    public static String normalizar(String rut) {
        if (rut == null) return null;
        String limpio = rut.replace(".", "").replace(" ", "").toUpperCase();
        Matcher m = FORMATO.matcher(limpio);
        return m.matches() ? m.group(1) + "-" + m.group(2) : null;
    }

    /** Formato canónico "12.345.678-9" a partir de cualquier variante. null si no tiene forma de RUT. */
    public static String conPuntos(String rut) {
        String n = normalizar(rut);
        if (n == null) return null;
        String cuerpo = n.substring(0, n.length() - 2);
        char dv = n.charAt(n.length() - 1);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cuerpo.length(); i++) {
            if (i > 0 && (cuerpo.length() - i) % 3 == 0) sb.append('.');
            sb.append(cuerpo.charAt(i));
        }
        return sb + "-" + dv;
    }

    /** true si tiene forma de RUT y el dígito verificador es correcto (módulo 11). */
    public static boolean esValido(String rut) {
        String n = normalizar(rut);
        if (n == null) return false;
        String cuerpo = n.substring(0, n.length() - 2);
        char dv = n.charAt(n.length() - 1);

        int suma = 0;
        int multiplo = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * multiplo;
            multiplo = multiplo < 7 ? multiplo + 1 : 2;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : (resto == 10 ? 'K' : (char) ('0' + resto));
        return dv == esperado;
    }
}