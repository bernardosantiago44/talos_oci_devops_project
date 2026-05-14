package com.springboot.MyTodoList.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Wrapper for a list of semantic search results.
 * Includes metadata about the search operation.
 */
@Schema(description = "Semantic search response with metadata and ranked results.")
public record SemanticSearchResultList(
        @Schema(description = "Original search query.", example = "analytics dashboard")
        String query,
        @Schema(description = "Number of returned results.", example = "3")
        int totalResults,
        @Schema(description = "Whether AI embeddings were available for this search.", example = "true")
        boolean aiAvailable,
        @Schema(description = "Fallback message when keyword search was used.", example = "AI service unavailable; returned keyword-based results.")
        String fallbackMessage,
        @Schema(description = "Ranked search results.")
        List<SemanticSearchResponse> results
) {
    /**
     * Factory for a successful search result.
     */
    public static SemanticSearchResultList success(String query, List<SemanticSearchResponse> results) {
        return new SemanticSearchResultList(query, results.size(), true, null, results);
    }

    /**
     * Factory for a fallback when AI is unavailable — returns keyword-based results.
     */
    public static SemanticSearchResultList fallback(String query, List<SemanticSearchResponse> results, String message) {
        return new SemanticSearchResultList(query, results.size(), false, message, results);
    }
}
