package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.dao.TrainerDao;
import com.fitcore.model.AuditLog;
import com.fitcore.model.Member;
import com.fitcore.model.Trainer;
import com.fitcore.model.User;
import com.fitcore.security.Permission;
import com.fitcore.security.SecurityContext;
import com.fitcore.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Business service managing trainer profiles, specializations, and assignments.
 */
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);
    private final TrainerDao trainerDao;
    private final MemberDao memberDao;
    private final AuditLogDao auditLogDao;

    public TrainerService() {
        this(new TrainerDao(), new MemberDao(), new AuditLogDao());
    }

    public TrainerService(TrainerDao trainerDao, MemberDao memberDao, AuditLogDao auditLogDao) {
        this.trainerDao = trainerDao;
        this.memberDao = memberDao;
        this.auditLogDao = auditLogDao;
    }

    public Trainer registerTrainer(Trainer trainer) {
        SecurityContext.requirePermission(Permission.MANAGE_TRAINERS);
        validateTrainer(trainer, null);

        if (trainer.getTrainerCode() == null || trainer.getTrainerCode().trim().isEmpty()) {
            trainer.setTrainerCode(trainerDao.generateNextCode());
        }
        if (trainer.getStatus() == null) {
            trainer.setStatus("ACTIVE");
        }

        boolean created = trainerDao.create(trainer);
        if (!created) {
            throw new RuntimeException("Failed to register trainer in the database.");
        }

        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                "CREATE_TRAINER",
                "TRAINER",
                String.valueOf(trainer.getId()),
                "Registered certified trainer: " + trainer.getName() + " (" + trainer.getSpecialization() + ")"
        ));

        logger.info("Trainer registered: {} ({})", trainer.getName(), trainer.getTrainerCode());
        return trainer;
    }

    public boolean updateTrainer(Trainer trainer) {
        SecurityContext.requirePermission(Permission.MANAGE_TRAINERS);
        if (trainer.getId() == null) {
            throw new IllegalArgumentException("Trainer ID is required for update.");
        }
        validateTrainer(trainer, trainer.getId());

        boolean updated = trainerDao.update(trainer);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "UPDATE_TRAINER",
                    "TRAINER",
                    String.valueOf(trainer.getId()),
                    "Updated trainer profile for: " + trainer.getName()
            ));
        }
        return updated;
    }

    public boolean deactivateTrainer(int trainerId) {
        SecurityContext.requirePermission(Permission.MANAGE_TRAINERS);
        Trainer trainer = trainerDao.findById(trainerId);
        if (trainer == null) {
            throw new IllegalArgumentException("Trainer not found.");
        }

        trainer.setStatus("INACTIVE");
        boolean updated = trainerDao.update(trainer);
        if (updated) {
            User current = SecurityContext.getCurrentUser();
            auditLogDao.log(new AuditLog(
                    null,
                    current != null ? current.getId() : null,
                    current != null ? current.getUsername() : "SYSTEM",
                    "DEACTIVATE_TRAINER",
                    "TRAINER",
                    String.valueOf(trainer.getId()),
                    "Deactivated trainer: " + trainer.getName()
            ));
        }
        return updated;
    }

    public Trainer getTrainerById(int id) {
        return trainerDao.findById(id);
    }

    public List<Trainer> searchTrainers(String status, String query) {
        return trainerDao.findAll(status, query);
    }

    public List<Member> getAssignedMembers(int trainerId) {
        return memberDao.findAll("ACTIVE", null, trainerId);
    }

    private void validateTrainer(Trainer trainer, Integer existingId) {
        ValidationUtil.requireNonEmpty(trainer.getName(), "Trainer Name");
        ValidationUtil.requireNonEmpty(trainer.getPhone(), "Phone Number");
        ValidationUtil.requireNonEmpty(trainer.getEmail(), "Email Address");
        ValidationUtil.requireNonEmpty(trainer.getSpecialization(), "Specialization");

        if (!ValidationUtil.isValidEmail(trainer.getEmail())) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (!ValidationUtil.isValidPhone(trainer.getPhone())) {
            throw new IllegalArgumentException("Please provide a valid phone number.");
        }

        if (trainerDao.existsByEmail(trainer.getEmail(), existingId)) {
            throw new IllegalArgumentException("A trainer with email '" + trainer.getEmail() + "' already exists.");
        }
    }
}
