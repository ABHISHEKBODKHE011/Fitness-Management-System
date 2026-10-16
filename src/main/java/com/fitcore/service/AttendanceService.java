package com.fitcore.service;

import com.fitcore.config.AppConfig;
import com.fitcore.dao.AttendanceDao;
import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.dao.MembershipDao;
import com.fitcore.model.*;
import com.fitcore.security.Permission;
import com.fitcore.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Business service managing member check-in verification, duplicate prevention, and daily rosters.
 */
public class AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    private final AttendanceDao attendanceDao;
    private final MemberDao memberDao;
    private final MembershipDao membershipDao;
    private final AuditLogDao auditLogDao;

    public AttendanceService() {
        this(new AttendanceDao(), new MemberDao(), new MembershipDao(), new AuditLogDao());
    }

    public AttendanceService(AttendanceDao attendanceDao, MemberDao memberDao, MembershipDao membershipDao, AuditLogDao auditLogDao) {
        this.attendanceDao = attendanceDao;
        this.memberDao = memberDao;
        this.membershipDao = membershipDao;
        this.auditLogDao = auditLogDao;
    }

    /**
     * Checks in a member for today's session with eligibility and duplicate prevention checks.
     */
    public Attendance checkIn(int memberId, String notes) {
        SecurityContext.requirePermission(Permission.RECORD_ATTENDANCE);

        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member ID " + memberId + " not found.");
        }

        if (!member.isActive()) {
            throw new IllegalStateException("Check-in rejected: Member account is deactivated.");
        }

        // Check if member has an active membership if required by gym policy
        if (AppConfig.isRequireActiveMembershipForAttendance()) {
            Membership activeMs = membershipDao.findActiveMembershipForMember(memberId);
            if (activeMs == null) {
                throw new IllegalStateException("Check-in rejected: Member does not have an active membership plan.");
            }
            if (activeMs.getEndDate().isBefore(LocalDate.now())) {
                throw new IllegalStateException("Check-in rejected: Membership expired on " + activeMs.getEndDate() + ".");
            }
        }

        // Single check-in per day enforcement
        LocalDate today = LocalDate.now();
        if (AppConfig.isSingleCheckInPerDay() && attendanceDao.hasCheckedInToday(memberId, today)) {
            throw new IllegalStateException("Member " + member.getFullName() + " has already checked in today (" + today + ").");
        }

        Attendance attendance = new Attendance();
        attendance.setMemberId(memberId);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInDate(today);
        attendance.setNotes(notes != null ? notes.trim() : "Standard gym check-in");

        boolean saved = attendanceDao.checkIn(attendance);
        if (!saved) {
            throw new RuntimeException("Failed to record attendance check-in.");
        }

        attendance.setMemberCode(member.getMemberCode());
        attendance.setMemberName(member.getFullName());
        attendance.setMemberPhone(member.getPhone());

        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                "CHECK_IN",
                "ATTENDANCE",
                String.valueOf(attendance.getId()),
                "Checked in member: " + member.getFullName() + " (" + member.getMemberCode() + ")"
        ));

        logger.info("Member {} ({}) checked in at {}", member.getFullName(), member.getMemberCode(), attendance.getCheckInTime());
        return attendance;
    }

    public List<Attendance> getTodayAttendance() {
        return attendanceDao.findTodayAttendance();
    }

    public List<Attendance> getAttendanceHistory(LocalDate start, LocalDate end, String search) {
        return attendanceDao.findByDateRange(start, end, search);
    }

    public List<Attendance> getMemberAttendanceHistory(int memberId) {
        return attendanceDao.findByMemberId(memberId);
    }

    public int getTodayAttendanceCount() {
        return attendanceDao.countToday();
    }

    public Map<LocalDate, Integer> getMonthlyAttendanceSummary(int year, int month) {
        return attendanceDao.getMonthlyAttendanceSummary(year, month);
    }
}
