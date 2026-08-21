package com.chatbot.chatbot_be.service;

import java.util.List;
import java.util.Map;

public interface HrmsService {

    // ── Counts ────────────────────────────────────────────────────────────────
    int countActiveEmployees();
    int countAllEmployees();
    int countOnProbation();
    int countConfirmedEmployees();
    int countPresentToday();
    int countAbsentToday();
    int countOnLeaveToday();
    int countPendingLeaves();
    int countPendingPayroll();
    int countActiveProjects();
    int countDepartments();

    // ── Percentages ───────────────────────────────────────────────────────────
    double attendanceRateToday();
    double leaveApprovalRate();
    double payrollProcessedRate();
    double avgPerformancePercent();
    double avgWorkingHours();
    double avgPerformanceRating();

    // ── Lists ─────────────────────────────────────────────────────────────────
    List<Map<String, Object>> getAllEmployees();
    List<Map<String, Object>> attendanceToday();
    List<Map<String, Object>> pendingLeavesList();
    List<Map<String, Object>> recentPayroll();
    List<Map<String, Object>> activeProjectsList();
    List<Map<String, Object>> projectAssignments();
    List<Map<String, Object>> performanceList();
    List<Map<String, Object>> getAllDepartments();
    List<Map<String, Object>> getAllUsers();
    Map<String, Object> authenticateUser(String username, String password);
    int registerUser(String email, String username, String password);
    List<Map<String, Object>> searchLeaveApplications(int empNo, String fromDate, String toDate);

    // ── Graph data ────────────────────────────────────────────────────────────
    List<Map<String, Object>> employeesByDepartment();
    List<Map<String, Object>> employeesByEmploymentType();
    List<Map<String, Object>> employeesByStatus();
    List<Map<String, Object>> joiningTrendByYear();
    List<Map<String, Object>> monthlyAttendanceTrend();
    List<Map<String, Object>> monthlySalaryCost();
    List<Map<String, Object>> leavesByType();
    List<Map<String, Object>> employeeListByMaritalStatus();
    List<Map<String, Object>> employeesBySalary();
    

    // ── User config ───────────────────────────────────────────────────────────
    Map<String, Object> getUserConfig(String userId);
    int saveUserConfig(Map<String, Object> config);

    // ── Dynamic query ─────────────────────────────────────────────────────────
    List<Map<String, Object>> runDynamicQuery(String sql);
    List<Map<String, Object>> getSchemaStructure(String schemaName);
}
