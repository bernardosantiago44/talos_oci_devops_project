package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.*;
import com.springboot.MyTodoList.repository.TagsRepository;
import com.springboot.MyTodoList.repository.WorkItemTagAssignmentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;

import java.util.List;

@Service
public class WorkItemTagAssignmentService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkItemTagAssignmentService.class);
    private final WorkItemTagAssignmentRepository assignmentRepository;
    private final TagsRepository tagsRepository;

    public WorkItemTagAssignmentService(WorkItemTagAssignmentRepository assignmentRepository, TagsRepository tagsRepository) {
        this.assignmentRepository = assignmentRepository;
        this.tagsRepository = tagsRepository;
    }
    
    @Transactional
    public void replaceTags(WorkItem workItem, List<String> tagIds) {
        var uniqueIds = tagIds.stream().distinct().toList();
        
        List<Tag> tags = tagsRepository.findAllById(uniqueIds);
        
        assignmentRepository.deleteByWorkItem_WorkItemId(workItem.getWorkItemId());
        workItem.getTags().clear();
        
        for (Tag tag : tags) {
            WorkItemTag workItemTag = saveAssignmentToRepository(workItem, tag);
            workItem.getTags().add(workItemTag);
        }
        LOGGER.info("Replaced tags for workItemId: {} count: {}", workItem.getWorkItemId(), tags.size());
    }
    
    private WorkItemTag saveAssignmentToRepository(WorkItem workItem, Tag tag) {
        WorkItemTag assignment = new WorkItemTag();
        assignment.setId(new WorkItemTagId(workItem.getWorkItemId(), tag.getTagId()));
        assignment.setWorkItem(workItem);
        assignment.setTag(tag);
        
        return assignment;
    }
}
