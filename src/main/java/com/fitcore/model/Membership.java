package com.fitcore.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing an assigned or renewed membership instance.
 */
public class Membership {
    private Integer id;
    private String membershipCode;
    private Integer memberId;
    private Integer planId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal agreedFee;
    private String status; // ACTIVE, EXPIRED, UPCOMING, CANCELLED
    private Integer renewedFromId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Display helpers
    private String memberCode;
    private String memberName;
    private String planName;
    private int planDurationDays;

    public Membership() {
        this.agreedFee = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }

    public Membership(Integer id, String membershipCode, Integer memberId, Integer planId, LocalDate startDate, LocalDate endDate, BigDecimal agreedFee, String status) {
        this.id = id;
        this.membershipCode = membershipCode;
        this.memberId = memberId;
        this.planId = planId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.agreedFee = agreedFee != null ? agreedFee : BigDecimal.ZERO;
        this.status = status != null ? status : "ACTIVE";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMembershipCode() {
        return membershipCode;
    }

    public void setMembershipCode(String membershipCode) {
        this.membershipCode = membershipCode;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAgreedFee() {
        return agreedFee;
    }

    public void setAgreedFee(BigDecimal agreedFee) {
        this.agreedFee = agreedFee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRenewedFromId() {
        return renewedFromId;
    }

    public void setRenewedFromId(Integer renewedFromId) {
        this.renewedFromId = renewedFromId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public int getPlanDurationDays() {
        return planDurationDays;
    }

    public void setPlanDurationDays(int planDurationDays) {
        this.planDurationDays = planDurationDays;
    }

    /**
     * Dynamically calculates status based on today's date if not CANCELLED.
     */
    public String calculateDynamicStatus(LocalDate referenceDate) {
        if ("CANCELLED".equalsIgnoreCase(this.status)) {
            return "CANCELLED";
        }
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
        }
        if (referenceDate.isBefore(startDate)) {
            return "UPCOMING";
        } else if (referenceDate.isAfter(endDate)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }

    @Override
    public String toString() {
        return membershipCode + " (" + planName + ": " + startDate + " to " + endDate + ")";
    }
}
