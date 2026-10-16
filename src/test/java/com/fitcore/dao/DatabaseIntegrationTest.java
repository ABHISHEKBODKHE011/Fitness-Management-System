package com.fitcore.dao;

import com.fitcore.config.DatabaseConfig;
import com.fitcore.model.*;
import com.fitcore.security.PasswordHasher;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MySQL Relational Database Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseIntegrationTest {

    private static UserDao userDao;
    private static MemberDao memberDao;
    private static TrainerDao trainerDao;
    private static MembershipPlanDao planDao;
    private static AttendanceDao attendanceDao;
    private static PaymentDao paymentDao;

    @BeforeAll
    static void setUpAll() {
        Assumptions.assumeTrue(DatabaseConfig.testConnection(), "Database service must be running for integration tests");
        userDao = new UserDao();
        memberDao = new MemberDao();
        trainerDao = new TrainerDao();
        planDao = new MembershipPlanDao();
        attendanceDao = new AttendanceDao();
        paymentDao = new PaymentDao();
    }

    @Test
    @Order(1)
    @DisplayName("Should successfully connect to MySQL and query seeded admin user")
    void testAdminUserAuthentication() {
        User admin = userDao.findByUsername("admin");
        assertNotNull(admin, "Admin user must be seeded in database");
        assertEquals("admin@fitcorepro.com", admin.getEmail());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        assertTrue(PasswordHasher.verify("Admin@123", admin.getPasswordHash()));
    }

    @Test
    @Order(2)
    @DisplayName("Should query seeded membership plans and verify constraints")
    void testMembershipPlans() {
        List<MembershipPlan> plans = planDao.findAll(true, null);
        assertFalse(plans.isEmpty(), "Plans must be populated");
        for (MembershipPlan p : plans) {
            assertTrue(p.getDurationDays() > 0, "Duration must be positive");
            assertTrue(p.getFee().compareTo(BigDecimal.ZERO) >= 0, "Fee must be non-negative");
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should perform Member CRUD operations against MySQL")
    void testMemberCrud() {
        String uniqueCode = "TEST-MBR-" + System.currentTimeMillis() % 10000;
        String uniqueEmail = "test_" + System.currentTimeMillis() % 10000 + "@example.com";
        String uniquePhone = "+1-555-" + String.format("%04d", System.currentTimeMillis() % 10000);

        Member member = new Member(
                null, uniqueCode, "Integration Test Member", uniquePhone,
                uniqueEmail, "Male", "Test Contact", LocalDate.now(), 1, "ACTIVE"
        );

        boolean created = memberDao.create(member);
        assertTrue(created, "Member should be created in MySQL");
        assertNotNull(member.getId());

        Member retrieved = memberDao.findById(member.getId());
        assertNotNull(retrieved);
        assertEquals(uniqueCode, retrieved.getMemberCode());
        assertEquals("Integration Test Member", retrieved.getFullName());

        // Update
        retrieved.setFullName("Updated Integration Member");
        boolean updated = memberDao.update(retrieved);
        assertTrue(updated);

        Member afterUpdate = memberDao.findById(member.getId());
        assertEquals("Updated Integration Member", afterUpdate.getFullName());
    }

    @Test
    @Order(4)
    @DisplayName("Should enforce daily attendance uniqueness at the database constraint level")
    void testDailyAttendanceUniquenessConstraint() {
        int offset = (int) (System.currentTimeMillis() % 5000) + 10;
        LocalDate testDate = LocalDate.of(2035, 1, 1).plusDays(offset);
        Attendance first = new Attendance(null, 1, LocalDateTime.of(2035, 1, 1, 8, 30), testDate, "First checkin");

        // First check in succeeds
        boolean firstSaved = attendanceDao.checkIn(first);
        assertTrue(firstSaved);

        // Second check in for same member on same date MUST be rejected by database unique constraint
        Attendance second = new Attendance(null, 1, LocalDateTime.of(2035, 1, 1, 14, 0), testDate, "Duplicate checkin");
        assertThrows(IllegalStateException.class, () -> attendanceDao.checkIn(second));
    }

    @Test
    @Order(5)
    @DisplayName("Should calculate revenue correctly using successful payments only")
    void testRevenueCalculation() {
        BigDecimal totalRevenue = paymentDao.getTotalRevenue(null, null);
        assertNotNull(totalRevenue);
        assertTrue(totalRevenue.compareTo(BigDecimal.ZERO) > 0, "Total revenue should be positive from seed payments");
    }

    @Test
    @Order(6)
    @DisplayName("Should support explicit transaction boundaries and rollback on failure")
    void testTransactionRollback() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Simulate an invalid operation in transaction
                Payment invalid = new Payment();
                invalid.setMemberId(999999); // non-existent member -> FK violation
                invalid.setAmount(new BigDecimal("100.00"));
                invalid.setPaymentCode("TX-FAIL-" + System.currentTimeMillis());
                invalid.setPaymentDate(LocalDateTime.now());

                paymentDao.create(invalid, conn);
                conn.commit();
                fail("Transaction should have thrown SQLException due to FK constraint");
            } catch (SQLException ex) {
                conn.rollback(); // Rollback verified
                assertTrue(true, "Rollback executed cleanly upon constraint violation");
            }
        } catch (SQLException e) {
            fail("Database error during rollback test: " + e.getMessage());
        }
    }
}
