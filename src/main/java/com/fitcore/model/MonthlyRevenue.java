package com.fitcore.model;

import java.math.BigDecimal;

/**
 * Data transfer object for monthly revenue analytics.
 */
public class MonthlyRevenue {
    private String monthPeriod; // YYYY-MM
    private String monthLabel;  // e.g. "Oct 2026"
    private int transactionCount;
    private BigDecimal totalCollected;

    public MonthlyRevenue() {
        this.totalCollected = BigDecimal.ZERO;
    }

    public MonthlyRevenue(String monthPeriod, String monthLabel, int transactionCount, BigDecimal totalCollected) {
        this.monthPeriod = monthPeriod;
        this.monthLabel = monthLabel;
        this.transactionCount = transactionCount;
        this.totalCollected = totalCollected != null ? totalCollected : BigDecimal.ZERO;
    }

    public String getMonthPeriod() {
        return monthPeriod;
    }

    public void setMonthPeriod(String monthPeriod) {
        this.monthPeriod = monthPeriod;
    }

    public String getMonthLabel() {
        return monthLabel;
    }

    public void setMonthLabel(String monthLabel) {
        this.monthLabel = monthLabel;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public BigDecimal getTotalCollected() {
        return totalCollected;
    }

    public void setTotalCollected(BigDecimal totalCollected) {
        this.totalCollected = totalCollected;
    }
}
