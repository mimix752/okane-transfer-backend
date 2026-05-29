package com.okanetransfer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String code;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    private boolean used = false;

    public OtpCode() {}

    public OtpCode(String username, String code, LocalDateTime expiresAt) {
        this.username = username;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getCode() { return code; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}