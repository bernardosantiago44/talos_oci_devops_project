package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "TIME_ENTRY", schema = "CHATBOT_USER")
public final class TimeEntry {
    @Id
    @Column(name = "TIME_ENTRY_ID")
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "WORK_ITEM_ID")
    private WorkItem workItem;
    
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private AppUser user;
    
    @Column(name = "MINUTES")
    private Integer minutes;
    
    @Column(name = "STARTED_AT")
    private OffsetDateTime startedAt;
    
    @Column(name = "ENDED_AT")
    private OffsetDateTime endedAt;
    
    @Column(name = "CREATED_AT")
    private OffsetDateTime createdAt;
    
    @Lob
    @Column(name = "NOTE")
    private String note;
}
