# FitCore Pro — Database Schema & Data Modeling Design

## 1. Schema Overview

The FitCore Pro relational schema is designed for **MySQL 8.x** and **MariaDB 10.x+**, normalized to **Third Normal Form (3NF)** and **Boyce-Codd Normal Form (BCNF)**. It ensures zero unintended data anomalies, enforces strict referential integrity, and optimizes analytical query execution.

- **Database Name**: `fitcore_pro`
- **Default Character Set**: `utf8mb4`
- **Default Collation**: `utf8mb4_unicode_ci`
- **Storage Engine**: `InnoDB` (ACID transaction support, row-level locking, foreign key constraints)

---

## 2. Entity Relational Architecture & Tables

### 2.1 `users` Table
Stores staff accounts, hashed credentials, and role assignments.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique internal user identifier |
| `username` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` | Login account handle |
| `password_hash`| `VARCHAR(255)`| `NOT NULL` | BCrypt salt-hashed secret |
| `full_name` | `VARCHAR(100)`| `NOT NULL` | Display name |
| `email` | `VARCHAR(100)`| `NOT NULL`, `UNIQUE` | Staff corporate email |
| `role` | `ENUM` | `ADMIN`, `RECEPTIONIST`, `TRAINER` | Access control role |
| `status` | `ENUM` | `ACTIVE`, `INACTIVE` | Account state |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Account creation timestamp |
| `updated_at` | `TIMESTAMP` | `ON UPDATE CURRENT_TIMESTAMP` | Last updated timestamp |

### 2.2 `trainers` Table
Stores certified gym trainers and their fitness specializations.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique trainer ID |
| `user_id` | `INT` | `NULL`, `FK -> users(id)` | Linked login account (if trainer has login access) |
| `trainer_code` | `VARCHAR(20)` | `NOT NULL`, `UNIQUE` | Natural business code (e.g. `TRN-001`) |
| `name` | `VARCHAR(100)`| `NOT NULL` | Full legal name |
| `phone` | `VARCHAR(20)` | `NOT NULL` | Primary contact number |
| `email` | `VARCHAR(100)`| `NOT NULL`, `UNIQUE` | Professional email |
| `specialization`|`VARCHAR(100)`| `NOT NULL` | Fitness focus (CrossFit, HIIT, Yoga, etc.) |
| `status` | `ENUM` | `ACTIVE`, `INACTIVE` | Availability status |

### 2.3 `membership_plans` Table
Defines available membership tiers, validity periods, and pricing.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique plan ID |
| `plan_code` | `VARCHAR(20)` | `NOT NULL`, `UNIQUE` | Tier code (e.g. `PLN-M01`, `PLN-Y12`) |
| `name` | `VARCHAR(100)`| `NOT NULL` | Plan name (e.g. Monthly Basic) |
| `duration_days`| `INT` | `NOT NULL`, `CHECK(duration_days > 0)` | Duration in days |
| `fee` | `DECIMAL(10,2)`| `NOT NULL`, `CHECK(fee >= 0)` | Base price |
| `description` | `TEXT` | `NULL` | Perks, amenities, and benefits |
| `status` | `ENUM` | `ACTIVE`, `INACTIVE` | Active catalog offering |

### 2.4 `members` Table
Stores gym client registration, contact data, and emergency information.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Member primary key |
| `member_code` | `VARCHAR(20)` | `NOT NULL`, `UNIQUE` | Natural ID (e.g. `MBR-1001`) |
| `full_name` | `VARCHAR(100)`| `NOT NULL` | Member's full name |
| `phone` | `VARCHAR(20)` | `NOT NULL` | Phone number |
| `email` | `VARCHAR(100)`| `NOT NULL` | Email address |
| `gender` | `VARCHAR(30)` | `NOT NULL` | Inclusive gender selection |
| `emergency_contact`| `VARCHAR(100)`| `NULL` | Emergency contact name & phone |
| `joining_date`| `DATE` | `NOT NULL` | Initial registration date |
| `assigned_trainer_id`|`INT` | `NULL`, `FK -> trainers(id)` | Current primary personal trainer |
| `status` | `ENUM` | `ACTIVE`, `INACTIVE` | Member account status |

### 2.5 `memberships` Table
Represents historical and active membership purchases.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Membership record ID |
| `membership_code`| `VARCHAR(30)`| `NOT NULL`, `UNIQUE` | Code (e.g. `MS-202610-001`) |
| `member_id` | `INT` | `NOT NULL`, `FK -> members(id)` | Subscribing member |
| `plan_id` | `INT` | `NOT NULL`, `FK -> plans(id)` | Purchased plan template |
| `start_date` | `DATE` | `NOT NULL` | Validity start date |
| `end_date` | `DATE` | `NOT NULL`, `CHECK(end_date >= start_date)` | Inclusive end date |
| `agreed_fee` | `DECIMAL(10,2)`| `NOT NULL`, `CHECK(agreed_fee >= 0)` | Locked-in purchase price |
| `status` | `ENUM` | `ACTIVE`, `EXPIRED`, `UPCOMING`, `CANCELLED` | Subscription state |
| `renewed_from_id`|`INT` | `NULL`, `FK -> memberships(id)` | Link to previous membership in renewal chain |
| `notes` | `TEXT` | `NULL` | Administrative remarks |

### 2.6 `attendance` Table
Tracks daily member check-in visits.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Attendance ID |
| `member_id` | `INT` | `NOT NULL`, `FK -> members(id)` | Attending member |
| `check_in_time`| `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Exact time of check-in |
| `check_in_date`| `DATE` | `NOT NULL` | Calendar date |
| `notes` | `VARCHAR(255)`| `NULL` | Check-in memo |
| **Constraint** | `UNIQUE` | `(member_id, check_in_date)` | **Enforces strictly 1 visit per member per day** |

