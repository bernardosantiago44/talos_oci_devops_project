package com.springboot.MyTodoList.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Schema(description = "Request payload for editing an existing tag. Null fields will remain unchanged. " +
        "Only provided fields will be updated."
)
public class UpdateTagRequest {
    @Schema(description = "Tag name", example = "Important")
    @Nullable
    @Length(max = 100)
    private String name;

    @Schema(description = "Tag hexadecimal color", example = "#5BFFAE")
    @Nullable
    private String color;

    @Schema(description = "Tag description", example = "High importancy")
    @Length(max = 300)
    @Nullable
    private String description;
}
