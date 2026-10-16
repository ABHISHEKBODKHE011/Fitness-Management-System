package com.fitcore.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated dashboard metrics model calculated directly from the MySQL database.
 */
public class DashboardMetrics {
    private int totalMembers;
    private int activeMembers;
    private int activeTrainers;
    private int todayAttendanceCount;
    private int activeMemberships;
    private int expiredMemberships;
    private int expiringSoonMemberships;
    private BigDecimal totalRevenueCollected;
    private BigDecimal currentMonthRevenue;
    private int pendingPaymentsCount;

    private List<MonthlyRevenue> monthlyRevenues = new ArrayList<>();
    private List<PlanDistribution> planDistributions = new ArrayList<>();
    private List<Member> recentMembers = new ArrayList<>();
    private List<Payment> recentPayments = new ArrayList<>();

    public DashboardMetrics() {
        this.totalRevenueCollected = BigDecimal.ZERO;
        this.currentMonthRevenue = BigDecimal.ZERO;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }

    public int getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(int activeMembers) {
        this.activeMembers = activeMembers;
    }

    public int getActiveTrainers() {
        return activeTrainers;
    }

    public void setActiveTrainers(int activeTrainers) {
        this.activeTrainers = activeTrainers;
    }

    public int getTodayAttendanceCount() {
        return todayAttendanceCount;
    }

    public void setTodayAttendanceCount(int todayAttendanceCount) {
        this.todayAttendanceCount = todayAttendanceCount;
    }

    public int getActiveMemberships() {
        return activeMemberships;
    }

    public void setActiveMemberships(int activeMemberships) {
        this.activeMemberships = activeMemberships;
    }

    public int getExpiredMemberships() {
        return expiredMemberships;
    }

    public void setExpiredMemberships(int expiredMemberships) {
        this.expiredMemberships = expiredMemberships;
    }

    public int getExpiringSoonMemberships() {
        return expiringSoonMemberships;
    }

    public void setExpiringSoonMemberships(int expiringSoonMemberships) {
        this.expiringSoonMemberships = expiringSoonMemberships;
    }

    public BigDecimal getTotalRevenueCollected() {
        return totalRevenueCollected;
    }

    public void setTotalRevenueCollected(BigDecimal totalRevenueCollected) {
        this.totalRevenueCollected = totalRevenueCollected;
    }

    public BigDecimal getCurrentMonthRevenue() {
        return currentMonthRevenue;
    }

    public void setCurrentMonthRevenue(BigDecimal currentMonthRevenue) {
        this.currentMonthRevenue = currentMonthRevenue;
    }

    public int getPendingPaymentsCount() {
        return pendingPaymentsCount;
    }

    public void setPendingPaymentsCount(int pendingPaymentsCount) {
        this.pendingPaymentsCount = pendingPaymentsCount;
    }

    public List<MonthlyRevenue> getMonthlyRevenues() {
        return monthlyRevenues;
    }

    public void setMonthlyRevenues(List<MonthlyRevenue> monthlyRevenues) {
        this.monthlyRevenues = monthlyRevenues;
    }

    public List<PlanDistribution> getPlanDistributions() {
        return planDistributions;
    }

    public void setPlanDistributions(List<PlanDistribution> planDistributions) {
        this.planDistributions = planDistributions;
    }

    public List<Member> getRecentMembers() {
        return recentMembers;
    }

    public void setRecentMembers(List<Member> recentMembers) {
        this.recentMembers = recentMembers;
    }

    public List<Payment> getRecentPayments() {
        return recentPayments;
    }

    public void setRecentPayments(List<Payment> recentPayments) {
        this.recentPayments = recentPayments;
    }
}
