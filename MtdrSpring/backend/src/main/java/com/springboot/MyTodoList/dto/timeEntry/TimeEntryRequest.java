package com.springboot.MyTodoList.dto.timeEntry;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for logging time against a work item.")
public class TimeEntryRequest {
    @Schema(description = "Work item identifier.", example = "wi-1")
    @NotBlank 
    String workItemId;
    
    @Schema(description = "User logging the time.", example = "user-1")
    @NotBlank 
    String userId;
    
    @Schema(description = "Time spent in minutes.", example = "90", minimum = "1")
    @NotNull 
    @Min(1) 
    Integer minutes;

    @Schema(description = "Optional note for the time entry.", example = "Implemented repository tests.")
    private String note;
    
    public String getNote() {
        if (note == null) return "";
        return note;
    }
}
