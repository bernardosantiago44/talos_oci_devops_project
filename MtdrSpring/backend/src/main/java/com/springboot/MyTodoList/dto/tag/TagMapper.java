package com.springboot.MyTodoList.dto.tag;

import com.springboot.MyTodoList.model.Tag;

public class TagMapper {
    public static TagResponse toResponse(Tag tag) {
        return new TagResponse(tag);
    }
}
