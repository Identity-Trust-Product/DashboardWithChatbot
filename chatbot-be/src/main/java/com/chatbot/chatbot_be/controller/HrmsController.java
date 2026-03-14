package com.chatbot.chatbot_be.controller;

import com.chatbot.chatbot_be.service.HrmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ====================================================================
 *  HrmsController  —  Backend REST API  (port 9090)
 *
 *  All endpoints return Map<String,Object> or List<Map<String,Object>>.
 *  Zero POJOs. FE at port 9091 calls these via HrmsProxyService.
 * ====================================================================
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:9091")
public class HrmsController {

    private static final Logger log = LoggerFactory.getLogger(HrmsController.class);

    @Autowired
    private HrmsService svc;

    // =========================================================================
    //  EMPLOYEE — COUNT endpoints
    // =========================================================================

    @GetMapping("/employees/count")
    public Map<String, Object> totalEmployees() {
        return Map.of("value", svc.countAllEmployees());
    }

    @GetMapping("/employees/count/active")
    public Map<String, Object> activeEmployees() {
        return Map.of("value", svc.countActiveEmployees());
    }

    @GetMapping("/employees/count/probation")
    public Map<String, Object> onProbation() {
        return Map.of("value", svc.countOnProbation());
    }

    @GetMapping("/employees/count/confirmed")
    public Map<String, Object> confirmed() {
        return Map.of("value", svc.countConfirmedEmployees());
    }

    // =========================================================================
    //  EMPLOYEE — LIST + GRAPH endpoints
    // =========================================================================

    @GetMapping("/employees/list")
    public List<Map<String, Object>> employeeList() {
        log.info("GET /api/employees/list");
        return svc.getAllEmployees();
    }

    @GetMapping("/employees/by-department")
    public List<Map<String, Object>> byDepartment() {
        return svc.employeesByDepartment();
    }

    @GetMapping("/employees/by-employment-type")
    public List<Map<String, Object>> byEmploymentType() {
        return svc.employeesByEmploymentType();
    }

    @GetMapping("/employees/by-status")
    public List<Map<String, Object>> byStatus() {
        return svc.employeesByStatus();
    }

    @GetMapping("/employees/joining-trend")
    public List<Map<String, Object>> joiningTrend() {
        return svc.joiningTrendByYear();
    }

    // =========================================================================
    //  DEPARTMENTS
    // =========================================================================

    @GetMapping("/departments/count")
    public Map<String, Object> deptCount() {
        return Map.of("value", svc.countDepartments());
    }

    @GetMapping("/departments/list")
    public List<Map<String, Object>> deptList() {
        return svc.getAllDepartments();
    }

    // =========================================================================
    //  ATTENDANCE
    // =========================================================================

    @GetMapping("/attendance/present-today/count")
    public Map<String, Object> presentToday() {
        return Map.of("value", svc.countPresentToday());
    }

    @GetMapping("/attendance/absent-today/count")
    public Map<String, Object> absentToday() {
        return Map.of("value", svc.countAbsentToday());
    }

    @GetMapping("/attendance/rate-today")
    public Map<String, Object> attendanceRate() {
        return Map.of("value", svc.attendanceRateToday());
    }

    @GetMapping("/attendance/avg-hours")
    public Map<String, Object> avgHours() {
        return Map.of("value", svc.avgWorkingHours());
    }

    @GetMapping("/attendance/today")
    public List<Map<String, Object>> attendanceList() {
        log.info("GET /api/attendance/today");
        return svc.attendanceToday();
    }

    @GetMapping("/attendance/monthly-trend")
    public List<Map<String, Object>> monthlyTrend() {
        return svc.monthlyAttendanceTrend();
    }

    // =========================================================================
    //  LEAVES
    // =========================================================================

    @GetMapping("/leaves/pending/count")
    public Map<String, Object> pendingLeaves() {
        return Map.of("value", svc.countPendingLeaves());
    }

    @GetMapping("/leaves/on-leave-today/count")
    public Map<String, Object> onLeaveToday() {
        return Map.of("value", svc.countOnLeaveToday());
    }

    @GetMapping("/leaves/approval-rate")
    public Map<String, Object> leaveApprovalRate() {
        return Map.of("value", svc.leaveApprovalRate());
    }

    @GetMapping("/leaves/by-type")
    public List<Map<String, Object>> leavesByType() {
        return svc.leavesByType();
    }

    @GetMapping("/leaves/pending/list")
    public List<Map<String, Object>> pendingLeaveList() {
        return svc.pendingLeavesList();
    }

    @GetMapping("/leaves/search")
    public List<Map<String, Object>> searchLeaves(
            @RequestParam(defaultValue = "0")  int    empNo,
            @RequestParam(required = false)    String fromDate,
            @RequestParam(required = false)    String toDate) {

        String from = fromDate != null ? fromDate : java.time.LocalDate.now().toString();
        String to   = toDate   != null ? toDate   : java.time.LocalDate.now().toString();
        return svc.searchLeaveApplications(empNo, from, to);
    }

    // =========================================================================
    //  PAYROLL
    // =========================================================================

    @GetMapping("/payroll/pending/count")
    public Map<String, Object> pendingPayroll() {
        return Map.of("value", svc.countPendingPayroll());
    }

