package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.WorkItem;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, String> {
    @EntityGraph(attributePaths = {
            "assignments",
            "assignments.assignedUser",
            "assignments.assignedByUser"
    })
    @Query("""
        SELECT wi FROM WorkItem wi
        JOIN wi.assignments wia
        JOIN wia.assignedUser u
        WHERE u.telegramUserId = :userId
          AND wia.unassignedAt IS NULL
        ORDER BY wi.createdAt DESC
    """)
    List<WorkItem> findByTelegramUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {
            "assignments",
            "assignments.assignedUser",
            "assignments.assignedByUser"
    })
    @NonNull
    List<WorkItem> findAll();

    @Override
    @EntityGraph(attributePaths = {
            "assignments",
            "assignments.assignedUser",
            "assignments.assignedByUser"
    })
    @NonNull
    Optional<WorkItem> findById(@NonNull String id);
}
