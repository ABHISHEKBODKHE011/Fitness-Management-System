package com.fitcore.model;

import java.time.LocalDateTime;

/**
 * Domain entity representing a certified fitness trainer.
 */
public class Trainer {
    private Integer id;
    private Integer userId;
    private String trainerCode;
    private String name;
    private String phone;
    private String email;
    private String specialization;
    private String status; // ACTIVE, INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional calculated fields for display
    private int assignedMemberCount;

    public Trainer() {
        this.status = "ACTIVE";
    }

    public Trainer(Integer id, Integer userId, String trainerCode, String name, String phone, String email, String specialization, String status) {
        this.id = id;
        this.userId = userId;
        this.trainerCode = trainerCode;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.specialization = specialization;
        this.status = status != null ? status : "ACTIVE";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTrainerCode() {
        return trainerCode;
    }

    public void setTrainerCode(String trainerCode) {
        this.trainerCode = trainerCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getAssignedMemberCount() {
        return assignedMemberCount;
    }

    public void setAssignedMemberCount(int assignedMemberCount) {
        this.assignedMemberCount = assignedMemberCount;
    }

    @Override
    public String toString() {
        return name + " (" + specialization + ")";
    }
}
