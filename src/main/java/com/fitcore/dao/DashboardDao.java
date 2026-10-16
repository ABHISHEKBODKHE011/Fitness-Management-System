package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for dashboard KPI analytics, financial trends, and distribution charts.
 */
public class DashboardDao {

    private static final Logger logger = LoggerFactory.getLogger(DashboardDao.class);

    public DashboardMetrics getMetrics() {
        DashboardMetrics metrics = new DashboardMetrics();

        // 1. KPI Counts and Revenue Sums
        String sqlKpi =
                "SELECT " +
                "(SELECT COUNT(*) FROM members) AS total_members, " +
                "(SELECT COUNT(*) FROM members WHERE status = 'ACTIVE') AS active_members, " +
                "(SELECT COUNT(*) FROM trainers WHERE status = 'ACTIVE') AS active_trainers, " +
                "(SELECT COUNT(*) FROM attendance WHERE check_in_date = CURRENT_DATE()) AS today_attendance, " +
                "(SELECT COUNT(*) FROM memberships WHERE status = 'ACTIVE') AS active_memberships, " +
                "(SELECT COUNT(*) FROM memberships WHERE status = 'EXPIRED') AS expired_memberships, " +
                "(SELECT COUNT(*) FROM memberships WHERE status = 'ACTIVE' AND end_date BETWEEN CURRENT_DATE() AND DATE_ADD(CURRENT_DATE(), INTERVAL 7 DAY)) AS expiring_soon, " +
                "(SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'SUCCESS') AS total_revenue, " +
                "(SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'SUCCESS' AND MONTH(payment_date) = MONTH(CURRENT_DATE()) AND YEAR(payment_date) = YEAR(CURRENT_DATE())) AS month_revenue, " +
                "(SELECT COUNT(*) FROM payments WHERE payment_status = 'PENDING') AS pending_payments";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlKpi);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                metrics.setTotalMembers(rs.getInt("total_members"));
                metrics.setActiveMembers(rs.getInt("active_members"));
                metrics.setActiveTrainers(rs.getInt("active_trainers"));
                metrics.setTodayAttendanceCount(rs.getInt("today_attendance"));
                metrics.setActiveMemberships(rs.getInt("active_memberships"));
                metrics.setExpiredMemberships(rs.getInt("expired_memberships"));
                metrics.setExpiringSoonMemberships(rs.getInt("expiring_soon"));
                metrics.setTotalRevenueCollected(rs.getBigDecimal("total_revenue"));
                metrics.setCurrentMonthRevenue(rs.getBigDecimal("month_revenue"));
                metrics.setPendingPaymentsCount(rs.getInt("pending_payments"));
            }
        } catch (SQLException e) {
            logger.error("Error calculating dashboard KPI metrics", e);
        }

        metrics.setMonthlyRevenues(getMonthlyRevenueTrend(6));
        metrics.setPlanDistributions(getPlanDistribution());
        metrics.setRecentMembers(getRecentMembers(5));
        metrics.setRecentPayments(getRecentPayments(5));

        return metrics;
    }

    public List<MonthlyRevenue> getMonthlyRevenueTrend(int months) {
        List<MonthlyRevenue> list = new ArrayList<>();
        String sql =
                "SELECT " +
                "DATE_FORMAT(payment_date, '%Y-%m') AS month_period, " +
                "DATE_FORMAT(payment_date, '%b %Y') AS month_label, " +
                "COUNT(id) AS tx_count, " +
                "COALESCE(SUM(amount), 0.00) AS total_collected " +
                "FROM payments " +
                "WHERE payment_status = 'SUCCESS' " +
                "  AND payment_date >= DATE_SUB(CURRENT_DATE(), INTERVAL ? MONTH) " +
                "GROUP BY DATE_FORMAT(payment_date, '%Y-%m'), DATE_FORMAT(payment_date, '%b %Y') " +
                "ORDER BY month_period ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, months > 0 ? months : 6);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new MonthlyRevenue(
                            rs.getString("month_period"),
                            rs.getString("month_label"),
                            rs.getInt("tx_count"),
                            rs.getBigDecimal("total_collected")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving monthly revenue trend", e);
        }
        return list;
    }

    public List<PlanDistribution> getPlanDistribution() {
        List<PlanDistribution> list = new ArrayList<>();
        String sql =
                "SELECT " +
                "p.name AS plan_name, " +
                "p.plan_code, " +
                "COUNT(m.id) AS total_subs, " +
                "COALESCE(SUM(m.agreed_fee), 0.00) AS total_rev " +
                "FROM membership_plans p " +
                "LEFT JOIN memberships m ON p.id = m.plan_id " +
                "GROUP BY p.id, p.name, p.plan_code " +
                "ORDER BY total_subs DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new PlanDistribution(
                        rs.getString("plan_name"),
                        rs.getString("plan_code"),
                        rs.getInt("total_subs"),
                        rs.getBigDecimal("total_rev")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error calculating plan distribution", e);
        }
        return list;
    }

    public List<Member> getRecentMembers(int limit) {
        List<Member> list = new ArrayList<>();
        String sql =
                "SELECT m.id, m.member_code, m.full_name, m.phone, m.email, m.gender, m.emergency_contact, " +
                "m.joining_date, m.assigned_trainer_id, m.status, m.created_at, m.updated_at, " +
                "t.name AS trainer_name, " +
                "(SELECT ms.status FROM memberships ms WHERE ms.member_id = m.id AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1) AS current_status, " +
                "(SELECT p.name FROM memberships ms JOIN membership_plans p ON ms.plan_id = p.id WHERE ms.member_id = m.id AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1) AS current_plan_name, " +
                "(SELECT ms.end_date FROM memberships ms WHERE ms.member_id = m.id AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1) AS current_end_date " +
                "FROM members m " +
                "LEFT JOIN trainers t ON m.assigned_trainer_id = t.id " +
                "ORDER BY m.id DESC LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Member m = new Member();
                    m.setId(rs.getInt("id"));
                    m.setMemberCode(rs.getString("member_code"));
                    m.setFullName(rs.getString("full_name"));
                    m.setPhone(rs.getString("phone"));
                    m.setEmail(rs.getString("email"));
                    m.setStatus(rs.getString("status"));
                    m.setAssignedTrainerName(rs.getString("trainer_name"));
                    m.setCurrentPlanName(rs.getString("current_plan_name"));
                    m.setCurrentMembershipStatus(rs.getString("current_status"));
                    Date ed = rs.getDate("current_end_date");
                    if (ed != null) m.setCurrentMembershipEndDate(ed.toLocalDate());
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting recent members", e);
        }
        return list;
    }

    public List<Payment> getRecentPayments(int limit) {
        List<Payment> list = new ArrayList<>();
        String sql =
                "SELECT p.id, p.payment_code, p.member_id, p.membership_id, p.amount, p.payment_date, " +
                "p.payment_method, p.payment_status, p.transaction_reference, p.receipt_notes, " +
                "m.member_code, m.full_name AS member_name, mp.name AS plan_name " +
                "FROM payments p " +
                "JOIN members m ON p.member_id = m.id " +
                "LEFT JOIN memberships ms ON p.membership_id = ms.id " +
                "LEFT JOIN membership_plans mp ON ms.plan_id = mp.id " +
                "ORDER BY p.payment_date DESC LIMIT ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payment p = new Payment();
                    p.setId(rs.getInt("id"));
                    p.setPaymentCode(rs.getString("payment_code"));
                    p.setMemberId(rs.getInt("member_id"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    Timestamp pt = rs.getTimestamp("payment_date");
                    if (pt != null) p.setPaymentDate(pt.toLocalDateTime());
                    p.setPaymentMethod(rs.getString("payment_method"));
                    p.setPaymentStatus(rs.getString("payment_status"));
                    p.setTransactionReference(rs.getString("transaction_reference"));
                    p.setMemberCode(rs.getString("member_code"));
                    p.setMemberName(rs.getString("member_name"));
                    p.setPlanName(rs.getString("plan_name"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting recent payments", e);
        }
        return list;
    }
}
