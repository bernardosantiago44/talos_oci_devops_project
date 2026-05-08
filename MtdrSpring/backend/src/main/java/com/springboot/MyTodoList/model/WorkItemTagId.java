package com.springboot.MyTodoList.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.Objects;

@Getter
@Setter
@Embeddable
@Table(name = "WORK_ITEM_TAG", schema = "CHATBOT_USER")
public class WorkItemTagId {
    @Column(name = "WORK_ITEM_ID", length = 36)
    @Length(min = 36, max = 36)
    private String workItemId;
    
    @Column(name = "TAG_ID", length = 36)
    private String tagId;

    public WorkItemTagId(String workItemId, String tagId) {
        this.workItemId = workItemId;
        this.tagId = tagId;
    }

    protected WorkItemTagId() {}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WorkItemTagId that)) return false;
        return Objects.equals(workItemId, that.workItemId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workItemId, tagId);
    }
}