### 2.7 `payments` Table
Financial billing ledger of member remittances.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Payment entry ID |
| `payment_code`| `VARCHAR(30)` | `NOT NULL`, `UNIQUE` | Natural receipt code (`PAY-202610-001`) |
| `member_id` | `INT` | `NOT NULL`, `FK -> members(id)` | Remitting member |
| `membership_id`|`INT` | `NULL`, `FK -> memberships(id)` | Associated membership package |
| `amount` | `DECIMAL(10,2)`| `NOT NULL`, `CHECK(amount > 0)` | Monetary sum collected |
| `payment_date`| `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Date and time collected |
| `payment_method`|`ENUM` | `CASH`, `UPI`, `CARD`, `BANK_TRANSFER` | Payment channel |
| `payment_status`|`ENUM` | `SUCCESS`, `PENDING`, `FAILED`, `REFUNDED`| Settlement status |
| `transaction_reference`|`VARCHAR(100)`| `NULL`, `UNIQUE` | External auth/UPI/Wire ref |
| `receipt_notes`|`TEXT` | `NULL` | Cashier memo |

### 2.8 `audit_logs` Table
Tamper-evident system activity and administrative audit trail.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `INT` | `AUTO_INCREMENT`, `PRIMARY KEY` | Audit entry ID |
| `user_id` | `INT` | `NULL`, `FK -> users(id)` | Actor staff user ID |
| `username` | `VARCHAR(50)` | `NOT NULL` | Snapshot of username |
| `action` | `VARCHAR(50)` | `NOT NULL` | Action code (`CREATE_MEMBER`, `RENEW`, etc.) |
| `entity_type` | `VARCHAR(50)` | `NOT NULL` | Target table (`MEMBER`, `PAYMENT`, etc.) |
| `entity_id` | `VARCHAR(50)` | `NULL` | Target record ID |
| `details` | `TEXT` | `NULL` | Descriptive audit details |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Timestamp of event |

---

## 3. Business Rules & Conventions

### 3.1 Inclusive End-Date Convention
FitCore Pro strictly uses the **inclusive end-date convention**:
$$\text{EndDate} = \text{StartDate} + \text{DurationInDays} - 1$$
- **Example**: A 30-day plan starting on `2026-10-01` begins on Day 1 (`2026-10-01`) and concludes on Day 30 (`2026-10-30`). The member is eligible to check in through 23:59:59 of October 30.
- When renewing seamlessly, the renewal begins on $\text{EndDate} + 1$ (`2026-10-31`), preventing coverage gaps or double billing.

### 3.2 Single Daily Check-in Rule
Enforced at two complementary levels:
1. **Application Layer**: `AttendanceService` checks `hasCheckedInToday(memberId, today)` and returns a user-friendly message.
2. **Database Engine Layer**: The `UNIQUE KEY (member_id, check_in_date)` constraint guarantees that even simultaneous race conditions cannot insert duplicate daily attendance entries.

### 3.3 Historical Data Preservation
Deleting records from `members` or `membership_plans` is prevented via `ON DELETE RESTRICT` on foreign keys. Instead, FitCore Pro implements **soft deactivation** (`status = 'INACTIVE'`). This guarantees that financial ledgers, audit logs, and attendance histories remain valid and immutable.
