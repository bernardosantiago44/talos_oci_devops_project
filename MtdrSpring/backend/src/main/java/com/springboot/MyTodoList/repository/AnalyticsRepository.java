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
            SELECT
                u.USER_ID,
                u.NAME AS developer,
                S.NAME AS sprint,
                NVL(SUM(te.MINUTES), 0) / 60.0 AS total_hours_worked,
                COUNT(DISTINCT CASE
                                   WHEN wi.STATUS IN ('DONE', 'COMPLETED', 'CLOSED')
                                       THEN wi.WORK_ITEM_ID
                    END) AS tasks_completed
            FROM APP_USER u
                     LEFT JOIN CHATBOT_USER.TIME_ENTRY te ON u.USER_ID = te.USER_ID
                     LEFT JOIN CHATBOT_USER.WORK_ITEM wi ON te.WORK_ITEM_ID = wi.WORK_ITEM_ID
                     LEFT JOIN CHATBOT_USER.SPRINT s ON wi.SPRINT_ID = s.SPRINT_ID
            GROUP BY u.USER_ID, u.NAME, S.SPRINT_ID, S.NAME
            ORDER BY u.NAME, S.NAME
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
                "SELECT WORK_ITEM_ID, TITLE, STATUS, SPRINT_ID FROM CHATBOT_USER.WORK_ITEM WHERE WORK_ITEM_ID LIKE 'wi-d%' ORDER BY WORK_ITEM_ID");
        List<Map<String, Object>> assignments = jdbcTemplate.queryForList(
                "SELECT ASSIGNMENT_ID, WORK_ITEM_ID, USER_ID FROM CHATBOT_USER.WORK_ITEM_ASSIGNMENT WHERE WORK_ITEM_ID LIKE 'wi-d%' ORDER BY ASSIGNMENT_ID");
        List<Map<String, Object>> timeEntries = jdbcTemplate.queryForList(
                "SELECT TIME_ENTRY_ID, WORK_ITEM_ID, MINUTES FROM CHATBOT_USER.TIME_ENTRY WHERE WORK_ITEM_ID LIKE 'wi-d%' ORDER BY TIME_ENTRY_ID");

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
