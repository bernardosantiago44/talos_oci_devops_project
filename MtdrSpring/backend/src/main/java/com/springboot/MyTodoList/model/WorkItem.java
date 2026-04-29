package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "WORK_ITEM", schema = "CHATBOT_USER")
public class WorkItem {

    @Id
    @Column(name = "WORK_ITEM_ID")
    private String workItemId;

    @Column(name = "SPRINT_ID")
    private String sprintId;

    @Column(name = "CREATED_BY_USER_ID")
    private String createdByUserId;

    @Column(name = "WORK_TYPE")
    private String workType;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION", columnDefinition = "CLOB")
    private String description;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "PRIORITY")
    private String priority;

    @Column(name = "EXTERNAL_LINK")
    private String externalLink;

    @Column(name = "ESTIMATED_MINUTES")
    private Integer estimatedMinutes;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "CREATED_AT")
    private OffsetDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private OffsetDateTime updatedAt;

    @Column(name = "COMPLETED_AT")
    private OffsetDateTime completedAt;


    @ManyToMany
    @JoinTable(
        name = "work_item_assignment",
        schema = "CHATBOT_USER",
        joinColumns = @JoinColumn(name = "WORK_ITEM_ID"), 
        inverseJoinColumns = @JoinColumn(name = "USER_ID") 
    )
    private Set<Assignee> assignedUsers = new HashSet<>();

    public WorkItem() {}

}

