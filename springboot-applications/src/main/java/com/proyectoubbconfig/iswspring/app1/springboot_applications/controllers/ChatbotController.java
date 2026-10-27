package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    private final RestClient restClient;

    public ChatbotController(@Value("${chatbot.service.url:http://localhost:8000}") String chatbotUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(chatbotUrl)
                .build();
    }

    public record ChatRequestDTO(String mensaje) {}
    public record PythonRagRequest(String mensaje, String session_id) {}
    public record ChatResponseDTO(String respuesta) {}

    @PostMapping("/preguntar")
    public ResponseEntity<ChatResponseDTO> preguntar(
            @RequestBody ChatRequestDTO request,
            Authentication authentication) {

        if (request.mensaje() == null || request.mensaje().isBlank()) {
            return ResponseEntity.badRequest().body(new ChatResponseDTO("Por favor escribe una consulta."));
        }

        // Usamos el RUT de la sesión de Spring Security como session_id para la memoria de LangChain
        String rutSesion = authentication.getName();

        try {
            ChatResponseDTO respuestaPython = restClient.post()
                    .uri("/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PythonRagRequest(request.mensaje().trim(), rutSesion))
                    .retrieve()
                    .body(ChatResponseDTO.class);

            return ResponseEntity.ok(respuestaPython);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ChatResponseDTO("El asistente virtual no está disponible en este momento."));
        }
    }
}