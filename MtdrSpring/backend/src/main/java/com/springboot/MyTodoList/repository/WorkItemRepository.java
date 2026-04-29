package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.WorkItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, String> {
    @Query(value = "SELECT wi.WORK_ITEM_ID, wi.TITLE, wi.STATUS, wi.PRIORITY, wi.DUE_DATE, " +
            "       s.NAME AS SPRINT_NAME " +
            "FROM WORK_ITEM wi " +
            "JOIN WORK_ITEM_ASSIGNMENT wia ON wi.WORK_ITEM_ID = wia.WORK_ITEM_ID " +
            "  AND wia.UNASSIGNED_AT IS NULL " +
            "JOIN APP_USER u ON wia.USER_ID = u.USER_ID " +
            "LEFT JOIN SPRINT s ON wi.SPRINT_ID = s.SPRINT_ID " +
            "WHERE u.TELEGRAM_USER_ID = :userId " +
            "ORDER BY wi.CREATED_AT DESC, ", 
    nativeQuery = true)
    List<WorkItem> findForUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {
            "assignments",
            "assignments.assignedUser",
            "assignments.assignedByUser"
    })
    List<WorkItem> findAll();
}
