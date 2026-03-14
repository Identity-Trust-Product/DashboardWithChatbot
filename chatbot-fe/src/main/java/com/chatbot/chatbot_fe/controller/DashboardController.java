package com.chatbot.chatbot_fe.controller;


import com.chatbot.chatbot_fe.proxy.HrmsProxyService;
import com.chatbot.chatbot_fe.service.DashboardService;
import com.chatbot.chatbot_fe.service.UserInfoService;
import com.chatbot.chatbot_fe.service.DashboardInvoker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.Supplier;

/**
 * ====================================================================
 *  DashboardController  —  FE (port 9091)
 *  DB: HRMS PostgreSQL schema (employees, attendance, leaves, payroll,
 *      performance, projects, departments, users, roles)
 *  BE: localhost:9090 via HrmsProxyService
 *
 *  ZERO POJOs — everything is Map<String, Object>
 *  If DB changes column names → update application.properties only
 * ====================================================================
 */
@Controller
public class DashboardController {

    private static final Logger log    = LoggerFactory.getLogger(DashboardController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ── Column config (@Value — change in application.properties only) ────────
    @Value("${col.emp.id:id}")                    private String colEmpId;
    @Value("${col.emp.code:employee_code}")        private String colEmpCode;
    @Value("${col.emp.firstName:first_name}")      private String colEmpFirst;
    @Value("${col.emp.lastName:last_name}")        private String colEmpLast;
    @Value("${col.emp.email:email}")               private String colEmpEmail;
    @Value("${col.emp.phone:phone}")               private String colEmpPhone;
    @Value("${col.emp.dob:date_of_birth}")         private String colEmpDob;
    @Value("${col.emp.gender:gender}")             private String colEmpGender;
    @Value("${col.emp.department:department_name}")private String colEmpDept;
    @Value("${col.emp.designation:designation}")   private String colEmpDesig;
    @Value("${col.emp.empType:employment_type}")   private String colEmpType;
    @Value("${col.emp.joinDate:joining_date}")     private String colEmpJoinDate;
    @Value("${col.emp.probationEnd:probation_end_date}") private String colEmpProbEnd;
    @Value("${col.emp.status:status}")             private String colEmpStatus;
    @Value("${col.emp.salary:basic_salary}")       private String colEmpSalary;

    @Value("${col.leave.status:status}")           private String colLeaveStatus;
    @Value("${col.leave.type:leave_type}")         private String colLeaveType;

    @Value("${col.userconfig.dashboardData:dashboardData}")  private String colDashboardData;
    @Value("${col.userconfig.todoList:todoList}")             private String colTodoList;
    @Value("${col.userconfig.queryPref:queryPreference}")     private String colQueryPref;

    @Value("${col.module.name:moduleName}")        private String colModuleName;

    // ── Services ──────────────────────────────────────────────────────────────
    @Autowired private DashboardService  dashboard;
    @Autowired private UserInfoService   userInfo;
    @Autowired private DashboardInvoker  dashboardInvoker;
    @Autowired private HrmsProxyService  proxy;

    // =========================================================================
    //  STATIC HELPERS
    // =========================================================================

    private static Map<String, Object> metric(String key, String type, Object value, String url) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key",   key);
        m.put("type",  type);
        m.put("value", value);
        m.put("url",   url);
        return m;
    }

    private static Object get(Map<String, Object> map, String key) {
        return map == null ? null : map.get(key);
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = get(map, key);
        return v == null ? "" : v.toString();
    }

