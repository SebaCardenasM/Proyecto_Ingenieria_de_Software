package com.proyectoubbconfig.iswspring.app1.springboot_applications.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.services.ConfiguracionChatbotService;

@Controller
public class ConfiguracionChatbotController {

    @Autowired
    private ConfiguracionChatbotService configuracionService;

    @GetMapping("/coordinador/configuracion-ia")
    public String verConfiguracion(Model model) {
        model.addAttribute("promptSistema", configuracionService.obtenerPromptSistema());
        model.addAttribute("preguntasDiagnosticas", configuracionService.obtenerPreguntasDiagnosticas());
        model.addAttribute("saludoInicial", configuracionService.obtenerSaludoInicial());
        model.addAttribute("volver", "/monitoreo-chat");
        return "Coordinador/configuracion-ia";
    }

    @PostMapping("/coordinador/configuracion-ia/guardar")
    public String guardarConfiguracion(@RequestParam("promptSistema") String prompt,
                                       @RequestParam("preguntasDiagnosticas") String preguntas,
                                       @RequestParam("saludoInicial") String saludo,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        String usuario = (authentication != null) ? authentication.getName() : "COORDINADOR";
        configuracionService.guardarConfiguracion(prompt, preguntas, saludo, usuario);
        redirectAttributes.addFlashAttribute("mensajeExito", "¡Instrucciones del asistente virtual actualizadas correctamente!");
        return "redirect:/coordinador/configuracion-ia";
    }

    @PostMapping("/coordinador/configuracion-ia/restablecer")
    public String restablecerConfiguracion(Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        String usuario = (authentication != null) ? authentication.getName() : "COORDINADOR";
        configuracionService.restablecerPorDefecto(usuario);
        redirectAttributes.addFlashAttribute("mensajeExito", "Se han restablecido las instrucciones y preguntas predeterminadas según la documentación oficial.");
        return "redirect:/coordinador/configuracion-ia";
    }
}
