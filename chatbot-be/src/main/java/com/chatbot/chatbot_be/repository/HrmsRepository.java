package com.chatbot.chatbot_be.repository;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;

import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * ====================================================================
 *  HrmsRepository  —  ALL DB queries, zero POJOs, all Map returns
 *  Schema: hrms (PostgreSQL)
 * ====================================================================
 */
@Repository
public class HrmsRepository {

    private static final Logger log = LoggerFactory.getLogger(HrmsRepository.class);

    @Autowired
	private NamedParameterJdbcOperations namedParameterJdbcOperations;
    
    @Autowired
    private JdbcTemplate jdbc;

    // =========================================================================
    //  EMPLOYEES
    // =========================================================================

    public int countActiveEmployees() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.employees WHERE status = 'ACTIVE'", Integer.class);
    }

    public int countAllEmployees() {
        log.info("count of employees");
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.employees", Integer.class);
    }

    public int countOnProbation() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.employees WHERE probation_end_date >= CURRENT_DATE AND status='ACTIVE'",
            Integer.class);
    }

    public int countConfirmedEmployees() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.employees WHERE (probation_end_date IS NULL OR probation_end_date < CURRENT_DATE) AND status='ACTIVE'",
            Integer.class);
    }

    /** Full employee list with department name joined */
    public List<Map<String, Object>> getAllEmployees() {
        return jdbc.queryForList(
            "SELECT e.id, e.employee_code, e.first_name, e.last_name, e.email, e.phone, " +
            "       e.date_of_birth, e.gender, e.marital_status, e.designation, " +
            "       e.employment_type, e.joining_date, e.probation_end_date, " +
            "       e.work_location, e.status, e.basic_salary, e.created_at, " +
            "       d.name AS department_name " +
            "FROM hrms.employees e " +
            "LEFT JOIN hrms.departments d ON e.department_id = d.id " +
            "ORDER BY e.first_name");
    }

    /** Employees by department (for graph widget) */
    public List<Map<String, Object>> employeesByDepartment() {
        return jdbc.queryForList(
            "SELECT d.name AS label, COUNT(e.id) AS value " +
            "FROM hrms.departments d " +
            "LEFT JOIN hrms.employees e ON e.department_id = d.id AND e.status='ACTIVE' " +
            "GROUP BY d.name ORDER BY value DESC");
    }

    public List<Map<String, Object>> employeeListByMaritalStatus(){
    
        return jdbc.queryForList("select marital_status AS label, count(id) AS value from hrms.employees group by marital_status");
    }

    public List<Map<String, Object>> employeesBySalary(){

        return jdbc.queryForList("select first_name AS label, basic_salary AS value from hrms.employees");
}

    /** Employees by employment type */
    public List<Map<String, Object>> employeesByEmploymentType() {
        return jdbc.queryForList(
            "SELECT COALESCE(employment_type, 'Unspecified') AS label, COUNT(*) AS value " +
            "FROM hrms.employees WHERE status='ACTIVE' " +
            "GROUP BY employment_type ORDER BY value DESC");
    }

    /** Headcount by status */
    public List<Map<String, Object>> employeesByStatus() {
        return jdbc.queryForList(
            "SELECT COALESCE(status, 'Unknown') AS label, COUNT(*) AS value " +
            "FROM hrms.employees GROUP BY status ORDER BY value DESC");
    }

    /** Joining trend by year */
    public List<Map<String, Object>> joiningTrendByYear() {
        return jdbc.queryForList(
            "SELECT EXTRACT(YEAR FROM joining_date)::text AS label, COUNT(*) AS value " +
            "FROM hrms.employees WHERE joining_date IS NOT NULL " +
            "GROUP BY EXTRACT(YEAR FROM joining_date) ORDER BY label");
    }

    // =========================================================================
    //  DEPARTMENTS
    // =========================================================================

    public int countDepartments() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM hrms.departments", Integer.class);
    }

    public List<Map<String, Object>> getAllDepartments() {
        return jdbc.queryForList("SELECT * FROM hrms.departments ORDER BY name");
    }

    // =========================================================================
    //  ATTENDANCE
    // =========================================================================

    public int countPresentToday() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.attendance WHERE date = CURRENT_DATE AND status = 'PRESENT'",
            Integer.class);
    }

    public int countAbsentToday() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.attendance WHERE date = CURRENT_DATE AND status = 'ABSENT'",
            Integer.class);
    }

    public double attendanceRateToday() {
        int total  = countActiveEmployees();
        int present = countPresentToday();
        if (total == 0) return 0.0;
        return Math.round((present * 100.0 / total) * 100.0) / 100.0;
    }

    public double avgWorkingHours() {
        Double avg = jdbc.queryForObject(
            "SELECT AVG(total_hours) FROM hrms.attendance WHERE total_hours IS NOT NULL AND date >= CURRENT_DATE - INTERVAL '30 days'",
            Double.class);
        return avg == null ? 0.0 : Math.round(avg * 100.0) / 100.0;
    }

    /** Today's attendance list with employee name */
    public List<Map<String, Object>> attendanceToday() {
        return jdbc.queryForList(
            "SELECT e.employee_code, e.first_name || ' ' || e.last_name AS employee_name, " +
            "       a.check_in, a.check_out, a.total_hours, a.status, a.remarks, " +
            "       d.name AS department " +
            "FROM hrms.attendance a " +
            "JOIN hrms.employees e ON a.employee_id = e.id " +
            "LEFT JOIN hrms.departments d ON e.department_id = d.id " +
            "WHERE a.date = CURRENT_DATE ORDER BY e.first_name");
    }

    /** Monthly attendance trend (last 6 months) */
    public List<Map<String, Object>> monthlyAttendanceTrend() {
        return jdbc.queryForList(
            "SELECT TO_CHAR(date, 'Mon YYYY') AS label, " +
            "       COUNT(CASE WHEN status='PRESENT' THEN 1 END) AS present, " +
            "       COUNT(CASE WHEN status='ABSENT'  THEN 1 END) AS absent, " +
            "       COUNT(CASE WHEN status='LEAVE'   THEN 1 END) AS on_leave " +
            "FROM hrms.attendance " +
            "WHERE date >= CURRENT_DATE - INTERVAL '6 months' " +
            "GROUP BY TO_CHAR(date, 'Mon YYYY'), DATE_TRUNC('month', date) " +
            "ORDER BY DATE_TRUNC('month', date)");
    }

    // =========================================================================
    //  LEAVES
    // =========================================================================

    public int countPendingLeaves() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.leaves WHERE status = 'PENDING'", Integer.class);
    }

    public int countOnLeaveToday() {
        return jdbc.queryForObject(
            "SELECT COUNT(DISTINCT employee_id) FROM hrms.leaves " +
            "WHERE CURRENT_DATE BETWEEN start_date AND end_date AND status = 'APPROVED'",
            Integer.class);
    }

    public double leaveApprovalRate() {
        Integer total    = jdbc.queryForObject("SELECT COUNT(*) FROM hrms.leaves", Integer.class);
        Integer approved = jdbc.queryForObject("SELECT COUNT(*) FROM hrms.leaves WHERE status='APPROVED'", Integer.class);
        if (total == null || total == 0) return 0.0;
        return Math.round((approved * 100.0 / total) * 100.0) / 100.0;
    }

    /** Leave requests by type (graph) */
    public List<Map<String, Object>> leavesByType() {
        return jdbc.queryForList(
            "SELECT COALESCE(leave_type, 'Other') AS label, COUNT(*) AS value " +
            "FROM hrms.leaves GROUP BY leave_type ORDER BY value DESC");
    }

    /** Pending leave requests list */
    public List<Map<String, Object>> pendingLeavesList() {
        return jdbc.queryForList(
            "SELECT e.employee_code, e.first_name || ' ' || e.last_name AS employee_name, " +
            "       l.leave_type, l.start_date, l.end_date, l.total_days, l.reason, l.status, " +
            "       d.name AS department " +
            "FROM hrms.leaves l " +
            "JOIN hrms.employees e ON l.employee_id = e.id " +
            "LEFT JOIN hrms.departments d ON e.department_id = d.id " +
            "WHERE l.status = 'PENDING' ORDER BY l.created_at DESC");
    }

    public List<Map<String, Object>> searchLeaveApplications(int empNo, String fromDate, String toDate) {
        return jdbc.queryForList(
            "SELECT l.*, e.first_name || ' ' || e.last_name AS employee_name " +
            "FROM hrms.leaves l " +
            "JOIN hrms.employees e ON l.employee_id = e.id " +
            "WHERE (? = 0 OR l.employee_id = ?) " +
            "  AND l.start_date >= ?::date AND l.end_date <= ?::date " +
            "ORDER BY l.created_at DESC",
            empNo, empNo, fromDate, toDate);
    }

    // =========================================================================
    //  PAYROLL
    // =========================================================================

    public int countPendingPayroll() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.payroll WHERE payment_status = 'PENDING'", Integer.class);
    }

    public double payrollProcessedRate() {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM hrms.payroll", Integer.class);
        Integer paid  = jdbc.queryForObject("SELECT COUNT(*) FROM hrms.payroll WHERE payment_status='PAID'", Integer.class);
        if (total == null || total == 0) return 0.0;
        return Math.round((paid * 100.0 / total) * 100.0) / 100.0;
    }

    /** Monthly payroll cost (last 6 months) — for graph */
    public List<Map<String, Object>> monthlySalaryCost() {
        return jdbc.queryForList(
            "SELECT year::text || '-' || LPAD(month::text,2,'0') AS label, " +
            "       SUM(net_salary) AS value " +
            "FROM hrms.payroll " +
            "WHERE (year * 100 + month) >= " +
            "      (EXTRACT(YEAR FROM CURRENT_DATE)::int * 100 + EXTRACT(MONTH FROM CURRENT_DATE)::int - 6) " +
            "GROUP BY year, month ORDER BY year, month");
    }

    /** Recent payroll records */
    public List<Map<String, Object>> recentPayroll() {
        return jdbc.queryForList(
            "SELECT e.employee_code, e.first_name || ' ' || e.last_name AS employee_name, " +
            "       p.month, p.year, p.gross_salary, p.net_salary, p.payment_status, p.payment_date, " +
            "       d.name AS department " +
            "FROM hrms.payroll p " +
            "JOIN hrms.employees e ON p.employee_id = e.id " +
            "LEFT JOIN hrms.departments d ON e.department_id = d.id " +
            "ORDER BY p.created_at DESC LIMIT 50");
    }

    // =========================================================================
    //  PERFORMANCE
    // =========================================================================

    public double avgPerformanceRating() {
        Double avg = jdbc.queryForObject(
            "SELECT AVG(rating) FROM hrms.performance WHERE rating IS NOT NULL", Double.class);
        return avg == null ? 0.0 : Math.round(avg * 100.0) / 100.0;
    }

    public double avgPerformancePercent() {
        double avg = avgPerformanceRating();
        return Math.round((avg / 5.0 * 100.0) * 100.0) / 100.0;
    }

    public List<Map<String, Object>> performanceList() {
        return jdbc.queryForList(
            "SELECT e.employee_code, e.first_name || ' ' || e.last_name AS employee_name, " +
            "       p.review_period, p.rating, p.feedback, " +
            "       d.name AS department " +
            "FROM hrms.performance p " +
            "JOIN hrms.employees e ON p.employee_id = e.id " +
            "LEFT JOIN hrms.departments d ON e.department_id = d.id " +
            "ORDER BY p.created_at DESC");
    }

    // =========================================================================
    //  PROJECTS
    // =========================================================================

    public int countActiveProjects() {
        return jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.projects WHERE end_date >= CURRENT_DATE OR end_date IS NULL",
            Integer.class);
    }

    public List<Map<String, Object>> activeProjectsList() {
        return jdbc.queryForList(
            "SELECT p.id, p.name, p.description, p.start_date, p.end_date, " +
            "       COUNT(pa.employee_id) AS team_size " +
            "FROM hrms.projects p " +
            "LEFT JOIN hrms.project_assignments pa ON pa.project_id = p.id " +
            "WHERE p.end_date >= CURRENT_DATE OR p.end_date IS NULL " +
            "GROUP BY p.id ORDER BY p.start_date DESC");
    }

    public List<Map<String, Object>> projectAssignments() {
        return jdbc.queryForList(
            "SELECT e.first_name || ' ' || e.last_name AS employee_name, " +
            "       e.employee_code, pr.name AS project_name, " +
            "       pa.role, pa.assigned_date, pa.end_date " +
            "FROM hrms.project_assignments pa " +
            "JOIN hrms.employees e ON pa.employee_id = e.id " +
            "JOIN hrms.projects pr ON pa.project_id = pr.id " +
            "ORDER BY pa.assigned_date DESC");
    }

    // =========================================================================
    //  USERS / ROLES
    // =========================================================================

    public List<Map<String, Object>> getAllUsers() {
        return jdbc.queryForList(
            "SELECT u.username, u.email, u.role_id, r.name AS role, u.is_active, " +
            "u.created_at, u.updated_at " +
            "FROM hrms.users u LEFT JOIN hrms.roles r ON u.role_id = r.id " +
            "ORDER BY u.username");
    }

    public List<Map<String, Object>> getUsersForIdentityOsMigration() {
        return jdbc.queryForList(
            "SELECT u.id AS external_user_id, u.username, u.email, r.name AS role, " +
            "u.is_active, u.created_at, u.updated_at " +
            "FROM hrms.users u LEFT JOIN hrms.roles r ON u.role_id = r.id " +
            "ORDER BY u.username");
    }

    public Map<String, Object> findUserForAuthentication(String username) {
        return jdbc.query(
            "SELECT u.username, u.is_active, u.hashed_password AS password_value, " +
            "u.role_id, r.name AS role " +
            "FROM hrms.users u LEFT JOIN hrms.roles r ON u.role_id = r.id " +
            "WHERE LOWER(u.username) = LOWER(?) LIMIT 1",
            ps -> ps.setString(1, username),
            rs -> rs.next() ? userRow(rs) : new LinkedHashMap<>());
    }

    private Map<String, Object> userRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("username", rs.getString("username"));
        user.put("is_active", rs.getBoolean("is_active"));
        user.put("password_value", rs.getString("password_value"));
        user.put("role", rs.getString("role"));
        return user;
    }

    public boolean userExists(String email, String username) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM hrms.users WHERE LOWER(email)=LOWER(?) OR LOWER(username)=LOWER(?)",
            Integer.class, email, username);
        return count != null && count > 0;
    }

    public int insertUser(String email, String username, String encodedPassword) {
        return jdbc.update(
            "INSERT INTO hrms.users " +
            "(username, email, hashed_password, role_id, is_active, created_at, updated_at) " +
            "VALUES (?, ?, ?, (SELECT id FROM hrms.roles WHERE LOWER(name)='user' LIMIT 1), true, now(), now())",
            username, email, encodedPassword);
    }

    // =========================================================================
    //  USER DASHBOARD CONFIG  (store as JSON in new table)
    // =========================================================================

    public Map<String, Object> getUserConfig(String userId) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM hrms.user_dashboard_config WHERE user_id = ?", userId);
            return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
        } catch (Exception e) {
            log.warn("user_dashboard_config table not found or empty: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    public int saveUserConfig(Map<String, Object> config) {
        String userId = (String) config.get("user_id");
        try {
            int exists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM hrms.user_dashboard_config WHERE user_id=?",
                Integer.class, userId);
            if (exists > 0) {
                return jdbc.update(
                    "UPDATE hrms.user_dashboard_config SET dashboard_data=?::jsonb, " +
                    "todo_list=?::jsonb, query_preference=?::jsonb, updated_at=now() WHERE user_id=?",
                    config.get("dashboard_data"), config.get("todo_list"),
                    config.get("query_preference"), userId);
            } else {
                return jdbc.update(
                    "INSERT INTO hrms.user_dashboard_config " +
                    "(user_id, dashboard_data, todo_list, query_preference, created_at, updated_at) " +
                    "VALUES (?,?::jsonb,?::jsonb,?::jsonb,now(),now())",
                    userId, config.get("dashboard_data"),
                    config.get("todo_list"), config.get("query_preference"));
            }
        } catch (Exception e) {
            log.error("saveUserConfig error: {}", e.getMessage(), e);
            return 0;
        }
    }

    // =========================================================================
    //  DYNAMIC QUERY (Query Builder)
    // =========================================================================

    public List<Map<String, Object>> runDynamicQuery(String sql) {
        // Safety: only allow SELECT
        String trimmed = sql.trim().toUpperCase();
        if (!trimmed.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are permitted.");
        }
        return jdbc.queryForList(sql);
    }

    
   public List<Map<String, Object>> getSchemaStructure(String schemaName) {

    String sql =
        "SELECT nsp.nspname AS schema_name, " +
        "cls.relname AS table_name, " +
        "pgd_table.description AS table_description, " +
        "att.attname AS column_name, " +
        "format_type(att.atttypid, att.atttypmod) AS data_type, " +
        "pgd_column.description AS column_description, " +
        "CASE con.contype " +
        " WHEN 'p' THEN 'PRIMARY KEY' " +
        " WHEN 'f' THEN 'FOREIGN KEY' " +
        " WHEN 'u' THEN 'UNIQUE' " +
        " WHEN 'c' THEN 'CHECK' " +
        " ELSE 'No Constraint' " +
        " END AS column_constraint, " +
        "fnsp.nspname AS reference_schema, " +
        "fcls.relname AS reference_table, " +
        "fatt.attname AS reference_column " +
        "FROM pg_class cls " +
        "JOIN pg_namespace nsp " +
        "ON nsp.oid = cls.relnamespace " +
        "AND cls.relkind = 'r' " +
        "JOIN pg_attribute att " +
        "ON att.attrelid = cls.oid " +
        "AND att.attnum > 0 " +
        "AND NOT att.attisdropped " +
        "LEFT JOIN pg_description pgd_table " +
        "ON pgd_table.objoid = cls.oid " +
        "AND pgd_table.objsubid = 0 " +
        "LEFT JOIN pg_description pgd_column " +
        "ON pgd_column.objoid = cls.oid " +
        "AND pgd_column.objsubid = att.attnum " +
        "LEFT JOIN pg_constraint con " +
        "ON con.conrelid = cls.oid " +
        "AND att.attnum = ANY (con.conkey) " +
        "LEFT JOIN pg_class fcls " +
        "ON fcls.oid = con.confrelid " +
        "AND con.contype = 'f' " +
        "LEFT JOIN pg_namespace fnsp " +
        "ON fnsp.oid = fcls.relnamespace " +
        "LEFT JOIN pg_attribute fatt " +
        "ON fatt.attrelid = con.confrelid " +
        "AND fatt.attnum = ANY (con.confkey) " +
        "AND con.contype = 'f' " +
        "WHERE nsp.nspname = ? " +
        "ORDER BY schema_name, table_name, att.attnum";

    return jdbc.queryForList(sql, schemaName);
}

	public List<Map<String, Object>> executeDynamicQuery(String sql) {
		  return namedParameterJdbcOperations.getJdbcOperations().query(sql, rs -> {
	            List<Map<String, Object>> resultList = new ArrayList<>();
	            ResultSetMetaData metaData = rs.getMetaData();
	            int columnCount = metaData.getColumnCount();

	            while (rs.next()) {
	                Map<String, Object> row = new HashMap<>();
	                for (int i = 1; i <= columnCount; i++) {
	                    row.put(metaData.getColumnLabel(i), rs.getObject(i));
	                }
	                resultList.add(row);
	            }
	            return resultList;
	        });
	}
}
