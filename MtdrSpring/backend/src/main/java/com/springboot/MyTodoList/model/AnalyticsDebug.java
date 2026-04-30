package com.springboot.MyTodoList.model;

import java.util.List;
import java.util.Map;

public record AnalyticsDebug(
        List<Map<String, Object>> workItems,
        List<Map<String, Object>> assignments,
        List<Map<String, Object>> timeEntries
) {
}
