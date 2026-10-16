package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.dao.MembershipDao;
import com.fitcore.dao.MembershipPlanDao;
import com.fitcore.model.*;
import com.fitcore.security.SecurityContext;
import com.fitcore.util.DateTimeUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Membership Lifecycle & Renewal Business Logic Tests")
class MembershipServiceTest {

    @Mock
    private MembershipDao membershipDao;

    @Mock
    private MembershipPlanDao planDao;

    @Mock
    private MemberDao memberDao;

    @Mock
    private AuditLogDao auditLogDao;

    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(membershipDao, planDao, memberDao, auditLogDao);
        User admin = new User(1, "admin", "hash", "Admin", "admin@example.com", Role.ADMIN, "ACTIVE");
        SecurityContext.setCurrentUser(admin);
    }

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    @DisplayName("Should assign membership with inclusive end-date math and fee lock-in")
    void testAssignMembershipCalculations() throws Exception {
        Member member = new Member(1, "MBR-1001", "Johnathan Davis", "+1-555-1101", "jdavis@example.com", "Male", null, LocalDate.now(), null, "ACTIVE");
        MembershipPlan plan = new MembershipPlan(1, "PLN-M01", "Monthly Basic", 30, new BigDecimal("49.99"), "Standard access", "ACTIVE");

        when(memberDao.findById(1)).thenReturn(member);
        when(planDao.findById(1)).thenReturn(plan);
        when(membershipDao.hasOverlappingActiveMembership(eq(1), any(), any(), isNull())).thenReturn(false);
        when(membershipDao.generateNextCode()).thenReturn("MS-202610-001");
        when(membershipDao.create(any(Membership.class), any(Connection.class))).thenReturn(true);

        LocalDate start = LocalDate.of(2026, 10, 1);
        Membership assigned = membershipService.assignMembership(1, 1, start, new BigDecimal("49.99"), "Test assignment");

        assertNotNull(assigned);
        assertEquals(LocalDate.of(2026, 10, 1), assigned.getStartDate());
        // 30 days inclusive: Oct 1 + 29 = Oct 30
        assertEquals(LocalDate.of(2026, 10, 30), assigned.getEndDate());
        assertEquals(new BigDecimal("49.99"), assigned.getAgreedFee());
        assertEquals("ACTIVE", assigned.getStatus());
    }

    @Test
    @DisplayName("Should reject membership assignment for deactivated members")
    void testDeactivatedMemberRejected() {
        Member inactiveMember = new Member(8, "MBR-1008", "Mia Robinson", "+1-555-1108", "mrobinson@example.com", "Female", null, LocalDate.now(), null, "INACTIVE");
        when(memberDao.findById(8)).thenReturn(inactiveMember);

        assertThrows(IllegalStateException.class, () ->
                membershipService.assignMembership(8, 1, LocalDate.now(), new BigDecimal("49.99"), null));
    }

    @Test
    @DisplayName("Should reject overlapping active memberships")
    void testOverlappingActiveMembershipsRejected() {
        Member member = new Member(1, "MBR-1001", "Johnathan Davis", "+1-555-1101", "jdavis@example.com", "Male", null, LocalDate.now(), null, "ACTIVE");
        MembershipPlan plan = new MembershipPlan(1, "PLN-M01", "Monthly Basic", 30, new BigDecimal("49.99"), "Standard access", "ACTIVE");

        when(memberDao.findById(1)).thenReturn(member);
        when(planDao.findById(1)).thenReturn(plan);
        when(membershipDao.hasOverlappingActiveMembership(eq(1), any(), any(), isNull())).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                membershipService.assignMembership(1, 1, LocalDate.of(2026, 10, 15), null, null));
    }

    @Test
    @DisplayName("Should renew membership linking to previous without overwriting history")
    void testRenewMembershipPreservesHistory() throws Exception {
        Membership previous = new Membership(
                6, "MS-202607-006", 6, 1,
                LocalDate.of(2026, 7, 15), LocalDate.of(2026, 8, 13),
                new BigDecimal("49.99"), "EXPIRED"
        );
        MembershipPlan plan = new MembershipPlan(2, "PLN-Q03", "Quarterly Standard", 90, new BigDecimal("129.99"), "Standard access", "ACTIVE");

        when(membershipDao.findById(6)).thenReturn(previous);
        when(planDao.findById(2)).thenReturn(plan);
        when(membershipDao.generateNextCode()).thenReturn("MS-202608-007");
        when(membershipDao.create(any(Membership.class), any(Connection.class))).thenReturn(true);

        LocalDate renewalStart = LocalDate.of(2026, 8, 14);
        Membership renewal = membershipService.renewMembership(6, 2, renewalStart, new BigDecimal("129.99"), "Renewal upgrade");

        assertNotNull(renewal);
        assertEquals(Integer.valueOf(6), renewal.getRenewedFromId()); // Linked to previous
        assertEquals(LocalDate.of(2026, 8, 14), renewal.getStartDate());
        assertEquals(DateTimeUtil.calculateEndDate(renewalStart, 90), renewal.getEndDate());
        assertEquals(new BigDecimal("129.99"), renewal.getAgreedFee());
    }
}
