package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.SeccionPautaDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.PlantillaPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PlantillaPautaRepository;

@Service
public class PlantillaPautaService {

    private static final Logger log = LoggerFactory.getLogger(PlantillaPautaService.class);

    @Autowired
    private PlantillaPautaRepository plantillaRepository;

    public static final String DEFAULT_ESPECIALIDAD_JSON = "["
            + "{\"titulo\":\"ASPECTOS DE LA ESTRUCTURA DE LA CLASE\",\"items\":["
            + "\"El objetivo de la clase se presenta a los estudiantes. Es claro y pertinente al nivel.\","
            + "\"El profesor/a desarrolla un inicio de la clase generando retroalimentación y/o activación de conocimientos previos.\","
            + "\"El profesor/a realiza un desarrollo de la clase de manera pertinente al objetivo planteado.\","
            + "\"El profesor/a realiza un desarrollo de la clase con tareas matemáticas pertinentes y coherentes con el objetivo planteado.\","
            + "\"El profesor/a realiza un cierre de la clase con actividad(es) clave(s) para la verificación del cumplimiento del objetivo planteado.\","
            + "\"El profesor/a realiza un cierre de la clase integrando y/o sintetizando los aprendizajes de la clase.\""
            + "]},"
            + "{\"titulo\":\"TAREAS MATEMÁTICAS PROPUESTAS\",\"items\":["
            + "\"El profesor/a hace uso de los errores y dificultades de los estudiantes como una instancia de devolución.\","
            + "\"El profesor/a propone tareas matemáticas que se relacionen con alguna de las habilidades de: resolución de problemas, representación, modelación y/o argumentación y comunicación.\","
            + "\"Las tareas matemáticas están contextualizadas al entorno de los estudiantes.\","
            + "\"El profesor/a propone variadas estrategias de resolución de las tareas matemáticas presentadas.\","
            + "\"El profesor/a explica, comprueba y/o demuestra las definiciones, teoremas, proposiciones o procedimientos de manera clara y adecuadas al nivel educativo.\","
            + "\"El profesor/a utiliza estrategias de trabajo colaborativo para las/os estudiantes en las actividades de la clase.\""
            + "]},"
            + "{\"titulo\":\"AMBIENTE DE AULA Y RECURSOS DE APRENDIZAJE\",\"items\":["
            + "\"El profesor/a hace uso de un lenguaje acorde al nivel de enseñanza.\","
            + "\"El profesor/a genera oportunidades de participación de los estudiantes durante toda la clase.\","
            + "\"El profesor/a propicia un ambiente para el desarrollo de toda la clase.\","
            + "\"El profesor/a brinda atención a todos los estudiantes del aula.\","
            + "\"El profesor/a hace uso de materiales y/o recursos para el desarrollo de los aprendizajes en la clase.\","
            + "\"El profesor/a promueve la autoestima, evitando el rechazo, fobia o miedo a la matemática.\","
            + "\"El profesor/a promueve el diálogo y comunicación entre estudiantes favoreciendo la inclusión en la clase.\","
            + "\"El profesor/a realiza observación sistemática del proceso cognitivo de los estudiantes durante toda la clase.\""
            + "]}"
            + "]";

    public static final String DEFAULT_ORIENTACION_JSON = "["
            + "{\"titulo\":\"DIMENSIÓN A: DISEÑO DE LA CLASE Y PLANIFICACIÓN DE LA ENSEÑANZA\",\"items\":["
            + "\"Comunica a las/os estudiantes los propósitos de la clase y los aprendizajes a lograr.\","
            + "\"Utiliza el tiempo disponible para la enseñanza en función de los objetivos de la clase.\","
            + "\"Especifica en las unidades pedagógicas de orientación, los tres momentos de la clase: inicio, desarrollo y cierre.\""
            + "]},"
            + "{\"titulo\":\"DIMENSIÓN B: GENERACIÓN DE AMBIENTE PROPICIO PARA EL APRENDIZAJE\",\"items\":["
            + "\"Formula preguntas y concede el tiempo necesario para resolverlos.\","
            + "\"Posee la capacidad de empatía, de atención personalizada propiciada por la clase y escucha activamente a los y las estudiantes.\","
            + "\"Establece un clima de relaciones interpersonales respetuosas y empáticas con sus estudiantes.\","
            + "\"Establece normas de comportamiento que son conocidas y comprensibles para sus estudiantes.\""
            + "]},"
            + "{\"titulo\":\"DIMENSIÓN C: USO DE METODOLOGÍAS Y DIDÁCTICA PARA LA ENSEÑANZA\",\"items\":["
            + "\"Utiliza diferentes metodologías pedagógicas, que motiven el interés de los y las estudiantes durante las clases.\","
            + "\"Ofrece oportunidades y desafíos de participación activas a los y las estudiantes del grupo curso, considerando su diversidad.\","
            + "\"Demuestra conocer variadas estrategias de enseñanza y actividades congruentes con el proceso de enseñanza y aprendizaje.\""
            + "]},"
            + "{\"titulo\":\"DIMENSIÓN D: EVALUACIÓN DE LOS APRENDIZAJES\",\"items\":["
            + "\"Emplea instrumentos de evaluación, acorde a las temáticas desarrolladas en el grupo curso.\","
            + "\"Utiliza criterios de evaluación coherentes con los objetivos de aprendizaje propuestos.\","
            + "\"Evalúa el grado en que las/os estudiantes alcanzaron los aprendizajes esperados.\""
            + "]},"
            + "{\"titulo\":\"DIMENSIÓN E: EVALUACIÓN DE SU DESEMPEÑO\",\"items\":["
            + "\"Reflexiona sobre la propia práctica pedagógica, considerando la capacidad de preparar y gestionar curricularmente sus clases en el área de orientación.\","
            + "\"Evalúa el nivel de logro de los objetivos de aprendizaje planteados, valorando los aciertos y analizando los errores.\","
            + "\"Evidencia el cumplimiento de los compromisos contraídos, presentación personal, puntualidad y responsabilidad profesional.\","
            + "\"Analiza críticamente su desempeño durante las clases, a partir de los resultados de aprendizaje de las y los estudiantes.\","
            + "\"Presenta anticipadamente la planificación de la clase, de acuerdo a la unidad didáctica, para su debida retroalimentación.\""
            + "]}"
            + "]";

