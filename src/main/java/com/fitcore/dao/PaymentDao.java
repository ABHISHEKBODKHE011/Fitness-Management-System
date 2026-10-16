package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for payments, billing ledger, and transaction references.
 */
public class PaymentDao {

    private static final Logger logger = LoggerFactory.getLogger(PaymentDao.class);

    private static final String BASE_SELECT =
            "SELECT p.id, p.payment_code, p.member_id, p.membership_id, p.amount, p.payment_date, " +
            "p.payment_method, p.payment_status, p.transaction_reference, p.receipt_notes, p.created_at, " +
            "m.member_code, m.full_name AS member_name, " +
            "mp.name AS plan_name " +
            "FROM payments p " +
            "JOIN members m ON p.member_id = m.id " +
            "LEFT JOIN memberships ms ON p.membership_id = ms.id " +
            "LEFT JOIN membership_plans mp ON ms.plan_id = mp.id ";

    public boolean create(Payment payment, Connection conn) throws SQLException {
        String sql = "INSERT INTO payments (payment_code, member_id, membership_id, amount, payment_date, " +
                     "payment_method, payment_status, transaction_reference, receipt_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, payment.getPaymentCode());
            stmt.setInt(2, payment.getMemberId());
            if (payment.getMembershipId() != null) {
                stmt.setInt(3, payment.getMembershipId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setBigDecimal(4, payment.getAmount());
            stmt.setTimestamp(5, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setString(6, payment.getPaymentMethod());
            stmt.setString(7, payment.getPaymentStatus());
            stmt.setString(8, payment.getTransactionReference());
            stmt.setString(9, payment.getReceiptNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        payment.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean create(Payment payment) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return create(payment, conn);
        } catch (SQLException e) {
            logger.error("Error creating payment: {}", payment.getPaymentCode(), e);
            return false;
        }
    }

    public Payment findById(int id) {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by id: {}", id, e);
        }
        return null;
    }

    public Payment findByCode(String paymentCode) {
        String sql = BASE_SELECT + "WHERE p.payment_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paymentCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payment by code: {}", paymentCode, e);
        }
        return null;
    }

    public List<Payment> findByMemberId(int memberId) {
        List<Payment> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE p.member_id = ? ORDER BY p.payment_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding payments for member id: {}", memberId, e);
        }
        return list;
    }

    public List<Payment> findAll(LocalDate startDate, LocalDate endDate, String method, String status, String searchQuery) {
        List<Payment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (startDate != null) {
            sql.append("AND DATE(p.payment_date) >= ? ");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append("AND DATE(p.payment_date) <= ? ");
            params.add(Date.valueOf(endDate));
        }
        if (method != null && !method.equalsIgnoreCase("ALL")) {
            sql.append("AND p.payment_method = ? ");
            params.add(method.toUpperCase());
        }
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND p.payment_status = ? ");
            params.add(status.toUpperCase());
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (p.payment_code LIKE ? OR p.transaction_reference LIKE ? OR m.member_code LIKE ? OR m.full_name LIKE ?) ");
            String q = "%" + searchQuery.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        sql.append("ORDER BY p.payment_date DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error querying payments", e);
        }
        return list;
    }

    public boolean existsByTransactionReference(String reference, Integer excludeId) {
        if (reference == null || reference.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM payments WHERE transaction_reference = ? AND (? IS NULL OR id != ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reference.trim());
            if (excludeId != null) {
                stmt.setInt(2, excludeId);
                stmt.setInt(3, excludeId);
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.INTEGER);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking transaction reference duplicate: {}", reference, e);
        }
        return false;
    }

    public BigDecimal getTotalRevenue(LocalDate start, LocalDate end) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'SUCCESS' ");
        List<Object> params = new ArrayList<>();
        if (start != null) {
            sql.append("AND DATE(payment_date) >= ? ");
            params.add(Date.valueOf(start));
        }
        if (end != null) {
            sql.append("AND DATE(payment_date) <= ? ");
            params.add(Date.valueOf(end));
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating total revenue", e);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getCurrentMonthRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0.00) FROM payments " +
                     "WHERE payment_status = 'SUCCESS' " +
                     "AND YEAR(payment_date) = YEAR(CURRENT_DATE()) " +
                     "AND MONTH(payment_date) = MONTH(CURRENT_DATE())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting current month revenue", e);
        }
        return BigDecimal.ZERO;
    }

    public int getPendingPaymentsCount() {
        String sql = "SELECT COUNT(*) FROM payments WHERE payment_status = 'PENDING'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting pending payments count", e);
        }
        return 0;
    }

    public String generateNextCode() {
        String datePrefix = LocalDate.now().toString().replace("-", "").substring(0, 6);
        String sql = "SELECT COUNT(*) FROM payments WHERE payment_code LIKE ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "PAY-" + datePrefix + "-%");
            int count = 1;
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1) + 1;
                }
            }
            return String.format("PAY-%s-%03d", datePrefix, count);
        } catch (SQLException e) {
            return "PAY-" + datePrefix + "-" + (System.currentTimeMillis() % 1000);
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setPaymentCode(rs.getString("payment_code"));
        p.setMemberId(rs.getInt("member_id"));
        int mid = rs.getInt("membership_id");
        if (!rs.wasNull()) {
            p.setMembershipId(mid);
        }
        p.setAmount(rs.getBigDecimal("amount"));
        Timestamp pt = rs.getTimestamp("payment_date");
        if (pt != null) p.setPaymentDate(pt.toLocalDateTime());
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setPaymentStatus(rs.getString("payment_status"));
        p.setTransactionReference(rs.getString("transaction_reference"));
        p.setReceiptNotes(rs.getString("receipt_notes"));
        p.setMemberCode(rs.getString("member_code"));
        p.setMemberName(rs.getString("member_name"));
        p.setPlanName(rs.getString("plan_name"));

        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) p.setCreatedAt(ct.toLocalDateTime());
        return p;
    }
}
