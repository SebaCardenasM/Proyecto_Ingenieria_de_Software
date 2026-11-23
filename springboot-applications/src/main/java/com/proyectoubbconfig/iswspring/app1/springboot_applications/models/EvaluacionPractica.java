package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modelo único de evaluación ponderada por práctica.
 *
 * - Cuando practica == null: almacena la estructura base de componentes y pesos (%)
 *   para un numeroPractica (1 a 6).
 * - Cuando practica != null: almacena la evaluación individual de una práctica de un
 *   estudiante, conteniendo en sus arrays/lista cada componente con su peso (%) y su nota.
 */
@Entity
@Table(name = "evaluacion_practica")
public class EvaluacionPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_practica", nullable = false)
    private Integer numeroPractica;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", unique = true, nullable = true)
    private Practica practica;

    @Column(name = "personalizada", nullable = false)
    private boolean personalizada = false;

    @Column(name = "componentes_json", columnDefinition = "TEXT", nullable = false)
    private String componentesJson = "[]";

    @Column(name = "nota_final")
    private Double notaFinal;

    @Column(name = "porcentaje_evaluado")
    private Double porcentajeEvaluado = 0.0;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public EvaluacionPractica() {
    }

    /**
     * Subestructura embebida en este mismo modelo para representar cada componente
     * de evaluación (identificador, nombre, peso en % y nota opcional 1.0 a 7.0).
     */
    public static class Componente {
        private String uid;
        private String nombre;
        private Double peso;
        private Double nota;
        private boolean bloqueada = false;
        private String origen;

        public Componente() {
        }

        public Componente(String uid, String nombre, Double peso, Double nota) {
            this(uid, nombre, peso, nota, false, null);
        }

        public Componente(String uid, String nombre, Double peso, Double nota, boolean bloqueada, String origen) {
            this.uid = uid;
            this.nombre = nombre;
            this.peso = peso;
            this.nota = nota;
            this.bloqueada = bloqueada;
            this.origen = origen;
        }

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public Double getPeso() { return peso; }
        public void setPeso(Double peso) { this.peso = peso; }

        public Double getNota() { return nota; }
        public void setNota(Double nota) { this.nota = nota; }

        public boolean isBloqueada() { return bloqueada; }
        public void setBloqueada(boolean bloqueada) { this.bloqueada = bloqueada; }

        public String getOrigen() { return origen; }
        public void setOrigen(String origen) { this.origen = origen; }

        public Double getAportePonderado() {
            if (nota == null || peso == null) return null;
            return Math.round((nota * (peso / 100.0)) * 100.0) / 100.0;
        }
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.fechaActualizacion = LocalDateTime.now();
        if (this.componentesJson == null || this.componentesJson.isBlank()) {
            this.componentesJson = "[]";
        }
    }

    // ------------------------------------------------------------------
    // Acceso a los componentes como Lista y como Arrays
    // ------------------------------------------------------------------

    @Transient
    public List<Componente> getComponentes() {
        return parsearJson(this.componentesJson);
    }

    public void setComponentes(List<Componente> componentes) {
        this.componentesJson = serializarJson(componentes);
        recalcularResumen(componentes);
    }

    @Transient
    public Componente[] getComponentesArray() {
        List<Componente> lista = getComponentes();
        return lista.toArray(new Componente[0]);
    }

    public void setComponentesArray(Componente[] componentes) {
        if (componentes == null) {
            setComponentes(new ArrayList<>());
        } else {
            setComponentes(Arrays.asList(componentes));
        }
    }

    @Transient
    public String[] getNombresComponentes() {
        List<Componente> lista = getComponentes();
        String[] arr = new String[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            arr[i] = lista.get(i).getNombre();
        }
        return arr;
    }

    @Transient
    public Double[] getPesosComponentes() {
        List<Componente> lista = getComponentes();
        Double[] arr = new Double[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            arr[i] = lista.get(i).getPeso();
        }
        return arr;
    }

    @Transient
    public Double[] getNotasComponentes() {
        List<Componente> lista = getComponentes();
        Double[] arr = new Double[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            arr[i] = lista.get(i).getNota();
        }
        return arr;
    }

    // ------------------------------------------------------------------
    // Cálculos ponderados
    // ------------------------------------------------------------------

    public void recalcularResumen() {
        recalcularResumen(getComponentes());
    }

    private void recalcularResumen(List<Componente> componentes) {
        if (componentes == null || componentes.isEmpty()) {
            this.notaFinal = null;
            this.porcentajeEvaluado = 0.0;
            return;
        }
        double sumaPonderada = 0.0;
        double pesoEval = 0.0;
        for (Componente c : componentes) {
            if (c.getNota() != null && c.getPeso() != null && c.getPeso() > 0) {
                sumaPonderada += c.getNota() * c.getPeso();
                pesoEval += c.getPeso();
            }
        }
        this.porcentajeEvaluado = Math.round(pesoEval * 100.0) / 100.0;
        if (pesoEval > 0.0) {
            double promedio = sumaPonderada / pesoEval;
            this.notaFinal = Math.round(promedio * 10.0) / 10.0;
        } else {
            this.notaFinal = null;
        }
    }

    @Transient
    public double getPorcentajeTotal() {
        double total = 0.0;
        for (Componente c : getComponentes()) {
            if (c.getPeso() != null) {
                total += c.getPeso();
            }
        }
        return Math.round(total * 100.0) / 100.0;
    }

    @Transient
    public double getNotaAcumulada() {
        double acum = 0.0;
        for (Componente c : getComponentes()) {
            if (c.getNota() != null && c.getPeso() != null) {
                acum += c.getNota() * (c.getPeso() / 100.0);
            }
        }
        return Math.round(acum * 100.0) / 100.0;
    }

    @Transient
    public Double getNotaNecesariaAprobacion() {
        double restante = Math.round((100.0 - (porcentajeEvaluado != null ? porcentajeEvaluado : 0.0)) * 100.0) / 100.0;
        if (restante <= 0.009) {
            return null;
        }
        double necesaria = (4.0 - getNotaAcumulada()) / (restante / 100.0);
        return Math.round(necesaria * 10.0) / 10.0;
    }

    @Transient
    public boolean isCompleta() {
        return porcentajeEvaluado != null && porcentajeEvaluado >= 99.99;
    }

    // ------------------------------------------------------------------
    // Serialización / deserialización JSON sin dependencias externas
    // ------------------------------------------------------------------

    public static String serializarJson(List<Componente> lista) {
        if (lista == null || lista.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Componente c = lista.get(i);
            if (i > 0) sb.append(',');
            sb.append('{');
            sb.append("\"uid\":\"").append(escapar(c.getUid())).append("\",");
            sb.append("\"nombre\":\"").append(escapar(c.getNombre())).append("\",");
            sb.append("\"peso\":").append(c.getPeso() != null ? c.getPeso() : 0.0).append(',');
            sb.append("\"nota\":").append(c.getNota() != null ? c.getNota() : "null").append(',');
            sb.append("\"bloqueada\":").append(c.isBloqueada()).append(',');
            sb.append("\"origen\":").append(c.getOrigen() != null ? "\"" + escapar(c.getOrigen()) + "\"" : "null");
            sb.append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    public static List<Componente> parsearJson(String json) {
        List<Componente> resultado = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return resultado;
        }
        int len = json.length();
        int i = 0;

        while (i < len) {
            while (i < len && json.charAt(i) != '{') i++;
            if (i >= len) break;
            i++; // saltar '{'

            Componente comp = new Componente();
            while (i < len) {
                i = saltarEspaciosYComas(json, i);
                if (i >= len || json.charAt(i) == '}') {
                    i++;
                    break;
                }
                if (json.charAt(i) != '"') {
                    i++;
                    continue;
                }
                int[] nextIdx = new int[1];
                String clave = leerCadenaJson(json, i, nextIdx);
                i = nextIdx[0];

                while (i < len && json.charAt(i) != ':') i++;
                i++; // saltar ':'
                while (i < len && Character.isWhitespace(json.charAt(i))) i++;
                if (i >= len) break;

                if (json.charAt(i) == '"') {
                    String valor = leerCadenaJson(json, i, nextIdx);
                    i = nextIdx[0];
                    if ("uid".equals(clave)) comp.setUid(valor);
                    else if ("nombre".equals(clave)) comp.setNombre(valor);
                    else if ("origen".equals(clave)) comp.setOrigen(valor);
                } else {
                    int inicio = i;
                    while (i < len && json.charAt(i) != ',' && json.charAt(i) != '}' && !Character.isWhitespace(json.charAt(i))) {
                        i++;
                    }
                    String token = json.substring(inicio, i).trim();
                    if (!"null".equalsIgnoreCase(token) && !token.isEmpty()) {
                        if ("bloqueada".equals(clave)) {
                            comp.setBloqueada("true".equalsIgnoreCase(token));
                        } else {
                            try {
                                double num = Double.parseDouble(token);
                                if ("peso".equals(clave)) comp.setPeso(num);
                                else if ("nota".equals(clave)) comp.setNota(num);
                            } catch (NumberFormatException ignorado) {
                            }
                        }
                    }
                }
            }
            if (comp.getNombre() != null || comp.getUid() != null) {
                resultado.add(comp);
            }
        }
        return resultado;
    }

    private static int saltarEspaciosYComas(String s, int i) {
        int len = s.length();
        while (i < len && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) {
            i++;
        }
        return i;
    }

    private static String leerCadenaJson(String s, int i, int[] nextIdx) {
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        i++; // saltar '"' inicial
        while (i < len) {
            char c = s.charAt(i++);
            if (c == '"') {
                break;
            }
            if (c == '\\' && i < len) {
                char esc = s.charAt(i++);
                switch (esc) {
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    default:   sb.append(esc); break;
                }
            } else {
                sb.append(c);
            }
        }
        nextIdx[0] = i;
        return sb.toString();
    }

    private static String escapar(String texto) {
        if (texto == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': break;
                default:   sb.append(c < 0x20 ? ' ' : c);
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Getters y Setters de entidad
    // ------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroPractica() { return numeroPractica; }
    public void setNumeroPractica(Integer numeroPractica) { this.numeroPractica = numeroPractica; }

    public Practica getPractica() { return practica; }
    public void setPractica(Practica practica) { this.practica = practica; }

    public boolean isPersonalizada() { return personalizada; }
    public void setPersonalizada(boolean personalizada) { this.personalizada = personalizada; }

    public String getComponentesJson() { return componentesJson; }
    public void setComponentesJson(String componentesJson) { this.componentesJson = componentesJson; }

    public Double getNotaFinal() { return notaFinal; }
    public void setNotaFinal(Double notaFinal) { this.notaFinal = notaFinal; }

    public Double getPorcentajeEvaluado() { return porcentajeEvaluado; }
    public void setPorcentajeEvaluado(Double porcentajeEvaluado) { this.porcentajeEvaluado = porcentajeEvaluado; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
