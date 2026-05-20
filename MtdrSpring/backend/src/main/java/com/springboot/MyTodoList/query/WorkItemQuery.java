package com.springboot.MyTodoList.query;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WorkItemQuery {
    @Nullable
    private List<String> status;

    @Nullable
    private String sprintId;

    @Nullable
    private List<String> sprints;

    @Nullable
    private List<String> assignees;

    @Nullable
    private String workType;

    @Nullable
    private String priority;

    @Nullable
    private String search;

    public boolean isEmpty() {
        return hasNoValues(status)
                && isBlank(sprintId)
                && hasNoValues(sprints)
                && hasNoValues(assignees)
                && isBlank(workType)
                && isBlank(priority)
                && isBlank(search);
    }

    private boolean hasNoValues(List<String> values) {
        return values == null || values.stream().allMatch(this::isBlank);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
