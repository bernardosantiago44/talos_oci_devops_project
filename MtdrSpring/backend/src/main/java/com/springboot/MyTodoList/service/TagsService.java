package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.tag.CreateTagRequest;
import com.springboot.MyTodoList.dto.tag.TagMapper;
import com.springboot.MyTodoList.dto.tag.TagResponse;
import com.springboot.MyTodoList.exception.BusinessRuleException;
import com.springboot.MyTodoList.model.Tag;
import com.springboot.MyTodoList.repository.TagsRepository;
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
        Tag saved = tagsRepository.save(request.toTag());
        LOGGER.info("Tag created with id {}", saved.getTagId());
        
        return TagMapper.toResponse(saved);
    }
    
    private void validateTagNameDoesNotExist(String name) {
        if (tagsRepository.existsByName(name)) {
            LOGGER.error("A tag with the name {} already exists", name);
            throw new BusinessRuleException("Tag with that name already exists.");
        }
    }
}
