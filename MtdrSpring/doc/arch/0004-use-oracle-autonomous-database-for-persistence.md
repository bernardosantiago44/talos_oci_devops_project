# 4. Use Oracle Autonomous Database for persistence

Date: 2026-05-29

## Status

Accepted

## Context

The backend persists application data through Spring Data JPA repositories and Oracle JDBC. Terraform provisions an Oracle Autonomous Database, and the Kubernetes backend deployment mounts database wallet credentials through a secret.

## Decision

We will use Oracle Autonomous Database as the system of record for users, work items, assignments, tags, sprints, time entries, and analytics data.

## Consequences

The database platform aligns with the OCI deployment architecture and reduces operational database management for the team. The backend remains responsible for schema compatibility, JDBC configuration, wallet handling, and repository-level data access.
