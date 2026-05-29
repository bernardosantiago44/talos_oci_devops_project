# 5. Keep external integrations behind the backend

Date: 2026-05-29

## Status

Accepted

## Context

The system integrates with Telegram for bot interactions and with AI providers for semantic search and assistant capabilities. These integrations require credentials, error handling, and business workflow coordination.

## Decision

We will keep Telegram and AI provider integrations behind the Spring Boot backend. The frontend will interact only with the backend API, and the backend will own outbound integration calls, credential usage, fallback behavior, and mapping external responses into application workflows.

## Consequences

Credentials and integration policies stay server-side. The frontend remains simpler and less exposed to external API changes. Backend availability and resilience become more important because user-facing integration behavior depends on the monolith.
