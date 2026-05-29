# 1. Use C4 Structurizr for architecture documentation

Date: 2026-05-29

## Status

Accepted

## Context

The project needs a main architecture deliverable that is versioned with the repository and can show system landscape, context, container, component, deployment, and dynamic views. The team also needs the model to align with the actors and responsibilities identified in the component-based thinking exercise: Manager, Developer, and System.

## Decision

We will document the architecture with the C4 model using Structurizr DSL. The source model will live at the repository root in `workspace.dsl` and `model.dsl`, with `model.json` generated from the validated local Structurizr workspace. Architecture decisions will be recorded as ADRs in `doc/arch` using `adr-tools`.

## Consequences

The architecture can be rendered locally with Docker and reviewed as diagrams instead of only text. The DSL becomes the source of truth for diagrams, so changes to architecture documentation must update the model and regenerate `model.json`.
