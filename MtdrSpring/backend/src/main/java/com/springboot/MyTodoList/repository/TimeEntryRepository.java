package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TimeEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

@Repository
@Transactional
@EnableTransactionManagement
public interface TimeEntryRepository extends JpaRepository<TimeEntry, String> {
    Optional<TimeEntry> findByWorkItem_WorkItemIdAndUser_UserId(String workItemId, String userId);
}
