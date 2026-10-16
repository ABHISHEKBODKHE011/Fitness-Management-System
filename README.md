# FitCore Pro — Advanced Fitness Management System
### Enterprise Desktop Gym Management System | Java 21 LTS + Swing (FlatLaf) + JDBC + MySQL 8.x

---

## 1. Project Overview & Problem Statement

Modern gyms and fitness facilities face operational challenges when relying on disparate spreadsheets, fragmented paper logs, or outdated desktop software:
- Lack of centralized member tracking and historical preservation.
- Error-prone membership validity date calculations leading to coverage disputes or revenue loss.
- Inadequate check-in validation resulting in duplicate visits or attendance by members with expired subscriptions.
- Unencrypted password storage, lack of audit accountability, and missing role separation.
- Destructive record deletion that obliterates financial and billing history.

**FitCore Pro** solves these challenges by delivering an enterprise-grade desktop application built with **Java 21 LTS**, modern **FlatLaf Swing**, **JDBC**, and **MySQL 8.x**. It integrates strict role-based access control, an inclusive validity date engine, automated daily attendance enforcement, financial billing with printable official receipts, and real-time dashboard analytics powered by **JFreeChart**.

---

## 2. Technology Stack & Dependency Versions

| Component | Technology | Version | Purpose |
|---|---|---|---|
| **Runtime** | Java LTS | `21.0.12` (Temurin / OpenJDK) | Modern language features (Pattern matching, records, sealed interfaces) |
| **User Interface** | Java Swing + FlatLaf | `3.4.1` | Polished desktop interface with deep navy `#0F172A` theme |
| **Database** | MySQL / MariaDB | `8.x` / `10.11+` | ACID relational storage, constraints, and foreign keys |
| **JDBC Driver** | MySQL Connector/J | `8.3.0` | Production relational database connectivity |
| **Connection Pool**| HikariCP | `5.1.0` | High-performance, zero-leak connection pooling |
| **Security** | jBCrypt | `0.4` | Defensive salted password hashing |
| **Data Analytics** | JFreeChart | `1.5.4` | Revenue trend bar charts and plan share pie plots |
| **Logging** | SLF4J + Logback | `2.0.12` / `1.5.3` | Structured console and rolling file logging |
| **Build Tool** | Apache Maven | `3.9.6` | Dependency resolution and fat JAR packaging |
| **Testing** | JUnit 5 + Mockito | `5.10.2` / `5.11.0`| Automated unit and live integration testing |

---

## 3. System Architecture & Relational Design

The application implements a clean **Layered N-Tier Architecture**:
- **Presentation Layer**: Swing views, custom `ModernTable`, `StatCard`, `StatusBadge`, and responsive forms.
- **Service Layer**: Business validation, role authorization checks, transactions, and audit logging.
- **DAO Layer**: Parameterized `PreparedStatement` queries with try-with-resources.
- **Configuration Layer**: HikariCP connection pool with externalized environment variable support.
- **Utility Layer**: Inclusive date arithmetic, currency formatting, CSV export, and receipt printing.

```
       +-----------------------+
       |   FitCore Pro Swing   |
       |  (FlatLaf Navy Theme) |
       +-----------+-----------+
                   |
                   v
       +-----------------------+
       |     Service Layer     | <--- SecurityContext (RBAC)
       +-----------+-----------+
                   |
                   v
       +-----------------------+
       |       DAO Layer       | <--- HikariCP Pool
       +-----------+-----------+
                   |
                   v
       +-----------------------+
       |   MySQL 8.x Storage   |
       +-----------------------+
```

