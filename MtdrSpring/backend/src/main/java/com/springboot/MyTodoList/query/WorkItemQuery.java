package com.springboot.MyTodoList.query;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkItemQuery {
    @Nullable
    private String status;
    
    public boolean isEmpty() {
        return status == null;
    }
}
