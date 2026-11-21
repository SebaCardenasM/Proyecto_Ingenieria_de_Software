package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;

/** Saca el texto de un archivo subido (PDF y DOCX) para que el asistente lo pueda revisar. */
@Component
public class ExtractorTextoDocumento {

    private static final Logger log = LoggerFactory.getLogger(ExtractorTextoDocumento.class);

    public static final int MAX_CARACTERES = 12000;
    private static final int MAX_XML_BYTES = 8 * 1024 * 1024;
    private static final Path RAIZ_SUBIDAS = Paths.get("uploads").toAbsolutePath().normalize();

    /** Texto limpio y recortado, o vacío si el formato no se puede leer (escaneado, .doc, imagen...). */
    public Optional<String> extraer(String rutaServidor) {
        if (rutaServidor == null || rutaServidor.isBlank()) return Optional.empty();

        Path archivo = Paths.get(rutaServidor).toAbsolutePath().normalize();
        if (!archivo.startsWith(RAIZ_SUBIDAS) || !Files.isRegularFile(archivo)) {
            return Optional.empty();
        }

        String nombre = archivo.getFileName().toString().toLowerCase();
        try {
            String texto;
            if (nombre.endsWith(".pdf")) {
                texto = FileSystemDocumentLoader.loadDocument(archivo, new ApachePdfBoxDocumentParser()).text();
            } else if (nombre.endsWith(".docx")) {
                texto = extraerDocx(archivo);
            } else {
                return Optional.empty();
            }

            texto = (texto == null ? "" : texto)
                    .replaceAll("[\\t\\x0B\\f\\r ]+", " ")
                    .replaceAll("\\n{3,}", "\n\n")
                    .trim();

            if (texto.length() < 30) return Optional.empty();   // casi sin texto (p. ej. PDF escaneado)
            return Optional.of(texto.length() > MAX_CARACTERES ? texto.substring(0, MAX_CARACTERES) : texto);

        } catch (IOException | RuntimeException e) {
            log.warn("No se pudo extraer texto de {}: {}", nombre, e.getMessage());
            return Optional.empty();
        }
    }

    private String extraerDocx(Path archivo) throws IOException {
        try (ZipFile zip = new ZipFile(archivo.toFile())) {
            ZipEntry entrada = zip.getEntry("word/document.xml");
            if (entrada == null) return "";
            try (InputStream in = zip.getInputStream(entrada)) {
                String xml = new String(in.readNBytes(MAX_XML_BYTES), StandardCharsets.UTF_8);
                return xml.replace("</w:p>", "\n")
                        .replace("<w:tab/>", " ")
                        .replaceAll("<[^>]+>", "")
                        .replace("&lt;", "<").replace("&gt;", ">")
                        .replace("&quot;", "\"").replace("&apos;", "'")
                        .replace("&amp;", "&");
            }
        }
    }
}