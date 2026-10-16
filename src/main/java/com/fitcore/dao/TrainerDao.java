package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for certified trainers and member assignments.
 */
public class TrainerDao {

    private static final Logger logger = LoggerFactory.getLogger(TrainerDao.class);

    public boolean create(Trainer trainer) {
        String sql = "INSERT INTO trainers (user_id, trainer_code, name, phone, email, specialization, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (trainer.getUserId() != null) {
                stmt.setInt(1, trainer.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, trainer.getTrainerCode());
            stmt.setString(3, trainer.getName());
            stmt.setString(4, trainer.getPhone());
            stmt.setString(5, trainer.getEmail());
            stmt.setString(6, trainer.getSpecialization());
            stmt.setString(7, trainer.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        trainer.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating trainer: {}", trainer.getName(), e);
        }
        return false;
    }

    public boolean update(Trainer trainer) {
        String sql = "UPDATE trainers SET user_id = ?, name = ?, phone = ?, email = ?, specialization = ?, status = ? " +
                     "WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (trainer.getUserId() != null) {
                stmt.setInt(1, trainer.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, trainer.getName());
            stmt.setString(3, trainer.getPhone());
            stmt.setString(4, trainer.getEmail());
            stmt.setString(5, trainer.getSpecialization());
            stmt.setString(6, trainer.getStatus());
            stmt.setInt(7, trainer.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating trainer id: {}", trainer.getId(), e);
        }
        return false;
    }

    public Trainer findById(int id) {
        String sql = "SELECT t.id, t.user_id, t.trainer_code, t.name, t.phone, t.email, t.specialization, t.status, " +
                     "t.created_at, t.updated_at, " +
                     "(SELECT COUNT(*) FROM members m WHERE m.assigned_trainer_id = t.id AND m.status = 'ACTIVE') AS member_count " +
                     "FROM trainers t WHERE t.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding trainer by id: {}", id, e);
        }
        return null;
    }

    public Trainer findByCode(String code) {
        String sql = "SELECT t.id, t.user_id, t.trainer_code, t.name, t.phone, t.email, t.specialization, t.status, " +
                     "t.created_at, t.updated_at, " +
                     "(SELECT COUNT(*) FROM members m WHERE m.assigned_trainer_id = t.id AND m.status = 'ACTIVE') AS member_count " +
                     "FROM trainers t WHERE t.trainer_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding trainer by code: {}", code, e);
        }
        return null;
    }

    public List<Trainer> findAll(String statusFilter, String searchQuery) {
        List<Trainer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.user_id, t.trainer_code, t.name, t.phone, t.email, t.specialization, t.status, " +
                "t.created_at, t.updated_at, " +
                "(SELECT COUNT(*) FROM members m WHERE m.assigned_trainer_id = t.id AND m.status = 'ACTIVE') AS member_count " +
                "FROM trainers t WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND t.status = ? ");
            params.add(statusFilter.toUpperCase());
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (t.trainer_code LIKE ? OR t.name LIKE ? OR t.phone LIKE ? OR t.specialization LIKE ?) ");
            String q = "%" + searchQuery.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        sql.append("ORDER BY t.id ASC");

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
            logger.error("Error searching trainers", e);
        }
        return list;
    }

    public boolean existsByEmail(String email, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM trainers WHERE email = ? AND (? IS NULL OR id != ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
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
            logger.error("Error checking trainer email existence: {}", email, e);
        }
        return false;
    }

    public String generateNextCode() {
        String sql = "SELECT MAX(id) FROM trainers";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int maxId = 0;
            if (rs.next()) {
                maxId = rs.getInt(1);
            }
            return String.format("TRN-%03d", maxId + 1);
        } catch (SQLException e) {
            logger.error("Error generating next trainer code", e);
            return "TRN-" + System.currentTimeMillis() % 1000;
        }
    }

    public int countActive() {
        String sql = "SELECT COUNT(*) FROM trainers WHERE status = 'ACTIVE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting active trainers", e);
        }
        return 0;
    }

    private Trainer mapRow(ResultSet rs) throws SQLException {
        Trainer t = new Trainer();
        t.setId(rs.getInt("id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) {
            t.setUserId(uid);
        }
        t.setTrainerCode(rs.getString("trainer_code"));
        t.setName(rs.getString("name"));
        t.setPhone(rs.getString("phone"));
        t.setEmail(rs.getString("email"));
        t.setSpecialization(rs.getString("specialization"));
        t.setStatus(rs.getString("status"));
        t.setAssignedMemberCount(rs.getInt("member_count"));

        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) t.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) t.setUpdatedAt(ut.toLocalDateTime());
        return t;
    }
}
