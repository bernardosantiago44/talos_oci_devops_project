package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.model.WorkItemTag;
import com.springboot.MyTodoList.model.WorkItemTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkItemTagAssignmentRepository extends JpaRepository<WorkItemTag, WorkItemTagId> {
    void deleteByWorkItem_WorkItemId(String workItemId);
}
