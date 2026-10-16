# FitCore Pro — Automated Testing Strategy & Execution Report

## 1. Quality Assurance Strategy

FitCore Pro adheres to strict test-driven and behavioral verification practices. The test suite comprises:
1. **Unit Tests (`src/test/java/com/fitcore/service`, `security`, `validation`)**: Test business rules, state transitions, validation, and role-based permissions in isolation using **JUnit 5** and **Mockito 5**.
2. **Integration Tests (`src/test/java/com/fitcore/dao`)**: Execute against a live **MySQL / MariaDB** database instance, validating live queries, foreign key cascades, unique constraints, and transaction rollbacks.

---

## 2. Test Execution Summary

The test suite was executed via Apache Maven and JUnit Platform Surefire Runner on Windows 11 with Java 21 LTS.

```
-------------------------------------------------------
 T E S T S   E X E C U T I O N   R E S U L T S
-------------------------------------------------------
[INFO] Running com.fitcore.security.PasswordHasherTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.security.RoleAuthorizationTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.validation.ValidationUtilTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.validation.DateTimeUtilTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.service.MemberServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.service.MembershipServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.service.AttendanceServiceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.service.PaymentServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 - PASSED
[INFO] Running com.fitcore.dao.DatabaseIntegrationTest (Live MySQL)
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 - PASSED
-------------------------------------------------------
[INFO] Results:
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
-------------------------------------------------------
```

**Overall Result**: **39 Passed / 39 Executed (100% Pass Rate)**

---

## 3. Comprehensive Requirement Test Matrix

| # | Requirement / Workflow | Test Class | Test Method | Result |
|---|---|---|---|---|
| 1 | Valid & Invalid Login | `PasswordHasherTest` | `testHashAndVerifyCorrect`, `testVerifyIncorrect` | **PASSED** |
| 2 | Password Hashing (BCrypt) | `PasswordHasherTest` | `testNullOrEmptyPassword`, `testVerifyMalformed` | **PASSED** |
| 3 | Role-Based Authorization | `RoleAuthorizationTest` | `testAdminHasAllPermissions`, `testReceptionistPermissions`, `testTrainerPermissions` | **PASSED** |
| 4 | Permission Violation Trapping | `RoleAuthorizationTest` | `testSecurityContextEnforcement` | **PASSED** |
| 5 | Email & Phone Validation | `ValidationUtilTest` | `testValidEmails`, `testInvalidEmails`, `testValidPhoneNumbers` | **PASSED** |
| 6 | Monetary Checks | `ValidationUtilTest` | `testMonetaryAmounts` | **PASSED** |
| 7 | Inclusive End-Date Math | `DateTimeUtilTest` | `testInclusiveEndDateCalculation` | **PASSED** |
| 8 | Expiration Detection | `DateTimeUtilTest` | `testExpiredDetection`, `testExpiringSoon` | **PASSED** |
| 9 | Member Registration | `MemberServiceTest` | `testRegisterMemberSuccess` | **PASSED** |
| 10 | Duplicate Email Prevention | `MemberServiceTest` | `testDuplicateEmailRejected` | **PASSED** |
| 11 | Duplicate Phone Prevention | `MemberServiceTest` | `testDuplicatePhoneRejected` | **PASSED** |
| 12 | Soft Deactivation | `MemberServiceTest` | `testSoftDeactivateMember` | **PASSED** |
| 13 | Membership Fee Lock-in | `MembershipServiceTest`| `testAssignMembershipCalculations` | **PASSED** |
| 14 | Deactivated Member Rejection | `MembershipServiceTest`| `testDeactivatedMemberRejected` | **PASSED** |
| 15 | Overlapping Membership Block | `MembershipServiceTest`| `testOverlappingActiveMembershipsRejected` | **PASSED** |
| 16 | Renewal Preserving History | `MembershipServiceTest`| `testRenewMembershipPreservesHistory` | **PASSED** |
| 17 | Normal Member Check-in | `AttendanceServiceTest`| `testCheckInSuccess` | **PASSED** |
| 18 | Single Daily Check-in Rule | `AttendanceServiceTest`| `testDuplicateCheckInRejected` | **PASSED** |
| 19 | Ineligible Member Check-in | `AttendanceServiceTest`| `testNoActiveMembershipCheckInRejected`, `testDeactivatedMemberCheckInRejected` | **PASSED** |
| 20 | Record Valid Payment | `PaymentServiceTest` | `testRecordPaymentSuccess` | **PASSED** |
| 21 | Zero / Negative Amount Block | `PaymentServiceTest` | `testInvalidAmountRejected` | **PASSED** |
| 22 | Duplicate Transaction Ref | `PaymentServiceTest` | `testDuplicateTransactionReferenceRejected` | **PASSED** |
| 23 | Live MySQL Admin Auth | `DatabaseIntegrationTest`| `testAdminUserAuthentication` | **PASSED** |
| 24 | Live MySQL Member CRUD | `DatabaseIntegrationTest`| `testMemberCrud` | **PASSED** |
| 25 | Engine-level Daily Attendance UNIQUE | `DatabaseIntegrationTest`| `testDailyAttendanceUniquenessConstraint` | **PASSED** |
| 26 | Live Revenue Summation | `DatabaseIntegrationTest`| `testRevenueCalculation` | **PASSED** |
| 27 | Explicit Transaction Rollback | `DatabaseIntegrationTest`| `testTransactionRollback` | **PASSED** |

---

## 4. How to Execute Tests on Windows

In PowerShell:
```powershell
# Set Java 21 environment
$env:JAVA_HOME = "C:\Users\abhib\.tools\jdk-21.0.12.1+1"
$env:Path = "C:\Users\abhib\.tools\jdk-21.0.12.1+1\bin;C:\Users\abhib\.tools\apache-maven-3.9.6\bin;" + $env:Path

# Run full test suite
mvn test
```
