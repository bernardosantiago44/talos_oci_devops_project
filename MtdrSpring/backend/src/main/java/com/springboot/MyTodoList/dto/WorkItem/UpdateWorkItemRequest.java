package com.springboot.MyTodoList.dto.WorkItem;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public final class UpdateWorkItemRequest {
    private String sprintId;
    private String workType;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String externalLink;
    private Integer estimatedMinutes;
    private LocalDate dueDate;
    private OffsetDateTime completedAt;
    private List<String> assigneeIds;
}
