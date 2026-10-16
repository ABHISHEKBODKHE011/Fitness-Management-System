package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.model.AuditLog;
import com.fitcore.model.User;
import com.fitcore.security.SecurityContext;

import java.util.List;

/**
 * Audit log recording and compliance inspection service.
 */
public class AuditService {

    private final AuditLogDao auditLogDao;

    public AuditService() {
        this(new AuditLogDao());
    }

    public AuditService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    public void log(String action, String entityType, String entityId, String details) {
        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                action,
                entityType,
                entityId,
                details
        ));
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDao.findAll(limit);
    }
}
