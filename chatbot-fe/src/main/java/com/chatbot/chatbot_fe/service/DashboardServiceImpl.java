package com.chatbot.chatbot_fe.service;


import com.chatbot.chatbot_fe.proxy.HrmsProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ====================================================================
 *  DashboardServiceImpl  —  FE Service layer
 *
 *  All data comes from the BE via HrmsProxyService (REST calls).
 *  Zero direct DB access from FE. Zero POJOs.
 *
 *  Column/field name config is in application.properties:
 *    col.emp.* / col.leave.* / col.userconfig.*
 * ====================================================================
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private HrmsProxyService proxy;

    // ── Employee counts ───────────────────────────────────────────────────────
    @Override public int getUnderProbationEmployee()  { return proxy.countOnProbation(); }
    @Override public int getConfirmedEmployeeCount()  { return proxy.countConfirmedEmployees(); }

    /** Swipe count = present today (closest match to old swipe logic) */
    @Override public int getSwipeDataByEmpNo()        { return proxy.countPresentToday(); }

    // ── Employee lists ────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getAllEmployeesAsMap() {
        return proxy.getAllEmployeesAsMap();
    }

    // ── OCF → now mapped to leave types (closest semantic match) ─────────────
    // Old: Circular / Communication File / Office Order from OCF table
    // New: same widget reused for Leave breakdown by type
    @Override
    public List<Map<String, Object>> getAllOcfAsMap() {
        return proxy.leavesByType();
    }

    // ── Module list ───────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getModuleListAsMap() {
        // Static modules — add more here as your app grows
        List<Map<String, Object>> modules = new ArrayList<>();

        addModule(modules, "Human Resource Department");
        addModule(modules, "Attendance");
        addModule(modules, "Payroll");
        addModule(modules, "Performance");
        addModule(modules, "Projects");

        return modules;
    }

    private void addModule(List<Map<String, Object>> list, String name) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("moduleName", name);
        list.add(m);
    }

    // ── User dashboard config ─────────────────────────────────────────────────
    @Override
    public Map<String, Object> getUserConfigAsMap(String userId) {
        Map<String, Object> config = proxy.getUserConfigAsMap(userId);
        // Map BE keys to FE expected keys (new DB uses snake_case)
        remapKey(config, "dashboard_data",   "dashboardData");
        remapKey(config, "todo_list",        "todoList");
        remapKey(config, "query_preference", "queryPreference");
        return config;
    }

    private void remapKey(Map<String, Object> map, String oldKey, String newKey) {
        if (map.containsKey(oldKey) && !map.containsKey(newKey)) {
            map.put(newKey, map.get(oldKey));
        }
    }

    @Override
    public int addUpdateUserDashboardConfigAsMap(Map<String, Object> config) {
        // Map FE camelCase keys → BE snake_case keys
        Map<String, Object> beConfig = new LinkedHashMap<>();
        beConfig.put("user_id",         config.getOrDefault("userid", config.get("user_id")));
        beConfig.put("dashboard_data",  config.getOrDefault("dashboardData",  config.get("dashboard_data")));
        beConfig.put("todo_list",       config.getOrDefault("todoList",       config.get("todo_list")));
        beConfig.put("query_preference",config.getOrDefault("queryPreference",config.get("query_preference")));
        return proxy.addUpdateUserDashboardConfigAsMap(beConfig);
    }

    // ── Todo tasks ────────────────────────────────────────────────────────────
    @Override
    public void addUpdateTaskAsMap(List<Map<String, Object>> tasks, String userId) {
        proxy.addUpdateTaskAsMap(tasks, userId);
    }

    // ── Favorites ─────────────────────────────────────────────────────────────
    @Override
    public String saveFavQuery(String fav, String userId) {
        return proxy.saveFavQuery(fav, userId);
    }

    // ── Schema & query ────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getUserGeneratedQueryOutput(String sql) {
        return proxy.getUserGeneratedQueryOutput(sql);
    }

    @Override
    public List<Map<String, Object>> getSchemaStructureAsMap(String schemaName) {
        return proxy.getSchemaStructureAsMap(schemaName);
    }

    // ── Leave search ──────────────────────────────────────────────────────────
    @Override
    public List<Map<String, Object>> searchLeaveApplicationsAsMap(int empNo, String from, String to) {
        return proxy.searchLeaveApplicationsAsMap(empNo, from, to);
    }

    // ── Chatbot ───────────────────────────────────────────────────────────────
    @Override
    public Set<String> getSerivceWiseDataInFile(String moduleID) {
        return proxy.getSerivceWiseDataInFile(moduleID);
    }

    @Override
    public List<Map<String, Object>> getSelectedQuestionsAnswer(String question) {
        return proxy.getSelectedQuestionsAnswer(question);
    }

    @Override
    public List<Map<String, Object>> getAnswerByAskQuetionUsingRAG(String que) {
        return proxy.getAnswerByAskQuetionUsingRAG(que);
    }
}