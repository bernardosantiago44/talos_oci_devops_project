package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.tag.CreateTagRequest;
import com.springboot.MyTodoList.dto.tag.TagResponse;
import com.springboot.MyTodoList.dto.tag.UpdateTagRequest;
import com.springboot.MyTodoList.service.TagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tags")
@Tag(name = "Tags", description = "Tag lookup endpoints.")
public class TagsController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TagsController.class);
    private final TagsService tagsService;

    public TagsController(TagsService tagsService) {
        this.tagsService = tagsService;
    }

    @GetMapping
    @Operation(summary = "List tags", description = "Returns all saved tags.")
    public ResponseEntity<List<TagResponse>> get() {
        LOGGER.info("Fetched tags");
        return ResponseEntity.ok(tagsService.findAll());
    }
    
    @PostMapping
    @Operation(summary = "Create a tag", description = "Creates a new tag.")
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody
            CreateTagRequest request
    ) {
        TagResponse response = tagsService.createTag(request);
        LOGGER.info("Tag created: {}", response);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "Update an existing tag", description = "Only provided fields will be updated.")
    public ResponseEntity<TagResponse> updateTag(@PathVariable String id, 
                                                 @Valid @RequestBody UpdateTagRequest request
    ) {
        TagResponse response = tagsService.updateTag(id, request);
        LOGGER.info("Updated tag {}", id);
        
        return ResponseEntity.ok(response);
    }
}
