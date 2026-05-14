package com.springboot.MyTodoList.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for semantic search queries.
 */
@Getter
@Setter
@Schema(description = "Request payload for semantic work item search.")
public class SemanticSearchRequest {

    @Schema(description = "Natural-language search query.", example = "tasks related to analytics dashboard charts", minLength = 2, maxLength = 500)
    @NotBlank(message = "Search query must not be blank")
    @Size(min = 2, max = 500, message = "Query must be between 2 and 500 characters")
    private String query;

    /**
     * Optional: maximum number of results to return (defaults to 10).
     */
    @Schema(description = "Maximum number of results to return.", example = "10", defaultValue = "10")
    private Integer maxResults = 10;
}
