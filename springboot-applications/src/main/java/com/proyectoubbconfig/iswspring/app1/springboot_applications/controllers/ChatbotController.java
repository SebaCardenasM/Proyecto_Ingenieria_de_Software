package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.InteraccionIARepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.ChatbotService;

@Controller
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private InteraccionIARepository interaccionIARepository;

    public record ChatRequest(String mensaje) {}
    public record ChatResponse(String respuesta) {}

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

    // Vista de Analíticas para el Profesor / Coordinador (HU 2)
    @GetMapping("/profesor/analiticas-ia")
    public String verAnaliticasIa(Model model) {
        model.addAttribute("frecuentes", interaccionIARepository.obtenerDudasMasFrecuentes());
        model.addAttribute("ultimasDudas", interaccionIARepository.findTop20ByOrderByFechaDesc());
        return "Profesor/analiticas-ia";
    }
}