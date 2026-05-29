# 2. Use a monolithic layered architecture

Date: 2026-05-29

## Status

Accepted

## Context

MtdrSpring supports a cohesive work management domain: work items, assignments, sprints, time entries, tags, reporting, KPI analytics, semantic search, and Telegram interactions. The codebase is implemented as a single Spring Boot application with controllers, services, repositories, and JPA domain entities.

## Decision

We will use one backend monolith with a layered internal architecture: REST controllers handle HTTP concerns, services own business workflows, repositories encapsulate persistence, and the domain model represents persisted project data. We choose this because the team is small, the domain boundaries are strongly related, deployment is simpler, shared transactions are easier to manage, and distributed-system overhead is not justified.

## Consequences

The system can be built, tested, deployed, and operated as one backend deployable. Internal components must remain modular so the monolith does not become a tightly coupled codebase. If independent scaling, independent release cycles, or strict domain isolation become necessary later, the component boundaries documented in Structurizr can guide extraction.
