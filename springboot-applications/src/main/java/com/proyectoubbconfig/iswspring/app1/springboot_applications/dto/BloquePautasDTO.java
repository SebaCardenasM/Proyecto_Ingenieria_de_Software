package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import java.util.ArrayList;
import java.util.List;

public class BloquePautasDTO {
    private String nombreBloque;
    private TipoPauta tipoPauta;
    private String icono;
    private String badgeClass;
    private List<PautaSlotDTO> slots = new ArrayList<>();

    public BloquePautasDTO() {}

    public BloquePautasDTO(String nombreBloque, TipoPauta tipoPauta, String icono, String badgeClass) {
        this.nombreBloque = nombreBloque;
        this.tipoPauta = tipoPauta;
        this.icono = icono;
        this.badgeClass = badgeClass;
    }

    public String getNombreBloque() { return nombreBloque; }
    public void setNombreBloque(String nombreBloque) { this.nombreBloque = nombreBloque; }

    public TipoPauta getTipoPauta() { return tipoPauta; }
    public void setTipoPauta(TipoPauta tipoPauta) { this.tipoPauta = tipoPauta; }

    public String getBadgeClass() { return badgeClass; }
    public void setBadgeClass(String badgeClass) { this.badgeClass = badgeClass; }
    public String getColorClase() { return badgeClass; }
    public void setColorClase(String colorClase) { this.badgeClass = colorClase; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public String getIconoClase() { return icono; }
    public void setIconoClase(String iconoClase) { this.icono = iconoClase; }

    public List<PautaSlotDTO> getSlots() { return slots; }
    public void setSlots(List<PautaSlotDTO> slots) { this.slots = slots; }

    public void addSlot(PautaSlotDTO slot) {
        if (this.slots == null) this.slots = new ArrayList<>();
        this.slots.add(slot);
    }
}
