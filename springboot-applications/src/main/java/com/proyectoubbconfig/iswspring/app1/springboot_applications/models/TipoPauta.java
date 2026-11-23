package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

public enum TipoPauta {
    ESPECIALIDAD("Especialidad", 20, 1),
    ORIENTACION("Orientación", 18, 2);

    public static final int NOTA_MAXIMA = 7;

    private final String etiqueta;
    private final int totalCriterios;
    private final int notaMinima;

    TipoPauta(String etiqueta, int totalCriterios, int notaMinima) {
        this.etiqueta = etiqueta;
        this.totalCriterios = totalCriterios;
        this.notaMinima = notaMinima;
    }

    public String getEtiqueta() { return etiqueta; }
    public int getTotalCriterios() { return totalCriterios; }
    public int getNotaMinima() { return notaMinima; }
    public int getPuntajeMaximo() { return totalCriterios * NOTA_MAXIMA; }

    /** Tolerante: cualquier valor desconocido o nulo cae en ESPECIALIDAD. */
    public static TipoPauta desde(String valor) {
        if (valor == null) return ESPECIALIDAD;
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ESPECIALIDAD;
        }
    }
}