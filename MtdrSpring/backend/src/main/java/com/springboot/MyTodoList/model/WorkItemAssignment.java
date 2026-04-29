package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "WORK_ITEM_ASSIGNMENT", schema = "CHATBOT_USER")
public class WorkItemAssignment {

    @Id
    @Column(name = "ASSIGNMENT_ID")
    private String assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WORK_ITEM_ID")
    private WorkItem workItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private AppUser assignedUser;

    @Column(name = "ASSIGNMENT_ROLE")
    private String assignmentRole;

    @Column(name = "ASSIGNED_AT")
    private OffsetDateTime assignedAt;

    @Column(name = "UNASSIGNED_AT")
    private OffsetDateTime unassignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_BY_USER_ID")
    private AppUser assignedByUser;

    public WorkItemAssignment() {}
}