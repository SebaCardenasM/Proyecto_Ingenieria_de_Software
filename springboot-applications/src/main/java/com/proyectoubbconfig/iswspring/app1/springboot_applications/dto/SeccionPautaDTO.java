package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import java.util.ArrayList;
import java.util.List;

public class SeccionPautaDTO {

    private String titulo;
    private List<String> items = new ArrayList<>();

    public SeccionPautaDTO() {}

    public SeccionPautaDTO(String titulo, List<String> items) {
        this.titulo = titulo;
        if (items != null) {
            this.items = items;
        }
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }
}
