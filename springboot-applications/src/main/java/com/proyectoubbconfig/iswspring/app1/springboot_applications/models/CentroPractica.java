package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "centro_practica")
public class CentroPractica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String region;
    private String comuna;
    private String calle;
    private String numero;

    @OneToMany(mappedBy = "centroPractica")
    private List<Practica> practicas;

    public CentroPractica() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }

    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public List<Practica> getPracticas() { return practicas; }
    public void setPracticas(List<Practica> practicas) { this.practicas = practicas; }

    @Transient
    public String getDireccion() {
        StringBuilder sb = new StringBuilder();
        if (calle != null && !calle.trim().isEmpty()) {
            sb.append(calle.trim());
        }
        if (numero != null && !numero.trim().isEmpty()) {
            if (!sb.isEmpty()) sb.append(" #");
            sb.append(numero.trim());
        }
        return sb.toString();
    }

    @Transient
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        String dir = getDireccion();
        if (!dir.isEmpty()) {
            sb.append(dir);
        }
        if (comuna != null && !comuna.trim().isEmpty()) {
            if (!sb.isEmpty()) sb.append(" · ");
            sb.append(comuna.trim());
        }
        if (region != null && !region.trim().isEmpty()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(region.trim());
        }
        return sb.toString();
    }
}