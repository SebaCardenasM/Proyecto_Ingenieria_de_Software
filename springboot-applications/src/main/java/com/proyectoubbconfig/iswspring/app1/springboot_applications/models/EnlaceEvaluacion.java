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

/**
 * Enlace temporal y de un solo uso que permite a un profesor colaborador (sin cuenta)
 * enviar su pauta de evaluación en línea.
 */
@Entity
@Table(name = "enlace_evaluacion")
public class EnlaceEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token aleatorio (256 bits) que va en la URL
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    // Id de la planificación confirmada (sin FK: si se elimina el documento, el enlace deja de ser válido)
    @Column(nullable = false)
    private Long planificacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practica_id", nullable = false)
    private Practica practica;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private Boolean usado = false;

    private LocalDateTime fechaUso;

    // Id de la pauta que se envió con este enlace
    private Long pautaId;

    public EnlaceEvaluacion() {}

    public boolean isUsado() { return Boolean.TRUE.equals(usado); }

    public Long getId() { return id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getPlanificacionId() { return planificacionId; }
    public void setPlanificacionId(Long planificacionId) { this.planificacionId = planificacionId; }

    public Practica getPractica() { return practica; }
    public void setPractica(Practica practica) { this.practica = practica; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public void setUsado(Boolean usado) { this.usado = usado; }

    public LocalDateTime getFechaUso() { return fechaUso; }
    public void setFechaUso(LocalDateTime fechaUso) { this.fechaUso = fechaUso; }

    public Long getPautaId() { return pautaId; }
    public void setPautaId(Long pautaId) { this.pautaId = pautaId; }
}
