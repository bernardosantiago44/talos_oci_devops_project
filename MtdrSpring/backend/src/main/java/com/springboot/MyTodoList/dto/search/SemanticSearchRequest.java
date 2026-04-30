package com.springboot.MyTodoList.dto.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for semantic search queries.
 */
@Getter
@Setter
public class SemanticSearchRequest {

    @NotBlank(message = "Search query must not be blank")
    @Size(min = 2, max = 500, message = "Query must be between 2 and 500 characters")
    private String query;

    /**
     * Optional: maximum number of results to return (defaults to 10).
     */
    private Integer maxResults = 10;
}
