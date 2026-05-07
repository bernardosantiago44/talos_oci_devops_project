package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.DeveloperSprintAnalytics;
import com.springboot.MyTodoList.model.SprintVelocityRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsRepositoryTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AnalyticsRepository repository;

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findDeveloperSprintAnalyticsUsesProvidedQueryAndMapsRows() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        repository.findDeveloperSprintAnalytics();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RowMapper<DeveloperSprintAnalytics>> mapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), mapperCaptor.capture());

        assertThat(sqlCaptor.getValue())
                .contains("NVL(SUM(te.MINUTES), 0) / 60.0 AS total_hours_worked")
                .contains("COUNT(DISTINCT CASE")
                .contains("LEFT JOIN CHATBOT_USER.TIME_ENTRY te ON u.USER_ID = te.USER_ID")
                .contains("GROUP BY u.USER_ID, u.NAME, S.SPRINT_ID, S.NAME");

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("USER_ID")).thenReturn("user-1");
        when(resultSet.getString("DEVELOPER")).thenReturn("Ada Lovelace");
        when(resultSet.getString("SPRINT")).thenReturn("Sprint 1");
        when(resultSet.getDouble("TOTAL_HOURS_WORKED")).thenReturn(2.5);
        when(resultSet.getLong("TASKS_COMPLETED")).thenReturn(4L);

        DeveloperSprintAnalytics row = mapperCaptor.getValue().mapRow(resultSet, 0);

        assertThat(row).isEqualTo(new DeveloperSprintAnalytics("user-1", "Ada Lovelace", "Sprint 1", 2.5, 4));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findSprintVelocityRowsUsesVelocityQueryAndMapsDateValues() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        repository.findSprintVelocityRows();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<RowMapper<SprintVelocityRow>> mapperCaptor = ArgumentCaptor.forClass(RowMapper.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), mapperCaptor.capture());

        assertThat(sqlCaptor.getValue())
                .contains("FROM CHATBOT_USER.SPRINT s")
                .contains("LEFT JOIN CHATBOT_USER.WORK_ITEM wi ON wi.SPRINT_ID = s.SPRINT_ID")
                .contains("SUM(CASE WHEN wi.STATUS IN ('DONE','COMPLETED','CLOSED') THEN 1 ELSE 0 END) AS completed_tasks");

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("SPRINT_ID")).thenReturn("sprint-1");
        when(resultSet.getString("SPRINT_NAME")).thenReturn("Sprint 1");
        when(resultSet.getString("SPRINT_STATUS")).thenReturn("ACTIVE");
        when(resultSet.getObject("START_DATE")).thenReturn(LocalDate.of(2026, 4, 1));
        when(resultSet.getObject("END_DATE")).thenReturn(null);
        when(resultSet.getLong("TOTAL_TASKS")).thenReturn(5L);
        when(resultSet.getLong("COMPLETED_TASKS")).thenReturn(3L);

        SprintVelocityRow row = mapperCaptor.getValue().mapRow(resultSet, 0);

        assertThat(row).isEqualTo(new SprintVelocityRow("sprint-1", "Sprint 1", "ACTIVE", "2026-04-01", null, 5, 3));
    }
}
