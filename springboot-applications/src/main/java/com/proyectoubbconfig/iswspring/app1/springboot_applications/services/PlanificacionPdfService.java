package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Planificacion;

@Service
public class PlanificacionPdfService {

    private static final String CARPETA = "uploads/practicas/";
    private static final float M = 50, LH = 12, PAD = 4, TAM = 9, ANCHO = PDRectangle.A4.getWidth() - 2 * M;
    private static final PDFont NORMAL = PDType1Font.HELVETICA;
    private static final PDFont NEGRITA = PDType1Font.HELVETICA_BOLD;

    private record Celda(String texto, boolean etiqueta) {}
    private static Celda e(String t) { return new Celda(t, true); }    // etiqueta (negrita, fondo celeste)
    private static Celda d(String t) { return new Celda(t == null ? "" : t, false); }

    /** Genera el PDF con el formato de planificación diversificada y devuelve la ruta para rutaServidor. */
    public String generar(Planificacion p) {
        return generarEn(p, CARPETA);
    }

    public String generarEn(Planificacion p, String carpetaDestino) {
        Map<String, String> v = p.getCampos();

        // El formulario no pide año ni período: se deducen de la fecha de la clase
        String fecha = v.getOrDefault("fecha", "");
        String ano = "", periodo = "";
        if (fecha.length() >= 7) {
            ano = fecha.substring(0, 4);
            try { periodo = Integer.parseInt(fecha.substring(5, 7)) >= 8 ? "II Semestre" : "I Semestre"; }
            catch (NumberFormatException ex) { /* queda vacío */ }
        }

        try (PDDocument doc = new PDDocument()) {
            Pdf w = new Pdf(doc);
            w.titulo("Planificación Diversificada de Aula", NEGRITA, 14);
            w.titulo("Establecimiento: " + v.getOrDefault("establecimiento", ""), NEGRITA, 11);

            float[] dos = {150, ANCHO - 150};
            float[] cuatro = {90, (ANCHO - 180) / 2, 90, (ANCHO - 180) / 2};
            float[] car = {120, (ANCHO - 120) / 3, (ANCHO - 120) / 3, (ANCHO - 120) / 3};

            w.fila(dos, e("Equipo de Co-Enseñanza"), d(v.get("equipo")));
            w.fila(car, e("Caracterización Grupo-Curso"),
                    d("Estudiantes con NEET:\n" + v.getOrDefault("neet", "")),
                    d("Estudiantes con NEEP:\n" + v.getOrDefault("neep", "")),
                    d("Estilos de aprendizaje:\n" + v.getOrDefault("estilos", "")));
            w.fila(dos, e("Otros datos de importancia"), d(v.get("otrosDatos")));
            w.fila(cuatro, e("AÑO"), d(ano), e("PERÍODO"), d(periodo));
            w.fila(cuatro, e("Asignatura"), d(v.get("asignatura")), e("Curso"), d(v.get("curso")));
            w.fila(dos, e("Unidad o eje temático"), d(v.get("unidad")));
            w.fila(dos, e("Objetivo de aprendizaje (OA) / indicadores de evaluación"), d(v.get("oa")));
            w.fila(dos, e("Objetivo de aprendizaje transversal"), d(v.get("oat")));
            w.fila(dos, e("Objetivo de la clase"), d(v.get("objetivoClase")));
            w.fila(cuatro, e("Fecha"), d(fecha), e("Duración de la clase"), d(v.get("duracion")));
            w.fila(dos, e("Recursos Didácticos"), d(v.get("recursos")));
            w.fila(dos, e("Evaluación de la clase"), d(v.get("evaluacion")));
            w.fila(dos, e("Principios DUA"), d(v.get("dua")));

            w.espacio(12);

            float[] mom = {ANCHO * 0.62f, ANCHO * 0.38f};
            w.fila(mom, e("Momentos de la clase"),
                    e("Diversificación de la Enseñanza\nIndique el Principio DUA y la/s acción/es a utilizar"));
            w.fila(mom, d("Inicio:\n" + v.getOrDefault("inicio", "")), d(v.get("inicioDua")));
            w.fila(mom, d("Desarrollo:\n" + v.getOrDefault("desarrollo", "")), d(v.get("desarrolloDua")));
            w.fila(mom, d("Cierre:\n" + v.getOrDefault("cierre", "")), d(v.get("cierreDua")));

            float[] una = {ANCHO};
            w.fila(una, d("Errores y Dificultades:\n" + v.getOrDefault("errores", "")));
            w.fila(una, d("Referencias Bibliográficas:\n" + v.getOrDefault("referencias", "")));
            if (v.get("anexos") != null && !v.get("anexos").isBlank()) {
                w.fila(una, d("Anexos:\n" + v.get("anexos")));
            }
            w.cerrar();

            Path dir = Paths.get(carpetaDestino).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String nombre = UUID.randomUUID() + ".pdf";
            doc.save(dir.resolve(nombre).toFile());
            String prefijo = carpetaDestino.endsWith("/") ? carpetaDestino : carpetaDestino + "/";
            return prefijo + nombre;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String limpio(String s) {   // las fuentes estándar solo admiten Latin-1
        return s.replace("\t", " ").replace("\r", "").replaceAll("[^\\n\\x20-\\x7E\\u00A0-\\u00FF]", "?");
    }

    private static class Pdf {
        final PDDocument doc;
        PDPageContentStream cs;
        float y;

        Pdf(PDDocument doc) throws IOException { this.doc = doc; nuevaPagina(); }

        void nuevaPagina() throws IOException {
            if (cs != null) cs.close();
            PDPage pg = new PDPage(PDRectangle.A4);
            doc.addPage(pg);
            cs = new PDPageContentStream(doc, pg);
            cs.setLineWidth(0.5f);
            y = PDRectangle.A4.getHeight() - M;
        }

        void espacio(float h) { y -= h; }

        void titulo(String t, PDFont f, float size) throws IOException {
            if (y < M + size) nuevaPagina();
            cs.beginText();
            cs.setFont(f, size);
            cs.newLineAtOffset(M, y - size);
            cs.showText(limpio(t));
            cs.endText();
            y -= size + 8;
        }

        /** Una fila de tabla; si es más alta que la página restante, continúa en la siguiente. */
        void fila(float[] anchos, Celda... celdas) throws IOException {
            List<List<String>> lineas = new ArrayList<>();
            int max = 1;
            for (int i = 0; i < celdas.length; i++) {
                PDFont f = celdas[i].etiqueta() ? NEGRITA : NORMAL;
                List<String> l = envolver(celdas[i].texto(), f, anchos[i] - 2 * PAD);
                lineas.add(l);
                max = Math.max(max, l.size());
            }
            int idx = 0;
            while (idx < max) {
                int caben = (int) ((y - M - 2 * PAD) / LH);
                if (caben < 1) { nuevaPagina(); continue; }
                int n = Math.min(caben, max - idx);
                float alto = n * LH + 2 * PAD;
                float x = M;
                for (int i = 0; i < celdas.length; i++) {
                    if (celdas[i].etiqueta()) {
                        cs.setNonStrokingColor(0.91f, 0.94f, 0.97f);
                        cs.addRect(x, y - alto, anchos[i], alto);
                        cs.fill();
                        cs.setNonStrokingColor(0f, 0f, 0f);
                    }
                    cs.addRect(x, y - alto, anchos[i], alto);
                    cs.stroke();

                    PDFont f = celdas[i].etiqueta() ? NEGRITA : NORMAL;
                    List<String> l = lineas.get(i);
                    float ty = y - PAD - TAM;
                    for (int k = idx; k < Math.min(idx + n, l.size()); k++) {
                        cs.beginText();
                        cs.setFont(f, TAM);
                        cs.newLineAtOffset(x + PAD, ty);
                        cs.showText(l.get(k));
                        cs.endText();
                        ty -= LH;
                    }
                    x += anchos[i];
                }
                y -= alto;
                idx += n;
            }
        }

        List<String> envolver(String texto, PDFont f, float ancho) throws IOException {
            List<String> out = new ArrayList<>();
            for (String par : limpio(texto).split("\n", -1)) {
                StringBuilder actual = new StringBuilder();
                for (String palabra : par.split(" ")) {
                    // palabras más largas que la celda (enlaces) se parten
                    while (f.getStringWidth(palabra) / 1000 * TAM > ancho && palabra.length() > 1) {
                        int corte = palabra.length() - 1;
                        while (corte > 1 && f.getStringWidth(palabra.substring(0, corte)) / 1000 * TAM > ancho) corte--;
                        if (actual.length() > 0) { out.add(actual.toString()); actual = new StringBuilder(); }
                        out.add(palabra.substring(0, corte));
                        palabra = palabra.substring(corte);
                    }
                    String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
                    if (f.getStringWidth(prueba) / 1000 * TAM > ancho && actual.length() > 0) {
                        out.add(actual.toString());
                        actual = new StringBuilder(palabra);
                    } else {
                        actual = new StringBuilder(prueba);
                    }
                }
                out.add(actual.toString());
            }
            return out;
        }

        void cerrar() throws IOException { cs.close(); }
    }
}