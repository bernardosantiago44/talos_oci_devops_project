package com.springboot.MyTodoList.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Configuration for the Alibaba Cloud DashScope Embedding API (Qwen).
 * Reads API key and URL from application.properties.
 */
@Getter
@Configuration
public class AlibabaEmbeddingConfig {

    @Value("${alibaba.api.key:}")
    private String apiKey;

    @Value("${alibaba.api.url:https://dashscope-intl.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding}")
    private String apiUrl;

    @Value("${alibaba.embedding.model:text-embedding-v3}")
    private String model;

    /**
     * Returns true if the Alibaba API key is configured and non-empty.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
