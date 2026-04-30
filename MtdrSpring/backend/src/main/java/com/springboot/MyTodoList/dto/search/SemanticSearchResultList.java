package com.springboot.MyTodoList.dto.search;

import java.util.List;

/**
 * Wrapper for a list of semantic search results.
 * Includes metadata about the search operation.
 */
public record SemanticSearchResultList(
        String query,
        int totalResults,
        boolean aiAvailable,
        String fallbackMessage,
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
