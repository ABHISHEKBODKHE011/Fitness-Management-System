-- ============================================================================
-- FitCore Pro - Seed Data (Fictitious Test Records)
-- Passwords:
--   admin         -> Admin@123
--   reception     -> Reception@123
--   trainer_mike  -> Trainer@123
-- ============================================================================

USE `fitcore_pro`;

-- 1. System Users
INSERT INTO `users` (`id`, `username`, `password_hash`, `full_name`, `email`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$Q1GWrP.3ojlnn2MWHFY2Z.Jjqt6H/4Xq4BNWJCFUj/bGpJGEQzkj.', 'Alexander Wright', 'admin@fitcorepro.com', 'ADMIN', 'ACTIVE'),
(2, 'reception', '$2a$10$3CQgxtviuW1CoBSO2DgaH.Eczizl9zQemnjJyQow5ZA9qm5axZCwe', 'Emily Watson', 'reception@fitcorepro.com', 'RECEPTIONIST', 'ACTIVE'),
(3, 'trainer_mike', '$2a$10$ZylkoyCktHWu6wInJr.oiuKqYLIAsRjvccWjRfWOqDaA9d2XDK67u', 'Michael Ross', 'mike.ross@fitcorepro.com', 'TRAINER', 'ACTIVE')
ON DUPLICATE KEY UPDATE `password_hash` = VALUES(`password_hash`), `full_name` = VALUES(`full_name`);

-- 2. Certified Trainers
INSERT INTO `trainers` (`id`, `user_id`, `trainer_code`, `name`, `phone`, `email`, `specialization`, `status`) VALUES
(1, 3, 'TRN-001', 'Michael Ross', '+1-555-0191', 'mike.ross@fitcorepro.com', 'CrossFit & Functional Strength', 'ACTIVE'),
(2, NULL, 'TRN-002', 'Sarah Connor', '+1-555-0192', 'sarah.connor@fitcorepro.com', 'HIIT & Cardiovascular Conditioning', 'ACTIVE'),
(3, NULL, 'TRN-003', 'David Vance', '+1-555-0193', 'david.vance@fitcorepro.com', 'Powerlifting & Rehabilitation', 'ACTIVE'),
(4, NULL, 'TRN-004', 'Elena Rostova', '+1-555-0194', 'elena.rostova@fitcorepro.com', 'Yoga & Core Flexibility', 'ACTIVE')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 3. Membership Plans
INSERT INTO `membership_plans` (`id`, `plan_code`, `name`, `duration_days`, `fee`, `description`, `status`) VALUES
(1, 'PLN-M01', 'Monthly Basic', 30, 49.99, 'Access to all standard gym floor equipment and locker facilities.', 'ACTIVE'),
(2, 'PLN-Q03', 'Quarterly Standard', 90, 129.99, 'Standard gym access plus 2 complimentary fitness assessment sessions.', 'ACTIVE'),
(3, 'PLN-S06', 'Semi-Annual Elite', 180, 239.99, 'Complete gym access, steam bath, pool, and 5 personal training vouchers.', 'ACTIVE'),
(4, 'PLN-Y12', 'Annual VIP Ultimate', 365, 429.99, 'All-inclusive 24/7 access, VIP lounge, dedicated trainer, and nutrition consultation.', 'ACTIVE'),
(5, 'PLN-D01', 'Daily Trial Pass', 1, 15.00, 'Single-day full access pass for prospective regular members.', 'INACTIVE')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 4. Members
INSERT INTO `members` (`id`, `member_code`, `full_name`, `phone`, `email`, `gender`, `emergency_contact`, `joining_date`, `assigned_trainer_id`, `status`) VALUES
(1, 'MBR-1001', 'Johnathan Davis', '+1-555-1101', 'jdavis@example.com', 'Male', 'Sarah Davis (+1-555-9001)', '2026-01-15', 1, 'ACTIVE'),
(2, 'MBR-1002', 'Sophia Martinez', '+1-555-1102', 'smartinez@example.com', 'Female', 'Carlos Martinez (+1-555-9002)', '2026-02-01', 2, 'ACTIVE'),
(3, 'MBR-1003', 'Liam Anderson', '+1-555-1103', 'landerson@example.com', 'Male', 'Karen Anderson (+1-555-9003)', '2026-03-10', 3, 'ACTIVE'),
(4, 'MBR-1004', 'Olivia Taylor', '+1-555-1104', 'otaylor@example.com', 'Female', 'Lucas Taylor (+1-555-9004)', '2026-05-18', 4, 'ACTIVE'),
(5, 'MBR-1005', 'Ethan Bennett', '+1-555-1105', 'ebennett@example.com', 'Male', 'Rachel Bennett (+1-555-9005)', '2026-06-01', 1, 'ACTIVE'),
(6, 'MBR-1006', 'Ava Jenkins', '+1-555-1106', 'ajenkins@example.com', 'Female', 'Mark Jenkins (+1-555-9006)', '2026-07-15', 2, 'ACTIVE'),
(7, 'MBR-1007', 'Noah Campbell', '+1-555-1107', 'ncampbell@example.com', 'Male', 'Jessica Campbell (+1-555-9007)', '2026-08-01', NULL, 'ACTIVE'),
(8, 'MBR-1008', 'Mia Robinson', '+1-555-1108', 'mrobinson@example.com', 'Female', 'Daniel Robinson (+1-555-9008)', '2025-11-10', NULL, 'INACTIVE')
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- 5. Trainer Assignments
INSERT INTO `trainer_assignments` (`id`, `member_id`, `trainer_id`, `assigned_date`, `unassigned_date`, `notes`) VALUES
(1, 1, 1, '2026-01-15', NULL, 'Strength conditioning program'),
(2, 2, 2, '2026-02-01', NULL, 'Cardio and weight management'),
(3, 3, 3, '2026-03-10', NULL, 'Powerlifting posture guidance'),
(4, 4, 4, '2026-05-18', NULL, 'Flexibility and recovery routines'),
(5, 5, 1, '2026-06-01', NULL, 'Hypertrophy target training'),
(6, 6, 2, '2026-07-15', NULL, 'Interval endurance track')
ON DUPLICATE KEY UPDATE `notes` = VALUES(`notes`);

-- 6. Memberships
-- Current simulated reference date is October 2026
INSERT INTO `memberships` (`id`, `membership_code`, `member_id`, `plan_id`, `start_date`, `end_date`, `agreed_fee`, `status`, `renewed_from_id`, `notes`) VALUES
(1, 'MS-202601-001', 1, 4, '2026-01-15', '2027-01-14', 429.99, 'ACTIVE', NULL, 'Annual VIP membership'),
(2, 'MS-202608-002', 2, 2, '2026-08-01', '2026-10-29', 129.99, 'ACTIVE', NULL, 'Quarterly pass - expiring soon'),
(3, 'MS-202609-003', 3, 1, '2026-09-20', '2026-10-19', 49.99, 'ACTIVE', NULL, 'Monthly subscription - expiring soon'),
(4, 'MS-202605-004', 4, 3, '2026-05-18', '2026-11-13', 239.99, 'ACTIVE', NULL, 'Semi-annual package'),
(5, 'MS-202609-005', 5, 2, '2026-09-01', '2026-11-29', 129.99, 'ACTIVE', NULL, 'Quarterly plan renewal'),
(6, 'MS-202607-006', 6, 1, '2026-07-15', '2026-08-13', 49.99, 'EXPIRED', NULL, 'First month completed'),
(7, 'MS-202608-007', 6, 1, '2026-08-14', '2026-09-12', 49.99, 'EXPIRED', 6, 'Second month renewal'),
(8, 'MS-202609-008', 6, 2, '2026-09-13', '2026-12-11', 129.99, 'ACTIVE', 7, 'Upgraded to quarterly plan'),
(9, 'MS-202610-009', 7, 1, '2026-10-01', '2026-10-30', 49.99, 'ACTIVE', NULL, 'Current month subscription'),
(10, 'MS-202511-010', 8, 2, '2025-11-10', '2026-02-07', 129.99, 'EXPIRED', NULL, 'Past inactive membership')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 7. Attendance Logs
INSERT INTO `attendance` (`id`, `member_id`, `check_in_time`, `check_in_date`, `notes`) VALUES
(1, 1, '2026-10-17 06:15:20', '2026-10-17', 'Morning workout session'),
(2, 2, '2026-10-17 07:05:10', '2026-10-17', 'Cardio studio check-in'),
(3, 3, '2026-10-17 08:30:45', '2026-10-17', 'Heavy weights area'),
(4, 5, '2026-10-17 09:12:00', '2026-10-17', 'Functional training check-in'),
(5, 6, '2026-10-17 09:45:15', '2026-10-17', 'Treadmill session'),
(6, 1, '2026-10-16 06:20:00', '2026-10-16', 'Leg day'),
(7, 2, '2026-10-16 07:10:00', '2026-10-16', 'HIIT session'),
(8, 4, '2026-10-16 17:30:00', '2026-10-16', 'Evening yoga session'),
(9, 7, '2026-10-16 18:45:00', '2026-10-16', 'Evening general access')
ON DUPLICATE KEY UPDATE `notes` = VALUES(`notes`);

-- 8. Payments Ledger
INSERT INTO `payments` (`id`, `payment_code`, `member_id`, `membership_id`, `amount`, `payment_date`, `payment_method`, `payment_status`, `transaction_reference`, `receipt_notes`) VALUES
(1, 'PAY-202601-001', 1, 1, 429.99, '2026-01-15 10:15:00', 'CARD', 'SUCCESS', 'TXN-CARD-99101', 'Annual VIP full payment via Visa'),
(2, 'PAY-202608-002', 2, 2, 129.99, '2026-08-01 11:20:00', 'UPI', 'SUCCESS', 'UPI-REF-88202', 'Quarterly fee via Google Pay'),
(3, 'PAY-202609-003', 3, 3, 49.99, '2026-09-20 14:05:00', 'CASH', 'SUCCESS', 'CASH-REC-00103', 'Monthly fee received at counter by Emily'),
(4, 'PAY-202605-004', 4, 4, 239.99, '2026-05-18 16:45:00', 'BANK_TRANSFER', 'SUCCESS', 'WIRE-ACH-77404', 'Semi-annual direct bank deposit verified'),
(5, 'PAY-202609-005', 5, 5, 129.99, '2026-09-01 09:30:00', 'UPI', 'SUCCESS', 'UPI-REF-55505', 'Quarterly renewal via PhonePe'),
(6, 'PAY-202607-006', 6, 6, 49.99, '2026-07-15 12:00:00', 'CASH', 'SUCCESS', 'CASH-REC-00106', 'First month cash payment'),
(7, 'PAY-202608-007', 6, 7, 49.99, '2026-08-14 15:10:00', 'CASH', 'SUCCESS', 'CASH-REC-00107', 'Second month cash payment'),
(8, 'PAY-202609-008', 6, 8, 129.99, '2026-09-13 10:00:00', 'CARD', 'SUCCESS', 'TXN-CARD-44808', 'Quarterly upgrade via Mastercard'),
(9, 'PAY-202610-009', 7, 9, 49.99, '2026-10-01 17:20:00', 'UPI', 'SUCCESS', 'UPI-REF-33709', 'Monthly membership UPI transaction')
ON DUPLICATE KEY UPDATE `amount` = VALUES(`amount`);

-- 9. Initial Audit Trail
INSERT INTO `audit_logs` (`id`, `user_id`, `username`, `action`, `entity_type`, `entity_id`, `details`, `created_at`) VALUES
(1, 1, 'admin', 'SYSTEM_INIT', 'SYSTEM', '1', 'Initial database schema and bootstrap configuration established.', '2026-01-01 00:00:00'),
(2, 2, 'reception', 'CREATE_MEMBER', 'MEMBER', '1', 'Registered member MBR-1001 (Johnathan Davis)', '2026-01-15 10:10:00'),
(3, 2, 'reception', 'RECORD_PAYMENT', 'PAYMENT', '1', 'Processed payment PAY-202601-001 ($429.99) for Johnathan Davis', '2026-01-15 10:15:00'),
(4, 2, 'reception', 'CHECK_IN', 'ATTENDANCE', '1', 'Checked in member MBR-1001 for daily workout', '2026-10-17 06:15:20')
ON DUPLICATE KEY UPDATE `action` = VALUES(`action`);
