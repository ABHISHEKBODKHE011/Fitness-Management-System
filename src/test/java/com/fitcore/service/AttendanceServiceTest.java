package com.fitcore.service;

import com.fitcore.dao.AttendanceDao;
import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.dao.MembershipDao;
import com.fitcore.model.*;
import com.fitcore.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Attendance & Check-in Verification Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceDao attendanceDao;

    @Mock
    private MemberDao memberDao;

    @Mock
    private MembershipDao membershipDao;

    @Mock
    private AuditLogDao auditLogDao;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(attendanceDao, memberDao, membershipDao, auditLogDao);
        User receptionist = new User(2, "reception", "hash", "Receptionist", "reception@example.com", Role.RECEPTIONIST, "ACTIVE");
        SecurityContext.setCurrentUser(receptionist);
    }

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    @DisplayName("Should successfully record check-in for active member with active membership")
    void testCheckInSuccess() {
        Member member = new Member(1, "MBR-1001", "Johnathan Davis", "+1-555-1101", "jdavis@example.com", "Male", null, LocalDate.now(), null, "ACTIVE");
        Membership activeMs = new Membership(1, "MS-202601-001", 1, 4, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(5), new BigDecimal("429.99"), "ACTIVE");

        when(memberDao.findById(1)).thenReturn(member);
        when(membershipDao.findActiveMembershipForMember(1)).thenReturn(activeMs);
        when(attendanceDao.hasCheckedInToday(eq(1), any(LocalDate.class))).thenReturn(false);
        when(attendanceDao.checkIn(any(Attendance.class))).thenReturn(true);

        Attendance a = attendanceService.checkIn(1, "Morning workout");

        assertNotNull(a);
        assertEquals(LocalDate.now(), a.getCheckInDate());
        verify(attendanceDao).checkIn(any(Attendance.class));
        verify(auditLogDao).log(any());
    }

    @Test
    @DisplayName("Should reject check-in when member has already checked in today")
    void testDuplicateCheckInRejected() {
        Member member = new Member(1, "MBR-1001", "Johnathan Davis", "+1-555-1101", "jdavis@example.com", "Male", null, LocalDate.now(), null, "ACTIVE");
        Membership activeMs = new Membership(1, "MS-202601-001", 1, 4, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(5), new BigDecimal("429.99"), "ACTIVE");

        when(memberDao.findById(1)).thenReturn(member);
        when(membershipDao.findActiveMembershipForMember(1)).thenReturn(activeMs);
        when(attendanceDao.hasCheckedInToday(eq(1), any(LocalDate.class))).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> attendanceService.checkIn(1, null));
        assertTrue(ex.getMessage().contains("already checked in today"));
        verify(attendanceDao, never()).checkIn(any());
    }

    @Test
    @DisplayName("Should reject check-in when member account is deactivated")
    void testDeactivatedMemberCheckInRejected() {
        Member inactive = new Member(8, "MBR-1008", "Mia Robinson", "+1-555-1108", "mrobinson@example.com", "Female", null, LocalDate.now(), null, "INACTIVE");
        when(memberDao.findById(8)).thenReturn(inactive);

        assertThrows(IllegalStateException.class, () -> attendanceService.checkIn(8, null));
        verify(attendanceDao, never()).checkIn(any());
    }

    @Test
    @DisplayName("Should reject check-in when member has no active membership plan")
    void testNoActiveMembershipCheckInRejected() {
        Member member = new Member(7, "MBR-1007", "Noah Campbell", "+1-555-1107", "ncampbell@example.com", "Male", null, LocalDate.now(), null, "ACTIVE");
        when(memberDao.findById(7)).thenReturn(member);
        when(membershipDao.findActiveMembershipForMember(7)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> attendanceService.checkIn(7, null));
        verify(attendanceDao, never()).checkIn(any());
    }
}
