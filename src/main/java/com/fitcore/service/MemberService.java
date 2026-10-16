package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.model.AuditLog;
import com.fitcore.model.Member;
import com.fitcore.model.User;
import com.fitcore.security.Permission;
import com.fitcore.security.SecurityContext;
import com.fitcore.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Business service managing member lifecycle, validations, and trainer linkages.
 */
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final MemberDao memberDao;
    private final AuditLogDao auditLogDao;

    public MemberService() {
        this(new MemberDao(), new AuditLogDao());
    }

    public MemberService(MemberDao memberDao, AuditLogDao auditLogDao) {
        this.memberDao = memberDao;
        this.auditLogDao = auditLogDao;
    }

    public Member registerMember(Member member) {
        SecurityContext.requirePermission(Permission.MANAGE_MEMBERS);
        validateMember(member, null);

        if (member.getMemberCode() == null || member.getMemberCode().trim().isEmpty()) {
            member.setMemberCode(memberDao.generateNextCode());
        }
        if (member.getStatus() == null) {
            member.setStatus("ACTIVE");
        }

        boolean created = memberDao.create(member);
        if (!created) {
            throw new RuntimeException("Failed to register member in the database.");
        }

        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                "CREATE_MEMBER",
                "MEMBER",
                String.valueOf(member.getId()),
                "Registered new member: " + member.getFullName() + " (" + member.getMemberCode() + ")"
        ));

        logger.info("Member registered successfully: {} ({})", member.getFullName(), member.getMemberCode());
        return member;
    }

    public boolean updateMember(Member member) {
        SecurityContext.requirePermission(Permission.MANAGE_MEMBERS);
        if (member.getId() == null) {
            throw new IllegalArgumentException("Member ID is required for update.");
        }
        validateMember(member, member.getId());

        boolean updated = memberDao.update(member);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "UPDATE_MEMBER",
                    "MEMBER",
                    String.valueOf(member.getId()),
                    "Updated member details for: " + member.getFullName() + " (" + member.getMemberCode() + ")"
            ));
        }
        return updated;
    }

    public boolean deactivateMember(int memberId) {
        SecurityContext.requirePermission(Permission.MANAGE_MEMBERS);
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member with ID " + memberId + " not found.");
        }

        member.setStatus("INACTIVE");
        boolean updated = memberDao.update(member);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "DEACTIVATE_MEMBER",
                    "MEMBER",
                    String.valueOf(member.getId()),
                    "Soft-deactivated member: " + member.getFullName() + " (" + member.getMemberCode() + ")"
            ));
        }
        return updated;
    }

    public boolean activateMember(int memberId) {
        SecurityContext.requirePermission(Permission.MANAGE_MEMBERS);
        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member with ID " + memberId + " not found.");
        }

        member.setStatus("ACTIVE");
        boolean updated = memberDao.update(member);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "ACTIVATE_MEMBER",
                    "MEMBER",
                    String.valueOf(member.getId()),
                    "Re-activated member: " + member.getFullName() + " (" + member.getMemberCode() + ")"
            ));
        }
        return updated;
    }

    public Member getMemberById(int id) {
        return memberDao.findById(id);
    }

    public Member getMemberByCode(String code) {
        return memberDao.findByCode(code);
    }

    public List<Member> searchMembers(String statusFilter, String searchQuery, Integer trainerId) {
        return memberDao.findAll(statusFilter, searchQuery, trainerId);
    }

    private void validateMember(Member member, Integer existingId) {
        ValidationUtil.requireNonEmpty(member.getFullName(), "Full Name");
        ValidationUtil.requireNonEmpty(member.getPhone(), "Phone Number");
        ValidationUtil.requireNonEmpty(member.getEmail(), "Email Address");
        ValidationUtil.requireNonEmpty(member.getGender(), "Gender");

        if (!ValidationUtil.isValidEmail(member.getEmail())) {
            throw new IllegalArgumentException("Please provide a valid email address (e.g., user@example.com).");
        }
        if (!ValidationUtil.isValidPhone(member.getPhone())) {
            throw new IllegalArgumentException("Please provide a valid phone number (digits and optional +/hyphens).");
        }
        if (member.getJoiningDate() == null) {
            throw new IllegalArgumentException("Joining date is required.");
        }

        if (memberDao.existsByEmail(member.getEmail(), existingId)) {
            throw new IllegalArgumentException("A member with email '" + member.getEmail() + "' already exists.");
        }
        if (memberDao.existsByPhone(member.getPhone(), existingId)) {
            throw new IllegalArgumentException("A member with phone '" + member.getPhone() + "' already exists.");
        }
    }
}
