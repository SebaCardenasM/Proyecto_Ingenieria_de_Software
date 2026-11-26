package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.dto.PautaSlotDTO;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.Practica;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionColaboradorService;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionColaboradorService.EnlaceNoDisponibleException;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionColaboradorService.Estado;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionColaboradorService.Validacion;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.EvaluacionPracticaService;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PlantillaPautaService;

/**
 * Formulario público del profesor colaborador (sin login). La ruta /evaluacion-colaborador/**
 * ya es pública en SecurityConfig; el control de acceso es el token del enlace.
 */
@Controller
@RequestMapping("/evaluacion-colaborador")
public class EvaluacionColaboradorController {

    @Autowired
    private EvaluacionColaboradorService servicio;

    @Autowired
    private EvaluacionPracticaService evaluacionPracticaService;

    @Autowired
    private PlantillaPautaService plantillaPautaService;

    @GetMapping("/{token}")
    @Transactional(readOnly = true)
    public String mostrar(@PathVariable String token, Model model, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        Validacion v = servicio.validar(token);
        if (v.estado() != Estado.OK) {
            return paginaEstado(model, v.estado().name());
        }
        cargarFormulario(model, token, v, null, null);
        return "Colaborador/evaluacion-colaborador";
    }

    // Sin @Transactional aquí a propósito: el servicio abre y revierte su propia transacción si algo falla
    @PostMapping("/{token}")
    public String enviar(@PathVariable String token, @RequestParam Map<String, String> params, Model model) {
        try {
            servicio.guardarPauta(token, params);
            return paginaEstado(model, "ENVIADA");
        } catch (EnlaceNoDisponibleException e) {
            return paginaEstado(model, e.getEstado().name());
        } catch (IllegalArgumentException e) {
            // Datos incompletos: vuelve al formulario conservando lo que escribió
            Validacion v = servicio.validar(token);
            if (v.estado() != Estado.OK) {
                return paginaEstado(model, v.estado().name());
            }
            cargarFormulario(model, token, v, e.getMessage(), params);
            return "Colaborador/evaluacion-colaborador";
        }
    }

    private void cargarFormulario(Model model, String token, Validacion v, String error, Map<String, String> valores) {
        Practica practica = servicio.obtenerPracticaDeToken(token);
        List<PautaSlotDTO> opcionesPauta = practica != null
                ? evaluacionPracticaService.obtenerSlotsParaColaborador(practica)
                : List.of();
        String nombreAsignatura = practica != null
                ? evaluacionPracticaService.nombreAsignatura(practica.getNumeroPractica() != null ? practica.getNumeroPractica() : 1)
                : "";
        boolean todasCompletadas = !opcionesPauta.isEmpty() && opcionesPauta.stream().allMatch(PautaSlotDTO::isCompletada);

        model.addAttribute("token", token);
        model.addAttribute("datos", v.datos());
        model.addAttribute("practica", practica);
        model.addAttribute("nombreAsignatura", nombreAsignatura);
        model.addAttribute("opcionesPauta", opcionesPauta);
        model.addAttribute("todasCompletadas", todasCompletadas);
        model.addAttribute("modoLectura", false);
        model.addAttribute("pautaJson", valores != null ? servicio.armarJson(valores) : "");
        model.addAttribute("valores", valores);
        model.addAttribute("error", error);
        model.addAttribute("esquemaEspecialidadJson", plantillaPautaService.obtenerEsquemaActivoJson(TipoPauta.ESPECIALIDAD));
        model.addAttribute("esquemaOrientacionJson", plantillaPautaService.obtenerEsquemaActivoJson(TipoPauta.ORIENTACION));
    }

    private String paginaEstado(Model model, String estado) {
        model.addAttribute("estado", estado);
        return "Colaborador/enlace-estado";
    }
}
