package com.springboot.MyTodoList.dto.WorkItem;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "Request payload for creating a work item.")
public class CreateWorkItemRequest { 
        @Schema(description = "Sprint that contains the work item.", example = "sprint-1")
        @NotBlank private String sprintId;

        @Schema(description = "User creating the work item.", example = "user-1")
        @NotBlank @NotNull
        private String createdByUserId;

        @Schema(description = "Type of work.", example = "TASK")
        @NotBlank private String workType;

        @Schema(description = "Short title for the work item.", example = "Build analytics dashboard")
        @NotBlank private String title;

        @Schema(description = "Detailed work item description.", example = "Create charts and KPI cards for team analytics.")
        private String description;

        @Schema(description = "Current work item status.", example = "NEW")
        @NotBlank private String status;

        @Schema(description = "Work item priority.", example = "HIGH")
        @NotBlank private String priority;

        @Schema(description = "External reference URL.", example = "https://example.com/work/wi-1")
        private String externalLink;

        @Schema(description = "Estimated effort in minutes.", example = "120")
        private Integer estimatedMinutes;

        @Schema(description = "Due date in ISO-8601 format.", example = "2026-05-15")
        private LocalDate dueDate;

        @Schema(description = "Completion timestamp when the work item is already complete.", example = "2026-05-15T14:30:00-06:00")
        private OffsetDateTime completedAt;

        @Schema(description = "Users to assign after creation.", example = "[\"user-1\", \"user-2\"]")
        private List<String> assigneeIds;
}
