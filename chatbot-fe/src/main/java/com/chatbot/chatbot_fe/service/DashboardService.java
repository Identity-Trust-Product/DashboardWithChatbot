package com.chatbot.chatbot_fe.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FE DashboardService interface.
 * All methods return Map or List<Map> — zero POJOs.
 */
public interface DashboardService {

    // ── Employee ──────────────────────────────────────────────────────────────
    int getUnderProbationEmployee();
    int getConfirmedEmployeeCount();
    int getSwipeDataByEmpNo();  // maps to present-today count in new DB

    List<Map<String, Object>> getAllEmployeesAsMap();
    List<Map<String, Object>> getAllOcfAsMap();         // maps to leavesByType in new DB
    List<Map<String, Object>> getModuleListAsMap();

    // ── User config ───────────────────────────────────────────────────────────
    Map<String, Object>       getUserConfigAsMap(String userId);
    int  addUpdateUserDashboardConfigAsMap(Map<String, Object> config);
    void addUpdateTaskAsMap(List<Map<String, Object>> tasks, String userId);
    String saveFavQuery(String fav, String userId);

    // ── Leave search ──────────────────────────────────────────────────────────
    List<Map<String, Object>> searchLeaveApplicationsAsMap(int empNo, String from, String to);

    // ── Schema + query ────────────────────────────────────────────────────────
    List<Map<String, Object>> getUserGeneratedQueryOutput(String sql);
    List<Map<String, Object>> getSchemaStructureAsMap(String schemaName);

    // ── Chatbot ───────────────────────────────────────────────────────────────
    Set<String>               getSerivceWiseDataInFile(String moduleID);
    List<Map<String, Object>> getSelectedQuestionsAnswer(String question);
    List<Map<String, Object>> getAnswerByAskQuetionUsingRAG(String que);
}
