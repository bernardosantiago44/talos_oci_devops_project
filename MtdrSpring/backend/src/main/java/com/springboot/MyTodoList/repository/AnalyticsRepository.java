package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.AnalyticsDebug;
import com.springboot.MyTodoList.model.DeveloperSprintAnalytics;
import com.springboot.MyTodoList.model.SprintVelocityRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class AnalyticsRepository {
    private static final String DEVELOPER_SPRINT_ANALYTICS_SQL = """
            WITH hours_by_user_sprint AS (
                SELECT te.USER_ID, wi.SPRINT_ID, SUM(te.MINUTES) / 60.0 AS total_hours
                FROM CHATBOT_USER.TIME_ENTRY te
                JOIN CHATBOT_USER.WORK_ITEM wi ON te.WORK_ITEM_ID = wi.WORK_ITEM_ID
                WHERE wi.SPRINT_ID IS NOT NULL
                GROUP BY te.USER_ID, wi.SPRINT_ID
            ),
            tasks_by_user_sprint AS (
                SELECT wia.USER_ID, wi.SPRINT_ID, COUNT(DISTINCT wi.WORK_ITEM_ID) AS tasks_completed
                FROM CHATBOT_USER.WORK_ITEM_ASSIGNMENT wia
                JOIN CHATBOT_USER.WORK_ITEM wi ON wia.WORK_ITEM_ID = wi.WORK_ITEM_ID
                WHERE wia.UNASSIGNED_AT IS NULL
                  AND wi.STATUS IN ('DONE', 'COMPLETED', 'CLOSED')
                GROUP BY wia.USER_ID, wi.SPRINT_ID
            )
            SELECT
                u.USER_ID,
                u.NAME AS developer,
                s.NAME AS sprint,
                NVL(h.total_hours, 0) AS total_hours_worked,
                NVL(t.tasks_completed, 0) AS tasks_completed
            FROM APP_USER u
            JOIN CHATBOT_USER.SPRINT s ON 1=1
            LEFT JOIN hours_by_user_sprint h ON h.USER_ID = u.USER_ID AND h.SPRINT_ID = s.SPRINT_ID
            LEFT JOIN tasks_by_user_sprint t ON t.USER_ID = u.USER_ID AND t.SPRINT_ID = s.SPRINT_ID
            WHERE h.USER_ID IS NOT NULL OR t.USER_ID IS NOT NULL
            ORDER BY u.NAME, s.NAME
            """;

    private static final String SPRINT_VELOCITY_SQL = """
            SELECT
              s.SPRINT_ID  AS sprint_id,
              s.NAME       AS sprint_name,
              s.STATUS     AS sprint_status,
              s.START_DATE,
              s.END_DATE,
              COUNT(wi.WORK_ITEM_ID) AS total_tasks,
              SUM(CASE WHEN wi.STATUS IN ('DONE','COMPLETED','CLOSED') THEN 1 ELSE 0 END) AS completed_tasks
            FROM CHATBOT_USER.SPRINT s
            LEFT JOIN CHATBOT_USER.WORK_ITEM wi ON wi.SPRINT_ID = s.SPRINT_ID
            GROUP BY s.SPRINT_ID, s.NAME, s.STATUS, s.START_DATE, s.END_DATE
            ORDER BY s.START_DATE DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DeveloperSprintAnalytics> findDeveloperSprintAnalytics() {
        return jdbcTemplate.query(DEVELOPER_SPRINT_ANALYTICS_SQL, this::mapDeveloperSprintAnalytics);
    }

    public List<SprintVelocityRow> findSprintVelocityRows() {
        return jdbcTemplate.query(SPRINT_VELOCITY_SQL, this::mapSprintVelocityRow);
    }

    public AnalyticsDebug findDebugData() {
        List<Map<String, Object>> workItems = jdbcTemplate.queryForList(
                "SELECT WORK_ITEM_ID, TITLE, STATUS, SPRINT_ID FROM CHATBOT_USER.WORK_ITEM ORDER BY CREATED_AT DESC FETCH FIRST 20 ROWS ONLY");
        List<Map<String, Object>> assignments = jdbcTemplate.queryForList(
                "SELECT ASSIGNMENT_ID, WORK_ITEM_ID, USER_ID FROM CHATBOT_USER.WORK_ITEM_ASSIGNMENT ORDER BY ASSIGNED_AT DESC FETCH FIRST 20 ROWS ONLY");
        List<Map<String, Object>> timeEntries = jdbcTemplate.queryForList(
                "SELECT TIME_ENTRY_ID, WORK_ITEM_ID, MINUTES FROM CHATBOT_USER.TIME_ENTRY ORDER BY TIME_ENTRY_ID DESC FETCH FIRST 20 ROWS ONLY");

        return new AnalyticsDebug(workItems, assignments, timeEntries);
    }

    private DeveloperSprintAnalytics mapDeveloperSprintAnalytics(ResultSet rs, int rowNum) throws SQLException {
        return new DeveloperSprintAnalytics(
                rs.getString("USER_ID"),
                rs.getString("DEVELOPER"),
                rs.getString("SPRINT"),
                rs.getDouble("TOTAL_HOURS_WORKED"),
                rs.getLong("TASKS_COMPLETED")
        );
    }

    private SprintVelocityRow mapSprintVelocityRow(ResultSet rs, int rowNum) throws SQLException {
        return new SprintVelocityRow(
                rs.getString("SPRINT_ID"),
                rs.getString("SPRINT_NAME"),
                rs.getString("SPRINT_STATUS"),
                getStringOrNull(rs, "START_DATE"),
                getStringOrNull(rs, "END_DATE"),
                rs.getLong("TOTAL_TASKS"),
                rs.getLong("COMPLETED_TASKS")
        );
    }

    private String getStringOrNull(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        return value == null ? null : value.toString();
    }
}