    @GetMapping("/payroll/processed-rate")
    public Map<String, Object> payrollRate() {
        return Map.of("value", svc.payrollProcessedRate());
    }

    @GetMapping("/payroll/monthly-cost")
    public List<Map<String, Object>> payrollCost() {
        return svc.monthlySalaryCost();
    }

    @GetMapping("/payroll/recent")
    public List<Map<String, Object>> recentPayroll() {
        log.info("GET /api/payroll/recent");
        return svc.recentPayroll();
    }

    // =========================================================================
    //  PERFORMANCE
    // =========================================================================

    @GetMapping("/performance/avg-rating")
    public Map<String, Object> avgRating() {
        return Map.of("value", svc.avgPerformanceRating());
    }

    @GetMapping("/performance/avg-percent")
    public Map<String, Object> avgPercent() {
        return Map.of("value", svc.avgPerformancePercent());
    }

    @GetMapping("/performance/list")
    public List<Map<String, Object>> performanceList() {
        return svc.performanceList();
    }

    // =========================================================================
    //  PROJECTS
    // =========================================================================

    @GetMapping("/projects/active/count")
    public Map<String, Object> activeProjects() {
        return Map.of("value", svc.countActiveProjects());
    }

    @GetMapping("/projects/active/list")
    public List<Map<String, Object>> projectList() {
        return svc.activeProjectsList();
    }

    @GetMapping("/projects/assignments")
    public List<Map<String, Object>> assignments() {
        return svc.projectAssignments();
    }

    // =========================================================================
    //  USER CONFIG (Dashboard preferences, todo, favorites)
    // =========================================================================

    @GetMapping("/user/config/{userId}")
    public Map<String, Object> getUserConfig(@PathVariable String userId) {
        log.info("GET /api/user/config/{}", userId);
        return svc.getUserConfig(userId);
    }

    @PostMapping("/user/config/save")
    public ResponseEntity<Map<String, Object>> saveUserConfig(
            @RequestBody Map<String, Object> config) {
        log.info("POST /api/user/config/save for user: {}", config.get("user_id"));
        int result = svc.saveUserConfig(config);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", result > 0);
        resp.put("message", result > 0 ? "Saved successfully" : "Save failed");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/user/todo/save")
    public ResponseEntity<Map<String, Object>> saveTodo(
            @RequestBody Map<String, Object> payload) {
        // payload: { user_id, todo_list: [...] }
        log.info("POST /api/user/todo/save for user: {}", payload.get("user_id"));
        int result = svc.saveUserConfig(payload);
        return ResponseEntity.ok(Map.of("success", result > 0));
    }

    @PostMapping("/user/favorite/add")
    public ResponseEntity<String> addFavorite(@RequestBody Map<String, Object> payload) {
        log.info("POST /api/user/favorite/add: {}", payload);
        // Favorites are stored inside query_preference JSON — handled by saveUserConfig
        int result = svc.saveUserConfig(payload);
        return ResponseEntity.ok(result > 0 ? "Favorite saved" : "Save failed");
    }

    @PostMapping("/user/favorite/remove")
    public ResponseEntity<String> removeFavorite(@RequestBody Map<String, Object> payload) {
        log.info("POST /api/user/favorite/remove: {}", payload);
        int result = svc.saveUserConfig(payload);
        return ResponseEntity.ok(result > 0 ? "Favorite removed" : "Remove failed");
    }

    // =========================================================================
    //  DYNAMIC QUERY BUILDER
    // =========================================================================

    @PostMapping("/query/run")
    public ResponseEntity<?> runQuery(@RequestBody Map<String, String> req) {
        try {
            log.info("POST /api/query/run sql: {}", req.get("sql"));
            List<Map<String, Object>> result = svc.runDynamicQuery(req.get("sql"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Query error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/schema/structure")
    public ResponseEntity<List<Map<String, Object>>> getSchema(
            @RequestBody Map<String, String> body) {
        try {
            String schema = body.getOrDefault("schemaName", "hrms");
            log.info("POST /api/schema/structure schema={}", schema);
            return ResponseEntity.ok(svc.getSchemaStructure(schema));
        } catch (Exception e) {
            log.error("Schema error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    // =========================================================================
    //  CHATBOT (Ollama stub — wire your prompts here)
    // =========================================================================

    @GetMapping("/chatbot/modules")
    public Set<String> chatbotModules(@RequestParam String moduleId) {
        // Return service names for the given module
        // TODO: Load from properties or DB
        Set<String> services = new LinkedHashSet<>();
        services.add("Employee Info");
        services.add("Attendance");
        services.add("Leave Status");
        services.add("Payroll");
        services.add("Performance");
        return services;
    }

    @GetMapping("/chatbot/answer")
    public List<Map<String, Object>> chatbotAnswer(@RequestParam String question) {
        // TODO: Connect to Ollama endpoint here
        // For testing, return a stub response
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("question", question);
        resp.put("answer",   "This feature is powered by Ollama AI. Integration coming soon.");
        resp.put("source",   "stub");
        return List.of(resp);
    }

    @GetMapping("/chatbot/rag")
    public List<Map<String, Object>> chatbotRag(@RequestParam String question) {
        // TODO: Connect to Ollama RAG endpoint
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("question", question);
        resp.put("answer",   "RAG-based answer: Integration with Ollama pending.");
        resp.put("source",   "rag-stub");
        return List.of(resp);
    }
}

