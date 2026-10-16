package com.fitcore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain entity representing a gym membership plan offering.
 */
public class MembershipPlan {
    private Integer id;
    private String planCode;
    private String name;
    private int durationDays;
    private BigDecimal fee;
    private String description;
    private String status; // ACTIVE, INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MembershipPlan() {
        this.fee = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }

    public MembershipPlan(Integer id, String planCode, String name, int durationDays, BigDecimal fee, String description, String status) {
        this.id = id;
        this.planCode = planCode;
        this.name = name;
        this.durationDays = durationDays;
        this.fee = fee != null ? fee : BigDecimal.ZERO;
        this.description = description;
        this.status = status != null ? status : "ACTIVE";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return name + " (" + durationDays + " days - $" + fee + ")";
    }
}
