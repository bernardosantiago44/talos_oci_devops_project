package com.springboot.MyTodoList.dto.tag;

import com.springboot.MyTodoList.model.Tag;

public class TagMapper {
    public static TagResponse toResponse(Tag tag) {
        return new TagResponse(tag);
    }
    
    public static void applyUpdates(Tag tag, UpdateTagRequest request) {
        if (request.getName() != null) {
            tag.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }
        
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
    }
}
