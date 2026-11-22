package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.CamposPlanificacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.CamposPlanificacion.Campo;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.CamposPlanificacion.Seccion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.*;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.*;

@Service
public class PlanificacionOnlineService {

    private static final Comparator<Practica> CRONOLOGICO = Comparator
            .comparing(Practica::getAno, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()))
            .thenComparing(Practica::getSemestre, Comparator.nullsFirst(Comparator.<Integer>naturalOrder()));

    @Value("${documentos.revision-ia.obligatoria:false}")
    private boolean revisionIaObligatoria;

    @Autowired private PlanificacionRepository planificacionRepository;
    @Autowired private EstudianteRepository estudianteRepository;
    @Autowired private PracticaRepository practicaRepository;

    /**
     * Crea o actualiza la planificación en línea del estudiante.
     * enviar=false -> BORRADOR (sin exigir campos); enviar=true -> ENVIADO (valida los obligatorios).
     * @throws IllegalArgumentException con un mensaje para mostrar al usuario
     */
    @Transactional
    public Planificacion guardar(String rutEstudiante, Long idPlanificacion, Integer numeroPractica,
                                 Map<String, String> params, boolean enviar) {

        Estudiante estudiante = estudianteRepository.findById(rutEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró al estudiante."));
        if (numeroPractica == null) {
            throw new IllegalArgumentException("Selecciona a qué práctica corresponde la planificación.");
        }

        Planificacion p;
        if (idPlanificacion != null) {
            p = planificacionRepository.findById(idPlanificacion)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró la planificación."));
            if (!p.isOnline() || p.getEstudiante() == null || !rutEstudiante.equals(p.getEstudiante().getRut())) {
                throw new IllegalArgumentException("No puedes editar esta planificación.");
            }
            if (!p.isBorrador()) {
                throw new IllegalArgumentException("Esta planificación ya fue enviada y no puede modificarse.");
            }
        } else {
            p = new Planificacion();
            p.setEstudiante(estudiante);
            p.setRutaServidor(Planificacion.RUTA_ONLINE);
        }

        // ---- limpiar y validar campos
        Map<String, String> limpios = new LinkedHashMap<>();
        for (Seccion s : CamposPlanificacion.SECCIONES) {
            for (Campo c : s.campos()) {
                String v = params.get(c.clave());
                if (v == null || v.isBlank()) continue;
                v = v.trim();
                if (v.length() > c.max()) {
                    throw new IllegalArgumentException("El campo «" + c.etiqueta() + "» es demasiado largo (máximo "
                            + c.max() + " caracteres).");
                }
                limpios.put(c.clave(), v);
            }
        }
        if (limpios.containsKey("fecha")) {
            try { LocalDate.parse(limpios.get("fecha")); }
            catch (DateTimeParseException e) { throw new IllegalArgumentException("La fecha ingresada no es válida."); }
        }
        if (enviar) {
            if (revisionIaObligatoria) {
                throw new IllegalArgumentException("Guárdala como borrador, pásala por la revisión del asistente "
                        + "y luego envíala desde «Mis Evidencias».");
            }
            List<String> faltan = CamposPlanificacion.faltantes(limpios);
            if (!faltan.isEmpty()) {
                throw new IllegalArgumentException("Completa los campos obligatorios: " + String.join(", ", faltan) + ".");
            }
        }

        // ---- práctica (el intento más reciente con ese número, igual que en las revisiones)
        Practica practica = practicaRepository.findByEstudiante(estudiante).stream()
                .filter(x -> numeroPractica.equals(x.getNumeroPractica()))
                .max(CRONOLOGICO).orElse(null);

        // ---- persistir
        p.getCampos().clear();
        p.getCampos().putAll(limpios);
        p.setNumeroPractica(numeroPractica);
        p.setPractica(practica);
        p.setNombreArchivo("Planificación en línea - " + limpios.getOrDefault("curso", "sin curso")
                + (limpios.containsKey("fecha") ? " (" + limpios.get("fecha") + ")" : ""));
        p.setFechaSubida(LocalDateTime.now());
        p.setEstado(enviar ? EstadoDocumento.ENVIADO : EstadoDocumento.BORRADOR);
        p.setRevisadoIa(false);          // el contenido cambió: hay que revisarlo de nuevo
        p.setRevisionIa(null);
        p.setFechaRevisionIa(null);
        return planificacionRepository.save(p);
    }
}