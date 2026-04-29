package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.WorkItemMapper;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.exception.WorkItemNotFoundException;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkItemService {
    private final WorkItemRepository workItemRepository;
    
    public WorkItemService(WorkItemRepository repository) {
        this.workItemRepository = repository;
    }
    
    public List<WorkItemResponse> findAll() {
        return workItemRepository
                .findAll()
                .stream()
                .map(WorkItemMapper::toResponse)
                .toList();
    }
    
    public WorkItemResponse findById(String id) {
        return workItemRepository
                .findById(id)
                .map(WorkItemMapper::toResponse)
                .orElseThrow(() -> new WorkItemNotFoundException(id));
    }
    
    public List<WorkItemResponse> findForUserId(String userId) {
        return workItemRepository
                .findForUserId(userId)
                .stream()
                .map(WorkItemMapper::toResponse)
                .toList();
    }
}
