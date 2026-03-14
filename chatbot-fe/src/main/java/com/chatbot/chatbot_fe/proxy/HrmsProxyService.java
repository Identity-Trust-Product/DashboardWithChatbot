package com.chatbot.chatbot_fe.proxy;



import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * ====================================================================
 *  HrmsProxyService  —  FE → BE communication layer
 *
 *  All calls go to:  http://localhost:9090/api/...
 *  All return types: Map<String,Object> or List<Map<String,Object>>
 *  Zero POJOs. If BE adds a new field, FE gets it automatically.
 *
 *  BE base URL is configurable:  be.base.url=http://localhost:9090
 * ====================================================================
 */
@Service
public class HrmsProxyService {

    private static final Logger log = LoggerFactory.getLogger(HrmsProxyService.class);

    @Value("${be.base.url:http://localhost:9090}")
    private String beBaseUrl;

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper  mapper = new ObjectMapper();

    // ── Reusable JSON headers ─────────────────────────────────────────────────
    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    // =========================================================================
    //  GENERIC GET  →  Map<String, Object>
    // =========================================================================
    private Map<String, Object> getMap(String path) {
        try {
            String url = beBaseUrl + path;
            log.debug("GET {}", url);
            ResponseEntity<Map<String, Object>> resp = rest.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            log.error("GET {} failed: {}", path, e.getMessage());
            return Collections.emptyMap();
        }
    }

    // =========================================================================
    //  GENERIC GET  →  List<Map<String, Object>>
    // =========================================================================
    private List<Map<String, Object>> getList(String path) {
        try {
            String url = beBaseUrl + path;
            log.debug("GET {}", url);
            ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("GET {} failed: {}", path, e.getMessage());
            return Collections.emptyList();
        }
    }