    @Transactional
    public PlantillaPauta obtenerOAsegurarPlantilla(TipoPauta tipo) {
        if (tipo == null) tipo = TipoPauta.ESPECIALIDAD;
        final TipoPauta tFinal = tipo;
        return plantillaRepository.findById(tipo).orElseGet(() -> {
            String titulo = (tFinal == TipoPauta.ORIENTACION)
                    ? "Pauta de Observación - Orientación"
                    : "Pauta de Observación - Especialidad / Matemática";
            String desc = (tFinal == TipoPauta.ORIENTACION)
                    ? "Formulario web de observación de clases para prácticas en el área de orientación pedagógica."
                    : "Formulario web de observación de clases para prácticas en el área de especialidad matemática.";
            String jsonDef = (tFinal == TipoPauta.ORIENTACION) ? DEFAULT_ORIENTACION_JSON : DEFAULT_ESPECIALIDAD_JSON;
            PlantillaPauta nueva = new PlantillaPauta(tFinal, titulo, desc, jsonDef, "SISTEMA");
            return plantillaRepository.save(nueva);
        });
    }

    @Transactional(readOnly = true)
    public String obtenerEsquemaActivoJson(TipoPauta tipo) {
        PlantillaPauta p = obtenerOAsegurarPlantilla(tipo);
        return (p.getEsquemaJson() != null && !p.getEsquemaJson().isBlank())
                ? p.getEsquemaJson()
                : obtenerEsquemaDefault(tipo);
    }

    @Transactional(readOnly = true)
    public List<SeccionPautaDTO> parsearSecciones(String json) {
        List<SeccionPautaDTO> secciones = new ArrayList<>();
        if (json == null || json.isBlank()) return secciones;

        int len = json.length();
        int i = 0;

        while (i < len) {
            while (i < len && json.charAt(i) != '{') i++;
            if (i >= len) break;
            i++; // saltar '{'

            String titulo = "";
            List<String> items = new ArrayList<>();

            while (i < len && json.charAt(i) != '}') {
                i = saltarEspaciosYComas(json, i);
                if (i >= len || json.charAt(i) == '}') break;

                if (json.charAt(i) != '"') {
                    i++;
                    continue;
                }
                int[] nextIdx = new int[1];
                String clave = leerCadenaJson(json, i, nextIdx);
                i = nextIdx[0];

                while (i < len && json.charAt(i) != ':') i++;
                i++; // saltar ':'
                i = saltarEspaciosYComas(json, i);
                if (i >= len) break;

                if ("titulo".equalsIgnoreCase(clave)) {
                    if (json.charAt(i) == '"') {
                        titulo = leerCadenaJson(json, i, nextIdx);
                        i = nextIdx[0];
                    }
                } else if ("items".equalsIgnoreCase(clave)) {
                    if (json.charAt(i) == '[') {
                        i++; // saltar '['
                        while (i < len && json.charAt(i) != ']') {
                            i = saltarEspaciosYComas(json, i);
                            if (i >= len || json.charAt(i) == ']') break;
                            if (json.charAt(i) == '"') {
                                String item = leerCadenaJson(json, i, nextIdx);
                                i = nextIdx[0];
                                if (!item.trim().isEmpty()) {
                                    items.add(item.trim());
                                }
                            } else {
                                i++;
                            }
                        }
                        if (i < len && json.charAt(i) == ']') i++; // saltar ']'
                    }
                } else {
                    i++;
                }
            }
            if (i < len && json.charAt(i) == '}') i++;

            if (!titulo.trim().isEmpty() || !items.isEmpty()) {
                secciones.add(new SeccionPautaDTO(titulo.trim(), items));
            }
        }
        return secciones;
    }

