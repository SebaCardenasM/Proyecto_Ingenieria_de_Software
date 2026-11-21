package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Aviso dentro de la plataforma para un estudiante (opcionalmente con un enlace temporal). */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rut_estudiante", referencedColumnName = "rut", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enlace_id")
    private EnlaceEvaluacion enlace;

    @Column(nullable = false)
    private Boolean leida = false;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public Notificacion() {}

    public Notificacion(Estudiante estudiante, String titulo, String mensaje, EnlaceEvaluacion enlace) {
        this.estudiante = estudiante;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.enlace = enlace;
        this.leida = false;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Estudiante getEstudiante() { return estudiante; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public EnlaceEvaluacion getEnlace() { return enlace; }
    public boolean isLeida() { return Boolean.TRUE.equals(leida); }
    public void setLeida(Boolean leida) { this.leida = leida; }
    public LocalDateTime getFecha() { return fecha; }
}
