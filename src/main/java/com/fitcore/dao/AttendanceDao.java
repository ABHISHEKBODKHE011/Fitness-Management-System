package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.Attendance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access operations for member attendance check-ins and logs.
 */
public class AttendanceDao {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceDao.class);

    private static final String BASE_SELECT =
            "SELECT a.id, a.member_id, a.check_in_time, a.check_in_date, a.notes, a.created_at, " +
            "m.member_code, m.full_name AS member_name, m.phone AS member_phone, " +
            "COALESCE((SELECT ms.status FROM memberships ms WHERE ms.member_id = m.id AND ms.status = 'ACTIVE' ORDER BY ms.end_date DESC LIMIT 1), 'NONE') AS membership_status " +
            "FROM attendance a " +
            "JOIN members m ON a.member_id = m.id ";

    public boolean checkIn(Attendance attendance) {
        String sql = "INSERT INTO attendance (member_id, check_in_time, check_in_date, notes) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attendance.getMemberId());
            stmt.setTimestamp(2, Timestamp.valueOf(attendance.getCheckInTime()));
            stmt.setDate(3, Date.valueOf(attendance.getCheckInDate()));
            stmt.setString(4, attendance.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        attendance.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warn("Member {} already checked in on {}: {}", attendance.getMemberId(), attendance.getCheckInDate(), e.getMessage());
            throw new IllegalStateException("Member has already checked in today (" + attendance.getCheckInDate() + ").");
        } catch (SQLException e) {
            logger.error("Error executing attendance check-in for member id: {}", attendance.getMemberId(), e);
        }
        return false;
    }

    public boolean hasCheckedInToday(int memberId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM attendance WHERE member_id = ? AND check_in_date = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setDate(2, Date.valueOf(date != null ? date : LocalDate.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking today's attendance for member id: {}", memberId, e);
        }
        return false;
    }

    public List<Attendance> findTodayAttendance() {
        return findByDate(LocalDate.now());
    }

    public List<Attendance> findByDate(LocalDate date) {
        List<Attendance> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE a.check_in_date = ? ORDER BY a.check_in_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding attendance for date: {}", date, e);
        }
        return list;
    }

    public List<Attendance> findByDateRange(LocalDate start, LocalDate end, String search) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (start != null) {
            sql.append("AND a.check_in_date >= ? ");
            params.add(Date.valueOf(start));
        }
        if (end != null) {
            sql.append("AND a.check_in_date <= ? ");
            params.add(Date.valueOf(end));
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (m.member_code LIKE ? OR m.full_name LIKE ? OR m.phone LIKE ?) ");
            String q = "%" + search.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        sql.append("ORDER BY a.check_in_time DESC");

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
            logger.error("Error querying attendance by date range", e);
        }
        return list;
    }

    public List<Attendance> findByMemberId(int memberId) {
        List<Attendance> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE a.member_id = ? ORDER BY a.check_in_time DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding attendance history for member id: {}", memberId, e);
        }
        return list;
    }

    public int countToday() {
        String sql = "SELECT COUNT(*) FROM attendance WHERE check_in_date = CURRENT_DATE()";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting today's attendance", e);
        }
        return 0;
    }

    public Map<LocalDate, Integer> getMonthlyAttendanceSummary(int year, int month) {
        Map<LocalDate, Integer> map = new HashMap<>();
        String sql = "SELECT check_in_date, COUNT(*) AS cnt FROM attendance " +
                     "WHERE YEAR(check_in_date) = ? AND MONTH(check_in_date) = ? " +
                     "GROUP BY check_in_date ORDER BY check_in_date ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("check_in_date").toLocalDate(), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error generating monthly attendance summary", e);
        }
        return map;
    }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setMemberId(rs.getInt("member_id"));
        Timestamp cit = rs.getTimestamp("check_in_time");
        if (cit != null) a.setCheckInTime(cit.toLocalDateTime());
        Date cid = rs.getDate("check_in_date");
        if (cid != null) a.setCheckInDate(cid.toLocalDate());
        a.setNotes(rs.getString("notes"));
        a.setMemberCode(rs.getString("member_code"));
        a.setMemberName(rs.getString("member_name"));
        a.setMemberPhone(rs.getString("member_phone"));
        a.setMembershipStatus(rs.getString("membership_status"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) a.setCreatedAt(ct.toLocalDateTime());
        return a;
    }
}