    // =========================================================================
    //  GENERIC POST  →  Map<String, Object>
    // =========================================================================
    private Map<String, Object> postMap(String path, Object body) {
        try {
            String url = beBaseUrl + path;
            log.debug("POST {}", url);
            HttpEntity<Object> req = new HttpEntity<>(body, jsonHeaders());
            ResponseEntity<Map<String, Object>> resp = rest.exchange(
                url, HttpMethod.POST, req,
                new ParameterizedTypeReference<Map<String, Object>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            log.error("POST {} failed: {}", path, e.getMessage());
            return Collections.emptyMap();
        }
    }

    // =========================================================================
    //  GENERIC POST  →  List<Map<String, Object>>
    // =========================================================================
    private List<Map<String, Object>> postList(String path, Object body) {
        try {
            String url = beBaseUrl + path;
            log.debug("POST {}", url);
            HttpEntity<Object> req = new HttpEntity<>(body, jsonHeaders());
            ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                url, HttpMethod.POST, req,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("POST {} failed: {}", path, e.getMessage());
            return Collections.emptyList();
        }
    }

    // =========================================================================
    //  GENERIC POST  →  String (for simple string responses)
    // =========================================================================
    private String postString(String path, Object body) {
        try {
            String url = beBaseUrl + path;
            HttpEntity<Object> req = new HttpEntity<>(body, jsonHeaders());
            return rest.postForObject(url, req, String.class);
        } catch (Exception e) {
            log.error("POST {} failed: {}", path, e.getMessage());
            return "error";
        }
    }

    // =========================================================================
    //  EMPLOYEE
    // =========================================================================

    public int countActiveEmployees() {
        Object v = getMap("/api/employees/count/active").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public int countAllEmployees() {
        Object v = getMap("/api/employees/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public int countOnProbation() {
        Object v = getMap("/api/employees/count/probation").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public int countConfirmedEmployees() {
        Object v = getMap("/api/employees/count/confirmed").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public List<Map<String, Object>> getAllEmployeesAsMap() {
        return getList("/api/employees/list");
    }

    public List<Map<String, Object>> employeesByDepartment() {
        return getList("/api/employees/by-department");
    }

    public List<Map<String, Object>> employeesByEmploymentType() {
        return getList("/api/employees/by-employment-type");
    }

    public List<Map<String, Object>> employeesByStatus() {
        return getList("/api/employees/by-status");
    }

    public List<Map<String, Object>> joiningTrendByYear() {
        return getList("/api/employees/joining-trend");
    }

    // =========================================================================
    //  DEPARTMENTS
    // =========================================================================

    public int countDepartments() {
        Object v = getMap("/api/departments/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public List<Map<String, Object>> getAllDepartments() {
        return getList("/api/departments/list");
    }

    // =========================================================================
    //  ATTENDANCE
    // =========================================================================

    public int countPresentToday() {
        Object v = getMap("/api/attendance/present-today/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public int countAbsentToday() {
        Object v = getMap("/api/attendance/absent-today/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public double attendanceRateToday() {
        Object v = getMap("/api/attendance/rate-today").get("value");
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    public double avgWorkingHours() {
        Object v = getMap("/api/attendance/avg-hours").get("value");
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    public List<Map<String, Object>> attendanceToday() {
        return getList("/api/attendance/today");
    }

    public List<Map<String, Object>> monthlyAttendanceTrend() {
        return getList("/api/attendance/monthly-trend");
    }

    // =========================================================================
    //  LEAVES
    // =========================================================================

    public int countPendingLeaves() {
        Object v = getMap("/api/leaves/pending/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public int countOnLeaveToday() {
        Object v = getMap("/api/leaves/on-leave-today/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public double leaveApprovalRate() {
        Object v = getMap("/api/leaves/approval-rate").get("value");
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    public List<Map<String, Object>> leavesByType() {
        return getList("/api/leaves/by-type");
    }

    public List<Map<String, Object>> pendingLeavesList() {
        return getList("/api/leaves/pending/list");
    }

    public List<Map<String, Object>> searchLeaveApplicationsAsMap(int empNo, String from, String to) {
        return getList("/api/leaves/search?empNo=" + empNo + "&fromDate=" + from + "&toDate=" + to);
    }

    // =========================================================================
    //  PAYROLL
    // =========================================================================

    public int countPendingPayroll() {
        Object v = getMap("/api/payroll/pending/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public double payrollProcessedRate() {
        Object v = getMap("/api/payroll/processed-rate").get("value");
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    public List<Map<String, Object>> monthlySalaryCost() {
        return getList("/api/payroll/monthly-cost");
    }

    public List<Map<String, Object>> recentPayroll() {
        return getList("/api/payroll/recent");
    }

    // =========================================================================
    //  PERFORMANCE
    // =========================================================================

    public double avgPerformanceRating() {
        Object v = getMap("/api/performance/avg-rating").get("value");
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    public double avgPerformancePercent() {
        Object v = getMap("/api/performance/avg-percent").get("value");
        return v == null ? 0.0 : Double.parseDouble(v.toString());
    }

    public List<Map<String, Object>> performanceList() {
        return getList("/api/performance/list");
    }

    // =========================================================================
    //  PROJECTS
    // =========================================================================

    public int countActiveProjects() {
        Object v = getMap("/api/projects/active/count").get("value");
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    public List<Map<String, Object>> activeProjectsList() {
        return getList("/api/projects/active/list");
    }

    public List<Map<String, Object>> projectAssignments() {
        return getList("/api/projects/assignments");
    }

    // =========================================================================
    //  USER CONFIG
    // =========================================================================

    public Map<String, Object> getUserConfigAsMap(String userId) {
        Map<String, Object> config = getMap("/api/user/config/" + userId);
        // Ensure defaults so FE never gets NullPointerException
        config.putIfAbsent("dashboard_data",    "{}");
        config.putIfAbsent("todo_list",          "[]");
        config.putIfAbsent("query_preference",   "{}");
        return config;
    }

    public int addUpdateUserDashboardConfigAsMap(Map<String, Object> config) {
        Map<String, Object> result = postMap("/api/user/config/save", config);
        Object success = result.get("success");
        return Boolean.TRUE.equals(success) ? 1 : 0;
    }

    public void addUpdateTaskAsMap(List<Map<String, Object>> tasks, String userId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user_id",   userId);
        payload.put("todo_list", tasks);
        postMap("/api/user/todo/save", payload);
    }

    public String saveFavQuery(String favJson, String userId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("user_id",         userId);
        payload.put("query_preference", favJson);
        Map<String, Object> result = postMap("/api/user/favorite/add", payload);
        return result.getOrDefault("message", "done").toString();
    }

    // =========================================================================
    //  DYNAMIC QUERY BUILDER
    // =========================================================================

    public List<Map<String, Object>> getUserGeneratedQueryOutput(String sql) {
        return postList("/api/query/run", Map.of("sql", sql));
    }

    public List<Map<String, Object>> getSchemaStructureAsMap(String schemaName) {
        return postList("/api/schema/structure", Map.of("schemaName", schemaName));
    }

    // =========================================================================
    //  CHATBOT
    // =========================================================================

    public Set<String> getSerivceWiseDataInFile(String moduleId) {
        try {
            String url = beBaseUrl + "/api/chatbot/modules?moduleId=" + moduleId;
            ResponseEntity<Set<String>> resp = rest.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Set<String>>() {});
            return resp.getBody() != null ? resp.getBody() : new LinkedHashSet<>();
        } catch (Exception e) {
            log.error("chatbot modules error: {}", e.getMessage());
            return new LinkedHashSet<>();
        }
    }

    public List<Map<String, Object>> getSelectedQuestionsAnswer(String question) {
        return getList("/api/chatbot/answer?question=" +
                java.net.URLEncoder.encode(question, java.nio.charset.StandardCharsets.UTF_8));
    }

    public List<Map<String, Object>> getAnswerByAskQuetionUsingRAG(String que) {
        return getList("/api/chatbot/rag?question=" +
                java.net.URLEncoder.encode(que, java.nio.charset.StandardCharsets.UTF_8));
    }
}
