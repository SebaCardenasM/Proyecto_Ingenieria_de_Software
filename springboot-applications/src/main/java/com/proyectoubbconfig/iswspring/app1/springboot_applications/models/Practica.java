package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "Practica")
public class Practica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ano;
    private Integer semestre; // 1 o 2
    private Integer numeroPractica;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPractica estado = EstadoPractica.PENDIENTE;
    
    private Double notaAprobacion;
    
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @ManyToOne
    @JoinColumn(name = "rut_estudiante", referencedColumnName = "rut")
    private Estudiante estudiante;

    @ManyToOne
    @JoinColumn(name = "rut_profesor", referencedColumnName = "rut", nullable = true)
    private Profesor profesor;

    @ManyToOne
    @JoinColumn(name = "rut_colaborador", referencedColumnName = "rut", nullable = true)
    private Colaborador colaborador;

    @ManyToOne
    @JoinColumn(name = "rut_tutor", referencedColumnName = "rut", nullable = true)
    private Profesor tutor;

    @ManyToOne
    @JoinColumn(name = "centro_practica_id", referencedColumnName = "id", nullable = true)
    private CentroPractica centroPractica;

    @OneToMany(mappedBy = "practica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Documento> archivos = new ArrayList<>();

    public Practica() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }

    public Integer getNumeroPractica() { return numeroPractica; }
    public void setNumeroPractica(Integer numeroPractica) { this.numeroPractica = numeroPractica; }

    public EstadoPractica getEstado() { return estado; }
    public void setEstado(EstadoPractica estado) { this.estado = estado; }

    public Double getNotaAprobacion() { return notaAprobacion; }
    public void setNotaAprobacion(Double notaAprobacion) { this.notaAprobacion = notaAprobacion; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public Colaborador getColaborador() { return colaborador; }
    public void setColaborador(Colaborador colaborador) { this.colaborador = colaborador; }

    public CentroPractica getCentroPractica() { return centroPractica; }
    public void setCentroPractica(CentroPractica centroPractica) { this.centroPractica = centroPractica; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public List<Documento> getArchivos() { return archivos; }
    public void setArchivos(List<Documento> archivos) { this.archivos = archivos; }

    public Profesor getTutor() { return tutor; }
    public void setTutor(Profesor tutor) { this.tutor = tutor; }
}