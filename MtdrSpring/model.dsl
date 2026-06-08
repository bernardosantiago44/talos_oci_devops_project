model {
    manager = person "Manager" "Plans sprints, assigns work, monitors team progress, and analyzes KPIs."
    developer = person "Developer" "Manages assigned work, tracks time, reports progress or blockers, and uses Telegram for quick task interactions."
    systemActor = person "System" "Represents automated deployment, metric computation, activity logging, and project data storage concerns."

    telegram = softwareSystem "Telegram Bot API" "External messaging platform used by developers to interact with the bot." {
        tags "External"
    }
    aiProviders = softwareSystem "AI Providers" "External AI and embedding providers used for semantic search and assistant capabilities." {
        tags "External"
    }
    ociPlatform = softwareSystem "Oracle Cloud Infrastructure" "Cloud platform that hosts the containerized application, Kubernetes cluster, networking, registry, object storage, and database." {
        tags "External"
    }
    ciCdPipeline = softwareSystem "CI/CD Pipeline" "Builds, packages, and deploys application containers and infrastructure definitions." {
        tags "External"
    }

    mtdrSpring = softwareSystem "MtdrSpring" "Todo and work management platform for sprint planning, work assignment, time tracking, analytics, and Telegram interactions." {
        frontend = container "Frontend Web Application" "Browser-based UI for work items, sprints, tags, assignments, time entries, and analytics dashboards." "React, TypeScript, Nginx"
        backend = container "Backend API Layered Monolith" "Single Spring Boot deployable containing REST APIs, business services, Telegram bot integration, analytics, semantic search, and persistence access." "Java 25, Spring Boot" {
            restControllers = component "REST Controllers" "HTTP entrypoints for work items, users, sprints, tags, time entries, analytics, and semantic search." "Spring MVC Controllers" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/rest-controllers.puml"
            }
            workManagement = component "Work Management" "Creates, updates, searches, organizes, and deletes work items such as tasks, issues, bugs, and features." "Spring Service" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/work-management.puml"
            }
            timeTracking = component "Time Tracking" "Registers estimates, actual time, work sessions, and time notes for assigned work." "Spring Service" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/time-tracking.puml"
            }
            activityTracking = component "Activity Tracking" "Records relevant user actions, status changes, comments, blockers, and system activity for visibility." "Spring Service / Logging" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/activity-tracking.puml"
            }
            botInterface = component "Bot Interface" "Receives Telegram commands and translates them into backend use cases." "Telegram Bots Spring Boot" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/bot-interface.puml"
            }
            sprintManagement = component "Sprint Management" "Reads sprint definitions, goals, scope, and sprint status used by planning and work workflows." "Spring Service" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/sprint-management.puml"
            }
            assignmentManagement = component "Assignment Management" "Assigns and unassigns work items to team members with ownership tracking." "Spring Service" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/assignment-management.puml"
            }
            reportingAnalytics = component "Reporting and KPI Analytics" "Calculates and exposes productivity indicators, sprint velocity, team progress, and estimate-vs-actual data." "Spring Service" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/reporting-kpi-analytics.puml"
            }
            semanticSearch = component "Semantic Search and AI Integration" "Builds an in-memory vector index, requests embeddings, and returns semantically relevant work items." "Spring Service" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/semantic-search-ai-integration.puml"
            }
            persistenceRepositories = component "Persistence Repositories" "Encapsulates database access for users, work items, assignments, tags, sprints, time entries, and analytics queries." "Spring Data JPA" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/persistence-repositories.puml"
            }
            domainModel = component "Domain Model" "JPA entities and DTO mappers representing work items, users, assignments, tags, sprints, time entries, and analytics projections." "Java, JPA" {
                url "https://github.com/bernardosantiago44/talos_oci_devops_project/blob/arch/plantuml/MtdrSpring/doc/arch/generated/level-3/domain-model.puml"
            }
        }
        database = container "Oracle Autonomous Database" "Persists users, work items, assignments, tags, sprints, time entries, activity history, and analytics data." "Oracle Autonomous Database"
    }

    manager -> mtdrSpring "Plans and monitors work"
    developer -> mtdrSpring "Manages tasks and tracks progress"
    systemActor -> mtdrSpring "Triggers automated operational workflows"
    mtdrSpring -> ociPlatform "Runs on"

    manager -> mtdrSpring.frontend "Uses" "HTTPS"
    developer -> mtdrSpring.frontend "Uses" "HTTPS"
    developer -> telegram "Sends bot commands" "HTTPS"
    systemActor -> ciCdPipeline "Triggers builds and deployments"
    ciCdPipeline -> ociPlatform "Publishes images and applies infrastructure changes" "OCI APIs, kubectl, Terraform"

    telegram -> mtdrSpring.backend "Delivers bot updates" "HTTPS"
    mtdrSpring.frontend -> mtdrSpring.backend "Calls REST APIs" "JSON/HTTPS"
    mtdrSpring.frontend -> mtdrSpring.backend.restControllers "Sends API requests" "JSON/HTTPS"
    mtdrSpring.backend -> mtdrSpring.database "Reads and writes project data" "JDBC"
    mtdrSpring.backend -> aiProviders "Requests chat assistance and embeddings" "HTTPS"
    ociPlatform -> mtdrSpring.frontend "Hosts container"
    ociPlatform -> mtdrSpring.backend "Hosts container"
    ociPlatform -> mtdrSpring.database "Provides managed database"

    mtdrSpring.backend.restControllers -> mtdrSpring.backend.workManagement "Routes work item requests"
    mtdrSpring.backend.restControllers -> mtdrSpring.backend.timeTracking "Routes time entry requests"
    mtdrSpring.backend.restControllers -> mtdrSpring.backend.sprintManagement "Routes sprint requests"
    mtdrSpring.backend.restControllers -> mtdrSpring.backend.assignmentManagement "Routes assignment requests"
    mtdrSpring.backend.restControllers -> mtdrSpring.backend.reportingAnalytics "Routes analytics requests"
    mtdrSpring.backend.restControllers -> mtdrSpring.backend.semanticSearch "Routes semantic search requests"
    telegram -> mtdrSpring.backend.botInterface "Delivers bot updates" "HTTPS"
    mtdrSpring.backend.botInterface -> mtdrSpring.backend.workManagement "Looks up developer work"
    mtdrSpring.backend.botInterface -> mtdrSpring.backend.timeTracking "Records time from bot commands"
    mtdrSpring.backend.workManagement -> mtdrSpring.backend.assignmentManagement "Coordinates assignee changes"
    mtdrSpring.backend.workManagement -> mtdrSpring.backend.sprintManagement "Validates sprint references"
    mtdrSpring.backend.workManagement -> mtdrSpring.backend.activityTracking "Records work item changes"
    mtdrSpring.backend.assignmentManagement -> mtdrSpring.backend.activityTracking "Records assignment activity"
    mtdrSpring.backend.timeTracking -> mtdrSpring.backend.activityTracking "Records time-entry activity"
    mtdrSpring.backend.reportingAnalytics -> mtdrSpring.backend.persistenceRepositories "Queries metrics and reporting data"
    mtdrSpring.backend.semanticSearch -> mtdrSpring.backend.persistenceRepositories "Reads work items for indexing and search results"
    mtdrSpring.backend.semanticSearch -> aiProviders "Generates text embeddings" "HTTPS"
    mtdrSpring.backend.workManagement -> mtdrSpring.backend.persistenceRepositories "Persists work item state"
    mtdrSpring.backend.timeTracking -> mtdrSpring.backend.persistenceRepositories "Persists time entries"
    mtdrSpring.backend.sprintManagement -> mtdrSpring.backend.persistenceRepositories "Reads sprint data"
    mtdrSpring.backend.assignmentManagement -> mtdrSpring.backend.persistenceRepositories "Persists assignments"
    mtdrSpring.backend.activityTracking -> mtdrSpring.backend.persistenceRepositories "Persists activity records"
    mtdrSpring.backend.persistenceRepositories -> mtdrSpring.backend.domainModel "Maps database rows to domain entities"
    mtdrSpring.backend.persistenceRepositories -> mtdrSpring.database "Reads and writes" "JPA/JDBC"

    production = deploymentEnvironment "Production" {
        prodOci = deploymentNode "Oracle Cloud Infrastructure Region" "Production OCI tenancy and region." "OCI" {
            prodRegistry = infrastructureNode "OCI Container Registry" "Stores backend and frontend container images." "OCI Artifacts Container Registry"
            prodObjectStorage = infrastructureNode "Object Storage Bucket" "Stores database wallet and deployment artifacts where required." "OCI Object Storage"

            prodVcn = deploymentNode "Virtual Cloud Network" "Network boundary for OKE, ingress, and managed services." "OCI VCN" {
                prodOke = deploymentNode "OKE Cluster" "Kubernetes cluster provisioned by Terraform." "Oracle Kubernetes Engine" {
                    prodIngress = infrastructureNode "main-ingress" "Routes /api traffic to the backend service and / traffic to the frontend service." "Kubernetes Ingress"
                    prodFrontendService = infrastructureNode "frontend-service" "Cluster service exposing the frontend pod." "Kubernetes Service"
                    prodBackendService = infrastructureNode "backend-service" "Cluster service exposing the backend pod." "Kubernetes Service"
                    prodDbWalletSecret = infrastructureNode "db-wallet-secret" "Kubernetes secret mounted into the backend pod for database wallet credentials." "Kubernetes Secret"

                    prodFrontendPod = deploymentNode "Frontend Pod" "Runs the React static site served by Nginx." "Kubernetes Pod" {
                        prodFrontend = containerInstance mtdrSpring.frontend
                    }
                    prodBackendPod = deploymentNode "Backend Pod" "Runs the Spring Boot monolith." "Kubernetes Pod" {
                        prodBackend = containerInstance mtdrSpring.backend
                    }
                }
            }

            prodDatabaseNode = deploymentNode "Autonomous Database" "Managed relational database for project data." "Oracle Autonomous Database" {
                prodDatabase = containerInstance mtdrSpring.database
            }
        }

        production.prodOci.prodVcn.prodOke.prodIngress -> production.prodOci.prodVcn.prodOke.prodFrontendService "Routes /"
        production.prodOci.prodVcn.prodOke.prodIngress -> production.prodOci.prodVcn.prodOke.prodBackendService "Routes /api"
        production.prodOci.prodVcn.prodOke.prodFrontendService -> production.prodOci.prodVcn.prodOke.prodFrontendPod "Selects frontend pod"
        production.prodOci.prodVcn.prodOke.prodBackendService -> production.prodOci.prodVcn.prodOke.prodBackendPod "Selects backend pod"
        production.prodOci.prodVcn.prodOke.prodBackendPod -> production.prodOci.prodVcn.prodOke.prodDbWalletSecret "Mounts database wallet"
        ciCdPipeline -> production.prodOci.prodRegistry "Pushes images"
        ciCdPipeline -> production.prodOci.prodVcn.prodOke "Applies Kubernetes manifests"
        ciCdPipeline -> production.prodOci.prodDatabaseNode "Provisions database"
        production.prodOci.prodObjectStorage -> production.prodOci.prodDatabaseNode "Provides wallet/artifact storage"
    }
}
