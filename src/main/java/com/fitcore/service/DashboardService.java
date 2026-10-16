package com.fitcore.service;

import com.fitcore.dao.DashboardDao;
import com.fitcore.model.DashboardMetrics;

/**
 * Service aggregating high-level real metrics and trends for the visual dashboard.
 */
public class DashboardService {

    private final DashboardDao dashboardDao;

    public DashboardService() {
        this(new DashboardDao());
    }

    public DashboardService(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public DashboardMetrics getDashboardMetrics() {
        return dashboardDao.getMetrics();
    }
}
