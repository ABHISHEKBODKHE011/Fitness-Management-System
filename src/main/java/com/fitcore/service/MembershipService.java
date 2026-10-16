package com.fitcore.service;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.dao.MembershipDao;
import com.fitcore.dao.MembershipPlanDao;
import com.fitcore.model.*;
import com.fitcore.security.Permission;
import com.fitcore.security.SecurityContext;
import com.fitcore.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business service managing membership assignments, inclusive end-date validity, renewals, and cancellations.
 */
public class MembershipService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipService.class);
    private final MembershipDao membershipDao;
    private final MembershipPlanDao planDao;
    private final MemberDao memberDao;
    private final AuditLogDao auditLogDao;

    public MembershipService() {
        this(new MembershipDao(), new MembershipPlanDao(), new MemberDao(), new AuditLogDao());
    }

    public MembershipService(MembershipDao membershipDao, MembershipPlanDao planDao, MemberDao memberDao, AuditLogDao auditLogDao) {
        this.membershipDao = membershipDao;
        this.planDao = planDao;
        this.memberDao = memberDao;
        this.auditLogDao = auditLogDao;
    }

    /**
     * Assigns a new membership plan to a member with explicit transaction boundary.
     */
    public Membership assignMembership(int memberId, int planId, LocalDate startDate, BigDecimal agreedFee, String notes) {
        SecurityContext.requirePermission(Permission.ASSIGN_MEMBERSHIP);

        Member member = memberDao.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Member ID " + memberId + " not found.");
        }
        if (!member.isActive()) {
            throw new IllegalStateException("Cannot assign membership to a deactivated member.");
        }

        MembershipPlan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Membership Plan ID " + planId + " not found.");
        }
        if (!plan.isActive()) {
            throw new IllegalStateException("Cannot assign an inactive membership plan.");
        }

        if (startDate == null) {
            startDate = LocalDate.now();
        }

        // Calculate end date consistently using inclusive rule: start + duration - 1
        LocalDate endDate = DateTimeUtil.calculateEndDate(startDate, plan.getDurationDays());

        if (agreedFee == null || agreedFee.compareTo(BigDecimal.ZERO) < 0) {
            agreedFee = plan.getFee();
        }

        // Check for overlapping active memberships
        if (membershipDao.hasOverlappingActiveMembership(memberId, startDate, endDate, null)) {
            throw new IllegalStateException("Member already has an active membership overlapping with period " +
                    startDate + " to " + endDate + ".");
        }

        Membership membership = new Membership();
        membership.setMembershipCode(membershipDao.generateNextCode());
        membership.setMemberId(memberId);
        membership.setPlanId(planId);
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);
        membership.setAgreedFee(agreedFee);
        membership.setStatus("ACTIVE");
        membership.setNotes(notes);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean created = membershipDao.create(membership, conn);
                if (!created) {
                    throw new SQLException("Failed to persist membership record.");
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                logger.error("Transaction rolled back during membership assignment: {}", e.getMessage());
                throw new RuntimeException("Membership assignment failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during membership assignment: " + e.getMessage(), e);
        }

        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                "ASSIGN_MEMBERSHIP",
                "MEMBERSHIP",
                String.valueOf(membership.getId()),
                "Assigned plan '" + plan.getName() + "' to " + member.getFullName() +
                        " (" + startDate + " to " + endDate + ", $" + agreedFee + ")"
        ));

        logger.info("Assigned membership {} to member {}", membership.getMembershipCode(), member.getMemberCode());
        return membership;
    }

    /**
     * Renews a member's membership, linking back to the previous membership without overwriting historical records.
     */
    public Membership renewMembership(int previousMembershipId, int planId, LocalDate preferredStartDate, BigDecimal agreedFee, String notes) {
        SecurityContext.requirePermission(Permission.RENEW_MEMBERSHIP);

        Membership previous = membershipDao.findById(previousMembershipId);
        if (previous == null) {
            throw new IllegalArgumentException("Previous membership ID " + previousMembershipId + " not found.");
        }

        MembershipPlan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Membership Plan ID " + planId + " not found.");
        }

        // Renewal start date logic:
        // If preferredStartDate is provided, use it. Otherwise:
        // If previous is still active and ends in future/today, renewal starts on previous.endDate + 1 day
        // If previous is already expired, renewal starts today.
        LocalDate startDate;
        if (preferredStartDate != null) {
            startDate = preferredStartDate;
        } else {
            LocalDate today = LocalDate.now();
            if (previous.getEndDate() != null && !previous.getEndDate().isBefore(today)) {
                startDate = previous.getEndDate().plusDays(1);
            } else {
                startDate = today;
            }
        }

        LocalDate endDate = DateTimeUtil.calculateEndDate(startDate, plan.getDurationDays());

        if (agreedFee == null || agreedFee.compareTo(BigDecimal.ZERO) < 0) {
            agreedFee = plan.getFee();
        }

        Membership renewal = new Membership();
        renewal.setMembershipCode(membershipDao.generateNextCode());
        renewal.setMemberId(previous.getMemberId());
        renewal.setPlanId(planId);
        renewal.setStartDate(startDate);
        renewal.setEndDate(endDate);
        renewal.setAgreedFee(agreedFee);
        renewal.setStatus("ACTIVE");
        renewal.setRenewedFromId(previous.getId());
        renewal.setNotes(notes != null ? notes : "Renewed from " + previous.getMembershipCode());

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean created = membershipDao.create(renewal, conn);
                if (!created) {
                    throw new SQLException("Failed to persist renewal record.");
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                logger.error("Transaction rolled back during membership renewal: {}", e.getMessage());
                throw new RuntimeException("Membership renewal failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during renewal: " + e.getMessage(), e);
        }

        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                "RENEW_MEMBERSHIP",
                "MEMBERSHIP",
                String.valueOf(renewal.getId()),
                "Renewed membership from " + previous.getMembershipCode() +
                        " with plan '" + plan.getName() + "' (" + startDate + " to " + endDate + ")"
        ));

        logger.info("Renewed membership {} for member ID {}", renewal.getMembershipCode(), previous.getMemberId());
        return renewal;
    }

    public boolean cancelMembership(int membershipId, String reason) {
        SecurityContext.requirePermission(Permission.ASSIGN_MEMBERSHIP);
        Membership ms = membershipDao.findById(membershipId);
        if (ms == null) {
            throw new IllegalArgumentException("Membership not found.");
        }

        ms.setStatus("CANCELLED");
        ms.setNotes((ms.getNotes() != null ? ms.getNotes() + " | " : "") + "Cancelled: " + reason);
        boolean updated = membershipDao.update(ms);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "CANCEL_MEMBERSHIP",
                    "MEMBERSHIP",
                    String.valueOf(ms.getId()),
                    "Cancelled membership " + ms.getMembershipCode() + ". Reason: " + reason
            ));
        }
        return updated;
    }

    public List<Membership> getMembershipsForMember(int memberId) {
        return membershipDao.findByMemberId(memberId);
    }

    public Membership getActiveMembershipForMember(int memberId) {
        return membershipDao.findActiveMembershipForMember(memberId);
    }

    public List<Membership> searchMemberships(String status, String query) {
        return membershipDao.findAll(status, query);
    }

    public List<Membership> getExpiringSoon(int withinDays) {
        return membershipDao.findExpiringSoon(withinDays);
    }
}
