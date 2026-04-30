package com.springboot.MyTodoList.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.config.AlibabaEmbeddingConfig;
import com.springboot.MyTodoList.exception.AiServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Service responsible for generating text embeddings via Alibaba Cloud
 * DashScope API (Qwen text-embedding-v3).
 *
 * The generated embeddings are float arrays representing the semantic
 * meaning of the input text in high-dimensional vector space.
 */
@Service
public class EmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private static final int TIMEOUT_SECONDS = 30;

    private final AlibabaEmbeddingConfig config;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public EmbeddingService(AlibabaEmbeddingConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Generates a vector embedding for the given text input.
     *
     * @param text the text to embed
     * @return a float array representing the embedding vector
     * @throws AiServiceUnavailableException if the API call fails
     */
    public float[] generateEmbedding(String text) {
        if (!config.isConfigured()) {
            throw new AiServiceUnavailableException(
                    "Alibaba DashScope API key is not configured. "
                    + "Set 'alibaba.api.key' in application.properties."
            );
        }

        try {
            String requestBody = buildRequestBody(text);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getApiUrl()))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("DashScope API returned status {}: {}",
                        response.statusCode(), response.body());
                throw new AiServiceUnavailableException(
                        "DashScope API error (HTTP " + response.statusCode() + ")"
                );
            }

            return parseEmbeddingResponse(response.body());

        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            throw new AiServiceUnavailableException(
                    "Could not reach Alibaba DashScope API: " + e.getMessage(), e
            );
        }
    }

    /**
     * Builds the JSON request body for the DashScope embedding API.
     *
     * Request format:
     * {
     *   "model": "text-embedding-v3",
     *   "input": { "texts": ["your text here"] },
     *   "parameters": { "dimension": 1024, "output_type": "dense" }
     * }
     */
    private String buildRequestBody(String text) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("model", config.getModel());

            var input = objectMapper.createObjectNode();
            var texts = objectMapper.createArrayNode();
            texts.add(text);
            input.set("texts", texts);
            root.set("input", input);

            var parameters = objectMapper.createObjectNode();
            parameters.put("dimension", 1024);
            parameters.put("output_type", "dense");
            root.set("parameters", parameters);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new AiServiceUnavailableException("Failed to build request body", e);
        }
    }

    /**
     * Parses the DashScope embedding response.
     *
     * Response format:
     * {
     *   "output": {
     *     "embeddings": [
     *       { "text_index": 0, "embedding": [0.123, -0.456, ...] }
     *     ]
     *   },
     *   "usage": { "total_tokens": 5 },
     *   "request_id": "..."
     * }
     */
    private float[] parseEmbeddingResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode embeddings = root.path("output").path("embeddings");

            if (embeddings.isMissingNode() || !embeddings.isArray() || embeddings.isEmpty()) {
                log.error("Unexpected response format from DashScope: {}", responseBody);
                throw new AiServiceUnavailableException("Invalid embedding response format");
            }

            JsonNode embeddingArray = embeddings.get(0).path("embedding");
            float[] vector = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                vector[i] = (float) embeddingArray.get(i).asDouble();
            }

            log.debug("Generated embedding with {} dimensions", vector.length);
            return vector;

        } catch (AiServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse embedding response: {}", responseBody, e);
            throw new AiServiceUnavailableException("Failed to parse embedding response", e);
        }
    }
}
