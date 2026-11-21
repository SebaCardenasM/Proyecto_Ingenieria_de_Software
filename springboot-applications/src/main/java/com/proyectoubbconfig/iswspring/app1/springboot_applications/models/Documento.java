package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "Documento")
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

    // null = documentos anteriores a la cadena de revisión (se consideran ENVIADO)
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio")
    private EstadoDocumento estado;

    // Revisión previa con el asistente de IA (opcional u obligatoria según configuración)
    private Boolean revisadoIa = false;

    @Column(columnDefinition = "TEXT")
    private String revisionIa;

    private LocalDateTime fechaRevisionIa;

    @ManyToOne(fetch = FetchType.EAGER) 
    @JoinColumn(name = "practica_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "archivos", "estudiante", "profesor", "colaborador", "tutor"})
    private Practica practica;

    @ManyToOne(fetch = FetchType.EAGER)
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

    public EstadoDocumento getEstado() {
        return estado != null ? estado : EstadoDocumento.ENVIADO;
    }
    public void setEstado(EstadoDocumento estado) { this.estado = estado; }
    public boolean isBorrador() { return getEstado() == EstadoDocumento.BORRADOR; }

    public boolean isRevisadoIa() { return Boolean.TRUE.equals(revisadoIa); }
    public void setRevisadoIa(Boolean revisadoIa) { this.revisadoIa = revisadoIa; }

    public String getRevisionIa() { return revisionIa; }
    public void setRevisionIa(String revisionIa) { this.revisionIa = revisionIa; }

    public LocalDateTime getFechaRevisionIa() { return fechaRevisionIa; }
    public void setFechaRevisionIa(LocalDateTime fechaRevisionIa) { this.fechaRevisionIa = fechaRevisionIa; }

    public Practica getPractica() { return practica; }
    public void setPractica(Practica practica) { this.practica = practica; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
}