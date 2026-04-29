package com.springboot.MyTodoList.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "WORK_ITEM_ASSIGNMENT")
public class Assignee {
    @Id
    @Column(name = "assignment_id")
    private String assignmentId;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    /*@Column(name = "assignment_role")
    private String assignmentRole;
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    @Column(name = "unassigned_at")
    LocalDateTime unassignedAt;*/
}
