package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Interaccion_IA")
public class InteraccionIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String rutEstudiante;

    private Integer nivelPractica;

    @Column(nullable = false)
    private String categoriaDuda; // Ej: "PLANIFICACION", "INFORME FINAL", "RUBRICA / EVALUACION", "REGLAMENTO GENERAL"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptUsuario;

    @Column(columnDefinition = "TEXT")
    private String respuestaIa;

    private LocalDateTime fecha = LocalDateTime.now();

    public InteraccionIA() {}

    public InteraccionIA(String rutEstudiante, Integer nivelPractica, String categoriaDuda, String promptUsuario, String respuestaIa) {
        this.rutEstudiante = rutEstudiante;
        this.nivelPractica = nivelPractica;
        this.categoriaDuda = categoriaDuda;
        this.promptUsuario = promptUsuario;
        this.respuestaIa = respuestaIa;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getRutEstudiante() { return rutEstudiante; }
    public Integer getNivelPractica() { return nivelPractica; }
    public String getCategoriaDuda() { return categoriaDuda; }
    public String getPromptUsuario() { return promptUsuario; }
    public String getRespuestaIa() { return respuestaIa; }
    public LocalDateTime getFecha() { return fecha; }
}