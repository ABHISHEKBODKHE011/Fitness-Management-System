package com.fitcore.service;

import com.fitcore.dao.AuditLogDao;
import com.fitcore.dao.MemberDao;
import com.fitcore.dao.PaymentDao;
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
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Payment & Billing Service Tests")
class PaymentServiceTest {

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private MemberDao memberDao;

    @Mock
    private AuditLogDao auditLogDao;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentDao, memberDao, auditLogDao);
        User receptionist = new User(2, "reception", "hash", "Receptionist", "reception@example.com", Role.RECEPTIONIST, "ACTIVE");
        SecurityContext.setCurrentUser(receptionist);
    }

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    @DisplayName("Should successfully record a valid payment")
    void testRecordPaymentSuccess() throws Exception {
        Member member = new Member(1, "MBR-1001", "Johnathan Davis", "+1-555-1101", "jdavis@example.com", "Male", null, null, null, "ACTIVE");
        when(memberDao.findById(1)).thenReturn(member);
        when(paymentDao.generateNextCode()).thenReturn("PAY-202610-001");
        when(paymentDao.existsByTransactionReference("UPI-12345", null)).thenReturn(false);
        when(paymentDao.create(any(Payment.class))).thenReturn(true);

        Payment payment = new Payment();
        payment.setMemberId(1);
        payment.setAmount(new BigDecimal("129.99"));
        payment.setPaymentMethod("UPI");
        payment.setPaymentStatus("SUCCESS");
        payment.setTransactionReference("UPI-12345");

        Payment recorded = paymentService.recordPayment(payment);

        assertNotNull(recorded);
        assertEquals("PAY-202610-001", recorded.getPaymentCode());
        assertEquals("Johnathan Davis", recorded.getMemberName());
        verify(paymentDao).create(any(Payment.class));
        verify(auditLogDao).log(any());
    }

    @Test
    @DisplayName("Should reject payment with zero or negative amount")
    void testInvalidAmountRejected() {
        Member member = new Member(1, "MBR-1001", "Johnathan Davis", "+1-555-1101", "jdavis@example.com", "Male", null, null, null, "ACTIVE");
        when(memberDao.findById(1)).thenReturn(member);

        Payment zeroPayment = new Payment();
        zeroPayment.setMemberId(1);
        zeroPayment.setAmount(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> paymentService.recordPayment(zeroPayment));

        Payment negativePayment = new Payment();
        negativePayment.setMemberId(1);
        negativePayment.setAmount(new BigDecimal("-49.99"));

        assertThrows(IllegalArgumentException.class, () -> paymentService.recordPayment(negativePayment));
    }

    @Test
    @DisplayName("Should reject payment when transaction reference already exists in database")
    void testDuplicateTransactionReferenceRejected() throws Exception {
        Member member = new Member(1, "MBR-1001", "Johnathan Davis", "+1-555-1101", "jdavis@example.com", "Male", null, null, null, "ACTIVE");
        when(memberDao.findById(1)).thenReturn(member);
        when(paymentDao.existsByTransactionReference("TXN-DUP-999", null)).thenReturn(true);

        Payment dupPayment = new Payment();
        dupPayment.setMemberId(1);
        dupPayment.setAmount(new BigDecimal("49.99"));
        dupPayment.setTransactionReference("TXN-DUP-999");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.recordPayment(dupPayment));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(paymentDao, never()).create(any(Payment.class), any(Connection.class));
    }
}
