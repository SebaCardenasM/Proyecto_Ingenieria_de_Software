package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.InteraccionIA;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.InteraccionIARepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.ChatbotService;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.ConfiguracionChatbotService;
import java.util.Map;

@Controller
public class ChatbotController {

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private InteraccionIARepository interaccionIARepository;

    @Autowired
    private ConfiguracionChatbotService configuracionChatbotService;

    public record ChatRequest(String mensaje) {}
    public record ChatResponse(String respuesta) {}

    @GetMapping("/api/chat/configuracion-inicial")
    @ResponseBody
    public ResponseEntity<Map<String, String>> configuracionInicial() {
        return ResponseEntity.ok(Map.of(
                "saludo", configuracionChatbotService.obtenerSaludoInicial(),
                "preguntas", configuracionChatbotService.obtenerPreguntasDiagnosticas()
        ));
    }

    // Endpoint para HU 1, 3, 4 y 5
    @PostMapping("/api/chat/preguntar")
    @ResponseBody
    public ResponseEntity<ChatResponse> preguntar(@RequestBody ChatRequest request, Authentication authentication) {
        if (request.mensaje() == null || request.mensaje().isBlank()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Escribe una consulta."));
        }
        try {
            String respuesta = chatbotService.procesarConsulta(authentication.getName(), request.mensaje().trim());
            return ResponseEntity.ok(new ChatResponse(respuesta));
        } catch (RuntimeException e) {
            // Cualquier error inesperado (BD, embeddings, etc.): el JS siempre recibe "respuesta"
            return ResponseEntity.status(500)
                    .body(new ChatResponse("Ocurrió un error al procesar tu consulta. Intenta de nuevo."));
        }
    }

    // Paso 2 de la cadena: revisión de un borrador con el asistente (solo estudiantes, ver SecurityConfig)
    @PostMapping("/api/chat/revisar/{id}")
    @ResponseBody
    public ResponseEntity<ChatResponse> revisarDocumento(@PathVariable Long id, Authentication authentication) {
        try {
            String revision = chatbotService.revisarDocumento(authentication.getName(), id);
            return ResponseEntity.ok(new ChatResponse(revision));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ChatResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(new ChatResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(new ChatResponse("Ocurrió un error al revisar el documento. Intenta de nuevo."));
        }
    }
        public record MensajeHistorial(String pregunta, String respuesta, LocalDateTime fecha) {}

    // Historial del propio usuario: lo carga el panel del chatbot al abrirse
    @GetMapping("/api/chat/historial")
    @ResponseBody
    public List<MensajeHistorial> historial(Authentication authentication) {
        List<InteraccionIA> ultimas = new ArrayList<>(interaccionIARepository
                .findTop30ByRutEstudianteAndCategoriaDudaNotOrderByFechaDesc(
                        authentication.getName(), ChatbotService.CATEGORIA_REVISION));
        Collections.reverse(ultimas);
        return ultimas.stream()
                .map(i -> new MensajeHistorial(i.getPromptUsuario(), i.getRespuestaIa(), i.getFecha()))
                .toList();
    }

    // Resumen con IA: coordinación (todos) o profesor de asignatura (solo estudiantes de sus prácticas)
    @PostMapping("/api/chat/resumen/{rut}")
    @ResponseBody
    public ResponseEntity<ChatResponse> resumirEstudiante(@PathVariable String rut, Authentication authentication) {
        boolean esCoordinador = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_COORDINADOR"));
        if (!esCoordinador
                && !practicaRepository.existsByProfesorRutAndEstudianteRut(authentication.getName(), rut)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ChatResponse("No tienes acceso a los mensajes de este estudiante."));
        }
        try {
            return ResponseEntity.ok(new ChatResponse(chatbotService.resumirEstudiante(rut)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ChatResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(new ChatResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(new ChatResponse("Ocurrió un error al generar el resumen. Intenta de nuevo."));
        }
    }
    

    // Vista de Analíticas (HU 2). Coordinación ve todo; profesor de asignatura y tutor
    // solo ven las consultas de los estudiantes de sus propias prácticas.
    @GetMapping("/profesor/analiticas-ia")
    public String verAnaliticasIa(Model model, Authentication authentication) {
        if (tieneRol(authentication, "ROLE_COORDINADOR")) {
            model.addAttribute("frecuentes", interaccionIARepository.obtenerDudasMasFrecuentes());
            model.addAttribute("ultimasDudas", interaccionIARepository.findTop20ByOrderByFechaDesc());
            return "Profesor/analiticas-ia";
        }

        Set<String> ruts = new HashSet<>();
        if (tieneRol(authentication, "ROLE_PROFESOR_ASIGNATURA")) {
            ruts.addAll(practicaRepository.findRutsEstudiantesDeProfesor(authentication.getName()));
        }
        if (tieneRol(authentication, "ROLE_TUTOR")) {
            ruts.addAll(practicaRepository.findRutsEstudiantesDeTutor(authentication.getName()));
        }

        if (ruts.isEmpty()) {   // sin estudiantes: no se consulta (evita un IN vacío)
            model.addAttribute("frecuentes", List.of());
            model.addAttribute("ultimasDudas", List.of());
        } else {
            model.addAttribute("frecuentes", interaccionIARepository.obtenerDudasMasFrecuentesDe(ruts));
            model.addAttribute("ultimasDudas", interaccionIARepository.findTop20ByRutEstudianteInOrderByFechaDesc(ruts));
        }
        return "Profesor/analiticas-ia";
    }

    private boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(rol));
    }
}