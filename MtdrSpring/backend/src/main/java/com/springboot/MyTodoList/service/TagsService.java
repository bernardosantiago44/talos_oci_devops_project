package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.tag.CreateTagRequest;
import com.springboot.MyTodoList.dto.tag.UpdateTagRequest;
import com.springboot.MyTodoList.dto.tag.TagMapper;
import com.springboot.MyTodoList.dto.tag.TagResponse;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.exception.TagNotFoundException;
import com.springboot.MyTodoList.model.Tag;
import com.springboot.MyTodoList.repository.TagsRepository;
import com.springboot.MyTodoList.util.Validator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TagsService.class);
    private final TagsRepository tagsRepository;

    public TagsService(TagsRepository tagsRepository) {
        this.tagsRepository = tagsRepository;
    }
    
    public List<TagResponse> findAll() {
        return tagsRepository
                .findAll()
                .stream()
                .map(TagMapper::toResponse)
                .toList();
    }
    
    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        validateTagNameDoesNotExist(request.getName().trim());
        validateTagColor(request.getColor());
        
        Tag saved = tagsRepository.save(request.toTag());
        LOGGER.info("Tag created with id {}", saved.getTagId());
        
        return TagMapper.toResponse(saved);
    }
    
    @Transactional
    public TagResponse updateTag(String id, UpdateTagRequest request) {
        ensureTagExistsById(id);
        validateUpdateTagRequest(request);
        Tag tag = tagsRepository
                .findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
        
        if (request.getName() != null) {
            validateTagNameDoesNotExistExcludingId(request.getName(), id);
        }
        
        TagMapper.applyUpdates(tag, request);
        tagsRepository.save(tag);
        LOGGER.info("Updated tag {}: {}", tag.getTagId(), tag);
        
        return TagMapper.toResponse(tag);
    }
    
    @Transactional
    public void deleteTagById(String id) {
        tagsRepository.deleteById(id);
    }
    
    private void validateTagNameDoesNotExist(String name) {
        if (tagsRepository.existsByName(name)) {
            LOGGER.error("A tag with the name {} already exists", name);
            throw new BusinessRuleException("Tag with that name already exists.");
        }
    }
    
    private void validateTagNameDoesNotExistExcludingId(String name, String id) {
        if (tagsRepository.existsByNameAndTagIdNot(name, id)) {
            LOGGER.error("A tag with the name {} already exists and is not tag {}", name, id);
            throw new BusinessRuleException("Tag with that name already exists.");
        }
    }
    
    private void ensureTagExistsById(String id) {
        if (!tagsRepository.existsById(id)) {
            LOGGER.warn("Tag {} does not exist", id);
            throw new TagNotFoundException(id);
        }
    }
    
    private void validateUpdateTagRequest(UpdateTagRequest request) {
        if (request.getName() != null && request.getName().isBlank()) {
            LOGGER.warn("Invalid name in update request.");
            throw new BusinessRuleException("Name can not be empty.");
        }
        
        if (request.getColor() != null && request.getColor().isBlank()) {
            LOGGER.warn("Invalid color in update request: {}", request.getColor());
            throw new BusinessRuleException("Color must not be blank.");
        }
        
        validateTagColor(request.getColor());
    }
    
    private void validateTagColor(String color) {
        if (color != null && !Validator.isValidHexColor(color)) {
            throw new BusinessRuleException("Invalid color: " + color);
        }
    }
}
