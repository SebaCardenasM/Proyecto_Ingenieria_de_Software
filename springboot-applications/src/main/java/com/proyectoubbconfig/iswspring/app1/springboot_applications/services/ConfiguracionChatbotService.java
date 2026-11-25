package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.ConfiguracionChatbot;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.ConfiguracionChatbotRepository;

@Service
public class ConfiguracionChatbotService {

    public static final String CLAVE_PROMPT = "PROMPT_SISTEMA";
    public static final String CLAVE_PREGUNTAS = "PREGUNTAS_DIAGNOSTICAS";
    public static final String CLAVE_SALUDO = "SALUDO_INICIAL";

    public static final String DEFAULT_PROMPT = """
            Eres RetroPlani Mate V3, el Asistente Virtual Pedagógico de Prácticas de la carrera de Pedagogía en Educación Matemática de la Universidad del Bío-Bío (UBB).
            Tu objetivo es guiar, retroalimentar y orientar formativamente a las y los profesoras/es en formación en el diseño de sus planificaciones diversificadas, rúbricas y dudas de práctica.

            REGLAS PEDAGÓGICAS FUNDAMENTALES (NO NEGOCIABLES):
            1. ROL ORIENTADOR, NO RESOLUTIVO: NUNCA diseñes una clase, planificación, actividad, secuencia didáctica o instrumento desde cero por el estudiante. Tu rol es acompañar, cuestionar y hacer pensar.
            2. ESTRUCTURA DIDÁCTICA: Toda planificación debe articularse en los tres momentos de la clase: Inicio (activación de conocimientos previos y presentación clara del objetivo), Desarrollo (modelamiento, tareas matemáticas pertinentes, interacción) y Cierre (metacognición, verificación del logro y síntesis).
            3. ENFOQUE DUA Y DIVERSIFICACIÓN: Promueve los 3 principios del Diseño Universal para el Aprendizaje: Múltiples formas de representación, Múltiples formas de acción y expresión, y Múltiples formas de implicación/compromiso.
            4. RIGOR MATEMÁTICO: Promueve el desarrollo de las 4 habilidades curriculares clave: Resolver problemas, Modelar, Representar, y Argumentar y comunicar.
            5. PREGUNTAS DIAGNÓSTICAS ANTES DE RETROALIMENTAR: Cuando el estudiante consulte o comparta un diseño, antes de entregar conclusiones cerradas, formula preguntas diagnósticas que le permitan reflexionar sobre sus decisiones pedagógicas.
            6. PREGUNTAS DE REFLEXIÓN FINAL: Cierra tus intervenciones con preguntas orientadas a la acción y a la mejora continua.
            """;

    public static final String DEFAULT_PREGUNTAS = """
            1. Habilidad matemática principal: El objetivo de clase menciona una actividad, pero ¿cuál es la habilidad matemática principal (resolver problemas, modelar, representar, argumentar y comunicar) que esperas que desarrollen los estudiantes?
            2. Instrumento de evaluación formativa: Indicas que usarás evaluación formativa, pero ¿qué instrumento concreto utilizarás (ticket de salida, rúbrica, lista de cotejo o pauta de corrección)?
            3. Secuencia didáctica y modelamiento: En el desarrollo los estudiantes trabajan con una guía; ¿qué actividad o modelamiento previo les permitirá aprender o practicar el procedimiento antes de resolverla de forma autónoma?
            4. Acciones concretas DUA: Mencionas DUA, pero ¿qué apoyos o diversificación específicos usarás en cada momento para representación, acción/expresión o compromiso?
            5. Tratamiento de errores frecuentes: En errores y dificultades señalas posibles confusiones; ¿qué harás pedagógicamente si esos errores aparecen durante la clase?
            """;

    public static final String DEFAULT_SALUDO = """
            ¡Hola! Soy **RetroPlani Mate**, tu asistente pedagógico en Educación Matemática de la Universidad del Bío-Bío. Mi propósito es acompañarte y retroalimentar formativamente tus planificaciones y dudas del proceso de práctica pedagógica y profesional.
            """;

    @Autowired
    private ConfiguracionChatbotRepository repository;

    @Transactional(readOnly = true)
    public String obtenerPromptSistema() {
        return repository.findById(CLAVE_PROMPT)
                .map(ConfiguracionChatbot::getContenido)
                .filter(s -> !s.isBlank())
                .orElse(DEFAULT_PROMPT);
    }

    @Transactional(readOnly = true)
    public String obtenerPreguntasDiagnosticas() {
        return repository.findById(CLAVE_PREGUNTAS)
                .map(ConfiguracionChatbot::getContenido)
                .filter(s -> !s.isBlank())
                .orElse(DEFAULT_PREGUNTAS);
    }

    @Transactional(readOnly = true)
    public String obtenerSaludoInicial() {
        return repository.findById(CLAVE_SALUDO)
                .map(ConfiguracionChatbot::getContenido)
                .filter(s -> !s.isBlank())
                .orElse(DEFAULT_SALUDO);
    }

    @Transactional
    public void guardarConfiguracion(String prompt, String preguntas, String saludo, String usuario) {
        guardarOActualizar(CLAVE_PROMPT, prompt != null && !prompt.isBlank() ? prompt : DEFAULT_PROMPT,
                "Instrucciones generales de rol y límites pedagógicos del asistente", usuario);
        guardarOActualizar(CLAVE_PREGUNTAS, preguntas != null && !preguntas.isBlank() ? preguntas : DEFAULT_PREGUNTAS,
                "Preguntas diagnósticas e introductorias oficiales de la carrera", usuario);
        guardarOActualizar(CLAVE_SALUDO, saludo != null && !saludo.isBlank() ? saludo : DEFAULT_SALUDO,
                "Saludo y mensaje de bienvenida inicial del asistente", usuario);
    }

    @Transactional
    public void restablecerPorDefecto(String usuario) {
        guardarConfiguracion(DEFAULT_PROMPT, DEFAULT_PREGUNTAS, DEFAULT_SALUDO, usuario);
    }

    private void guardarOActualizar(String clave, String contenido, String descripcion, String usuario) {
        ConfiguracionChatbot config = repository.findById(clave).orElse(new ConfiguracionChatbot());
        config.setClave(clave);
        config.setContenido(contenido);
        config.setDescripcion(descripcion);
        config.setActualizadoPor(usuario != null ? usuario : "COORDINADOR");
        config.setFechaActualizacion(LocalDateTime.now());
        repository.save(config);
    }
}
