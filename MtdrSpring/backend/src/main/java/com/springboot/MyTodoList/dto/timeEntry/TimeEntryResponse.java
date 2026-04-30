package com.springboot.MyTodoList.dto.timeEntry;

import lombok.Getter;

@Getter
public class TimeEntryResponse {
    String workItemId;
    String userId;
    Integer minutes;
    String note;

    public TimeEntryResponse(String note, Integer minutes, String userId, String workItemId) {
        this.note = note;
        this.minutes = minutes;
        this.userId = userId;
        this.workItemId = workItemId;
    }
}
