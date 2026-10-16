package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.Membership;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access operations for member membership packages, validity dates, and renewal chains.
 */
public class MembershipDao {

    private static final Logger logger = LoggerFactory.getLogger(MembershipDao.class);

    private static final String BASE_SELECT =
            "SELECT ms.id, ms.membership_code, ms.member_id, ms.plan_id, ms.start_date, ms.end_date, " +
            "ms.agreed_fee, ms.status, ms.renewed_from_id, ms.notes, ms.created_at, ms.updated_at, " +
            "m.member_code, m.full_name AS member_name, " +
            "p.name AS plan_name, p.duration_days AS plan_duration " +
            "FROM memberships ms " +
            "JOIN members m ON ms.member_id = m.id " +
            "JOIN membership_plans p ON ms.plan_id = p.id ";

    public boolean create(Membership membership, Connection conn) throws SQLException {
        String sql = "INSERT INTO memberships (membership_code, member_id, plan_id, start_date, end_date, " +
                     "agreed_fee, status, renewed_from_id, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, membership.getMembershipCode());
            stmt.setInt(2, membership.getMemberId());
            stmt.setInt(3, membership.getPlanId());
            stmt.setDate(4, Date.valueOf(membership.getStartDate()));
            stmt.setDate(5, Date.valueOf(membership.getEndDate()));
            stmt.setBigDecimal(6, membership.getAgreedFee());
            stmt.setString(7, membership.getStatus());
            if (membership.getRenewedFromId() != null) {
                stmt.setInt(8, membership.getRenewedFromId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setString(9, membership.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        membership.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean create(Membership membership) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return create(membership, conn);
        } catch (SQLException e) {
            logger.error("Error creating membership: {}", membership.getMembershipCode(), e);
            return false;
        }
    }

    public boolean update(Membership membership) {
        String sql = "UPDATE memberships SET plan_id = ?, start_date = ?, end_date = ?, agreed_fee = ?, " +
                     "status = ?, renewed_from_id = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, membership.getPlanId());
            stmt.setDate(2, Date.valueOf(membership.getStartDate()));
            stmt.setDate(3, Date.valueOf(membership.getEndDate()));
            stmt.setBigDecimal(4, membership.getAgreedFee());
            stmt.setString(5, membership.getStatus());
            if (membership.getRenewedFromId() != null) {
                stmt.setInt(6, membership.getRenewedFromId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            stmt.setString(7, membership.getNotes());
            stmt.setInt(8, membership.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating membership id: {}", membership.getId(), e);
        }
        return false;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE memberships SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for membership id: {}", id, e);
        }
        return false;
    }

    public Membership findById(int id) {
        String sql = BASE_SELECT + "WHERE ms.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding membership by id: {}", id, e);
        }
        return null;
    }

    public List<Membership> findByMemberId(int memberId) {
        List<Membership> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE ms.member_id = ? ORDER BY ms.start_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding memberships for member id: {}", memberId, e);
        }
        return list;
    }

    public Membership findActiveMembershipForMember(int memberId) {
        String sql = BASE_SELECT + "WHERE ms.member_id = ? AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding active membership for member id: {}", memberId, e);
        }
        return null;
    }

    public List<Membership> findAll(String statusFilter, String searchQuery) {
        List<Membership> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append("AND ms.status = ? ");
            params.add(statusFilter.toUpperCase());
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (ms.membership_code LIKE ? OR m.member_code LIKE ? OR m.full_name LIKE ? OR p.name LIKE ?) ");
            String q = "%" + searchQuery.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append("ORDER BY ms.id DESC");

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
            logger.error("Error finding all memberships", e);
        }
        return list;
    }

    public List<Membership> findExpiringSoon(int withinDays) {
        List<Membership> list = new ArrayList<>();
        String sql = BASE_SELECT +
                     "WHERE ms.status = 'ACTIVE' " +
                     "AND ms.end_date BETWEEN CURRENT_DATE() AND DATE_ADD(CURRENT_DATE(), INTERVAL ? DAY) " +
                     "ORDER BY ms.end_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, withinDays);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding memberships expiring soon", e);
        }
        return list;
    }

    public boolean hasOverlappingActiveMembership(int memberId, LocalDate startDate, LocalDate endDate, Integer excludeMembershipId) {
        String sql = "SELECT COUNT(*) FROM memberships " +
                     "WHERE member_id = ? AND status = 'ACTIVE' " +
                     "AND (? IS NULL OR id != ?) " +
                     "AND (start_date <= ? AND end_date >= ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            if (excludeMembershipId != null) {
                stmt.setInt(2, excludeMembershipId);
                stmt.setInt(3, excludeMembershipId);
            } else {
                stmt.setNull(2, Types.INTEGER);
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setDate(4, Date.valueOf(endDate));
            stmt.setDate(5, Date.valueOf(startDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking overlapping memberships for member id: {}", memberId, e);
        }
        return false;
    }

    public String generateNextCode() {
        String datePrefix = LocalDate.now().toString().replace("-", "").substring(0, 6);
        String sql = "SELECT COUNT(*) FROM memberships WHERE membership_code LIKE ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "MS-" + datePrefix + "-%");
            int count = 1;
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1) + 1;
                }
            }
            return String.format("MS-%s-%03d", datePrefix, count);
        } catch (SQLException e) {
            return "MS-" + datePrefix + "-" + (System.currentTimeMillis() % 1000);
        }
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM memberships" +
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
            logger.error("Error counting memberships by status", e);
        }
        return 0;
    }

    private Membership mapRow(ResultSet rs) throws SQLException {
        Membership ms = new Membership();
        ms.setId(rs.getInt("id"));
        ms.setMembershipCode(rs.getString("membership_code"));
        ms.setMemberId(rs.getInt("member_id"));
        ms.setPlanId(rs.getInt("plan_id"));
        Date sd = rs.getDate("start_date");
        if (sd != null) ms.setStartDate(sd.toLocalDate());
        Date ed = rs.getDate("end_date");
        if (ed != null) ms.setEndDate(ed.toLocalDate());
        ms.setAgreedFee(rs.getBigDecimal("agreed_fee"));
        ms.setStatus(rs.getString("status"));
        int rf = rs.getInt("renewed_from_id");
        if (!rs.wasNull()) {
            ms.setRenewedFromId(rf);
        }
        ms.setNotes(rs.getString("notes"));
        ms.setMemberCode(rs.getString("member_code"));
        ms.setMemberName(rs.getString("member_name"));
        ms.setPlanName(rs.getString("plan_name"));
        ms.setPlanDurationDays(rs.getInt("plan_duration"));

        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) ms.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) ms.setUpdatedAt(ut.toLocalDateTime());
        return ms;
    }
}
