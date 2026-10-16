-- ============================================================================
-- FitCore Pro - Analytical & Reporting SQL Queries
-- ============================================================================

USE `fitcore_pro`;

-- 1. Dashboard KPI Summary Metrics
SELECT 
    (SELECT COUNT(*) FROM members) AS total_members,
    (SELECT COUNT(*) FROM members WHERE status = 'ACTIVE') AS active_members,
    (SELECT COUNT(*) FROM trainers WHERE status = 'ACTIVE') AS active_trainers,
    (SELECT COUNT(*) FROM attendance WHERE check_in_date = CURRENT_DATE()) AS today_attendance,
    (SELECT COUNT(*) FROM memberships WHERE status = 'ACTIVE') AS active_memberships,
    (SELECT COUNT(*) FROM memberships WHERE status = 'EXPIRED') AS expired_memberships,
    (SELECT COUNT(*) FROM memberships WHERE status = 'ACTIVE' AND end_date BETWEEN CURRENT_DATE() AND DATE_ADD(CURRENT_DATE(), INTERVAL 7 DAY)) AS expiring_soon_7d,
    (SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'SUCCESS') AS total_revenue_all_time,
    (SELECT COALESCE(SUM(amount), 0.00) FROM payments WHERE payment_status = 'SUCCESS' AND MONTH(payment_date) = MONTH(CURRENT_DATE()) AND YEAR(payment_date) = YEAR(CURRENT_DATE())) AS current_month_revenue;

-- 2. Monthly Revenue Trend (Last 6 Months)
SELECT 
    DATE_FORMAT(payment_date, '%Y-%m') AS month_period,
    DATE_FORMAT(payment_date, '%b %Y') AS month_label,
    COUNT(id) AS transaction_count,
    COALESCE(SUM(amount), 0.00) AS total_collected
FROM payments
WHERE payment_status = 'SUCCESS'
  AND payment_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 6 MONTH)
GROUP BY DATE_FORMAT(payment_date, '%Y-%m'), DATE_FORMAT(payment_date, '%b %Y')
ORDER BY month_period ASC;

-- 3. Membership Plan Distribution
SELECT 
    p.name AS plan_name,
    p.plan_code,
    COUNT(m.id) AS total_subscriptions,
    COALESCE(SUM(m.agreed_fee), 0.00) AS total_revenue_generated
FROM membership_plans p
LEFT JOIN memberships m ON p.id = m.plan_id
GROUP BY p.id, p.name, p.plan_code
ORDER BY total_subscriptions DESC;

-- 4. Memberships Expiring Soon (Next 7 to 14 Days)
SELECT 
    m.member_code,
    m.full_name AS member_name,
    m.phone,
    p.name AS plan_name,
    ms.start_date,
    ms.end_date,
    DATEDIFF(ms.end_date, CURRENT_DATE()) AS days_remaining
FROM memberships ms
JOIN members m ON ms.member_id = m.id
JOIN membership_plans p ON ms.plan_id = p.id
WHERE ms.status = 'ACTIVE'
  AND ms.end_date BETWEEN CURRENT_DATE() AND DATE_ADD(CURRENT_DATE(), INTERVAL 14 DAY)
ORDER BY ms.end_date ASC;

-- 5. Trainer Allocation & Workload Summary
SELECT 
    t.trainer_code,
    t.name AS trainer_name,
    t.specialization,
    t.status AS trainer_status,
    COUNT(m.id) AS assigned_member_count
FROM trainers t
LEFT JOIN members m ON t.id = m.assigned_trainer_id AND m.status = 'ACTIVE'
GROUP BY t.id, t.trainer_code, t.name, t.specialization, t.status
ORDER BY assigned_member_count DESC;

-- 6. Daily Attendance Summary for a Given Month
SELECT 
    a.check_in_date,
    COUNT(a.id) AS check_in_count
FROM attendance a
WHERE MONTH(a.check_in_date) = MONTH(CURRENT_DATE())
  AND YEAR(a.check_in_date) = YEAR(CURRENT_DATE())
GROUP BY a.check_in_date
ORDER BY a.check_in_date ASC;

-- 7. Payment Methods Breakdown
SELECT 
    payment_method,
    COUNT(id) AS payment_count,
    COALESCE(SUM(amount), 0.00) AS total_volume
FROM payments
WHERE payment_status = 'SUCCESS'
GROUP BY payment_method
ORDER BY total_volume DESC;
