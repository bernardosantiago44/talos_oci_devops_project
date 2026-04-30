package com.springboot.MyTodoList.exception;

/**
 * Thrown when the AI embedding service (Alibaba DashScope) is unreachable
 * or returns an error. The semantic search endpoint uses this to provide
 * a graceful fallback response.
 */
public class AiServiceUnavailableException extends RuntimeException {
    public AiServiceUnavailableException(String message) {
        super(message);
    }

    public AiServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
