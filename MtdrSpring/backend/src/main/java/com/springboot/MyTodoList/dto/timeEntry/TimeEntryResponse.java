package com.springboot.MyTodoList.dto.timeEntry;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Time entry data returned after logging time.")
public class TimeEntryResponse {
    @Schema(description = "Work item identifier.", example = "wi-1")
    String workItemId;

    @Schema(description = "User who logged the time.", example = "user-1")
    String userId;

    @Schema(description = "Time spent in minutes.", example = "90")
    Integer minutes;

    @Schema(description = "Time entry note.", example = "Implemented repository tests.")
    String note;

    public TimeEntryResponse(String note, Integer minutes, String userId, String workItemId) {
        this.note = note;
        this.minutes = minutes;
        this.userId = userId;
        this.workItemId = workItemId;
    }
}
