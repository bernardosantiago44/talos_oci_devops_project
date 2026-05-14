package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.*;
import com.springboot.MyTodoList.repository.TagsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;

import java.util.List;

@Service
public class WorkItemTagAssignmentService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkItemTagAssignmentService.class);
    @PersistenceContext
    private EntityManager entityManager;
    private final TagsRepository tagsRepository;

    public WorkItemTagAssignmentService(TagsRepository tagsRepository, EntityManager entityManager) {
        this.tagsRepository = tagsRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public void replaceTags(WorkItem workItem, List<String> tagIds) {
        var uniqueIds = tagIds.stream().distinct().toList();
        List<Tag> tags = tagsRepository.findAllById(uniqueIds);
        
        workItem.getTags().clear();
        entityManager.flush();
        
        for (Tag tag : tags) {
            WorkItemTag workItemTag = new WorkItemTag(workItem, tag);
            workItem.getTags().add(workItemTag);
        }
        
        LOGGER.info("Replaced tags for workItem: {}", workItem.getWorkItemId());
    }
    
    private WorkItemTag saveAssignmentToRepository(WorkItem workItem, Tag tag) {
        WorkItemTag assignment = new WorkItemTag();
        assignment.setId(new WorkItemTagId(workItem.getWorkItemId(), tag.getTagId()));
        assignment.setWorkItem(workItem);
        assignment.setTag(tag);
        
        return assignment;
    }
}
