package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.WorkItemAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkItemAssignmentRepository extends JpaRepository<WorkItemAssignment, String> {
    List<WorkItemAssignment> findByWorkItem_WorkItemId(String workItemId);

    Optional<WorkItemAssignment> findByWorkItem_WorkItemIdAndAssignedUser_UserId(String workItemId, String userId);

    boolean existsByWorkItem_WorkItemIdAndAssignedUser_UserId(String workItemId, String userId);

    void deleteByWorkItem_WorkItemId(String workItemId);
}
