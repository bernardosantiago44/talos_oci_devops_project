package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "WORK_ITEM_TAG")
@Getter
@Setter
public class WorkItemTag {

    @EmbeddedId
    private WorkItemTagId id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("workItemId")
    @JoinColumn(name = "WORK_ITEM_ID", nullable = false)
    private WorkItem workItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "TAG_ID", nullable = false)
    private Tag tag;
    
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public WorkItemTag() {}

    public WorkItemTag(@NonNull WorkItem workItem, @NonNull Tag tag) {
        this.workItem = workItem;
        this.tag = tag;
        this.id = new WorkItemTagId(workItem.getWorkItemId(), tag.getTagId());
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}