### Relational Schema Diagram
The complete Mermaid ER diagram is available in [`docs/er-diagram.mmd`](file:///docs/er-diagram.mmd) and [`docs/database-design.md`](file:///docs/database-design.md).

---

## 4. Key Functional Modules

### 4.1 Authentication & Role-Based Access Control (RBAC)
- Secure login with BCrypt password hashing.
- Three specialized roles:
  1. **Admin**: Full administrative privileges (user management, plan configuration, staff rosters, audit logs).
  2. **Receptionist**: Member registration, membership assignments, renewals, check-ins, payments, and receipts.
  3. **Trainer**: Read-only view of assigned members, member attendance logs, and workout check-ins.
- Safe automated initial administrator bootstrap: creates default admin if database is empty without hardcoding production credentials.

### 4.2 Executive Analytics Dashboard
- Real-time KPI summary cards calculated from MySQL:
  - Total registered members & currently active members.
  - Today's check-ins.
  - Memberships expiring in the next 7 days.
  - Current month revenue & all-time collected revenue.
- **JFreeChart Monthly Revenue Trend**: Bar chart tracking income over previous billing cycles.
- **JFreeChart Plan Distribution**: Color-coded pie chart illustrating membership tier popularity.
- Recent member registrations and payment receipts quick tables.

### 4.3 Member Management & 360° Profile
- Full CRUD operations with auto-generated member IDs (`MBR-1001`).
- Search by ID, code, name, phone, or email; filter by status (ACTIVE / INACTIVE).
- **360° Profile Dialog**: Comprehensive tabbed view displaying member personal info, full membership subscription history, attendance history, and payment receipts.
- **Soft Deactivation**: Prevents accidental deletion of historical records by marking accounts `INACTIVE`.

### 4.4 Trainer Roster & Client Allocation
- Certified trainer registration, specialization tracking, and contact details.
- Tracks active member assignments per trainer.
- "View Assigned Members" modal dialog showing clients trained by a specific coach.

### 4.5 Membership Plans & Pricing Catalog
- Configurable plan catalog: duration in days, standard fee, and description.
- Enforces strict positive duration (`duration_days > 0`) and non-negative fees.
- Protects historical subscriptions by preventing hard-deletion of referenced plans.

### 4.6 Membership Assignment & Renewal Engine
- Assigns plans to members and locks in the agreed fee at time of purchase.
- **Inclusive Validity Math**: $\text{EndDate} = \text{StartDate} + \text{Duration} - 1$.
- **Renewal History Preservation**: Renewing creates a new subscription linked to `renewed_from_id` while preserving the previous historical record.
- Prevents overlapping active memberships for the same member.

### 4.7 Attendance & Single Daily Check-in Enforcement
- Quick check-in with automatic current timestamp recording.
- **Single Check-in Per Day Rule**: Enforced at both application service level and database level via `UNIQUE KEY (member_id, check_in_date)`.
- Validates active membership eligibility before permitting entry.

### 4.8 Billing, Invoicing & Official Receipt Generator
- Supports recorded payment methods: `CASH`, `UPI`, `CARD`, `BANK_TRANSFER`.
- Enforces unique transaction/authorization reference codes.
- Distinguishes payment records from external payment gateways; collects no raw credit card numbers or banking secrets.
- **Printable Official Receipt**: Generates a professional branded gym receipt modal with print functionality (`ReceiptPrinter`).

### 4.9 Reporting & Data Export
- Tabbed reports: Expiring memberships (7, 14, 30 days), Active vs Expired subscriptions, Trainer member allocations, Revenue breakdown by payment method, and Daily attendance frequency.
- **CSV Data Export**: One-click export for any table or report to standard CSV files.

### 4.10 Security Audit Trail
- Real-time audit logging for logins, logouts, member registrations, membership assignments, renewals, and payments.

---

## 5. Default Credentials for Evaluation

The database is pre-seeded with fictitious staff accounts for testing:

| Username | Password | Role | Description |
|---|---|---|---|
| `admin` | `Admin@123` | `ADMIN` | Full administrative & financial access |
| `reception` | `Reception@123` | `RECEPTIONIST` | Front-desk registrations, billing, and check-ins |
| `trainer_mike` | `Trainer@123` | `TRAINER` | Trainer view of assigned clients & attendance |

*(The login screen includes preset buttons for instant 1-click credential filling).*

---

## 6. Windows 11 Step-by-Step Setup & Execution Guide

### Prerequisites
1. **Windows 11** (64-bit).
2. **Java 21 LTS** and **Apache Maven 3.9+** (Installed in `C:\Users\abhib\.tools` or standard PATH).
3. **MySQL 8.x** or **MariaDB 10.11+** running on `localhost:3306`.

### Step 1: Clone or Open the Project
Open PowerShell and navigate to the project directory:
```powershell
cd "c:\Users\abhib\Desktop\Projects\Fitness Management System"
```

### Step 2: Configure Environment & Database
Copy the example environment file:
```powershell
Copy-Item .env.example .env
```
Ensure your MySQL service is running. If setting up a fresh MySQL 8 installation, create the database and import schema and seed data:
```powershell
# Open MySQL client and run:
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

### Step 3: Compile and Run Automated Tests
Execute the full test suite with Maven:
```powershell
$env:JAVA_HOME = "C:\Users\abhib\.tools\jdk-21.0.12.1+1"
$env:Path = "C:\Users\abhib\.tools\jdk-21.0.12.1+1\bin;C:\Users\abhib\.tools\apache-maven-3.9.6\bin;" + $env:Path

mvn test
```
*(All 39 unit and integration tests will execute and report `BUILD SUCCESS`).*

### Step 4: Package Executable Fat JAR
```powershell
mvn clean package -DskipTests
```
This produces `target/fitcore-pro-1.0.0.jar` (~9 MB) containing all dependencies.

### Step 5: Launch the Application
Run via PowerShell:
```powershell
.\run.ps1
```
Or double-click `run.bat` in Windows File Explorer!

---

## 7. Recommended Screenshots for Submission

When compiling your project report, capture the following real screens:
1. **Login Screen**: Featuring the navy branding banner, demo role preset pills, and credentials form.
2. **Executive Dashboard**: Showing the 6 KPI cards, JFreeChart Monthly Revenue bar chart, and Plan Distribution pie chart.
3. **Member Management Directory**: Displaying members, status pill badges, search filters, and action buttons.
4. **Member 360° Profile Dialog**: Illustrating the tabbed view with membership history, attendance logs, and payments.
5. **Membership Assignment & Renewal Modal**: Showing the automatic inclusive end-date calculation.
6. **Quick Member Check-in Dialog**: Demonstrating instant member search and single daily check-in validation.
7. **Billing & Payments Ledger**: Displaying transactions, revenue totals, and payment methods.
8. **Official Printable Payment Receipt**: Showing the branded HTML invoice layout.
9. **Reports & Analytics Center**: Showing the expiring memberships table and CSV export dialog.
10. **System Security Audit Trail**: Showing administrative event timestamps and actions.

---

## 8. College PBL Requirements Checklist

- [x] Java 21 LTS implementation.
- [x] Modern Java Swing desktop interface with FlatLaf look & feel.
- [x] MySQL 8.x normalized relational schema with InnoDB engine.
- [x] JDBC connectivity via MySQL Connector/J and HikariCP connection pooling.
- [x] BCrypt password hashing via jBCrypt.
- [x] Role-Based Access Control (Admin, Receptionist, Trainer).
- [x] Safe initial administrator bootstrap procedure.
- [x] Real-time Dashboard with accurate SQL metrics and JFreeChart visual plots.
- [x] Member CRUD, contact uniqueness validation, and soft deactivation.
- [x] Trainer management, specializations, and member allocation lists.
- [x] Membership plans catalog with duration and fee validation.
- [x] Membership assignment and renewal engine with inclusive end-date math.
- [x] Attendance check-in with single-per-day enforcement (app + DB constraint).
- [x] Payment ledger, unique transaction references, and printable HTML receipts.
- [x] Analytical reports with date filters and CSV export.
- [x] Administrative security audit logging.
- [x] Automated JUnit 5 test suite (39/39 passing tests).
- [x] Packaged runnable standalone fat JAR (`target/fitcore-pro-1.0.0.jar`).
- [x] Complete documentation: Architecture, Database Design, Testing Report, and Mermaid ER Diagram.
