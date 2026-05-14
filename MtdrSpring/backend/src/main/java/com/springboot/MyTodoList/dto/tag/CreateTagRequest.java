package com.springboot.MyTodoList.dto.tag;

import com.springboot.MyTodoList.model.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Request payload for creating a new tag")
public class CreateTagRequest {
    @Schema(description = "Tag name", example = "Important")
    @NotBlank
    @Length(max = 100)
    private String name;
    
    @Schema(description = "Tag hexadecimal color", example = "#5BFFAE")
    @Length(max = 30)
    private String color;
    
    @Schema(description = "Tag description", example = "High importancy")
    @Length(max = 300)
    @Nullable
    private String description;
    
    public Tag toTag() {
        Tag tag = new Tag();
        
        tag.setTagId(UUID.randomUUID().toString());
        tag.setName(this.name);
        if (this.description != null) tag.setDescription(this.description.strip());
        if (this.color != null) tag.setColor(this.color.strip());
        tag.setCreatedAt(OffsetDateTime.now());
        
        return tag;
    }
}
