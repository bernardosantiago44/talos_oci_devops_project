package com.springboot.MyTodoList.dto.WorkItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class CreateWorkItemRequest { 
        @NotBlank private String sprintId;
        @NotBlank @NotNull
        private String createdByUserId;
        @NotBlank private String workType;
        @NotBlank private String title;
        private String description;
        @NotBlank private String status;
        @NotBlank private String priority;
        private String externalLink;
        private Integer estimatedMinutes;
        private LocalDate dueDate;
        private OffsetDateTime completedAt;
        private List<String> assigneeIds;
}
