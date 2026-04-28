package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(name = "SPRINT", schema = "CHATBOT_USER")
public class Sprint {

    @Id
    @Column(name = "SPRINT_ID")
    private String sprintId;

    @Column(name = "TEAM_ID")
    private String teamId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "GOAL")
    private String goal;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_AT")
    private OffsetDateTime createdAt;

    @Column(name = "CREATED_BY_USER_ID")
    private String createdByUserId;

    public Sprint() {}

}
