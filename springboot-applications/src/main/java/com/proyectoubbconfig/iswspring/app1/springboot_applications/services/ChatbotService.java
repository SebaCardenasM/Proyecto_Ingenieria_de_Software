package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Estudiante;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.InteraccionIA;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EstudianteRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.InteraccionIARepository;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-3.8-flash}")
    private String modelName;

    private final EstudianteRepository estudianteRepository;
    private final InteraccionIARepository interaccionIARepository;

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    private final EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    public ChatbotService(EstudianteRepository estudianteRepository,
                          InteraccionIARepository interaccionIARepository) {
        this.estudianteRepository = estudianteRepository;
        this.interaccionIARepository = interaccionIARepository;
    }

    @PostConstruct
    public void inicializarRag() {
        try {
            ClassPathResource resource = new ClassPathResource("rag-docs");
            if (!resource.exists()) {
                log.warn("Carpeta src/main/resources/rag-docs no encontrada.");
                return;
            }
            Path carpetaPdfs = resource.getFile().toPath();
            if (Files.isDirectory(carpetaPdfs)) {
                List<Document> documentos = FileSystemDocumentLoader.loadDocuments(
                        carpetaPdfs, new ApachePdfBoxDocumentParser()
                );
                if (!documentos.isEmpty()) {
                    EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                            .documentSplitter(DocumentSplitters.recursive(1000, 200))
                            .embeddingModel(embeddingModel)
                            .embeddingStore(embeddingStore)
                            .build();
                    ingestor.ingest(documentos);
                    log.info("RAG inicializado con {} documentos PDF cargados.", documentos.size());
                }
            }
        } catch (IOException | RuntimeException e) {
            log.warn("No se pudieron indexar PDFs al inicio: {}", e.getMessage());
        }
    }

    @Transactional
    public String procesarConsulta(String rutUsuario, String pregunta) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Configura la variable GEMINI_API_KEY en tu archivo .env para activar las respuestas de IA.";
        }

        Integer nivelPractica = detectarNivelPractica(rutUsuario);
        String contextoEstudiante = (nivelPractica != null)
                ? "El usuario es un estudiante cursando actualmente la Práctica Nivel " + nivelPractica + "."
                : "El usuario consulta sobre el proceso general de prácticas.";

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(pregunta).content())
                .maxResults(4)
                .build();

        StringBuilder sbFragmentos = new StringBuilder();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
        for (EmbeddingMatch<TextSegment> match : matches) {
            if (match.embedded() != null && match.embedded().text() != null) {
                if (!sbFragmentos.isEmpty()) {
                    sbFragmentos.append("\n---\n");
                }
                sbFragmentos.append(match.embedded().text());
            }
        }
        String fragmentosPdf = sbFragmentos.toString();

        String promptFinal = """
                Eres el Asistente Virtual de Prácticas Pedagógicas de la Universidad del Bío-Bío.
                Responde siempre en español, de forma clara y en máximo 4 oraciones.
                
                [DATOS DE SESIÓN DEL ALUMNO]:
                %s
                Adapta tu respuesta específicamente a ese nivel de práctica.
                
                [CONTEXTO DE DOCUMENTOS OFICIALES (RÚBRICAS, PAUTAS Y PROGRAMAS)]:
                %s
                
                [PREGUNTA DEL ESTUDIANTE]:
                %s
                """.formatted(
                contextoEstudiante,
                fragmentosPdf.isBlank() ? "Usa tu conocimiento general sobre prácticas pedagógicas." : fragmentosPdf,
                pregunta
        );

        ChatLanguageModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.3)
                .build();

        String respuesta = chatModel.generate(promptFinal);

        String categoria = clasificarDuda(pregunta);
        interaccionIARepository.save(new InteraccionIA(rutUsuario, nivelPractica, categoria, pregunta, respuesta));

        return respuesta;
    }

    private Integer detectarNivelPractica(String rut) {
        Optional<Estudiante> estOpt = estudianteRepository.findById(rut);
        if (estOpt.isEmpty()) return null;
        Estudiante est = estOpt.get();

        if (est.getPracticaActual() != null) {
            return est.getPracticaActual();
        }
        if (est.getPracticas() != null && !est.getPracticas().isEmpty()) {
            Practica ultima = null;
            for (Practica p : est.getPracticas()) {
                if (p.getNumeroPractica() == null) continue;
                if (ultima == null || esMasReciente(p, ultima)) {
                    ultima = p;
                }
            }
            if (ultima != null) {
                return ultima.getNumeroPractica();
            }
        }
        return 1;
    }

    private boolean esMasReciente(Practica a, Practica b) {
        int anoA = Objects.requireNonNullElse(a.getAno(), 0);
        int anoB = Objects.requireNonNullElse(b.getAno(), 0);
        if (anoA != anoB) return anoA > anoB;
        int semA = Objects.requireNonNullElse(a.getSemestre(), 0);
        int semB = Objects.requireNonNullElse(b.getSemestre(), 0);
        return semA > semB;
    }

    private String clasificarDuda(String p) {
        String lower = p.toLowerCase();
        if (lower.contains("planificaci") || lower.contains("clase")) return "PLANIFICACIONES DE CLASE";
        if (lower.contains("pauta") || lower.contains("rubrica") || lower.contains("rúbrica") || lower.contains("evaluaci") || lower.contains("nota")) return "RÚBRICAS Y EVALUACIÓN";
        if (lower.contains("informe") || lower.contains("entrega") || lower.contains("subir")) return "INFORMES Y ENTREGAS";
        if (lower.contains("colegio") || lower.contains("centro") || lower.contains("colaborador") || lower.contains("asistencia")) return "CENTRO DE PRÁCTICA";
        return "DUDAS GENERALES DE PRÁCTICA";
    }
}