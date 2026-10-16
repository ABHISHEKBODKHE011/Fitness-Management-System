# FitCore Pro — Software Architecture & Technical Design

## 1. Executive Architectural Overview

**FitCore Pro** is an enterprise-grade desktop fitness and gym management application engineered with **Java 21 LTS**, **Java Swing**, **FlatLaf**, **JDBC**, and **MySQL 8.x**.

The application follows a clean, decoupled **Layered N-Tier Architecture** ensuring strict separation of concerns, high maintainability, testability, and defensive security.

```
+-------------------------------------------------------------------------+
|                        PRESENTATION LAYER (UI)                          |
|  Java Swing + FlatLaf Modern Theme (Navy #0F172A, Clean Slate & White)  |
|  MainFrame | Sidebar | TopBar | StatCards | ModernTable | Dialogs & Forms|
+------------------------------------+------------------------------------+
                                     | Event Dispatch Thread (EDT)
                                     v
+-------------------------------------------------------------------------+
|                         SERVICE / BUSINESS LAYER                        |
|  AuthService | MemberService | TrainerService | MembershipPlanService   |
|  MembershipService | AttendanceService | PaymentService | AuditService   |
|  ---------------------------------------------------------------------  |
|  • RBAC Authorization Checks (Admin, Receptionist, Trainer)             |
|  • Inclusive Validity Date Calculations (start + duration - 1)          |
|  • Single Daily Check-in Policy Enforcement                             |
|  • Transaction Boundaries & Rollback Orchestration                     |
+------------------------------------+------------------------------------+
                                     | Plain Java Calls / DTOs
                                     v
+-------------------------------------------------------------------------+
|                        DAO / REPOSITORY LAYER (DATA)                    |
|  UserDao | MemberDao | TrainerDao | PlanDao | AttendanceDao | PaymentDao|
|  ---------------------------------------------------------------------  |
|  • Prepared Statements (100% SQL Injection Protection)                  |
|  • Auto-Close Try-With-Resources (Zero Resource / Connection Leaks)     |
|  • HikariCP High-Performance JDBC Connection Pool                       |
+------------------------------------+------------------------------------+
                                     | JDBC (TCP 3306)
                                     v
+-------------------------------------------------------------------------+
|                      RELATIONAL DATABASE STORAGE                        |
|  MySQL 8.x / MariaDB Relational Database Server                         |
|  Normalized InnoDB Tables | Foreign Keys | UNIQUE & CHECK Constraints   |
+-------------------------------------------------------------------------+
```

---

## 2. Layered Responsibilities

### 2.1 Presentation Layer (`com.fitcore.ui`)
- **Swing Desktop Interface**: Implemented natively in Java Swing without web wrappers or heavyweight browser engines.
- **FlatLaf Look & Feel**: Configured with custom typography (Segoe UI), generous component padding, 8px rounded corners, and curated color palettes (deep navy `#0F172A`, electric blue `#2563EB`, emerald green `#10B981`, cool slate `#64748B`, and rose `#EF4444`).
- **Responsive Layouts**: Flexible `BorderLayout`, `GridBagLayout`, and `CardLayout` ensuring seamless window resizing from 1024x680 to 4K displays.
- **Asynchronous Execution**: Heavy database queries and analytical computations run in background threads via `SwingWorker`, ensuring the UI thread remains 100% responsive without freezing.

### 2.2 Service Layer (`com.fitcore.service`)
- Enforces all business rules and validation before any database interaction.
- **Role-Based Authorization**: Every service method inspects `SecurityContext` using fine-grained `Permission` tokens.
- **Transactional Consistency**: Multi-step operations (e.g., membership renewal, payment recording, and member assignment) use explicit JDBC transactions with automatic rollback on error.
- **Audit Logging**: Important security and administrative actions are logged to `audit_logs`.

### 2.3 Data Access Layer (`com.fitcore.dao`)
- Encapsulates all SQL logic. No SQL query or JDBC call is ever placed inside Swing event listeners.
- Uses `PreparedStatement` exclusively with parameterized queries to eliminate SQL injection vulnerabilities.
- Employs try-with-resources blocks ensuring `Connection`, `PreparedStatement`, and `ResultSet` handles are returned to the pool immediately.

### 2.4 Configuration & Connection Pooling (`com.fitcore.config`)
- **DatabaseConfig**: Manages **HikariCP** (`HikariDataSource`), the fastest production-ready JDBC connection pool.
- Externalized properties: Reads configuration from system environment variables (`FITCORE_DB_*`), JVM arguments, and classpath `application.properties`.
- Thread-safe singleton with JVM shutdown hooks to close open sockets gracefully.

---

## 3. Core Architectural Patterns

| Pattern | Implementation in FitCore Pro |
|---|---|
| **Data Access Object (DAO)** | Isolates database persistence (`MemberDao`, `PaymentDao`, etc.) from business logic. |
| **Model-View-Controller (MVC)** | Presentation panels observe models populated by services and trigger actions without direct SQL. |
| **Data Transfer Object (DTO)** | `DashboardMetrics`, `MonthlyRevenue`, and `PlanDistribution` encapsulate multi-table summaries. |
| **Singleton** | `DatabaseConfig` and `SecurityContext` provide controlled, thread-safe global access. |
| **Defensive Password Hashing** | BCrypt via `jbcrypt` with work factor 10, protecting stored passwords against rainbow-table attacks. |
| **Unit of Work / Transaction** | Explicit transaction boundaries with `Connection.setAutoCommit(false)`, `commit()`, and `rollback()`. |

---

## 4. Security Architecture

1. **Password Security**: Passwords are never stored in plaintext. BCrypt generates unique per-user salts and hashes credentials securely.
2. **Role-Based Access Control (RBAC)**:
   - **ADMIN**: Full administrative and financial control, user management, plan editing, and audit trail inspection.
   - **RECEPTIONIST**: Front-desk operations: member registration, membership assignments, renewals, daily check-in, and payment recording.
   - **TRAINER**: Read-only member roster inspection for assigned clients, training schedules, and attendance views.
3. **Prepared Statements**: User inputs are bound via typed parameters (`stmt.setString()`, `stmt.setInt()`, `stmt.setBigDecimal()`), making SQL injection impossible.
4. **Zero Financial Credential Retention**: As required by PCI-DSS best practices, FitCore Pro records confirmation references and billing statuses, but never collects or stores raw credit card numbers, CVVs, or bank login credentials.
