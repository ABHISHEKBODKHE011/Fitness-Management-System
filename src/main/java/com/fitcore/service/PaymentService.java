package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.dao.PaymentDao;
import com.fitcore.model.*;
import com.fitcore.security.Permission;
import com.fitcore.security.SecurityContext;
import com.fitcore.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Business service managing payment ledger records, revenue tracking, and receipt generation.
 * Note: FitCore Pro records confirmed payment transactions. It does not store card numbers, CVVs,
 * or banking credentials.
 */
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentDao paymentDao;
    private final MemberDao memberDao;
    private final AuditLogDao auditLogDao;

    public PaymentService() {
        this(new PaymentDao(), new MemberDao(), new AuditLogDao());
    }

    public PaymentService(PaymentDao paymentDao, MemberDao memberDao, AuditLogDao auditLogDao) {
        this.paymentDao = paymentDao;
        this.memberDao = memberDao;
        this.auditLogDao = auditLogDao;
    }

    /**
     * Records a new payment ledger entry with validations and duplicate reference prevention.
     */
    public Payment recordPayment(Payment payment) {
        SecurityContext.requirePermission(Permission.MANAGE_PAYMENTS);

        if (payment.getMemberId() == null) {
            throw new IllegalArgumentException("Member ID is required.");
        }
        Member member = memberDao.findById(payment.getMemberId());
        if (member == null) {
            throw new IllegalArgumentException("Member ID " + payment.getMemberId() + " not found.");
        }

        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be strictly greater than 0.");
        }

        if (!ValidationUtil.isNotEmpty(payment.getPaymentMethod())) {
            payment.setPaymentMethod("CASH");
        }
        if (!ValidationUtil.isNotEmpty(payment.getPaymentStatus())) {
            payment.setPaymentStatus("SUCCESS");
        }

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        if (payment.getPaymentCode() == null || payment.getPaymentCode().trim().isEmpty()) {
            payment.setPaymentCode(paymentDao.generateNextCode());
        }

        // Validate transaction reference uniqueness if provided
        if (payment.getTransactionReference() != null && !payment.getTransactionReference().trim().isEmpty()) {
            String ref = payment.getTransactionReference().trim();
            payment.setTransactionReference(ref);
            if (paymentDao.existsByTransactionReference(ref, null)) {
                throw new IllegalArgumentException("A transaction with reference '" + ref + "' already exists.");
            }
        }

        boolean created = paymentDao.create(payment);
        if (!created) {
            throw new RuntimeException("Failed to record payment in the database.");
        }

        payment.setMemberCode(member.getMemberCode());
        payment.setMemberName(member.getFullName());

        User current = SecurityContext.getCurrentUser();
        auditLogDao.log(new AuditLog(
                null,
                current != null ? current.getId() : null,
                current != null ? current.getUsername() : "SYSTEM",
                "RECORD_PAYMENT",
                "PAYMENT",
                String.valueOf(payment.getId()),
                "Recorded payment " + payment.getPaymentCode() + " ($" + payment.getAmount() +
                        ", " + payment.getPaymentMethod() + ") for member: " + member.getFullName()
        ));

        logger.info("Recorded payment {} for member {}", payment.getPaymentCode(), member.getMemberCode());
        return payment;
    }

    public Payment getPaymentById(int id) {
        return paymentDao.findById(id);
    }

    public Payment getPaymentByCode(String code) {
        return paymentDao.findByCode(code);
    }

    public List<Payment> getPaymentsForMember(int memberId) {
        return paymentDao.findByMemberId(memberId);
    }

    public List<Payment> searchPayments(LocalDate start, LocalDate end, String method, String status, String search) {
        return paymentDao.findAll(start, end, method, status, search);
    }

    public BigDecimal getTotalRevenue(LocalDate start, LocalDate end) {
        return paymentDao.getTotalRevenue(start, end);
    }

    public BigDecimal getCurrentMonthRevenue() {
        return paymentDao.getCurrentMonthRevenue();
    }
}
