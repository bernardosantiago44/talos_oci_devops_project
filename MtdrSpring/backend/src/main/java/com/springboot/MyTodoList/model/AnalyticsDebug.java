package com.springboot.MyTodoList.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Raw analytics diagnostic data.")
public record AnalyticsDebug(
        @Schema(description = "Raw work item rows.")
        List<Map<String, Object>> workItems,
        @Schema(description = "Raw work item assignment rows.")
        List<Map<String, Object>> assignments,
        @Schema(description = "Raw time entry rows.")
        List<Map<String, Object>> timeEntries
) {
}
