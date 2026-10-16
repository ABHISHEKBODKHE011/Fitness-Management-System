package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.model.Member;
import com.fitcore.model.Role;
import com.fitcore.model.User;
import com.fitcore.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Member Service Business Logic Tests")
class MemberServiceTest {

    @Mock
    private MemberDao memberDao;

    @Mock
    private AuditLogDao auditLogDao;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberDao, auditLogDao);
        User admin = new User(1, "admin", "hash", "Admin User", "admin@example.com", Role.ADMIN, "ACTIVE");
        SecurityContext.setCurrentUser(admin);
    }

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    @DisplayName("Should successfully register a new member with valid details")
    void testRegisterMemberSuccess() {
        Member member = new Member(
                null, null, "Marcus Aurelius", "+1-555-8888",
                "marcus@example.com", "Male", "Faustina (+1-555-9999)",
                LocalDate.now(), null, "ACTIVE"
        );

        when(memberDao.existsByEmail("marcus@example.com", null)).thenReturn(false);
        when(memberDao.existsByPhone("+1-555-8888", null)).thenReturn(false);
        when(memberDao.generateNextCode()).thenReturn("MBR-1009");
        when(memberDao.create(any(Member.class))).thenAnswer(invocation -> {
            Member m = invocation.getArgument(0);
            m.setId(9);
            return true;
        });

        Member created = memberService.registerMember(member);

        assertNotNull(created);
        assertEquals(9, created.getId());
        assertEquals("MBR-1009", created.getMemberCode());
        verify(memberDao).create(member);
        verify(auditLogDao).log(any());
    }

    @Test
    @DisplayName("Should reject registration when email is a duplicate")
    void testDuplicateEmailRejected() {
        Member member = new Member(
                null, null, "John Smith", "+1-555-7777",
                "existing@example.com", "Male", null,
                LocalDate.now(), null, "ACTIVE"
        );

        when(memberDao.existsByEmail("existing@example.com", null)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> memberService.registerMember(member));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(memberDao, never()).create(any());
    }

    @Test
    @DisplayName("Should reject registration when phone number is a duplicate")
    void testDuplicatePhoneRejected() {
        Member member = new Member(
                null, null, "Jane Smith", "+1-555-0000",
                "jane@example.com", "Female", null,
                LocalDate.now(), null, "ACTIVE"
        );

        when(memberDao.existsByEmail("jane@example.com", null)).thenReturn(false);
        when(memberDao.existsByPhone("+1-555-0000", null)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> memberService.registerMember(member));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(memberDao, never()).create(any());
    }

    @Test
    @DisplayName("Should reject registration with invalid email or phone formats")
    void testInvalidFormatsRejected() {
        Member badEmail = new Member(
                null, null, "Test User", "+1-555-1111",
                "invalid-email-address", "Male", null,
                LocalDate.now(), null, "ACTIVE"
        );
        assertThrows(IllegalArgumentException.class, () -> memberService.registerMember(badEmail));

        Member badPhone = new Member(
                null, null, "Test User", "not-a-number",
                "test@example.com", "Male", null,
                LocalDate.now(), null, "ACTIVE"
        );
        assertThrows(IllegalArgumentException.class, () -> memberService.registerMember(badPhone));
    }

    @Test
    @DisplayName("Should soft-deactivate member preserving record")
    void testSoftDeactivateMember() {
        Member existing = new Member(5, "MBR-1005", "Ethan Bennett", "+1-555-1105", "ebennett@example.com", "Male", null, LocalDate.now(), null, "ACTIVE");
        when(memberDao.findById(5)).thenReturn(existing);
        when(memberDao.update(existing)).thenReturn(true);

        boolean deactivated = memberService.deactivateMember(5);
        assertTrue(deactivated);
        assertEquals("INACTIVE", existing.getStatus());
        verify(memberDao).update(existing);
    }
}