    public static Map<String, Object> loadModules(String serviceName) {
        try {
            Properties props = new Properties();
            props.load(new ClassPathResource(serviceName + ".properties").getInputStream());
            Map<String, Object> modules = new LinkedHashMap<>();
            for (String name : props.stringPropertyNames()) {
                String   value = props.getProperty(name);
                String[] parts = name.split("\\.");
                if (parts.length < 3) continue;
                String moduleKey = parts[1];
                @SuppressWarnings("unchecked")
                Map<String, Object> detail = (Map<String, Object>)
                        modules.computeIfAbsent(moduleKey, k -> new LinkedHashMap<>());
                if (parts[2].equals("description")) {
                    detail.put("description", value);
                } else if (parts[2].equals("services") && parts.length == 4) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> services = (Map<String, String>)
                            detail.computeIfAbsent("services", k -> new LinkedHashMap<>());
                    services.put(parts[3], value);
                }
            }
            return modules;
        } catch (IOException e) {
            log.warn("module.properties not found: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }


    // =========================================================================
    //
    //  GET /  —  MAIN DASHBOARD
    //
    //  Widget mapping for new HRMS DB:
    //  ──────────────────────────────────────────────────────────────────────
    //  count widgets:
    //    Total Employees, Present Today, On Leave Today, Absent Today,
    //    Pending Leaves, Active Projects, Departments, Pending Payroll
    //
    //  percentage widgets:
    //    Attendance Rate, Leave Approval Rate, Payroll Processed Rate, Avg Performance
    //
    //  speedometer widgets:
    //    Avg Performance Score, Avg Working Hours
    //
    //  graph widgets:
    //    Employees by Department, Employees by Employment Type,
    //    Monthly Attendance Trend, Monthly Payroll Cost, Leaves by Type, Joining Trend
    //
    //  list widgets:
    //    Employee Directory, Pending Leaves, Today Attendance,
    //    Recent Payroll, Active Projects, Performance Reviews
    //
    //  todo + favorites: stored in user config
    // =========================================================================
    @GetMapping("/")
    public String getEmployeeDashboardData(Model model) throws Exception {

        List<Map<String, Object>> metrics = new ArrayList<>();
        String userId = userInfo.getUser();
        log.info("userId : {}", userId);

        // ── 1. Load saved widget selections ───────────────────────────────────
        Map<String, Object> userConfig = dashboard.getUserConfigAsMap(userId);
        log.info("userConfig : {}", userConfig);
        try {
           // List<Map<String, Object>> savedMetrics = fetchDynamicUrl(str(userConfig, colDashboardData));
           Map<String,Object> dashboardMap =(Map<String,Object>) userConfig.get("dashboard_data");

        String json = "";

        if(dashboardMap != null){
            json = (String) dashboardMap.get("value");
        }

        List<Map<String, Object>> savedMetrics =fetchDynamicUrl(json);
            metrics.addAll(savedMetrics);
        } catch (Exception ex) {
            log.error("Error loading saved config: {}", ex);
        }

        // ── 2. COUNT widgets — instant KPIs ───────────────────────────────────
        // metrics.add(metric("Total Employees",     "count", proxy.countAllEmployees(),       "/emp/count/all"));
        // metrics.add(metric("Present Today",       "count", proxy.countPresentToday(),       "/attendance/present-today/count"));
        // metrics.add(metric("On Leave Today",      "count", proxy.countOnLeaveToday(),       "/leaves/on-leave-today/count"));
        // metrics.add(metric("Absent Today",        "count", proxy.countAbsentToday(),        "/attendance/absent-today/count"));
        // metrics.add(metric("Pending Leave Requests","count",proxy.countPendingLeaves(),     "/leaves/pending/count"));
        // metrics.add(metric("Active Projects",     "count", proxy.countActiveProjects(),     "/projects/active/count"));
        // metrics.add(metric("Departments",         "count", proxy.countDepartments(),        "/departments/count"));
        // metrics.add(metric("Pending Payroll",     "count", proxy.countPendingPayroll(),     "/payroll/pending/count"));

        // ── 3. PERCENTAGE widgets ─────────────────────────────────────────────
        // metrics.add(metric("Attendance Rate",         "percentage", proxy.attendanceRateToday(),  "/attendance/rate-today"));
        // metrics.add(metric("Leave Approval Rate",     "percentage", proxy.leaveApprovalRate(),    "/leaves/approval-rate"));
        // metrics.add(metric("Payroll Processed",       "percentage", proxy.payrollProcessedRate(), "/payroll/processed-rate"));
        // metrics.add(metric("Avg Performance Score",   "percentage", proxy.avgPerformancePercent(),"/performance/avg-percent"));

        // ── 4. SPEEDOMETER widgets ────────────────────────────────────────────
        // metrics.add(metric("Avg Performance Rating",  "speedometer", proxy.avgPerformanceRating(), "/performance/avg-rating"));
        // metrics.add(metric("Avg Daily Working Hours", "speedometer", proxy.avgWorkingHours(),       "/attendance/avg-hours"));

        // ── 5. GRAPH widgets ──────────────────────────────────────────────────
        // metrics.add(metric("Employees by Department",    "graph", proxy.employeesByDepartment(),     "/employees/by-department"));
        // metrics.add(metric("Employees by Type",          "graph", proxy.employeesByEmploymentType(), "/employees/by-employment-type"));
        // metrics.add(metric("Monthly Attendance Trend",   "graph", proxy.monthlyAttendanceTrend(),    "/attendance/monthly-trend"));
        // metrics.add(metric("Monthly Payroll Cost",       "graph", proxy.monthlySalaryCost(),         "/payroll/monthly-cost"));
        // metrics.add(metric("Leaves by Type",             "graph", proxy.leavesByType(),              "/leaves/by-type"));
        // metrics.add(metric("Employee Joining Trend",     "graph", proxy.joiningTrendByYear(),        "/employees/joining-trend"));

        // ── 6. LIST widgets ───────────────────────────────────────────────────
        // metrics.add(metric("Employee Directory",          "list", proxy.getAllEmployeesAsMap(),   "/get/employee-list"));
        // metrics.add(metric("Pending Leave Requests",      "list", proxy.pendingLeavesList(),      "/leaves/pending/list"));
        // metrics.add(metric("Today Attendance",            "list", proxy.attendanceToday(),        "/attendance/today"));
        // metrics.add(metric("Recent Payroll",              "list", proxy.recentPayroll(),          "/payroll/recent"));
        // metrics.add(metric("Active Projects",             "list", proxy.activeProjectsList(),     "/projects/active/list"));
        // metrics.add(metric("Performance Reviews",         "list", proxy.performanceList(),        "/performance/list"));

        // ── 7. TODO list ──────────────────────────────────────────────────────
        try {
            String todoRaw = str(userConfig, colTodoList);
            List<Map<String, Object>> todoList = MAPPER.readValue(
                    todoRaw.isEmpty() ? "[]" : todoRaw,
                    new TypeReference<List<Map<String, Object>>>() {});
            normalizeDates(todoList, "createdDate", "created_date");
            metrics.add(metric("To-Do List", "todo", todoList, "/dashboard/todo"));
        } catch (Exception ex) {
            log.error("todoList parse error: {}", ex.getMessage());
            metrics.add(metric("To-Do List", "todo", new ArrayList<>(), "/dashboard/todo"));
        }

        // ── 8. Query Favorites ────────────────────────────────────────────────
        try {
            ObjectMapper m2 = new ObjectMapper();
            m2.registerModule(new JavaTimeModule());
            String prefRaw = str(userConfig, colQueryPref);
            JsonNode root  = m2.readTree(prefRaw.isEmpty() ? "{}" : prefRaw);
            JsonNode favs  = root.get("favorites");
            List<Map<String, Object>> favorites = (favs != null && favs.isArray())
                    ? m2.convertValue(favs, new TypeReference<List<Map<String, Object>>>() {})
                    : Collections.emptyList();
            metrics.add(metric("favorites", "favorites", favorites, "/queryPreference"));
        } catch (Exception ex) {
            log.error("queryPreference parse error: {}", ex.getMessage());
        }

        // ── 9. Serialize + model ──────────────────────────────────────────────
        log.info("metrics count: {}", metrics.size());
        model.addAttribute("metrics",        metrics);
        model.addAttribute("metricsJson",    MAPPER.writeValueAsString(metrics));
        model.addAttribute("modulesDetails", loadModules("module"));

        return "dashboard/sampleDashboard";
    }

    private Map<String, Supplier<Object>> metricMethodMap() {

    Map<String, Supplier<Object>> map = new HashMap<>();

    map.put("/emp/count/all", proxy::countAllEmployees);
    map.put("/attendance/present-today/count", proxy::countPresentToday);
    map.put("/leaves/on-leave-today/count", proxy::countOnLeaveToday);
    map.put("/attendance/absent-today/count", proxy::countAbsentToday);
    map.put("/leaves/pending/count", proxy::countPendingLeaves);
    map.put("/projects/active/count", proxy::countActiveProjects);
    map.put("/departments/count", proxy::countDepartments);
    map.put("/payroll/pending/count", proxy::countPendingPayroll);
    map.put("/payroll/pending/count", proxy::countPendingPayroll);
    map.put("/employees/by-department", proxy::employeesByDepartment);
    map.put("/get/employee-list", proxy::getAllEmployeesAsMap);

    return map;
}

    // =========================================================================
    //  fetchDynamicUrl  —  same as original
    // =========================================================================
    //@GetMapping("/dashboard-test")
    // public List<Map<String, Object>> fetchDynamicUrl(String response) throws Exception {
    //     if (response == null || response.isBlank()) return Collections.emptyList();
    //     JsonNode root = MAPPER.readTree(response);
    //     List<Map<String, Object>> dashboardData = new ArrayList<>();
    //     for (JsonNode sel : root.path("selections")) {
    //         Map<String, Object> moduleMap = new LinkedHashMap<>();
    //         moduleMap.put("module", sel.path("module").asText());
    //         List<Map<String, Object>> metricsList = new ArrayList<>();
    //         for (JsonNode mn : sel.path("metrics")) {
    //             Map<String, Object> m = new LinkedHashMap<>();
    //             m.put("key",   mn.path("key").asText());
    //             m.put("type",  mn.path("type").asText());
    //             m.put("value", mn.path("value").isNull() ? null : mn.path("value").asText());
    //             m.put("url",   mn.path("url").asText());
    //             metricsList.add(m);
    //         }
    //         moduleMap.put("metrics", metricsList);
    //         dashboardData.add(moduleMap);
    //     }
    //     if (dashboardData.isEmpty()) return Collections.emptyList();
    //     dashboardInvoker.populateDashboardValues(dashboardData);
    //     @SuppressWarnings("unchecked")
    //     List<Map<String, Object>> first = (List<Map<String, Object>>) dashboardData.get(0).get("metrics");
    //     return first.stream()
    //             .map(m -> metric((String) m.get("key"), (String) m.get("type"), m.get("value"), (String) m.get("url")))
    //             .collect(Collectors.toList());
    // }

     @GetMapping("/dashboard-test")
    public List<Map<String, Object>> fetchDynamicUrl(String response) throws Exception {

    if (response == null || response.isBlank())
        return Collections.emptyList();

    Map<String, Supplier<Object>> methodMap = metricMethodMap();

    JsonNode root = MAPPER.readTree(response);
    List<Map<String, Object>> result = new ArrayList<>();

    for (JsonNode sel : root.path("selections")) {

        for (JsonNode mn : sel.path("metrics")) {

            String key  = mn.path("key").asText();
            String type = mn.path("type").asText();
            String url  = mn.path("url").asText();

            Object value = null;

            Supplier<Object> method = methodMap.get(url);

            if (method != null) {
                value = method.get();   // <-- proxy method called
            }

            result.add(metric(key, type, value, url));
        }
    }

    return result;
}


    // =========================================================================
    //  GET /configure-my-dashboard
    // =========================================================================
    // @GetMapping("/configure-my-dashboard")
    // public String configureMyDashboard(Model model) {

    //     String userId = userInfo.getUser();
    //     Map<String, Object> userConfig = dashboard.getUserConfigAsMap(userId);
    //     List<Map<String, Object>> moduleList = dashboard.getModuleListAsMap();
    //     List<Map<String, Object>> dashboardConfigs = new ArrayList<>();

    //     for (Map<String, Object> module : moduleList) {
    //         String moduleName = str(module, colModuleName);
    //         List<Map<String, Object>> metrics = getModuleMetrics(moduleName);
    //         if (!metrics.isEmpty()) {
    //             Map<String, Object> cfg = new LinkedHashMap<>();
    //             cfg.put("userId",  userId);
    //             cfg.put("module",  moduleName);
    //             cfg.put("metrics", metrics);
    //             dashboardConfigs.add(cfg);
    //         }
    //     }

    //     log.info("dashboardConfigs: {}", dashboardConfigs);
    //     model.addAttribute("savedDashboardConfig", str(userConfig, colDashboardData));
    //     model.addAttribute("dashboardConfigs",     dashboardConfigs);
    //     model.addAttribute("userId",               userId);
    //     model.addAttribute("modulelist",           moduleList);

    //     return "dashboard/configureMyDashboard";
    // }

   @GetMapping("/configure-my-dashboard")
public String configureMyDashboard(Model model) {

    String userId = userInfo.getUser();
    Map<String, Object> userConfig = dashboard.getUserConfigAsMap(userId);

    List<Map<String, Object>> moduleList = dashboard.getModuleListAsMap();
    List<Map<String, Object>> dashboardConfigs = new ArrayList<>();

    for (Map<String, Object> module : moduleList) {
        String moduleName = str(module, colModuleName);
        List<Map<String, Object>> metrics = getModuleMetrics(moduleName);

        if (!metrics.isEmpty()) {
            Map<String, Object> cfg = new LinkedHashMap<>();
            cfg.put("userId", userId);
            cfg.put("module", moduleName);
            cfg.put("metrics", metrics);
            dashboardConfigs.add(cfg);
        }
    }

    Object raw = userConfig.get(colDashboardData);

    String savedJson = "{}";

    if (raw != null) {

        String rawStr = raw.toString();

        // extract JSON between "value=" and ", null"
        if (rawStr.contains("value=")) {

            int start = rawStr.indexOf("value=") + 6;
            int end = rawStr.lastIndexOf(", null");

            savedJson = rawStr.substring(start, end);

        } else {
            savedJson = rawStr;
        }
    }

    log.info("Final JSON sent to UI: {}", savedJson);

    model.addAttribute("savedDashboardConfig", savedJson);
    model.addAttribute("dashboardConfigs", dashboardConfigs);
    model.addAttribute("userId", userId);
    model.addAttribute("modulelist", moduleList);

    return "dashboard/configureMyDashboard";
}


    /** Returns available metrics for each module — add new modules here */
    private List<Map<String, Object>> getModuleMetrics(String moduleName) {
        List<Map<String, Object>> m = new ArrayList<>();
        switch (moduleName) {
            case "Human Resource Department":
                m.add(metric("Total Employees",              "count",      null, "/emp/count/all"));
                m.add(metric("Confirmed Employees",          "count",      null, "/emp/count/confirmed"));
                m.add(metric("Employees on Probation",       "count",      null, "/emp/count/probation"));
                m.add(metric("Departments",                  "count",      null, "/departments/count"));
                m.add(metric("Employees by Department",      "graph",      null, "/employees/by-department"));
                m.add(metric("Employees by Type",            "graph",      null, "/employees/by-employment-type"));
                m.add(metric("Employee Joining Trend",       "graph",      null, "/employees/joining-trend"));
                m.add(metric("Headcount by Status",          "graph",      null, "/employees/by-status"));
                m.add(metric("Employee Directory",           "list",       null, "/get/employee-list"));
                break;
            case "Attendance":
                m.add(metric("Present Today",                "count",      null, "/attendance/present-today/count"));
                m.add(metric("Absent Today",                 "count",      null, "/attendance/absent-today/count"));
                m.add(metric("Attendance Rate",              "percentage", null, "/attendance/rate-today"));
                m.add(metric("Avg Daily Working Hours",      "speedometer",null, "/attendance/avg-hours"));
                m.add(metric("Monthly Attendance Trend",     "graph",      null, "/attendance/monthly-trend"));
                m.add(metric("Today Attendance",             "list",       null, "/attendance/today"));
                break;
            case "Payroll":
                m.add(metric("Pending Payroll",              "count",      null, "/payroll/pending/count"));
                m.add(metric("Payroll Processed",            "percentage", null, "/payroll/processed-rate"));
                m.add(metric("Monthly Payroll Cost",         "graph",      null, "/payroll/monthly-cost"));
                m.add(metric("Recent Payroll",               "list",       null, "/payroll/recent"));
                break;
            case "Performance":
                m.add(metric("Avg Performance Rating",       "speedometer",null, "/performance/avg-rating"));
                m.add(metric("Avg Performance Score",        "percentage", null, "/performance/avg-percent"));
                m.add(metric("Performance Reviews",          "list",       null, "/performance/list"));
                break;
            case "Projects":
                m.add(metric("Active Projects",              "count",      null, "/projects/active/count"));
                m.add(metric("Active Projects List",         "list",       null, "/projects/active/list"));
                m.add(metric("Project Assignments",          "list",       null, "/projects/assignments"));
                break;
        }
        return m;
    }


    // =========================================================================
    //  POST /configure-card
    // =========================================================================
    @PostMapping("/configure-card")
    public String configureNumberCards(
            @RequestParam("dashboardDataJson") String dashboardDataJson,
            Model model) {
        try {
            String userId = userInfo.getUser();
            log.info("configure-card userId:{} json:{}", userId, dashboardDataJson);

            Map<String, Object> config = new LinkedHashMap<>();
            config.put("userid",         userId);
            config.put("dashboardData",  dashboardDataJson);
            config.put("todoList",       "[]");
            config.put("queryPreference","{}");

            try {
                int r = dashboard.addUpdateUserDashboardConfigAsMap(config);
                log.info("save response: {}", r);
            } catch (Exception ex) {
                log.error("save config error: {}", ex.getMessage(), ex);
            }

            List<Map<String, Object>> moduleList = dashboard.getModuleListAsMap();
            model.addAttribute("dashboardConfigs", new ArrayList<>());
            model.addAttribute("userId",     userId);
            model.addAttribute("modulelist", moduleList);
            model.addAttribute("actionSuccess", "Dashboard configured successfully");
            return "dashboard/configureMyDashboard";
        } catch (Exception e) {
            log.error("configure-card error: {}", e.getMessage(), e);
            return "error";
        }
    }


    // =========================================================================
    //  REST ENDPOINTS — all connected to BE via proxy
    // =========================================================================

    // ── Generic fallback ──────────────────────────────────────────────────────
    @GetMapping("/{key}")
    @ResponseBody
    public Map<String, Object> genericMetric(@PathVariable String key) {
        return Map.of("value", proxy.countActiveEmployees());
    }

    // ── Employee counts ───────────────────────────────────────────────────────
    @GetMapping("/emp/count/all")       @ResponseBody public Map<String,Object> empTotal()     { return Map.of("value", proxy.countAllEmployees()); }
    @GetMapping("/emp/count/active")    @ResponseBody public Map<String,Object> empActive()    { return Map.of("value", proxy.countActiveEmployees()); }
    @GetMapping("/emp/count/probation") @ResponseBody public Map<String,Object> empProbation() { return Map.of("value", proxy.countOnProbation()); }
    @GetMapping("/emp/count/confirmed") @ResponseBody public Map<String,Object> empConfirmed() { return Map.of("value", proxy.countConfirmedEmployees()); }

    // Keep old URL aliases for backward compat
    @GetMapping("/emp/under-probation") @ResponseBody public Map<String,Object> probationOld() { return Map.of("value", proxy.countOnProbation()); }
    @GetMapping("/emp/confirmed")       @ResponseBody public Map<String,Object> confirmedOld() { return Map.of("value", proxy.countConfirmedEmployees()); }
    @GetMapping("/get/emp/count")       @ResponseBody public Map<String,Object> totalOld()     { return Map.of("value", proxy.countAllEmployees()); }

    // ── Employee lists ────────────────────────────────────────────────────────
    @GetMapping("/get/employee-list")
    @ResponseBody
    public List<Map<String, Object>> allEmployees() {
        return proxy.getAllEmployeesAsMap();
    }

    @GetMapping("/get/employee-list/Ret-dt")
    @ResponseBody
    public Map<String, Object> employeeRetirementList() {
        List<Map<String, Object>> all = proxy.getAllEmployeesAsMap();
        List<Map<String, Object>> mapped = all.stream().map(emp -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Employee No",       get(emp, colEmpCode));
            row.put("Employee Name",     str(emp, colEmpFirst) + " " + str(emp, colEmpLast));
            row.put("Department",        get(emp, colEmpDept));
            row.put("Joining Date",      get(emp, colEmpJoinDate));
            row.put("Probation End Date",get(emp, colEmpProbEnd));
            return row;
        }).collect(Collectors.toList());
        return Map.of("value", mapped);
    }

    @GetMapping("/get/employee-list/personal")
    @ResponseBody
    public Map<String, Object> employeePersonalList() {
        List<Map<String, Object>> all = proxy.getAllEmployeesAsMap();
        List<Map<String, Object>> mapped = all.stream().map(emp -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("Employee No",       get(emp, colEmpCode));
            row.put("Employee Name",     str(emp, colEmpFirst) + " " + str(emp, colEmpLast));
            row.put("Designation",       get(emp, colEmpDesig));
            row.put("Employment Type",   get(emp, colEmpType));
            row.put("Date of Birth",     get(emp, colEmpDob));
            row.put("Gender",            get(emp, colEmpGender));
            row.put("Department",        get(emp, colEmpDept));
            return row;
        }).collect(Collectors.toList());
        return Map.of("value", mapped);
    }

