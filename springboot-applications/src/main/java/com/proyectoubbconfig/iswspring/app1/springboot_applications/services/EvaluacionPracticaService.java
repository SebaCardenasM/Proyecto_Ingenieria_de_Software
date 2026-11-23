package com.proyectoubbconfig.iswspring.app1.springboot_applications.services;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.BloquePautasDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.PautaSlotDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Documento;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EstadoPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EvaluacionPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EvaluacionPractica.Componente;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Informe;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.DocumentoRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.EvaluacionPracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PautaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EvaluacionPracticaService {

    public static final String[] NOMBRES_PRACTICA = {
            "Aproximación al sistema escolar",
            "Ambientes de aprendizaje",
            "Trabajo colaborativo en el aula",
            "Investigación en el aula",
            "Práctica Pedagógica",
            "Práctica Profesional"
    };

    private static final double TOLERANCIA_PORCENTAJE = 0.01;

    @Autowired
    private EvaluacionPracticaRepository evaluacionRepository;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired(required = false)
    private DocumentoRepository documentoRepository;

    public String nombreAsignatura(int numeroPractica) {
        if (numeroPractica >= 1 && numeroPractica <= NOMBRES_PRACTICA.length) {
            return numeroPractica + ". " + NOMBRES_PRACTICA[numeroPractica - 1];
        }
        return "Práctica " + numeroPractica;
    }

    public Map<Integer, String> mapaAsignaturas() {
        Map<Integer, String> mapa = new LinkedHashMap<>();
        for (int i = 1; i <= NOMBRES_PRACTICA.length; i++) {
            mapa.put(i, nombreAsignatura(i));
        }
        return mapa;
    }

    // ------------------------------------------------------------------
    // Plantillas base por número de práctica (1 a 6)
    // ------------------------------------------------------------------

    @Transactional
    public EvaluacionPractica obtenerOAsegurarPlantilla(int numeroPractica) {
        validarNumeroPractica(numeroPractica);
        return evaluacionRepository.findByNumeroPracticaAndPracticaIsNull(numeroPractica)
                .orElseGet(() -> {
                    EvaluacionPractica plantilla = new EvaluacionPractica();
                    plantilla.setNumeroPractica(numeroPractica);
                    plantilla.setPractica(null);
                    plantilla.setPersonalizada(false);
                    plantilla.setComponentes(componentesPorDefecto(numeroPractica));
                    return evaluacionRepository.save(plantilla);
                });
    }

    @Transactional(readOnly = true)
    public Set<String> obtenerUidsConNotas(int numeroPractica) {
        Set<String> bloqueados = new HashSet<>();
        List<EvaluacionPractica> evaluaciones = evaluacionRepository.findByNumeroPracticaAndPracticaIsNotNull(numeroPractica);
        for (EvaluacionPractica ev : evaluaciones) {
            if (ev.isPersonalizada()) continue;
            for (Componente c : ev.getComponentes()) {
                if (c.getUid() != null && c.getNota() != null) {
                    bloqueados.add(c.getUid());
                }
            }
        }
        return bloqueados;
    }

    /**
     * Guarda la estructura de componentes y pesos (%) para un numeroPractica.
     * Valida que los pesos sumen exactamente 100%, bloquea la eliminación de componentes
     * que ya poseen notas y recalcula automáticamente las prácticas en curso (PENDIENTE).
     */
    @Transactional
    public int guardarEstructura(int numeroPractica, String[] uids, String[] nombres, String[] pesos) {
        validarNumeroPractica(numeroPractica);
        List<Componente> nuevos = construirYValidarComponentes(uids, nombres, pesos, null, false);

        // Validar que no se elimine ningún componente que ya tenga notas registradas
        Set<String> bloqueados = obtenerUidsConNotas(numeroPractica);
        Set<String> uidsNuevos = new HashSet<>();
        for (Componente c : nuevos) {
            uidsNuevos.add(c.getUid());
        }
        for (String uidBloqueado : bloqueados) {
            if (!uidsNuevos.contains(uidBloqueado)) {
                throw new IllegalArgumentException(
                        "No se puede eliminar un componente que ya tiene notas registradas. " +
                        "Puedes renombrarlo o cambiar su porcentaje, pero no quitarlo.");
            }
        }

        EvaluacionPractica plantilla = obtenerOAsegurarPlantilla(numeroPractica);
        plantilla.setComponentes(nuevos);
        evaluacionRepository.save(plantilla);

        // Recalcular prácticas en curso (PENDIENTE) que usan la estructura base
        return recalcularPracticasEnCurso(numeroPractica, nuevos);
    }

    private int recalcularPracticasEnCurso(int numeroPractica, List<Componente> nuevaEstructura) {
        int actualizadas = 0;
        List<EvaluacionPractica> evaluaciones = evaluacionRepository.findByNumeroPracticaAndPracticaIsNotNull(numeroPractica);

        for (EvaluacionPractica ev : evaluaciones) {
            if (ev.isPersonalizada()) continue;
            Practica p = ev.getPractica();
            if (p == null || p.getEstado() != EstadoPractica.PENDIENTE) continue;

            Map<String, Double> notasActuales = new HashMap<>();
            for (Componente viejo : ev.getComponentes()) {
                if (viejo.getUid() != null && viejo.getNota() != null) {
                    notasActuales.put(viejo.getUid(), viejo.getNota());
                }
            }

            List<Componente> sincronizados = new ArrayList<>();
            for (Componente base : nuevaEstructura) {
                sincronizados.add(new Componente(
                        base.getUid(),
                        base.getNombre(),
                        base.getPeso(),
                        notasActuales.get(base.getUid())));
            }

            ev.setComponentes(sincronizados);
            evaluacionRepository.save(ev);

            if (ev.getNotaFinal() != null) {
                p.setNotaAprobacion(ev.getNotaFinal());
                practicaRepository.save(p);
            }
            actualizadas++;
        }
        return actualizadas;
    }

    // ------------------------------------------------------------------
    // Evaluación individual de una práctica de un estudiante
    // ------------------------------------------------------------------

    /**
     * Versión de solo lectura (segura dentro de @Transactional(readOnly = true),
     * por ejemplo en EstudianteController.miPractica). No realiza escrituras en BD.
     */
    @Transactional(readOnly = true)
    public EvaluacionPractica obtenerVistaParaPractica(Practica practica) {
        if (practica == null || practica.getId() == null) {
            return null;
        }
        Optional<EvaluacionPractica> existente = evaluacionRepository.findByPracticaId(practica.getId());
        if (existente.isPresent()) {
            EvaluacionPractica ev = existente.get();
            sincronizarNotasDesdePautas(practica, ev);
            return ev;
        }

        int num = practica.getNumeroPractica() != null ? practica.getNumeroPractica() : 1;
        EvaluacionPractica vista = new EvaluacionPractica();
        vista.setNumeroPractica(num);
        vista.setPractica(practica);
        vista.setPersonalizada(false);

        Optional<EvaluacionPractica> plantilla = evaluacionRepository.findByNumeroPracticaAndPracticaIsNull(num);
        List<Componente> base = plantilla.isPresent()
                ? plantilla.get().getComponentes()
                : componentesPorDefecto(num);

        List<Componente> copia = new ArrayList<>();
        for (Componente c : base) {
            copia.add(new Componente(c.getUid(), c.getNombre(), c.getPeso(), null));
        }
        vista.setComponentes(copia);
        sincronizarNotasDesdePautas(practica, vista);
        return vista;
    }

    @Transactional
    public EvaluacionPractica obtenerOAsegurarEvaluacionPractica(Practica practica) {
        Optional<EvaluacionPractica> existente = evaluacionRepository.findByPracticaId(practica.getId());
        EvaluacionPractica ev;
        if (existente.isPresent()) {
            ev = existente.get();
        } else {
            int num = practica.getNumeroPractica() != null ? practica.getNumeroPractica() : 1;
            EvaluacionPractica plantilla = obtenerOAsegurarPlantilla(num);

            ev = new EvaluacionPractica();
            ev.setNumeroPractica(num);
            ev.setPractica(practica);
            ev.setPersonalizada(false);

            List<Componente> copia = new ArrayList<>();
            for (Componente c : plantilla.getComponentes()) {
                copia.add(new Componente(c.getUid(), c.getNombre(), c.getPeso(), null));
            }
            ev.setComponentes(copia);
            ev = evaluacionRepository.save(ev);
        }

        boolean actualizado = sincronizarNotasDesdePautas(practica, ev);
        if (actualizado) {
            ev = evaluacionRepository.save(ev);
            practica.setNotaAprobacion(ev.getNotaFinal());
            practicaRepository.save(practica);
        }
        return ev;
    }

    @Transactional
    public EvaluacionPractica guardarEvaluacionPractica(
            Practica practica,
            String[] uids,
            String[] nombres,
            String[] pesos,
            String[] notas,
            boolean personalizada,
            EstadoPractica nuevoEstado) {

        EvaluacionPractica ev = obtenerOAsegurarEvaluacionPractica(practica);
        List<Componente> lista = construirYValidarComponentes(uids, nombres, pesos, notas, true);

        ev.setComponentes(lista);
        ev.setPersonalizada(personalizada);
        sincronizarNotasDesdePautas(practica, ev);
        ev = evaluacionRepository.save(ev);

        // Sincronizar la nota final ponderada con Practica.notaAprobacion
        practica.setNotaAprobacion(ev.getNotaFinal());
        if (nuevoEstado != null) {
            practica.setEstado(nuevoEstado);
        }
        practicaRepository.save(practica);

        return ev;
    }

    @Transactional
    public EvaluacionPractica restaurarDesdePlantilla(Practica practica) {
        int num = practica.getNumeroPractica() != null ? practica.getNumeroPractica() : 1;
        EvaluacionPractica plantilla = obtenerOAsegurarPlantilla(num);
        EvaluacionPractica ev = obtenerOAsegurarEvaluacionPractica(practica);

        Map<String, Double> notasPrevias = new HashMap<>();
        for (Componente c : ev.getComponentes()) {
            if (c.getUid() != null && c.getNota() != null) {
                notasPrevias.put(c.getUid(), c.getNota());
            }
        }

        List<Componente> restaurados = new ArrayList<>();
        for (Componente base : plantilla.getComponentes()) {
            restaurados.add(new Componente(
                    base.getUid(),
                    base.getNombre(),
                    base.getPeso(),
                    notasPrevias.get(base.getUid())));
        }

        ev.setPersonalizada(false);
        ev.setComponentes(restaurados);
        sincronizarNotasDesdePautas(practica, ev);
        ev = evaluacionRepository.save(ev);

        practica.setNotaAprobacion(ev.getNotaFinal());
        practicaRepository.save(practica);
        return ev;
    }

    // ------------------------------------------------------------------
    // Estructura de pautas requeridas por práctica (bloques y slots)
    // ------------------------------------------------------------------

    public List<BloquePautasDTO> obtenerBloquesPautasParaTutor(Practica practica) {
        List<BloquePautasDTO> bloques = new ArrayList<>();
        if (practica == null) return bloques;

        Long practicaId = practica.getId();
        Practica pReal = (practicaId != null) ? practicaRepository.findById(practicaId).orElse(practica) : practica;
        int num = pReal.getNumeroPractica() != null ? pReal.getNumeroPractica() : 1;
        if (num <= 2) {
            return bloques;
        }

        List<Pauta> pautas = pautaRepository.findByPracticaId(pReal.getId()).stream()
                .filter(p -> p.getTipoEvaluador() != TipoEvaluador.PROFESOR_COLABORADOR)
                .filter(p -> p.getRutaServidor() != null && p.getRutaServidor().startsWith("ONLINE_"))
                .sorted(Comparator.comparing(Pauta::getFechaSubida, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        BloquePautasDTO bloqueEsp = new BloquePautasDTO(
                "Pautas de Especialidad", TipoPauta.ESPECIALIDAD, "bi-mortarboard-fill", "bg-primary");

        BloquePautasDTO bloqueOri = new BloquePautasDTO(
                "Pautas de Orientación", TipoPauta.ORIENTACION, "bi-compass-fill", "bg-success");

        if (num == 3 || num == 4) {
            bloqueEsp.addSlot(new PautaSlotDTO("ESPECIALIDAD_1", TipoPauta.ESPECIALIDAD, 1,
                    "Pauta N° 1 - Observación de Clase (Académico UBB)",
                    "Evaluación del desempeño en aula centrada en la especialidad"));
            bloques.add(bloqueEsp);
        } else if (num == 5 || num == 6) {
            bloqueEsp.addSlot(new PautaSlotDTO("ESPECIALIDAD_1", TipoPauta.ESPECIALIDAD, 1,
                    "Pauta N° 1 - Especialidad (1.° Observación)",
                    "Primera observación de clase de matemática"));
            bloqueEsp.addSlot(new PautaSlotDTO("ESPECIALIDAD_2", TipoPauta.ESPECIALIDAD, 2,
                    "Pauta N° 2 - Especialidad (2.° Observación)",
                    "Segunda observación de clase de matemática"));
            bloques.add(bloqueEsp);

            bloqueOri.addSlot(new PautaSlotDTO("ORIENTACION_1", TipoPauta.ORIENTACION, 1,
                    "Pauta N° 1 - Orientación (1.° Observación)",
                    "Primera observación en convivencia, jefatura u orientación"));
            bloqueOri.addSlot(new PautaSlotDTO("ORIENTACION_2", TipoPauta.ORIENTACION, 2,
                    "Pauta N° 2 - Orientación (2.° Observación)",
                    "Segunda observación en convivencia, jefatura u orientación"));
            bloques.add(bloqueOri);
        }

        Set<Long> pautasAsignadas = new HashSet<>();
        for (BloquePautasDTO b : bloques) {
            for (PautaSlotDTO slot : b.getSlots()) {
                Pauta encontrada = pautas.stream()
                        .filter(p -> !pautasAsignadas.contains(p.getId()))
                        .filter(p -> p.getTipoPauta() == slot.getTipoPauta() && p.getNumeroObservacion() == slot.getNumeroObservacion())
                        .findFirst()
                        .orElse(null);

                if (encontrada == null) {
                    encontrada = pautas.stream()
                            .filter(p -> !pautasAsignadas.contains(p.getId()))
                            .filter(p -> p.getTipoPauta() == slot.getTipoPauta())
                            .findFirst()
                            .orElse(null);
                }

                if (encontrada != null) {
                    pautasAsignadas.add(encontrada.getId());
                    slot.setIdPauta(encontrada.getId());
                    slot.setPuntaje(encontrada.getPuntajeObtenido());
                    slot.setNota(calcularNotaPauta(encontrada));
                    slot.setFecha(encontrada.getFechaSubida());
                    slot.setNombreArchivo(encontrada.getNombreArchivo());
                    if ("ONLINE_ENVIADA".equalsIgnoreCase(encontrada.getRutaServidor())) {
                        slot.setCompletada(true);
                    } else {
                        slot.setTieneBorrador(true);
                    }
                }
            }
        }

        return bloques;
    }

    public List<PautaSlotDTO> obtenerSlotsParaColaborador(Practica practica) {
        List<PautaSlotDTO> slots = new ArrayList<>();
        if (practica == null) return slots;

        Long practicaId = practica.getId();
        Practica pReal = (practicaId != null) ? practicaRepository.findById(practicaId).orElse(practica) : practica;
        int num = pReal.getNumeroPractica() != null ? pReal.getNumeroPractica() : 1;
        if (num <= 2) {
            return slots;
        }

        if (num == 3 || num == 4) {
            slots.add(new PautaSlotDTO("ESPECIALIDAD_1", TipoPauta.ESPECIALIDAD, 1,
                    "Pauta N° 1 - Especialidad (Observación Profesor Colaborador)",
                    "Evaluación de la clase observada en el establecimiento"));
        } else if (num == 5) {
            slots.add(new PautaSlotDTO("ESPECIALIDAD_1", TipoPauta.ESPECIALIDAD, 1,
                    "Pauta N° 1 - Especialidad (1.° Observación)",
                    "Primera observación de clase de matemática"));
            slots.add(new PautaSlotDTO("ESPECIALIDAD_2", TipoPauta.ESPECIALIDAD, 2,
                    "Pauta N° 2 - Especialidad (2.° Observación)",
                    "Segunda observación de clase de matemática"));
            slots.add(new PautaSlotDTO("ORIENTACION_1", TipoPauta.ORIENTACION, 1,
                    "Pauta N° 1 - Orientación (1.° Observación)",
                    "Primera observación de orientación / jefatura"));
            slots.add(new PautaSlotDTO("ORIENTACION_2", TipoPauta.ORIENTACION, 2,
                    "Pauta N° 2 - Orientación (2.° Observación)",
                    "Segunda observación de orientación / jefatura"));
        } else if (num == 6) {
            slots.add(new PautaSlotDTO("ESPECIALIDAD_1", TipoPauta.ESPECIALIDAD, 1,
                    "Pauta N° 1 - Especialidad (1.° Observación)",
                    "Primera observación de clase de matemática"));
            slots.add(new PautaSlotDTO("ESPECIALIDAD_2", TipoPauta.ESPECIALIDAD, 2,
                    "Pauta N° 2 - Especialidad (2.° Observación)",
                    "Segunda observación de clase de matemática"));
        }

        List<Pauta> pautasColab = pautaRepository.findByPracticaId(pReal.getId()).stream()
                .filter(p -> p.getTipoEvaluador() == TipoEvaluador.PROFESOR_COLABORADOR)
                .filter(p -> "ONLINE_ENVIADA".equalsIgnoreCase(p.getRutaServidor()))
                .sorted(Comparator.comparing(Pauta::getFechaSubida, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        Set<Long> pautasAsignadas = new HashSet<>();
        for (PautaSlotDTO slot : slots) {
            Pauta encontrada = pautasColab.stream()
                    .filter(p -> !pautasAsignadas.contains(p.getId()))
                    .filter(p -> p.getTipoPauta() == slot.getTipoPauta() && p.getNumeroObservacion() == slot.getNumeroObservacion())
                    .findFirst()
                    .orElse(null);

            if (encontrada == null) {
                encontrada = pautasColab.stream()
                        .filter(p -> !pautasAsignadas.contains(p.getId()))
                        .filter(p -> p.getTipoPauta() == slot.getTipoPauta())
                        .findFirst()
                        .orElse(null);
            }

            if (encontrada != null) {
                pautasAsignadas.add(encontrada.getId());
                slot.setCompletada(true);
                slot.setIdPauta(encontrada.getId());
                slot.setPuntaje(encontrada.getPuntajeObtenido());
                slot.setNota(calcularNotaPauta(encontrada));
                slot.setFecha(encontrada.getFechaSubida());
                slot.setNombreArchivo(encontrada.getNombreArchivo());
            }
        }

        return slots;
    }

    // ------------------------------------------------------------------
    // Sincronización automática de notas desde Pautas de observación
    // ------------------------------------------------------------------

    public Double calcularNotaPauta(Pauta pauta) {
        if (pauta == null || pauta.getPuntajeObtenido() == null) {
            return null;
        }
        double puntaje = pauta.getPuntajeObtenido();
        if (puntaje <= 0.0) {
            return null;
        }
        if (puntaje <= 7.0 && puntaje >= 1.0) {
            return Math.round(puntaje * 10.0) / 10.0;
        }
        int totalCriterios = pauta.getTipoPauta() != null ? pauta.getTipoPauta().getTotalCriterios() : 20;
        if (totalCriterios <= 0) totalCriterios = 20;
        double nota = puntaje / (double) totalCriterios;
        if (nota < 1.0) nota = 1.0;
        if (nota > 7.0) nota = 7.0;
        return Math.round(nota * 10.0) / 10.0;
    }

    public boolean sincronizarNotasDesdePautas(Practica practica, EvaluacionPractica ev) {
        if (practica == null || practica.getId() == null || ev == null) {
            return false;
        }

        List<Pauta> pautas = pautaRepository.findByPracticaId(practica.getId());
        List<Pauta> pautasValidas = (pautas != null) ? pautas.stream()
                .filter(p -> p.getPuntajeObtenido() != null && p.getPuntajeObtenido() > 0)
                .filter(p -> p.getRutaServidor() == null || !"ONLINE_BORRADOR".equalsIgnoreCase(p.getRutaServidor()))
                .sorted(Comparator.comparing(Pauta::getFechaSubida, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList() : Collections.emptyList();

        List<Informe> informesValidos = new ArrayList<>();
        if (documentoRepository != null) {
            List<Documento> docs = documentoRepository.findByPracticaId(practica.getId());
            if (docs != null) {
                informesValidos.addAll(docs.stream()
                        .filter(d -> d instanceof Informe)
                        .map(d -> (Informe) d)
                        .filter(i -> i.getNota() != null && !i.isBorrador())
                        .toList());
            }
            if (informesValidos.isEmpty() && practica.getEstudiante() != null) {
                List<Documento> docsEst = documentoRepository.findByEstudiante(practica.getEstudiante());
                if (docsEst != null) {
                    informesValidos.addAll(docsEst.stream()
                            .filter(d -> d instanceof Informe)
                            .map(d -> (Informe) d)
                            .filter(i -> i.getNota() != null && !i.isBorrador()
                                    && (i.getNumeroPractica() == null || i.getNumeroPractica().equals(practica.getNumeroPractica())))
                            .toList());
                }
            }
        }

        if (pautasValidas.isEmpty() && informesValidos.isEmpty()) {
            return false;
        }

        List<Componente> componentes = ev.getComponentes();
        if (componentes == null || componentes.isEmpty()) {
            return false;
        }

        boolean modificado = false;
        Set<Long> pautasUsadas = new HashSet<>();

        List<Pauta> pautasTutor = pautasValidas.stream()
                .filter(p -> p.getTipoEvaluador() == TipoEvaluador.TUTOR_UNIVERSIDAD)
                .toList();

        List<Pauta> pautasColab = pautasValidas.stream()
                .filter(p -> p.getTipoEvaluador() == TipoEvaluador.PROFESOR_COLABORADOR)
                .toList();

        for (Componente c : componentes) {
            String nom = c.getNombre() != null ? c.getNombre().toLowerCase() : "";
            String uid = c.getUid() != null ? c.getUid().toLowerCase() : "";

            // 1. Práctica 5: Colaborador Especialidad (1° y 2° obs)
            if (nom.contains("colaborador") && nom.contains("especialidad")) {
                int obsNum = (nom.contains("2") || nom.contains("segunda") || uid.equals("p5_c4")) ? 2 : 1;
                Pauta p = pautasColab.stream()
                        .filter(pa -> !pautasUsadas.contains(pa.getId()))
                        .filter(pa -> pa.getTipoPauta() == TipoPauta.ESPECIALIDAD && pa.getNumeroObservacion() != null && pa.getNumeroObservacion() == obsNum)
                        .findFirst()
                        .orElse(null);

                if (p == null) {
                    List<Pauta> espPautas = pautasColab.stream()
                            .filter(pa -> !pautasUsadas.contains(pa.getId()))
                            .filter(pa -> pa.getTipoPauta() == TipoPauta.ESPECIALIDAD)
                            .toList();
                    if (!espPautas.isEmpty()) {
                        p = espPautas.get(0);
                    }
                }

                if (p == null) {
                    List<Pauta> disponibles = pautasColab.stream()
                            .filter(pa -> !pautasUsadas.contains(pa.getId()))
                            .toList();
                    if (!disponibles.isEmpty()) {
                        p = disponibles.get(0);
                    }
                }

                if (p != null) {
                    Double nota = calcularNotaPauta(p);
                    if (nota != null) {
                        c.setNota(nota);
                        c.setBloqueada(true);
                        c.setOrigen("Pauta online Colaborador (" + p.getNombreArchivo() + ")");
                        pautasUsadas.add(p.getId());
                        modificado = true;
                        continue;
                    }
                }
            }

            // 2. Práctica 5: Colaborador Orientación (1° y 2° obs)
            if (nom.contains("colaborador") && (nom.contains("orientaci") || nom.contains("orientacion"))) {
                int obsNum = (nom.contains("2") || nom.contains("segunda") || uid.equals("p5_c6")) ? 2 : 1;
                Pauta p = pautasColab.stream()
                        .filter(pa -> !pautasUsadas.contains(pa.getId()))
                        .filter(pa -> pa.getTipoPauta() == TipoPauta.ORIENTACION && pa.getNumeroObservacion() != null && pa.getNumeroObservacion() == obsNum)
                        .findFirst()
                        .orElse(null);

                if (p == null) {
                    List<Pauta> oriPautas = pautasColab.stream()
                            .filter(pa -> !pautasUsadas.contains(pa.getId()))
                            .filter(pa -> pa.getTipoPauta() == TipoPauta.ORIENTACION)
                            .toList();
                    if (!oriPautas.isEmpty()) {
                        p = oriPautas.get(0);
                    }
                }

                if (p != null) {
                    Double nota = calcularNotaPauta(p);
                    if (nota != null) {
                        c.setNota(nota);
                        c.setBloqueada(true);
                        c.setOrigen("Pauta online Colaborador (" + p.getNombreArchivo() + ")");
                        pautasUsadas.add(p.getId());
                        modificado = true;
                        continue;
                    }
                }
            }

            // 3. Colaborador en Prácticas 3, 4, 6 o genérico
            if (nom.contains("colaborador") || uid.contains("colab") || uid.equals("p3_c3") || uid.equals("p4_c3") || uid.equals("p6_c3")) {
                List<Pauta> disponibles = pautasColab.stream()
                        .filter(p -> !pautasUsadas.contains(p.getId()))
                        .toList();
                if (!disponibles.isEmpty()) {
                    Pauta p = disponibles.get(0);
                    Double nota = calcularNotaPauta(p);
                    if (nota != null) {
                        c.setNota(nota);
                        c.setBloqueada(true);
                        c.setOrigen("Pauta online Colaborador (" + p.getNombreArchivo() + ")");
                        pautasUsadas.add(p.getId());
                        modificado = true;
                        continue;
                    }
                }
            }

            // 4. Tutor / Académico UBB
            if (nom.contains("tutor") || nom.contains("académico") || nom.contains("academico") || uid.contains("tutor") || uid.equals("p3_c2") || uid.equals("p4_c2") || uid.equals("p6_c2")) {
                int obsNum = (nom.contains("2") || nom.contains("segunda")) ? 2 : 1;
                TipoPauta tipoReq = (nom.contains("orientaci") || nom.contains("orientacion")) ? TipoPauta.ORIENTACION : TipoPauta.ESPECIALIDAD;

                Pauta p = pautasTutor.stream()
                        .filter(pa -> !pautasUsadas.contains(pa.getId()))
                        .filter(pa -> pa.getTipoPauta() == tipoReq && pa.getNumeroObservacion() != null && pa.getNumeroObservacion() == obsNum)
                        .findFirst()
                        .orElse(null);

                if (p == null) {
                    p = pautasTutor.stream()
                            .filter(pa -> !pautasUsadas.contains(pa.getId()))
                            .filter(pa -> pa.getTipoPauta() == tipoReq)
                            .findFirst()
                            .orElse(null);
                }

                if (p == null) {
                    List<Pauta> disponibles = pautasTutor.stream()
                            .filter(pa -> !pautasUsadas.contains(pa.getId()))
                            .toList();
                    if (!disponibles.isEmpty()) {
                        p = disponibles.get(0);
                    }
                }

                if (p != null) {
                    Double nota = calcularNotaPauta(p);
                    if (nota != null) {
                        c.setNota(nota);
                        c.setBloqueada(true);
                        c.setOrigen("Pauta online Tutor (" + p.getNombreArchivo() + ")");
                        pautasUsadas.add(p.getId());
                        modificado = true;
                        continue;
                    }
                }
            }
        }

        // 5. Informes evaluados por el profesor
        if (!informesValidos.isEmpty()) {
            Set<Long> informesUsados = new HashSet<>();
            for (Componente c : componentes) {
                String nom = c.getNombre() != null ? c.getNombre().toLowerCase() : "";
                String uid = c.getUid() != null ? c.getUid().toLowerCase() : "";

                boolean esComponenteInforme = nom.contains("informe")
                        || uid.contains("informe")
                        || uid.equals("p1_c2") || uid.equals("p1_c3")
                        || uid.equals("p2_c3") || uid.equals("p3_c4")
                        || uid.equals("p4_c4") || uid.equals("p5_c8")
                        || uid.equals("p6_c4");

                if (!esComponenteInforme) {
                    continue;
                }

                // Si ya está bloqueada por pauta, no sobreescribir
                if (c.isBloqueada() && c.getOrigen() != null && !c.getOrigen().startsWith("Informe")) {
                    continue;
                }

                Informe inf = null;
                // Coincidencia más específica por nombre de archivo
                if (nom.contains("reflexivo")) {
                    inf = informesValidos.stream()
                            .filter(i -> !informesUsados.contains(i.getId()))
                            .filter(i -> i.getNombreArchivo().toLowerCase().contains("reflexivo"))
                            .findFirst().orElse(null);
                } else if (nom.contains("análisis") || nom.contains("analisis")) {
                    inf = informesValidos.stream()
                            .filter(i -> !informesUsados.contains(i.getId()))
                            .filter(i -> i.getNombreArchivo().toLowerCase().contains("análisis") || i.getNombreArchivo().toLowerCase().contains("analisis"))
                            .findFirst().orElse(null);
                }

                if (inf == null) {
                    inf = informesValidos.stream()
                            .filter(i -> !informesUsados.contains(i.getId()))
                            .findFirst().orElse(null);
                }

                if (inf != null && inf.getNota() != null) {
                    c.setNota(inf.getNota());
                    c.setBloqueada(true);
                    c.setOrigen("Informe evaluado (" + inf.getNombreArchivo() + ")");
                    informesUsados.add(inf.getId());
                    modificado = true;
                }
            }
        }

        if (modificado) {
            ev.setComponentes(componentes);
        }
        return modificado;
    }

    // ------------------------------------------------------------------
    // Validaciones comunes
    // ------------------------------------------------------------------

    private List<Componente> construirYValidarComponentes(
            String[] uids,
            String[] nombres,
            String[] pesos,
            String[] notas,
            boolean incluirNotas) {

        if (nombres == null || pesos == null || nombres.length == 0) {
            throw new IllegalArgumentException("Debes definir al menos un componente de evaluación.");
        }
        if (nombres.length != pesos.length) {
            throw new IllegalArgumentException("Los datos enviados no coinciden entre componentes y porcentajes.");
        }

        List<Componente> resultado = new ArrayList<>();
        double sumaPesos = 0.0;

        for (int i = 0; i < nombres.length; i++) {
            String nombre = nombres[i] != null ? nombres[i].trim() : "";
            if (nombre.isEmpty()) {
                throw new IllegalArgumentException("El componente #" + (i + 1) + " debe tener un nombre.");
            }
            if (nombre.length() > 160) {
                throw new IllegalArgumentException("El nombre del componente \"" + nombre + "\" supera los 160 caracteres.");
            }

            double peso;
            try {
                String pesoStr = pesos[i] != null ? pesos[i].trim().replace(',', '.') : "";
                peso = Double.parseDouble(pesoStr);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("El porcentaje del componente \"" + nombre + "\" no es un número válido.");
            }
            if (peso <= 0.0 || peso > 100.0) {
                throw new IllegalArgumentException("El porcentaje de \"" + nombre + "\" debe estar entre 0,1% y 100%.");
            }
            peso = Math.round(peso * 100.0) / 100.0;
            sumaPesos += peso;

            Double nota = null;
            if (incluirNotas && notas != null && i < notas.length) {
                String notaStr = notas[i] != null ? notas[i].trim().replace(',', '.') : "";
                if (!notaStr.isEmpty()) {
                    try {
                        nota = Double.parseDouble(notaStr);
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("La nota de \"" + nombre + "\" no es válida.");
                    }
                    if (nota < 1.0 || nota > 7.0) {
                        throw new IllegalArgumentException("La nota de \"" + nombre + "\" debe estar entre 1,0 y 7,0.");
                    }
                    nota = Math.round(nota * 10.0) / 10.0;
                }
            }

            String uid = (uids != null && i < uids.length && uids[i] != null && !uids[i].isBlank())
                    ? uids[i].trim()
                    : "comp_" + UUID.randomUUID().toString().substring(0, 8);

            resultado.add(new Componente(uid, nombre, peso, nota));
        }

        sumaPesos = Math.round(sumaPesos * 100.0) / 100.0;
        if (sumaPesos > 100.0 + TOLERANCIA_PORCENTAJE) {
            throw new IllegalArgumentException(
                    "La suma de los porcentajes supera el máximo permitido de 100% (actual: " +
                    String.format(Locale.US, "%.2f", sumaPesos).replace('.', ',') + "%).");
        }
        if (sumaPesos < 100.0 - TOLERANCIA_PORCENTAJE) {
            throw new IllegalArgumentException(
                    "Los porcentajes deben sumar exactamente 100% para guardar (actual: " +
                    String.format(Locale.US, "%.2f", sumaPesos).replace('.', ',') + "%).");
        }

        return resultado;
    }

    private void validarNumeroPractica(int numeroPractica) {
        if (numeroPractica < 1 || numeroPractica > NOMBRES_PRACTICA.length) {
            throw new IllegalArgumentException("Número de práctica inválido: " + numeroPractica);
        }
    }

    // ------------------------------------------------------------------
    // Estructuras predefinidas por asignatura (total 100% cada una)
    // ------------------------------------------------------------------

    public List<Componente> componentesPorDefecto(int numeroPractica) {
        return switch (numeroPractica) {
            case 1 -> List.of(
                    new Componente("p1_c1", "Talleres y actividades formativas", 30.0, null),
                    new Componente("p1_c2", "Informe de análisis del sistema escolar", 35.0, null),
                    new Componente("p1_c3", "Informe reflexivo final", 35.0, null)
            );
            case 2 -> List.of(
                    new Componente("p2_c1", "Análisis de casos y diseño de ambiente de aula", 30.0, null),
                    new Componente("p2_c2", "Simulación / Microenseñanza", 35.0, null),
                    new Componente("p2_c3", "Informe reflexivo final", 35.0, null)
            );
            case 3 -> List.of(
                    new Componente("p3_c1", "Planificaciones de clase", 20.0, null),
                    new Componente("p3_c2", "Pauta de observación Académico UBB", 25.0, null),
                    new Componente("p3_c3", "Pauta de observación Profesor Colaborador", 25.0, null),
                    new Componente("p3_c4", "Informe de trabajo colaborativo", 30.0, null)
            );
            case 4 -> List.of(
                    new Componente("p4_c1", "Planificaciones e instrumentos de aula", 20.0, null),
                    new Componente("p4_c2", "Pauta de observación Académico UBB", 25.0, null),
                    new Componente("p4_c3", "Pauta de observación Profesor Colaborador", 25.0, null),
                    new Componente("p4_c4", "Informe de investigación en el aula", 30.0, null)
            );
            case 5 -> List.of(
                    new Componente("p5_c1", "Especialidad UBB: Producto 1 Expectativas", 10.0, null),
                    new Componente("p5_c2", "Especialidad UBB: Producto 2 Contexto", 17.0, null),
                    new Componente("p5_c3", "Colaborador Especialidad: pauta 1.° observación", 5.0, null),
                    new Componente("p5_c4", "Colaborador Especialidad: pauta 2.° observación", 13.0, null),
                    new Componente("p5_c5", "Colaborador Orientación: pauta 1.° observación", 3.0, null),
                    new Componente("p5_c6", "Colaborador Orientación: pauta 2.° observación", 5.0, null),
                    new Componente("p5_c7", "Integración curricular: TANEP", 20.0, null),
                    new Componente("p5_c8", "Integración curricular: Informe final (Producto 3)", 27.0, null)
            );
            case 6 -> List.of(
                    new Componente("p6_c1", "Planificaciones y gestión pedagógica", 20.0, null),
                    new Componente("p6_c2", "Pautas de observación Tutor Universitario", 30.0, null),
                    new Componente("p6_c3", "Pautas de observación Profesor Colaborador", 20.0, null),
                    new Componente("p6_c4", "Informe final y portafolio profesional", 30.0, null)
            );
            default -> List.of(
                    new Componente("px_c1", "Evaluación de desempeño", 50.0, null),
                    new Componente("px_c2", "Informe final", 50.0, null)
            );
        };
    }
}
