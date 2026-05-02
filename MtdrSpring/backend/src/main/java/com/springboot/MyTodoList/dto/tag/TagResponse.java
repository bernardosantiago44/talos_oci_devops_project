package com.springboot.MyTodoList.dto.tag;

import com.springboot.MyTodoList.model.Tag;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TagResponse {
    private UUID tagId;
    private String name;
    @Nullable 
    private String color;
    @Nullable 
    private String description;
    private OffsetDateTime createdAt;

    public TagResponse(Tag tag) {
        this.tagId = UUID.fromString(tag.getTagId());
        this.name = tag.getName();
        this.color = tag.getColor();
        this.description = tag.getDescription();
        this.createdAt = tag.getCreatedAt();
    }
    
    public TagResponse(UUID tagId, String name, @Nullable String color, @Nullable String description, OffsetDateTime createdAt) {
        this.tagId = tagId;
        this.name = name;
        this.color = color;
        this.description = description;
        this.createdAt = createdAt;
    }
}
