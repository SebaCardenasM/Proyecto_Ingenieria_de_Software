package com.proyectoubbconfig.iswspring.app1.springboot_applications;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EvaluacionPractica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.EvaluacionPractica.Componente;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Pauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoEvaluador;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PautaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.PracticaRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionPracticaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EvaluacionPracticaServiceTest {

    @Autowired
    private EvaluacionPracticaService evaluacionService;

    @Autowired
    private PracticaRepository practicaRepository;

    @Autowired
    private PautaRepository pautaRepository;

    @Test
    void testCalcularNotaPauta() {
        Pauta pautaEsp = new Pauta();
        pautaEsp.setTipoPauta(TipoPauta.ESPECIALIDAD);
        pautaEsp.setPuntajeObtenido(140.0); // 140 / 20 = 7.0
        assertEquals(7.0, evaluacionService.calcularNotaPauta(pautaEsp));

        pautaEsp.setPuntajeObtenido(100.0); // 100 / 20 = 5.0
        assertEquals(5.0, evaluacionService.calcularNotaPauta(pautaEsp));

        pautaEsp.setPuntajeObtenido(20.0); // 20 / 20 = 1.0
        assertEquals(1.0, evaluacionService.calcularNotaPauta(pautaEsp));

        // Caso escala directa 1-7
        pautaEsp.setPuntajeObtenido(6.2);
        assertEquals(6.2, evaluacionService.calcularNotaPauta(pautaEsp));

        // Pauta de Orientación (18 criterios, max 126 pts)
        Pauta pautaOri = new Pauta();
        pautaOri.setTipoPauta(TipoPauta.ORIENTACION);
        pautaOri.setPuntajeObtenido(126.0); // 126 / 18 = 7.0
        assertEquals(7.0, evaluacionService.calcularNotaPauta(pautaOri));

        pautaOri.setPuntajeObtenido(90.0); // 90 / 18 = 5.0
        assertEquals(5.0, evaluacionService.calcularNotaPauta(pautaOri));
    }

    @Test
    void testObtenerBloquesYSlotsPorPractica() {
        Practica p5 = new Practica();
        p5.setNumeroPractica(5);
        p5.setId(99999L);

        var bloques = evaluacionService.obtenerBloquesPautasParaTutor(p5);
        assertEquals(2, bloques.size(), "Práctica 5 debe tener 2 bloques (Especialidad y Orientación)");
        assertEquals(2, bloques.get(0).getSlots().size(), "Bloque especialidad debe tener 2 slots");
        assertEquals(2, bloques.get(1).getSlots().size(), "Bloque orientación debe tener 2 slots");

        var slotsColab = evaluacionService.obtenerSlotsParaColaborador(p5);
        assertEquals(4, slotsColab.size(), "Colaborador en Práctica 5 debe tener 4 slots");
        assertEquals("ESPECIALIDAD_1", slotsColab.get(0).getIdentificadorSlot());
        assertEquals("ESPECIALIDAD_2", slotsColab.get(1).getIdentificadorSlot());
        assertEquals("ORIENTACION_1", slotsColab.get(2).getIdentificadorSlot());
        assertEquals("ORIENTACION_2", slotsColab.get(3).getIdentificadorSlot());
    }

    @Test
    void testSerializacionComponenteBloqueada() {
        Componente c = new Componente("c1", "Pauta Tutor", 25.0, 5.8, true, "Pauta online");
        String json = EvaluacionPractica.serializarJson(List.of(c));
        assertTrue(json.contains("\"bloqueada\":true"));
        assertTrue(json.contains("\"origen\":\"Pauta online\""));

        List<Componente> deserializados = EvaluacionPractica.parsearJson(json);
        assertEquals(1, deserializados.size());
        assertTrue(deserializados.get(0).isBloqueada());
        assertEquals("Pauta online", deserializados.get(0).getOrigen());
        assertEquals(5.8, deserializados.get(0).getNota());
    }

    @Test
    void testSincronizarNotasPautasEnPractica() {
        // Obtenemos cualquier práctica existente
        List<Practica> practicas = practicaRepository.findAll();
        assertFalse(practicas.isEmpty(), "Deben existir prácticas en demo");
        Practica practica = practicas.get(0);

        EvaluacionPractica ev = evaluacionService.obtenerOAsegurarEvaluacionPractica(practica);
        assertNotNull(ev);
        assertNotNull(ev.getComponentes());

        // Comprobamos que si existen pautas asociadas en los datos demo, sus componentes se bloquean
        List<Pauta> pautas = pautaRepository.findByPracticaId(practica.getId());
        if (!pautas.isEmpty() && pautas.stream().anyMatch(p -> p.getPuntajeObtenido() != null && p.getPuntajeObtenido() > 0 && !"ONLINE_BORRADOR".equals(p.getRutaServidor()))) {
            boolean tieneBloqueada = ev.getComponentes().stream().anyMatch(Componente::isBloqueada);
            assertTrue(tieneBloqueada, "Debe tener al menos un componente bloqueado desde la pauta");
        }
    }
}
