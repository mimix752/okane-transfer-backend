package com.okanetransfer.entity;

import com.okanetransfer.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agents")
@PrimaryKeyJoinColumn(name = "id")
public class Agent extends User {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Agent() {
    }

    public Agent(Long id, String username, String email, String password, String phone, Role role, LocalDateTime createdAt, LocalDateTime updatedAt, boolean enabled, Agency agency, boolean active, LocalDateTime createdAt1, LocalDateTime updatedAt1) {
        super(id, username, email, password, phone, role, createdAt, updatedAt, enabled);
        this.agency = agency;
        this.active = active;
        this.createdAt = createdAt1;
        this.updatedAt = updatedAt1;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}