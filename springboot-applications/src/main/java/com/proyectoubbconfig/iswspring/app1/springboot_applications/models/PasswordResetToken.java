package com.proyectoubbconfig.iswspring.app1.springboot_applications.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String rut;

    // Se guarda el hash (BCrypt), nunca el código en claro
    @Column(nullable = false)
    private String codigoHash;

    @Column(nullable = false)
    private LocalDateTime expira;

    private int intentos = 0;

    public PasswordResetToken() {}

    public PasswordResetToken(String rut, String codigoHash, LocalDateTime expira) {
        this.rut = rut;
        this.codigoHash = codigoHash;
        this.expira = expira;
    }

    public Long getId() { return id; }
    public String getRut() { return rut; }
    public String getCodigoHash() { return codigoHash; }
    public LocalDateTime getExpira() { return expira; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }
}
