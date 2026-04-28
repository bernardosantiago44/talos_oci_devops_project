package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.sprint.SprintMapper;
import com.springboot.MyTodoList.dto.sprint.SprintResponse;
import com.springboot.MyTodoList.exception.SprintNotFoundException;
import com.springboot.MyTodoList.repository.SprintRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SprintService {
    private static final Logger log = LoggerFactory.getLogger(SprintService.class);
    
    private final SprintRepository sprintRepository;
    
    public SprintService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }
    
    public List<SprintResponse> findAll() {
        return sprintRepository
                .findAll()
                .stream()
                .map(SprintMapper::toResponse)
                .toList();
    }
    
    public SprintResponse findById(String id) {
        return sprintRepository
                .findById(id)
                .map(SprintMapper::toResponse)
                .orElseThrow(() -> new SprintNotFoundException(id));
    }
}