    public static String serializarSecciones(List<SeccionPautaDTO> secciones) {
        if (secciones == null || secciones.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int s = 0; s < secciones.size(); s++) {
            SeccionPautaDTO sec = secciones.get(s);
            if (s > 0) sb.append(',');
            sb.append("{\"titulo\":\"").append(escaparJson(sec.getTitulo())).append("\",\"items\":[");
            List<String> items = sec.getItems();
            if (items != null) {
                for (int it = 0; it < items.size(); it++) {
                    if (it > 0) sb.append(',');
                    sb.append('"').append(escaparJson(items.get(it))).append('"');
                }
            }
            sb.append("]}");
        }
        sb.append(']');
        return sb.toString();
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
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'u':
                        if (i + 4 <= len) {
                            String hex = s.substring(i, i + 4);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException nfe) {
                                sb.append("\\u").append(hex);
                            }
                        }
                        break;
                    default:   sb.append(esc); break;
                }
            } else {
                sb.append(c);
            }
        }
        nextIdx[0] = i;
        return sb.toString();
    }

    public static String escaparJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 32) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public int obtenerTotalCriterios(TipoPauta tipo) {
        String json = obtenerEsquemaActivoJson(tipo);
        List<SeccionPautaDTO> secciones = parsearSecciones(json);
        if (secciones.isEmpty()) {
            return tipo != null ? tipo.getTotalCriterios() : 20;
        }
        int total = 0;
        for (SeccionPautaDTO s : secciones) {
            if (s.getItems() != null) {
                total += s.getItems().size();
            }
        }
        return total > 0 ? total : (tipo != null ? tipo.getTotalCriterios() : 20);
    }

    @Transactional(readOnly = true)
    public int obtenerPuntajeMaximo(TipoPauta tipo) {
        return obtenerTotalCriterios(tipo) * 7;
    }

    @Transactional
    public void guardarDefinitivo(TipoPauta tipo, String esquemaJson, String usuario) {
        validarEsquema(esquemaJson);
        PlantillaPauta p = obtenerOAsegurarPlantilla(tipo);
        p.setEsquemaJson(esquemaJson.trim());
        p.setEsquemaBorradorJson(null);
        p.setTieneBorrador(false);
        p.setFechaActualizacion(LocalDateTime.now());
        p.setActualizadoPor(usuario != null ? usuario : "COORDINADOR");
        plantillaRepository.save(p);
        log.info("Plantilla de pauta {} sobrescrita definitivamente por {}", tipo, usuario);
    }

    @Transactional
    public void guardarBorrador(TipoPauta tipo, String esquemaJson, String usuario) {
        validarEsquema(esquemaJson);
        PlantillaPauta p = obtenerOAsegurarPlantilla(tipo);
        p.setEsquemaBorradorJson(esquemaJson.trim());
        p.setTieneBorrador(true);
        p.setFechaActualizacion(LocalDateTime.now());
        p.setActualizadoPor(usuario != null ? usuario : "COORDINADOR");
        plantillaRepository.save(p);
        log.info("Borrador de plantilla {} guardado por {}", tipo, usuario);
    }

    @Transactional
    public void restablecerPorDefecto(TipoPauta tipo, String usuario) {
        PlantillaPauta p = obtenerOAsegurarPlantilla(tipo);
        p.setEsquemaJson(obtenerEsquemaDefault(tipo));
        p.setEsquemaBorradorJson(null);
        p.setTieneBorrador(false);
        p.setFechaActualizacion(LocalDateTime.now());
        p.setActualizadoPor(usuario != null ? usuario : "COORDINADOR");
        plantillaRepository.save(p);
        log.info("Plantilla de pauta {} restablecida por defecto por {}", tipo, usuario);
    }

    @Transactional
    public void descartarBorrador(TipoPauta tipo) {
        PlantillaPauta p = obtenerOAsegurarPlantilla(tipo);
        p.setEsquemaBorradorJson(null);
        p.setTieneBorrador(false);
        plantillaRepository.save(p);
        log.info("Borrador de plantilla {} descartado", tipo);
    }

    public String obtenerEsquemaDefault(TipoPauta tipo) {
        return (tipo == TipoPauta.ORIENTACION) ? DEFAULT_ORIENTACION_JSON : DEFAULT_ESPECIALIDAD_JSON;
    }

    private void validarEsquema(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("El formulario de la pauta no puede estar vacío.");
        }
        List<SeccionPautaDTO> secciones = parsearSecciones(json);
        if (secciones.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un apartado en la pauta.");
        }
        int totalItems = 0;
        for (SeccionPautaDTO s : secciones) {
            if (s.getTitulo() == null || s.getTitulo().trim().isEmpty()) {
                throw new IllegalArgumentException("Cada apartado debe tener un título asignado.");
            }
            if (s.getItems() == null || s.getItems().isEmpty()) {
                throw new IllegalArgumentException("El apartado '" + s.getTitulo() + "' debe contener al menos un criterio.");
            }
            for (String it : s.getItems()) {
                if (it == null || it.trim().isEmpty()) {
                    throw new IllegalArgumentException("Los criterios de evaluación no pueden estar vacíos.");
                }
                totalItems++;
            }
        }
        if (totalItems == 0) {
            throw new IllegalArgumentException("La pauta debe contener al menos un criterio evaluable.");
        }
    }
}
