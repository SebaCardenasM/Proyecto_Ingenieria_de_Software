package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "documentos")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numeroPractica;

    @Column(nullable = false)
    private String nombreArchivo;

    @Column(nullable = false)
    private String rutaServidor;

    private LocalDateTime fechaSubida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipoDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "archivos", "estudiante", "profesor"})
    private Practica practica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "practicas", "profesorCoordinador"})
    private Estudiante estudiante;

    public Documento() {}

    @PrePersist
    protected void onCreate() {
        if (this.fechaSubida == null) {
            this.fechaSubida = LocalDateTime.now();
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroPractica() { return numeroPractica; }
    public void setNumeroPractica(Integer numeroPractica) { this.numeroPractica = numeroPractica; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRutaServidor() { return rutaServidor; }
    public void setRutaServidor(String rutaServidor) { this.rutaServidor = rutaServidor; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public Practica getPractica() { return practica; }
    public void setPractica(Practica practica) { this.practica = practica; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
}