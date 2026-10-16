package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for member profiles, contact info, and trainer assignments.
 */
public class MemberDao {

    private static final Logger logger = LoggerFactory.getLogger(MemberDao.class);

    private static final String BASE_SELECT =
            "SELECT m.id, m.member_code, m.full_name, m.phone, m.email, m.gender, m.emergency_contact, " +
            "m.joining_date, m.assigned_trainer_id, m.status, m.created_at, m.updated_at, " +
            "t.name AS trainer_name, " +
            "(SELECT ms.status FROM memberships ms WHERE ms.member_id = m.id AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1) AS current_status, " +
            "(SELECT p.name FROM memberships ms JOIN membership_plans p ON ms.plan_id = p.id WHERE ms.member_id = m.id AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1) AS current_plan_name, " +
            "(SELECT ms.end_date FROM memberships ms WHERE ms.member_id = m.id AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1) AS current_end_date " +
            "FROM members m " +
            "LEFT JOIN trainers t ON m.assigned_trainer_id = t.id ";

    public boolean create(Member member) {
        String sql = "INSERT INTO members (member_code, full_name, phone, email, gender, emergency_contact, " +
                     "joining_date, assigned_trainer_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, member.getMemberCode());
            stmt.setString(2, member.getFullName());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getEmail());
            stmt.setString(5, member.getGender());
            stmt.setString(6, member.getEmergencyContact());
            stmt.setDate(7, Date.valueOf(member.getJoiningDate()));
            if (member.getAssignedTrainerId() != null) {
                stmt.setInt(8, member.getAssignedTrainerId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setString(9, member.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        member.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating member: {}", member.getFullName(), e);
        }
        return false;
    }

    public boolean update(Member member) {
        String sql = "UPDATE members SET full_name = ?, phone = ?, email = ?, gender = ?, emergency_contact = ?, " +
                     "joining_date = ?, assigned_trainer_id = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getFullName());
            stmt.setString(2, member.getPhone());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getGender());
            stmt.setString(5, member.getEmergencyContact());
            stmt.setDate(6, Date.valueOf(member.getJoiningDate()));
            if (member.getAssignedTrainerId() != null) {
                stmt.setInt(7, member.getAssignedTrainerId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setString(8, member.getStatus());
            stmt.setInt(9, member.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating member id: {}", member.getId(), e);
        }
        return false;
    }

    public Member findById(int id) {
        String sql = BASE_SELECT + "WHERE m.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding member by id: {}", id, e);
        }
        return null;
    }

    public Member findByCode(String code) {
        String sql = BASE_SELECT + "WHERE m.member_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding member by code: {}", code, e);
        }
        return null;
    }

    public List<Member> findAll(String statusFilter, String searchQuery, Integer trainerId) {
        List<Member> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND m.status = ? ");
            params.add(statusFilter.toUpperCase());
        }
        if (trainerId != null) {
            sql.append("AND m.assigned_trainer_id = ? ");
            params.add(trainerId);
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (m.member_code LIKE ? OR m.full_name LIKE ? OR m.phone LIKE ? OR m.email LIKE ?) ");
            String q = "%" + searchQuery.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        sql.append("ORDER BY m.id DESC");

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
            logger.error("Error querying members", e);
        }
        return list;
    }

    public boolean existsByEmail(String email, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM members WHERE email = ? AND (? IS NULL OR id != ?)";
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
            logger.error("Error checking member email duplicate: {}", email, e);
        }
        return false;
    }

    public boolean existsByPhone(String phone, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM members WHERE phone = ? AND (? IS NULL OR id != ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
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
            logger.error("Error checking member phone duplicate: {}", phone, e);
        }
        return false;
    }

    public String generateNextCode() {
        String sql = "SELECT MAX(id) FROM members";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int maxId = 1000;
            if (rs.next()) {
                int dbMax = rs.getInt(1);
                if (dbMax > 0) {
                    maxId = 1000 + dbMax;
                }
            }
            return String.format("MBR-%04d", maxId + 1);
        } catch (SQLException e) {
            return "MBR-" + (System.currentTimeMillis() % 10000);
        }
    }

    public int count(String status) {
        String sql = "SELECT COUNT(*) FROM members" +
                     (status != null && !status.equalsIgnoreCase("ALL") ? " WHERE status = ?" : "");
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (status != null && !status.equalsIgnoreCase("ALL")) {
                stmt.setString(1, status.toUpperCase());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error counting members", e);
        }
        return 0;
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getInt("id"));
        m.setMemberCode(rs.getString("member_code"));
        m.setFullName(rs.getString("full_name"));
        m.setPhone(rs.getString("phone"));
        m.setEmail(rs.getString("email"));
        m.setGender(rs.getString("gender"));
        m.setEmergencyContact(rs.getString("emergency_contact"));
        Date jd = rs.getDate("joining_date");
        if (jd != null) m.setJoiningDate(jd.toLocalDate());
        int tid = rs.getInt("assigned_trainer_id");
        if (!rs.wasNull()) {
            m.setAssignedTrainerId(tid);
        }
        m.setStatus(rs.getString("status"));
        m.setAssignedTrainerName(rs.getString("trainer_name"));
        m.setCurrentMembershipStatus(rs.getString("current_status"));
        m.setCurrentPlanName(rs.getString("current_plan_name"));
        Date ed = rs.getDate("current_end_date");
        if (ed != null) m.setCurrentMembershipEndDate(ed.toLocalDate());

        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) m.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) m.setUpdatedAt(ut.toLocalDateTime());
        return m;
    }
}
