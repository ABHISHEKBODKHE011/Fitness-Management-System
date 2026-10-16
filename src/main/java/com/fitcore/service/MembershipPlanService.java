package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MembershipPlanDao;
import com.fitcore.model.AuditLog;
import com.fitcore.model.MembershipPlan;
import com.fitcore.model.User;
import com.fitcore.security.Permission;
import com.fitcore.security.SecurityContext;
import com.fitcore.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business service managing membership catalog plans and pricing rules.
 */
public class MembershipPlanService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipPlanService.class);
    private final MembershipPlanDao planDao;
    private final AuditLogDao auditLogDao;

    public MembershipPlanService() {
        this(new MembershipPlanDao(), new AuditLogDao());
    }

    public MembershipPlanService(MembershipPlanDao planDao, AuditLogDao auditLogDao) {
        this.planDao = planDao;
        this.auditLogDao = auditLogDao;
    }

    public MembershipPlan createPlan(MembershipPlan plan) {
        SecurityContext.requirePermission(Permission.MANAGE_PLANS);
        validatePlan(plan);

        if (plan.getPlanCode() == null || plan.getPlanCode().trim().isEmpty()) {
            plan.setPlanCode(planDao.generateNextCode(plan.getDurationDays()));
        }
        if (plan.getStatus() == null) {
            plan.setStatus("ACTIVE");
        }

        boolean created = planDao.create(plan);
        if (!created) {
            throw new RuntimeException("Failed to create membership plan.");
        }

        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                "CREATE_PLAN",
                "PLAN",
                String.valueOf(plan.getId()),
                "Created plan: " + plan.getName() + " (" + plan.getDurationDays() + " days, $" + plan.getFee() + ")"
        ));

        logger.info("Membership plan created: {} ({})", plan.getName(), plan.getPlanCode());
        return plan;
    }

    public boolean updatePlan(MembershipPlan plan) {
        SecurityContext.requirePermission(Permission.MANAGE_PLANS);
        if (plan.getId() == null) {
            throw new IllegalArgumentException("Plan ID is required for update.");
        }
        validatePlan(plan);

        boolean updated = planDao.update(plan);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "UPDATE_PLAN",
                    "PLAN",
                    String.valueOf(plan.getId()),
                    "Updated plan details for: " + plan.getName()
            ));
        }
        return updated;
    }

    public boolean deactivatePlan(int planId) {
        SecurityContext.requirePermission(Permission.MANAGE_PLANS);
        MembershipPlan plan = planDao.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan with ID " + planId + " not found.");
        }

        plan.setStatus("INACTIVE");
        boolean updated = planDao.update(plan);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "DEACTIVATE_PLAN",
                    "PLAN",
                    String.valueOf(plan.getId()),
                    "Deactivated membership plan: " + plan.getName()
            ));
        }
        return updated;
    }

    public MembershipPlan getPlanById(int id) {
        return planDao.findById(id);
    }

    public List<MembershipPlan> getAllPlans(boolean activeOnly, String search) {
        return planDao.findAll(activeOnly, search);
    }

    private void validatePlan(MembershipPlan plan) {
        ValidationUtil.requireNonEmpty(plan.getName(), "Plan Name");

        if (plan.getDurationDays() <= 0) {
            throw new IllegalArgumentException("Plan duration must be at least 1 day.");
        }

        if (plan.getFee() == null || plan.getFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Plan fee cannot be negative.");
        }
    }
}
