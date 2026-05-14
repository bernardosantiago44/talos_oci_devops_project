package com.springboot.MyTodoList.dto.search;

import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single semantic search result: a work item paired with its
 * cosine similarity relevance score (0.0 – 1.0).
 */
@Schema(description = "Single semantic search result.")
public record SemanticSearchResponse(
        @Schema(description = "Matched work item.")
        WorkItemResponse workItem,
        @Schema(description = "Similarity score from 0.0 to 1.0.", example = "0.87")
        double relevanceScore
) {}
