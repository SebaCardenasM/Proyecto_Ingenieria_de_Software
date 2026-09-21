package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "practicas")
public class Practica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ano;
    private Integer semestre; // 1 o 2
    private Integer numeroPractica;
    
    // Cambiamos el Boolean por el Enum persistido como STRING
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPractica estado = EstadoPractica.PENDIENTE; // Se inicializa en PENDIENTE por defecto
    
    private Double notaAprobacion;
    
    // manejo de fechas
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate limitePlanificacion; // Hito a las 3 semanas
    private LocalDate limiteInformeFinal;  // Hito a las 6 semanas

    // Relación N:1 con Estudiante
    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    // Relación N:1 con Profesor
    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    @OneToMany(mappedBy = "practica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> archivos = new ArrayList<>();

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }

    public Integer getNumeroPractica() { return numeroPractica; }
    public void setNumeroPractica(Integer numeroPractica) { this.numeroPractica = numeroPractica; }

    // Getters y Setters para el nuevo campo estado
    public EstadoPractica getEstado() { return estado; }
    public void setEstado(EstadoPractica estado) { this.estado = estado; }

    public Double getNotaAprobacion() { return notaAprobacion; }
    public void setNotaAprobacion(Double notaAprobacion) { this.notaAprobacion = notaAprobacion; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalDate getLimitePlanificacion() { return limitePlanificacion; }
    public void setLimitePlanificacion(LocalDate limitePlanificacion) { this.limitePlanificacion = limitePlanificacion; }

    public LocalDate getLimiteInformeFinal() { return limiteInformeFinal; }
    public void setLimiteInformeFinal(LocalDate limiteInformeFinal) { this.limiteInformeFinal = limiteInformeFinal; }
    public List<Documento> getArchivos() {return archivos;}

    public void setArchivos(List<Documento> archivos) {this.archivos = archivos;}
}