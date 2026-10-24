package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.util.Map;

public class NotasFiltro {

    public static final int TOTAL_PRACTICAS = 6;

    private String rut = "";
    private String nombre = "";
    private String anoOp = "";
    private String anoVal = "";
    private final String[] ops = new String[TOTAL_PRACTICAS];
    private final String[] vals = new String[TOTAL_PRACTICAS];

    public NotasFiltro() {
        for (int i = 0; i < TOTAL_PRACTICAS; i++) {
            ops[i] = "";
            vals[i] = "";
        }
    }

    public static NotasFiltro desde(Map<String, String> p) {
        NotasFiltro f = new NotasFiltro();
        f.rut = limpio(p.get("rut"));
        f.nombre = limpio(p.get("nombre"));
        f.anoOp = limpio(p.get("anoOp"));
        f.anoVal = limpio(p.get("anoVal"));
        for (int i = 0; i < TOTAL_PRACTICAS; i++) {
            f.ops[i] = limpio(p.get("op" + (i + 1)));
            f.vals[i] = limpio(p.get("val" + (i + 1)));
        }
        return f;
    }

    public boolean cumple(NotaEstudianteFila fila) {
        if (!rut.isEmpty() && !contiene(fila.rut(), rut)) return false;
        if (!nombre.isEmpty() && !contiene(fila.nombreCompleto(), nombre)) return false;

        if (!compara(fila.anoIngreso() == null ? null : fila.anoIngreso().doubleValue(), anoOp, anoVal)) {
            return false;
        }
        for (int i = 0; i < TOTAL_PRACTICAS; i++) {
            if (!compara(fila.notas().get(i), ops[i], vals[i])) return false;
        }
        return true;
    }

    // Sin operador o sin valor válido, el filtro no se aplica.
    // Con filtro activo, una nota vacía no pasa.
    private static boolean compara(Double valor, String op, String refTxt) {
        Double ref = numero(refTxt);
        if (op.isEmpty() || ref == null) return true;
        if (valor == null) return false;
        return switch (op) {
            case "gt" -> valor > ref;
            case "gte" -> valor >= ref;
            case "lt" -> valor < ref;
            case "lte" -> valor <= ref;
            case "eq" -> Math.abs(valor - ref) < 0.001;
            default -> true;
        };
    }

    private static boolean contiene(String texto, String buscado) {
        return texto != null && texto.toLowerCase().contains(buscado.toLowerCase());
    }

    // Acepta coma o punto decimal
    private static Double numero(String txt) {
        if (txt == null || txt.isBlank()) return null;
        try {
            return Double.parseDouble(txt.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String limpio(String s) {
        return s == null ? "" : s.trim();
    }

    // Getters para que la plantilla vuelva a mostrar lo que se escribió
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getAnoOp() { return anoOp; }
    public String getAnoVal() { return anoVal; }
    public String[] getOps() { return ops; }
    public String[] getVals() { return vals; }
}