    // ── Attendance ────────────────────────────────────────────────────────────
    @GetMapping("/attendance/present-today/count") @ResponseBody public Map<String,Object> presentCount() { return Map.of("value", proxy.countPresentToday()); }
    @GetMapping("/attendance/absent-today/count")  @ResponseBody public Map<String,Object> absentCount()  { return Map.of("value", proxy.countAbsentToday()); }
    @GetMapping("/attendance/rate-today")          @ResponseBody public Map<String,Object> attRate()      { return Map.of("value", proxy.attendanceRateToday()); }
    @GetMapping("/attendance/avg-hours")           @ResponseBody public Map<String,Object> avgHours()     { return Map.of("value", proxy.avgWorkingHours()); }
    @GetMapping("/attendance/today")               @ResponseBody public List<Map<String,Object>> attToday(){ return proxy.attendanceToday(); }
    @GetMapping("/attendance/monthly-trend")       @ResponseBody public List<Map<String,Object>> attTrend(){ return proxy.monthlyAttendanceTrend(); }

    // Old aliases
    @GetMapping("/get-swipe-data-count")        @ResponseBody public Map<String,Object> swipeCount() { return Map.of("value", proxy.countPresentToday()); }
    @GetMapping("/get-percentage-attaidence")   @ResponseBody public Map<String,Object> attPct()     { return Map.of("value", proxy.attendanceRateToday()); }

