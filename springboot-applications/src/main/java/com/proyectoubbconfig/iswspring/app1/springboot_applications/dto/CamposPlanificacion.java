package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Estructura del formato de planificación diversificada (única fuente: formulario, validación y texto para la IA). */
public final class CamposPlanificacion {

    public record Campo(String clave, String etiqueta, String tipo, int max, boolean requerido, String ayuda) {
        public int filas() { return max > 1500 ? 5 : 3; }
    }
    public record Seccion(String titulo, List<Campo> campos) {}

    private CamposPlanificacion() {}

    private static Campo txt(String k, String e, int max, boolean req, String ayuda) { return new Campo(k, e, "text", max, req, ayuda); }
    private static Campo area(String k, String e, boolean req, String ayuda) { return new Campo(k, e, "textarea", 4000, req, ayuda); }
    private static Campo fecha(String k, String e, boolean req) { return new Campo(k, e, "date", 10, req, ""); }

    public static final List<Seccion> SECCIONES = List.of(
        new Seccion("1. Identificación", List.of(
            txt("establecimiento", "Establecimiento", 150, true, ""),
            txt("equipo", "Equipo de co-enseñanza", 300, false, "Docente en formación, profesor/a colaborador/a, docente PIE"),
            txt("asignatura", "Asignatura", 80, true, ""),
            txt("curso", "Curso", 60, true, "Ej: 1° Medio A"),
            txt("unidad", "Unidad o eje temático", 150, true, ""),
            fecha("fecha", "Fecha de la clase", true),
            txt("duracion", "Duración de la clase", 60, true, "Ej: 90 minutos")
        )),
        new Seccion("2. Caracterización del grupo-curso", List.of(
            area("neet", "Estudiantes con NEET (cantidad y diagnósticos)", false, ""),
            area("neep", "Estudiantes con NEEP (cantidad y diagnósticos)", false, ""),
            txt("estilos", "Estilos de aprendizaje", 150, false, "% visual / % kinestésico / % auditivo"),
            area("otrosDatos", "Otros datos de importancia", false, "")
        )),
        new Seccion("3. Objetivos", List.of(
            area("oa", "Objetivo de aprendizaje (OA) / indicadores", true, ""),
            area("oat", "Objetivo de aprendizaje transversal (actitud)", true, ""),
            area("objetivoClase", "Objetivo de la clase", true,
                 "Un solo verbo de habilidad + contenido + actitud (OAT).")
        )),
        new Seccion("4. Recursos y evaluación", List.of(
            area("recursos", "Recursos didácticos", true, "Todo recurso debe ir como anexo."),
            area("evaluacion", "Evaluación de la clase", true,
                 "Tipo, instrumento, pauta de corrección y cómo se evalúa el objetivo transversal."),
            area("dua", "Principios DUA a utilizar", false, "Representación, acción y expresión, compromiso.")
        )),
        new Seccion("5. Descripción de la clase", List.of(
            area("inicio", "Inicio (indica minutos)", true, "Actividades del docente y de los estudiantes, interacciones y recursos."),
            area("inicioDua", "Diversificación en el inicio (principio DUA y acciones)", false, ""),
            area("desarrollo", "Desarrollo (indica minutos)", true, ""),
            area("desarrolloDua", "Diversificación en el desarrollo (principio DUA y acciones)", false, ""),
            area("cierre", "Cierre (indica minutos)", true, "Síntesis, actividad breve de evaluación, indicaciones para la próxima clase."),
            area("cierreDua", "Diversificación en el cierre (principio DUA y acciones)", false, "")
        )),
        new Seccion("6. Errores, referencias y anexos", List.of(
            area("errores", "Errores y dificultades (y cómo los abordarás)", true, ""),
            area("referencias", "Referencias bibliográficas", true, "Formato APA."),
            area("anexos", "Anexos (enlaces con acceso abierto en modo editor)", false, "")
        ))
    );

    /** Etiquetas de los campos obligatorios que están vacíos. */
    public static List<String> faltantes(Map<String, String> valores) {
        List<String> faltan = new ArrayList<>();
        for (Seccion s : SECCIONES)
            for (Campo c : s.campos())
                if (c.requerido() && (valores.get(c.clave()) == null || valores.get(c.clave()).isBlank()))
                    faltan.add(c.etiqueta());
        return faltan;
    }

    /** Texto plano de la planificación (lo lee el asistente en la revisión). */
    public static String aTexto(Map<String, String> valores) {
        StringBuilder sb = new StringBuilder();
        for (Seccion s : SECCIONES) {
            sb.append(s.titulo()).append('\n');
            for (Campo c : s.campos()) {
                String v = valores.get(c.clave());
                if (v != null && !v.isBlank()) sb.append(c.etiqueta()).append(": ").append(v).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }
}