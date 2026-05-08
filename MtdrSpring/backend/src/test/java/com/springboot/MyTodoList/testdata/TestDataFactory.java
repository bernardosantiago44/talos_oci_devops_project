package com.springboot.MyTodoList.testdata;

import com.springboot.MyTodoList.dto.WorkItem.AssignedUserDto;
import com.springboot.MyTodoList.dto.WorkItem.CreateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.UpdateWorkItemRequest;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemAssignmentDto;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.dto.tag.TagResponse;
import com.springboot.MyTodoList.model.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {
    public static final String WORK_ITEM_ID = "wi-1";
    public static final String SPRINT_ID = "sprint-1";
    public static final String CREATOR_USER_ID = "creator-1";
    public static final String ASSIGNEE_USER_ID = "user-1";
    public static final String ASSIGNMENT_ID = "assignment-1";
    public static final String TAG_ID = "B38E0341-AA9B-486D-81BD-8E7C0339A49F\n";

    private TestDataFactory() {
    }

    public static CreateWorkItemRequest validCreateWorkItemRequest() {
        CreateWorkItemRequest request = new CreateWorkItemRequest();
        request.setSprintId(SPRINT_ID);
        request.setCreatedByUserId(CREATOR_USER_ID);
        request.setWorkType("TASK");
        request.setTitle("Build assignment tests");
        request.setDescription("Add starter backend tests");
        request.setStatus("NEW");
        request.setPriority(WorkItemPriority.HIGH);
        request.setExternalLink("https://example.com/work/wi-1");
        request.setEstimatedMinutes(30);
        request.setDueDate(LocalDate.of(2026, 5, 15));
        return request;
    }

    public static UpdateWorkItemRequest validUpdateWorkItemRequest() {
        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setTitle("Updated title");
        request.setStatus("IN_PROGRESS");
        request.setPriority(WorkItemPriority.MEDIUM);
        request.setEstimatedMinutes(45);
        return request;
    }

    public static WorkItem workItem() {
        return workItem(WORK_ITEM_ID);
    }

    public static WorkItem workItem(String id) {
        WorkItem workItem = new WorkItem();
        workItem.setWorkItemId(id);
        workItem.setSprintId(SPRINT_ID);
        workItem.setCreatedByUserId(CREATOR_USER_ID);
        workItem.setWorkType("TASK");
        workItem.setTitle("Build assignment tests");
        workItem.setDescription("Add starter backend tests");
        workItem.setStatus("NEW");
        workItem.setPriority(WorkItemPriority.HIGH);
        workItem.setEstimatedMinutes(30);
        workItem.setDueDate(LocalDate.of(2026, 5, 15));
        workItem.setCreatedAt(OffsetDateTime.parse("2026-04-29T10:00:00-06:00"));
        workItem.setUpdatedAt(OffsetDateTime.parse("2026-04-29T10:00:00-06:00"));
        return workItem;
    }

    public static AppUser appUser() {
        return appUser(ASSIGNEE_USER_ID);
    }

    public static AppUser appUser(String id) {
        AppUser user = new AppUser();
        user.setUserId(id);
        user.setName("Ada Lovelace");
        user.setEmail(id + "@example.com");
        user.setTelegramUserId("telegram-" + id);
        user.setCreatedAt(OffsetDateTime.parse("2026-04-29T09:00:00-06:00"));
        return user;
    }

    public static WorkItemAssignment assignment() {
        return assignment(workItem(), appUser());
    }

    public static WorkItemAssignment assignment(WorkItem workItem, AppUser user) {
        WorkItemAssignment assignment = new WorkItemAssignment();
        assignment.setAssignmentId(ASSIGNMENT_ID);
        assignment.setWorkItem(workItem);
        assignment.setAssignedUser(user);
        assignment.setAssignmentRole("ASSIGNEE");
        assignment.setAssignedAt(OffsetDateTime.parse("2026-04-29T11:00:00-06:00"));
        return assignment;
    }

    public static WorkItemResponse workItemResponse() {
        return new WorkItemResponse(
                WORK_ITEM_ID,
                SPRINT_ID,
                CREATOR_USER_ID,
                "TASK",
                List.of(assignmentDto()),
                List.of(tagResponse()),
                "Build assignment tests",
                "Add starter backend tests",
                "NEW",
                "HIGH",
                "https://example.com/work/wi-1",
                30,
                LocalDate.of(2026, 5, 15),
                OffsetDateTime.parse("2026-04-29T10:00:00-06:00"),
                OffsetDateTime.parse("2026-04-29T10:00:00-06:00"),
                null
        );
    }

    public static WorkItemAssignmentDto assignmentDto() {
        return new WorkItemAssignmentDto(
                ASSIGNMENT_ID,
                "ASSIGNEE",
                OffsetDateTime.parse("2026-04-29T11:00:00-06:00"),
                null,
                assignedUserDto(),
                null
        );
    }
    
    public static TagResponse tagResponse() {
        return new TagResponse(
                UUID.fromString(TAG_ID), 
                "tag-1", 
                "#112233", 
                "Description"
        );
    }

    public static AssignedUserDto assignedUserDto() {
        return new AssignedUserDto(
                ASSIGNEE_USER_ID,
                "Ada Lovelace",
                ASSIGNEE_USER_ID + "@example.com",
                "telegram-" + ASSIGNEE_USER_ID
        );
    }
}