    // ── Leaves ────────────────────────────────────────────────────────────────
    @GetMapping("/leaves/pending/count")        @ResponseBody public Map<String,Object> pendingLeaves() { return Map.of("value", proxy.countPendingLeaves()); }
    @GetMapping("/leaves/on-leave-today/count") @ResponseBody public Map<String,Object> onLeaveToday()  { return Map.of("value", proxy.countOnLeaveToday()); }
    @GetMapping("/leaves/approval-rate")        @ResponseBody public Map<String,Object> leaveRate()     { return Map.of("value", proxy.leaveApprovalRate()); }
    @GetMapping("/leaves/by-type")              @ResponseBody public List<Map<String,Object>> leaveTypes() { return proxy.leavesByType(); }
    @GetMapping("/leaves/pending/list")         @ResponseBody public List<Map<String,Object>> pendingLeaveList() { return proxy.pendingLeavesList(); }

    // Old alias
    @GetMapping("/list/search-approved-rejected-leave-applications")
    @ResponseBody
    public Map<String, Object> leaveCountOld() {
        List<Map<String, Object>> leaves = proxy.searchLeaveApplicationsAsMap(
                0, LocalDate.now().toString(), LocalDate.now().toString());
        long approved = leaves.stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(str(l, colLeaveStatus)))
                .count();
        return Map.of("value", approved);
    }

    // ── Payroll ───────────────────────────────────────────────────────────────
    @GetMapping("/payroll/pending/count")   @ResponseBody public Map<String,Object> payrollPending() { return Map.of("value", proxy.countPendingPayroll()); }
    @GetMapping("/payroll/processed-rate")  @ResponseBody public Map<String,Object> payrollRate()    { return Map.of("value", proxy.payrollProcessedRate()); }
    @GetMapping("/payroll/monthly-cost")    @ResponseBody public List<Map<String,Object>> payrollCost() { return proxy.monthlySalaryCost(); }
    @GetMapping("/payroll/recent")          @ResponseBody public List<Map<String,Object>> payrollList() { return proxy.recentPayroll(); }

    // ── Performance ───────────────────────────────────────────────────────────
    @GetMapping("/performance/avg-rating")  @ResponseBody public Map<String,Object> perfRating()  { return Map.of("value", proxy.avgPerformanceRating()); }
    @GetMapping("/performance/avg-percent") @ResponseBody public Map<String,Object> perfPercent() { return Map.of("value", proxy.avgPerformancePercent()); }
    @GetMapping("/performance/list")        @ResponseBody public List<Map<String,Object>> perfList() { return proxy.performanceList(); }

    // ── Projects ──────────────────────────────────────────────────────────────
    @GetMapping("/projects/active/count")   @ResponseBody public Map<String,Object> projCount()   { return Map.of("value", proxy.countActiveProjects()); }
    @GetMapping("/projects/active/list")    @ResponseBody public List<Map<String,Object>> projList() { return proxy.activeProjectsList(); }
    @GetMapping("/projects/assignments")    @ResponseBody public List<Map<String,Object>> projAssign(){ return proxy.projectAssignments(); }

    // ── Departments ───────────────────────────────────────────────────────────
    @GetMapping("/departments/count")  @ResponseBody public Map<String,Object> deptCount() { return Map.of("value", proxy.countDepartments()); }

    // ── Todo tasks ────────────────────────────────────────────────────────────
    @PostMapping("/dashboard/add-update-task")
    public ResponseEntity<String> addUpdateTask(@RequestBody List<Map<String, Object>> tasks) {
        log.info("addUpdateTask: {}", tasks);
        try {
            dashboard.addUpdateTaskAsMap(tasks, userInfo.getUser());
            return ResponseEntity.ok("Task updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    @PostMapping("/dashboard/add-task")
    @ResponseBody
    public Map<String, Object> addTask(@RequestBody List<Map<String, Object>> task) {
        return Collections.emptyMap();
    }

    // ── Query builder ─────────────────────────────────────────────────────────
    @PostMapping("/get-user-generated-query-output")
    public ResponseEntity<?> runQuery(@RequestBody Map<String, String> req) {
        try {
            log.info("runQuery sql: {}", req.get("sql"));
            return ResponseEntity.ok(dashboard.getUserGeneratedQueryOutput(req.get("sql")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/get-schema-structure")
    public ResponseEntity<List<Map<String, Object>>> getSchema(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(dashboard.getSchemaStructureAsMap(
                    body.getOrDefault("schemaName", "hrms")));
        } catch (Exception ex) {
            return ResponseEntity.status(500).build();
        }
    }

    // ── Favorites ─────────────────────────────────────────────────────────────
    @PostMapping("/user/favorite/add")
    public ResponseEntity<String> addFavorite(@RequestBody String fav) {
        return ResponseEntity.ok(dashboard.saveFavQuery(fav, userInfo.getUser()));
    }

    @PostMapping("/user/favorite/remove")
    public ResponseEntity<String> removeFavorite(@RequestBody String fav) {
        return ResponseEntity.ok(dashboard.saveFavQuery(fav, userInfo.getUser()));
    }

    // ── Chatbot ───────────────────────────────────────────────────────────────
    @GetMapping("/get-service-wise-data-in-file/{moduleID}")
    @ResponseBody
    public Set<String> getChatbotServices(@PathVariable String moduleID) {
        return dashboard.getSerivceWiseDataInFile(moduleID);
    }

    @GetMapping("/get-selected-question-answer/{question}")
    @ResponseBody
    public List<Map<String, Object>> getChatbotAnswer(@PathVariable String question) {
        long t = System.currentTimeMillis();
        List<Map<String, Object>> r = dashboard.getSelectedQuestionsAnswer(question);
        log.info("chatbot answer in {}ms", System.currentTimeMillis() - t);
        return r;
    }

    @GetMapping("/get-answer-by-ask-quetion-using-rag/{que}")
    @ResponseBody
    public List<Map<String, Object>> getRagAnswer(@PathVariable String que) {
        return dashboard.getAnswerByAskQuetionUsingRAG(que);
    }

    // ── News & Map ────────────────────────────────────────────────────────────
    @GetMapping("/breaking-news")
    public ResponseEntity<String> news() {
        try {
            return ResponseEntity.ok(
                new RestTemplate().getForObject("https://feeds.feedburner.com/ndtvnews-top-stories", String.class));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading news");
        }
    }

    @GetMapping("/world-map")
    public String worldMap() { return "dashboard/interactive_map"; }


    // =========================================================================
    //  HELPERS
    // =========================================================================

    private static void normalizeDates(List<Map<String, Object>> rows, String... keys) {
        SimpleDateFormat in  = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> row : rows) {
            for (String k : keys) {
                Object v = row.get(k);
                if (v == null) continue;
                String s = v.toString();
                if (s.matches("\\d{4}-\\d{2}-\\d{2}")) continue;
                try { row.put(k, out.format(in.parse(s))); } catch (Exception ignore) {}
            }
        }
    }
}
