package com.springboot.MyTodoList.dto.search;

import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;

/**
 * A single semantic search result: a work item paired with its
 * cosine similarity relevance score (0.0 – 1.0).
 */
public record SemanticSearchResponse(
        WorkItemResponse workItem,
        double relevanceScore
) {}
