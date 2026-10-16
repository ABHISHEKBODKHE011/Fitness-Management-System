package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.MembershipPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for membership plans and catalog tiers.
 */
public class MembershipPlanDao {

    private static final Logger logger = LoggerFactory.getLogger(MembershipPlanDao.class);

    public boolean create(MembershipPlan plan) {
        String sql = "INSERT INTO membership_plans (plan_code, name, duration_days, fee, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, plan.getPlanCode());
            stmt.setString(2, plan.getName());
            stmt.setInt(3, plan.getDurationDays());
            stmt.setBigDecimal(4, plan.getFee());
            stmt.setString(5, plan.getDescription());
            stmt.setString(6, plan.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        plan.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating membership plan: {}", plan.getName(), e);
        }
        return false;
    }

    public boolean update(MembershipPlan plan) {
        String sql = "UPDATE membership_plans SET name = ?, duration_days = ?, fee = ?, description = ?, status = ? " +
                     "WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, plan.getName());
            stmt.setInt(2, plan.getDurationDays());
            stmt.setBigDecimal(3, plan.getFee());
            stmt.setString(4, plan.getDescription());
            stmt.setString(5, plan.getStatus());
            stmt.setInt(6, plan.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating membership plan id: {}", plan.getId(), e);
        }
        return false;
    }

    public MembershipPlan findById(int id) {
        String sql = "SELECT id, plan_code, name, duration_days, fee, description, status, created_at, updated_at " +
                     "FROM membership_plans WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding membership plan by id: {}", id, e);
        }
        return null;
    }

    public MembershipPlan findByCode(String code) {
        String sql = "SELECT id, plan_code, name, duration_days, fee, description, status, created_at, updated_at " +
                     "FROM membership_plans WHERE plan_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding membership plan by code: {}", code, e);
        }
        return null;
    }

    public List<MembershipPlan> findAll(boolean activeOnly, String search) {
        List<MembershipPlan> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, plan_code, name, duration_days, fee, description, status, created_at, updated_at " +
                "FROM membership_plans WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (activeOnly) {
            sql.append("AND status = 'ACTIVE' ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (plan_code LIKE ? OR name LIKE ? OR description LIKE ?) ");
            String q = "%" + search.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append("ORDER BY duration_days ASC, id ASC");

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
            logger.error("Error querying membership plans", e);
        }
        return list;
    }

    /**
     * Checks if any historical memberships reference this plan.
     */
    public boolean isPlanReferenced(int planId) {
        String sql = "SELECT COUNT(*) FROM memberships WHERE plan_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, planId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking plan reference count for id: {}", planId, e);
        }
        return false;
    }

    public String generateNextCode(int durationDays) {
        String prefix = "PLN-";
        if (durationDays <= 30) prefix += "M";
        else if (durationDays <= 90) prefix += "Q";
        else if (durationDays <= 180) prefix += "S";
        else prefix += "Y";

        String sql = "SELECT COUNT(*) FROM membership_plans";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int count = 1;
            if (rs.next()) {
                count = rs.getInt(1) + 1;
            }
            return String.format("%s%02d", prefix, count);
        } catch (SQLException e) {
            return prefix + (System.currentTimeMillis() % 100);
        }
    }

    private MembershipPlan mapRow(ResultSet rs) throws SQLException {
        MembershipPlan p = new MembershipPlan();
        p.setId(rs.getInt("id"));
        p.setPlanCode(rs.getString("plan_code"));
        p.setName(rs.getString("name"));
        p.setDurationDays(rs.getInt("duration_days"));
        p.setFee(rs.getBigDecimal("fee"));
        p.setDescription(rs.getString("description"));
        p.setStatus(rs.getString("status"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) p.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) p.setUpdatedAt(ut.toLocalDateTime());
        return p;
    }
}
