package com.fitcore.model;

import java.math.BigDecimal;

/**
 * Data transfer object for membership plan popularity distribution.
 */
public class PlanDistribution {
    private String planName;
    private String planCode;
    private int totalSubscriptions;
    private BigDecimal totalRevenue;

    public PlanDistribution() {
        this.totalRevenue = BigDecimal.ZERO;
    }

    public PlanDistribution(String planName, String planCode, int totalSubscriptions, BigDecimal totalRevenue) {
        this.planName = planName;
        this.planCode = planCode;
        this.totalSubscriptions = totalSubscriptions;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public int getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(int totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
