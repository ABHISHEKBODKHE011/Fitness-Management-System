package com.fitcore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain entity representing payments and billing records.
 */
public class Payment {
    private Integer id;
    private String paymentCode;
    private Integer memberId;
    private Integer membershipId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paymentMethod; // CASH, UPI, CARD, BANK_TRANSFER
    private String paymentStatus; // SUCCESS, PENDING, FAILED, REFUNDED
    private String transactionReference;
    private String receiptNotes;
    private LocalDateTime createdAt;

    // Display helpers
    private String memberCode;
    private String memberName;
    private String planName;

    public Payment() {
        this.amount = BigDecimal.ZERO;
        this.paymentDate = LocalDateTime.now();
        this.paymentMethod = "CASH";
        this.paymentStatus = "SUCCESS";
    }

    public Payment(Integer id, String paymentCode, Integer memberId, Integer membershipId, BigDecimal amount, LocalDateTime paymentDate, String paymentMethod, String paymentStatus, String transactionReference, String receiptNotes) {
        this.id = id;
        this.paymentCode = paymentCode;
        this.memberId = memberId;
        this.membershipId = membershipId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.paymentStatus = paymentStatus != null ? paymentStatus : "SUCCESS";
        this.transactionReference = transactionReference;
        this.receiptNotes = receiptNotes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(String paymentCode) {
        this.paymentCode = paymentCode;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Integer getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Integer membershipId) {
        this.membershipId = membershipId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getReceiptNotes() {
        return receiptNotes;
    }

    public void setReceiptNotes(String receiptNotes) {
        this.receiptNotes = receiptNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public boolean isSuccessful() {
        return "SUCCESS".equalsIgnoreCase(this.paymentStatus);
    }
}
