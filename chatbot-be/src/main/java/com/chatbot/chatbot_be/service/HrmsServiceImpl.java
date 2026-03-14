package com.chatbot.chatbot_be.service;

import com.chatbot.chatbot_be.repository.HrmsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HrmsServiceImpl implements HrmsService {

    private static final Logger log = LoggerFactory.getLogger(HrmsServiceImpl.class);

    @Autowired
    private HrmsRepository repo;

    // ── Counts ────────────────────────────────────────────────────────────────
    @Override public int countActiveEmployees()    { return repo.countActiveEmployees(); }
    @Override public int countAllEmployees()       { return repo.countAllEmployees(); }
    @Override public int countOnProbation()        { return repo.countOnProbation(); }
    @Override public int countConfirmedEmployees() { return repo.countConfirmedEmployees(); }
    @Override public int countPresentToday()       { return repo.countPresentToday(); }
    @Override public int countAbsentToday()        { return repo.countAbsentToday(); }
    @Override public int countOnLeaveToday()       { return repo.countOnLeaveToday(); }
    @Override public int countPendingLeaves()      { return repo.countPendingLeaves(); }
    @Override public int countPendingPayroll()     { return repo.countPendingPayroll(); }
    @Override public int countActiveProjects()     { return repo.countActiveProjects(); }
    @Override public int countDepartments()        { return repo.countDepartments(); }

    // ── Percentages ───────────────────────────────────────────────────────────
    @Override public double attendanceRateToday()   { return repo.attendanceRateToday(); }
    @Override public double leaveApprovalRate()     { return repo.leaveApprovalRate(); }
    @Override public double payrollProcessedRate()  { return repo.payrollProcessedRate(); }
    @Override public double avgPerformancePercent() { return repo.avgPerformancePercent(); }
    @Override public double avgWorkingHours()       { return repo.avgWorkingHours(); }
    @Override public double avgPerformanceRating()  { return repo.avgPerformanceRating(); }

    // ── Lists ─────────────────────────────────────────────────────────────────
    @Override public List<Map<String, Object>> getAllEmployees()    { return repo.getAllEmployees(); }
    @Override public List<Map<String, Object>> attendanceToday()   { return repo.attendanceToday(); }
    @Override public List<Map<String, Object>> pendingLeavesList() { return repo.pendingLeavesList(); }
    @Override public List<Map<String, Object>> recentPayroll()     { return repo.recentPayroll(); }
    @Override public List<Map<String, Object>> activeProjectsList(){ return repo.activeProjectsList(); }
    @Override public List<Map<String, Object>> projectAssignments(){ return repo.projectAssignments(); }
    @Override public List<Map<String, Object>> performanceList()   { return repo.performanceList(); }
    @Override public List<Map<String, Object>> getAllDepartments() { return repo.getAllDepartments(); }
    @Override public List<Map<String, Object>> getAllUsers()        { return repo.getAllUsers(); }

    @Override
    public List<Map<String, Object>> searchLeaveApplications(int empNo, String fromDate, String toDate) {
        return repo.searchLeaveApplications(empNo, fromDate, toDate);
    }

    // ── Graph data ────────────────────────────────────────────────────────────
    @Override public List<Map<String, Object>> employeesByDepartment()     { return repo.employeesByDepartment(); }
    @Override public List<Map<String, Object>> employeesByEmploymentType() { return repo.employeesByEmploymentType(); }
    @Override public List<Map<String, Object>> employeesByStatus()         { return repo.employeesByStatus(); }
    @Override public List<Map<String, Object>> joiningTrendByYear()        { return repo.joiningTrendByYear(); }
    @Override public List<Map<String, Object>> monthlyAttendanceTrend()    { return repo.monthlyAttendanceTrend(); }
    @Override public List<Map<String, Object>> monthlySalaryCost()         { return repo.monthlySalaryCost(); }
    @Override public List<Map<String, Object>> leavesByType()              { return repo.leavesByType(); }

    // ── User config ───────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getUserConfig(String userId) {
        Map<String, Object> config = repo.getUserConfig(userId);
        // Ensure all keys always present to avoid null checks in controller
        config.putIfAbsent("dashboard_data", "{}");
        config.putIfAbsent("todo_list",       "[]");
        config.putIfAbsent("query_preference","{}");
        return config;
    }

    @Override
    public int saveUserConfig(Map<String, Object> config) {
        return repo.saveUserConfig(config);
    }

    // ── Dynamic query ─────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> runDynamicQuery(String sql) {
        log.info("Running dynamic query: {}", sql);
        return repo.runDynamicQuery(sql);
    }

    @Override
    public List<Map<String, Object>> getSchemaStructure(String schemaName) {
        log.info("getSchemaStructure: {}", schemaName);
        return repo.getSchemaStructure(schemaName);
    }
}