package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.WorkItem.WorkItemMapper;
import com.springboot.MyTodoList.dto.WorkItem.WorkItemResponse;
import com.springboot.MyTodoList.dto.search.SemanticSearchResponse;
import com.springboot.MyTodoList.dto.search.SemanticSearchResultList;
import com.springboot.MyTodoList.exception.AiServiceUnavailableException;
import com.springboot.MyTodoList.model.WorkItem;
import com.springboot.MyTodoList.repository.WorkItemRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementing RF-005: Semantic Task Search.
 *
 * Maintains an in-memory vector index of work item embeddings.
 * When a user submits a natural-language query, it:
 * 1. Converts the query into an embedding vector via EmbeddingService.
 * 2. Computes cosine similarity against all indexed work items.
 * 3. Returns the top-N most semantically relevant results.
 *
 * Falls back to keyword search if the AI service is unavailable.
 */
@Service
public class SemanticSearchService {
    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);

    private final EmbeddingService embeddingService;
    private final WorkItemRepository workItemRepository;

    /**
     * In-memory vector store: workItemId → embedding vector.
     */
    private final ConcurrentHashMap<String, float[]> vectorIndex = new ConcurrentHashMap<>();

    public SemanticSearchService(EmbeddingService embeddingService,
                                  WorkItemRepository workItemRepository) {
        this.embeddingService = embeddingService;
        this.workItemRepository = workItemRepository;
    }

    /**
     * Rebuilds the entire vector index on startup.
     * This is safe for small-to-medium datasets.
     */
    @PostConstruct
    public void buildIndex() {
        log.info("Building semantic search index...");
        try {
            List<WorkItem> allItems = workItemRepository.findAll();
            int indexed = 0;

            for (WorkItem item : allItems) {
                try {
                    String text = buildSearchableText(item);
                    float[] embedding = embeddingService.generateEmbedding(text);
                    vectorIndex.put(item.getWorkItemId(), embedding);
                    indexed++;
                } catch (AiServiceUnavailableException e) {
                    log.warn("AI service unavailable during indexing. Indexed {} of {} items.",
                            indexed, allItems.size());
                    break;
                } catch (Exception e) {
                    log.warn("Failed to index work item {}: {}", item.getWorkItemId(), e.getMessage());
                }
            }

            log.info("Semantic search index built: {}/{} items indexed", indexed, allItems.size());
        } catch (Exception e) {
            log.warn("Could not build semantic search index on startup: {}", e.getMessage());
        }
    }

    /**
     * Indexes or re-indexes a single work item (called after create/update).
     */
    public void indexWorkItem(WorkItem item) {
        try {
            String text = buildSearchableText(item);
            float[] embedding = embeddingService.generateEmbedding(text);
            vectorIndex.put(item.getWorkItemId(), embedding);
            log.debug("Indexed work item: {}", item.getWorkItemId());
        } catch (Exception e) {
            log.warn("Failed to index work item {}: {}", item.getWorkItemId(), e.getMessage());
        }
    }

    /**
     * Removes a work item from the index (called after delete).
     */
    public void removeFromIndex(String workItemId) {
        vectorIndex.remove(workItemId);
    }

    /**
     * Performs a semantic search against the vector index.
     * Falls back to keyword search if AI is unavailable.
     *
     * @param query      the user's natural language query
     * @param maxResults maximum number of results to return
     * @return a SemanticSearchResultList with ranked results
     */
    public SemanticSearchResultList search(String query, int maxResults) {
        try {
            return performSemanticSearch(query, maxResults);
        } catch (AiServiceUnavailableException e) {
            log.warn("AI service unavailable, falling back to keyword search: {}", e.getMessage());
            return performKeywordFallback(query, maxResults, e.getMessage());
        }
    }

    // ─── Semantic search (vector similarity) ──────────────────────────

    private SemanticSearchResultList performSemanticSearch(String query, int maxResults) {
        float[] queryEmbedding = embeddingService.generateEmbedding(query);

        if (vectorIndex.isEmpty()) {
            log.info("Vector index is empty; returning empty results");
            return SemanticSearchResultList.success(query, List.of());
        }

        // Compute similarities
        List<Map.Entry<String, Double>> scored = new ArrayList<>();
        for (Map.Entry<String, float[]> entry : vectorIndex.entrySet()) {
            double similarity = cosineSimilarity(queryEmbedding, entry.getValue());
            scored.add(Map.entry(entry.getKey(), similarity));
        }

        // Sort descending by similarity
        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Collect top result ids and scores first, then fetch work items in one batch
        List<SemanticSearchResponse> results = new ArrayList<>();
        int limit = Math.min(maxResults, scored.size());
        List<String> topIds = new ArrayList<>();
        Map<String, Double> scoresById = new HashMap<>();

        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> scoredEntry = scored.get(i);
            // Only include results with meaningful similarity (> 0.3 threshold)
            if (scoredEntry.getValue() < 0.3) break;

            String workItemId = scoredEntry.getKey();
            topIds.add(workItemId);
            scoresById.put(workItemId, Math.round(scoredEntry.getValue() * 1000.0) / 1000.0);
        }

        Map<String, WorkItem> itemsById = new HashMap<>();
        for (WorkItem item : workItemRepository.findAllById(topIds)) {
            itemsById.put(item.getWorkItemId(), item);
        }

        for (String workItemId : topIds) {
            WorkItem item = itemsById.get(workItemId);
            if (item != null) {
                WorkItemResponse response = WorkItemMapper.toResponse(item);
                results.add(new SemanticSearchResponse(
                        response,
                        scoresById.get(workItemId)
                ));
            }
        }

        log.info("Semantic search for '{}' returned {} results", query, results.size());
        return SemanticSearchResultList.success(query, results);
    }

    // ─── Keyword fallback ─────────────────────────────────────────────

    private SemanticSearchResultList performKeywordFallback(String query, int maxResults, String reason) {
        List<WorkItem> allItems = workItemRepository.findAll();
        String lowerQuery = query.toLowerCase();
        String[] tokens = lowerQuery.split("\\s+");

        List<SemanticSearchResponse> results = allItems.stream()
                .map(item -> {
                    String searchableText = buildSearchableText(item).toLowerCase();
                    // Score = number of query tokens found in the text
                    long matchCount = Arrays.stream(tokens)
                            .filter(searchableText::contains)
                            .count();
                    double score = tokens.length > 0 ? (double) matchCount / tokens.length : 0;
                    return Map.entry(item, score);
                })
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(maxResults)
                .map(e -> new SemanticSearchResponse(
                        WorkItemMapper.toResponse(e.getKey()),
                        e.getValue()
                ))
                .toList();

        String fallbackMsg = "AI semantic search is temporarily unavailable (" + reason
                + "). Showing keyword-based results instead.";

        return SemanticSearchResultList.fallback(query, results, fallbackMsg);
    }

    // ─── Utility methods ──────────────────────────────────────────────

    /**
     * Builds a single searchable text representation from a work item's
     * title, description, status, priority, and work type.
     */
    private String buildSearchableText(WorkItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.getTitle() != null) sb.append(item.getTitle()).append(". ");
        if (item.getDescription() != null) sb.append(item.getDescription()).append(". ");
        if (item.getStatus() != null) sb.append("Status: ").append(item.getStatus()).append(". ");
        if (item.getPriority() != null) sb.append("Priority: ").append(item.getPriority()).append(". ");
        if (item.getWorkType() != null) sb.append("Type: ").append(item.getWorkType()).append(".");
        return sb.toString().trim();
    }

    /**
     * Computes cosine similarity between two vectors.
     * Returns a value between -1 and 1, where 1 means identical direction.
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0;

        double dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0 : dotProduct / denominator;
    }
}
