package com.proyectoubbconfig.iswspring.app1.springboot_applications.dto;

import com.proyectoubbconfig.iswspring.app1.springboot_applications.models.TipoPauta;
import java.time.LocalDateTime;

public class PautaSlotDTO {
    private String idSlot;
    private TipoPauta tipoPauta;
    private int numeroObservacion;
    private String titulo;
    private String subtitulo;
    private boolean completada;
    private boolean tieneBorrador;
    private Long idPauta;
    private Double puntaje;
    private Double nota;
    private LocalDateTime fecha;
    private String curso;
    private String nombreArchivo;

    public PautaSlotDTO() {}

    public PautaSlotDTO(String idSlot, TipoPauta tipoPauta, int numeroObservacion, String titulo, String subtitulo) {
        this.idSlot = idSlot;
        this.tipoPauta = tipoPauta;
        this.numeroObservacion = numeroObservacion;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
    }

    public String getIdSlot() { return idSlot; }
    public void setIdSlot(String idSlot) { this.idSlot = idSlot; }
    public String getIdentificadorSlot() { return idSlot; }

    public TipoPauta getTipoPauta() { return tipoPauta; }
    public void setTipoPauta(TipoPauta tipoPauta) { this.tipoPauta = tipoPauta; }

    public int getNumeroObservacion() { return numeroObservacion; }
    public void setNumeroObservacion(int numeroObservacion) { this.numeroObservacion = numeroObservacion; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getSubtitulo() { return subtitulo; }
    public void setSubtitulo(String subtitulo) { this.subtitulo = subtitulo; }
    public String getDescripcion() { return subtitulo; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    public boolean isTieneBorrador() { return tieneBorrador; }
    public void setTieneBorrador(boolean tieneBorrador) { this.tieneBorrador = tieneBorrador; }

    public Long getIdPauta() { return idPauta; }
    public void setIdPauta(Long idPauta) { this.idPauta = idPauta; }

    public Double getPuntaje() { return puntaje; }
    public void setPuntaje(Double puntaje) { this.puntaje = puntaje; }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }
}
