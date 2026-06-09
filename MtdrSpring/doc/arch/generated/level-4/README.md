# Generated Level 4 diagrams

These PlantUML class diagrams map the compiled Spring Boot implementation to the
Level 3 backend components defined in `model.dsl`. Structurizr DSL remains the
source of truth for the conceptual C4 architecture.

Run the following command from `backend` to refresh all eleven diagrams:

```shell
mvn package
```

The Maven executions include each component's implementation classes and their
direct first-party collaborators. A class can therefore appear in more than one
diagram. The Activity Tracking view represents the services that currently
record activity through application logging.

| Level 3 component | Generated file | Primary implementation mapping |
| --- | --- | --- |
| REST Controllers | `rest-controllers.puml` | Controller and service packages |
| Work Management | `work-management.puml` | Work-item, tag, assignment, and sprint workflows |
| Time Tracking | `time-tracking.puml` | Time-entry controller, service, repository, DTOs, and entities |
| Activity Tracking | `activity-tracking.puml` | Services that currently emit activity logs |
| Bot Interface | `bot-interface.puml` | Telegram bot, client, helpers, commands, labels, and properties |
| Sprint Management | `sprint-management.puml` | Sprint controller, service, repository, DTOs, and entity |
| Assignment Management | `assignment-management.puml` | Work-item assignment workflow and collaborators |
| Reporting and KPI Analytics | `reporting-kpi-analytics.puml` | Analytics controller, service, repository, and projections |
| Semantic Search and AI Integration | `semantic-search-ai-integration.puml` | Semantic search, embedding, AI provider, and work-item collaborators |
| Persistence Repositories | `persistence-repositories.puml` | Repository and model packages |
| Domain Model | `domain-model.puml` | Model and DTO packages |
