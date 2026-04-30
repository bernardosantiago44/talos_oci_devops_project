package com.springboot.MyTodoList.dto.timeEntry;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeEntryRequest {
    @NotBlank 
    String workItemId;
    
    @NotBlank 
    String userId;
    
    @NotNull 
    @Min(1) 
    Integer minutes;
    private String note;
    
    public String getNote() {
        if (note == null) return "";
        return note;
    }
}
