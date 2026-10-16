package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for system audit trails and security activity tracking.
 */
public class AuditLogDao {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogDao.class);

    public boolean log(AuditLog entry) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entry.getUserId() != null) {
                stmt.setInt(1, entry.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, entry.getUsername() != null ? entry.getUsername() : "SYSTEM");
            stmt.setString(3, entry.getAction());
            stmt.setString(4, entry.getEntityType());
            stmt.setString(5, entry.getEntityId());
            stmt.setString(6, entry.getDetails());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        entry.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating audit log entry: {}", entry.getAction(), e);
        }
        return false;
    }

    public List<AuditLog> findAll(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT id, user_id, username, action, entity_type, entity_id, details, created_at " +
                     "FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving audit logs", e);
        }
        return list;
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getInt("id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) {
            a.setUserId(uid);
        }
        a.setUsername(rs.getString("username"));
        a.setAction(rs.getString("action"));
        a.setEntityType(rs.getString("entity_type"));
        a.setEntityId(rs.getString("entity_id"));
        a.setDetails(rs.getString("details"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) a.setCreatedAt(ct.toLocalDateTime());
        return a;
    }
}
