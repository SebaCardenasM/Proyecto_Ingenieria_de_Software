package com.proyectoubbconfig.iswspring.app1.springboot_applications.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ModeloGlobalAdvice {

    @Value("${documentos.revision-ia.obligatoria:false}")
    private boolean revisionIaObligatoria;

    @ModelAttribute("revisionIaObligatoria")
    public boolean revisionIaObligatoria() {
        return revisionIaObligatoria;
    }
}