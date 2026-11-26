package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.PlantillaPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.repositories.UsuarioRepository;
import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.PlantillaPautaService;

@Controller
@RequestMapping("/coordinador/formularios-pautas")
public class PlantillaPautaController {

    private static final Logger log = LoggerFactory.getLogger(PlantillaPautaController.class);

    @Autowired
    private PlantillaPautaService plantillaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public String verEditorPautas(
            @RequestParam(value = "tipo", defaultValue = "ESPECIALIDAD") String tipoStr,
            @RequestParam(value = "usarBorrador", defaultValue = "false") boolean usarBorrador,
            Model model,
            Authentication authentication) {

        cargarUsuario(model, authentication);

        TipoPauta tipo = TipoPauta.desde(tipoStr);
        PlantillaPauta plantilla = plantillaService.obtenerOAsegurarPlantilla(tipo);

        String esquemaActivo = plantillaService.obtenerEsquemaActivoJson(tipo);
        String esquemaBorrador = plantilla.getEsquemaBorradorJson();
        boolean tieneBorrador = plantilla.isTieneBorrador() && esquemaBorrador != null && !esquemaBorrador.isBlank();

        // Determinar qué esquema se carga inicialmente en el editor visual
        String esquemaInicial = (usarBorrador && tieneBorrador) ? esquemaBorrador : esquemaActivo;

        model.addAttribute("tipo", tipo);
        model.addAttribute("tipoStr", tipo.name());
        model.addAttribute("plantilla", plantilla);
        model.addAttribute("esquemaInicialJson", esquemaInicial);
        model.addAttribute("esquemaActivoJson", esquemaActivo);
        model.addAttribute("esquemaBorradorJson", esquemaBorrador);
        model.addAttribute("tieneBorrador", tieneBorrador);
        model.addAttribute("editandoBorrador", usarBorrador && tieneBorrador);
        model.addAttribute("totalCriterios", plantillaService.obtenerTotalCriterios(tipo));
        model.addAttribute("puntajeMaximo", plantillaService.obtenerPuntajeMaximo(tipo));

        return "Coordinador/editor-pautas";
    }

    @PostMapping("/guardar")
    public String guardarPauta(
            @RequestParam("tipo") String tipoStr,
            @RequestParam("accion") String accion,
            @RequestParam("esquemaJson") String esquemaJson,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        TipoPauta tipo = TipoPauta.desde(tipoStr);
        String usuario = (authentication != null) ? authentication.getName() : "COORDINADOR";

        try {
            if ("BORRADOR".equalsIgnoreCase(accion)) {
                plantillaService.guardarBorrador(tipo, esquemaJson, usuario);
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "Borrador guardado exitosamente. La evaluación activa continúa usando la versión oficial publicada hasta que pulses 'Guardar y Publicar'.");
                return "redirect:/coordinador/formularios-pautas?tipo=" + tipo.name() + "&usarBorrador=true";
            } else {
                plantillaService.guardarDefinitivo(tipo, esquemaJson, usuario);
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "¡Formulario de pauta de " + tipo.getEtiqueta() + " actualizado y publicado con éxito! Se ha sobreescrito la estructura de evaluación activa para tutores y colaboradores.");
                return "redirect:/coordinador/formularios-pautas?tipo=" + tipo.name();
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Error de validación al guardar pauta {}: {}", tipo, ex.getMessage());
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            return "redirect:/coordinador/formularios-pautas?tipo=" + tipo.name();
        } catch (Exception ex) {
            log.error("Error inesperado al guardar pauta {}: {}", tipo, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("mensajeError", "Ocurrió un error al guardar el formulario: " + ex.getMessage());
            return "redirect:/coordinador/formularios-pautas?tipo=" + tipo.name();
        }
    }

    @PostMapping("/restablecer")
    public String restablecerPauta(
            @RequestParam("tipo") String tipoStr,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        TipoPauta tipo = TipoPauta.desde(tipoStr);
        String usuario = (authentication != null) ? authentication.getName() : "COORDINADOR";

        try {
            plantillaService.restablecerPorDefecto(tipo, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Se ha restablecido la pauta de " + tipo.getEtiqueta() + " a su formato oficial predeterminado según la reglamentación.");
        } catch (Exception ex) {
            log.error("Error al restablecer pauta {}: {}", tipo, ex.getMessage(), ex);
            redirectAttributes.addFlashAttribute("mensajeError", "No fue posible restablecer el formulario: " + ex.getMessage());
        }

        return "redirect:/coordinador/formularios-pautas?tipo=" + tipo.name();
    }

    @PostMapping("/descartar-borrador")
    public String descartarBorrador(
            @RequestParam("tipo") String tipoStr,
            RedirectAttributes redirectAttributes) {

        TipoPauta tipo = TipoPauta.desde(tipoStr);
        plantillaService.descartarBorrador(tipo);
        redirectAttributes.addFlashAttribute("mensajeExito", "El borrador fue descartado. Se muestra la versión oficial activa.");
        return "redirect:/coordinador/formularios-pautas?tipo=" + tipo.name();
    }

    private void cargarUsuario(Model model, Authentication authentication) {
        if (authentication != null) {
            String rut = authentication.getName();
            usuarioRepository.findById(rut).ifPresent(u -> model.addAttribute("usuarioActivo", u));
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(r -> r.replace("ROLE_", ""))
                    .collect(Collectors.toSet());
            model.addAttribute("rolesUsuario", roles);
        }
    }
}
