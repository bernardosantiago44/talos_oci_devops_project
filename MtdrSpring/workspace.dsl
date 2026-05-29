workspace "MtdrSpring Architecture" "C4 architecture documentation for the MtdrSpring todo and work management application." {
    !identifiers hierarchical
    !include model.dsl

    views {
        systemLandscape "SystemLandscape" {
            include *
            autoLayout lr
        }

        systemContext mtdrSpring "SystemContext" {
            include *
            autoLayout lr
        }

        container mtdrSpring "Containers" {
            include *
            autoLayout lr
        }

        component mtdrSpring.backend "BackendComponents" {
            include *
            autoLayout lr
        }

        deployment mtdrSpring "Production" "ProductionDeployment" {
            include *
            autoLayout tb
        }

        dynamic mtdrSpring.backend "DynamicAssignWork" "Manager assigns a developer to a work item." {
            manager -> mtdrSpring.frontend "Chooses a work item and assignee"
            mtdrSpring.frontend -> mtdrSpring.backend.restControllers "PATCH /api/workitems/{id}/assignees/{userId}"
            mtdrSpring.backend.restControllers -> mtdrSpring.backend.assignmentManagement "Add assignee"
            mtdrSpring.backend.assignmentManagement -> mtdrSpring.backend.persistenceRepositories "Validate user and work item"
            mtdrSpring.backend.assignmentManagement -> mtdrSpring.backend.persistenceRepositories "Save assignment"
            mtdrSpring.backend.assignmentManagement -> mtdrSpring.backend.activityTracking "Record assignment activity"
            mtdrSpring.backend.restControllers -> mtdrSpring.frontend "Return assignment response"
            autoLayout lr
        }

        dynamic mtdrSpring.backend "DynamicTrackTime" "Developer records time against assigned work." {
            developer -> mtdrSpring.frontend "Submits time entry"
            mtdrSpring.frontend -> mtdrSpring.backend.restControllers "POST /api/time-entries"
            mtdrSpring.backend.restControllers -> mtdrSpring.backend.timeTracking "Create time entry"
            mtdrSpring.backend.timeTracking -> mtdrSpring.backend.persistenceRepositories "Validate work item and user"
            mtdrSpring.backend.timeTracking -> mtdrSpring.backend.persistenceRepositories "Save time entry"
            mtdrSpring.backend.timeTracking -> mtdrSpring.backend.activityTracking "Record time-entry activity"
            mtdrSpring.backend.restControllers -> mtdrSpring.frontend "Return created entry"
            autoLayout lr
        }

        dynamic mtdrSpring.backend "DynamicTelegramTaskLookup" "Developer asks the bot for assigned tasks." {
            developer -> telegram "Sends task lookup command"
            telegram -> mtdrSpring.backend.botInterface "Delivers update"
            mtdrSpring.backend.botInterface -> mtdrSpring.backend.workManagement "Find tasks by Telegram user"
            mtdrSpring.backend.workManagement -> mtdrSpring.backend.persistenceRepositories "Read user and assigned work"
            mtdrSpring.backend.persistenceRepositories -> mtdrSpring.database "Query assignments"
            mtdrSpring.backend.botInterface -> telegram "Send task summary"
            telegram -> developer "Displays response"
            autoLayout lr
        }

        dynamic mtdrSpring.backend "DynamicAnalyticsDashboard" "Manager opens the analytics dashboard." {
            manager -> mtdrSpring.frontend "Opens analytics dashboard"
            mtdrSpring.frontend -> mtdrSpring.backend.restControllers "GET /api/analytics/dashboard"
            mtdrSpring.backend.restControllers -> mtdrSpring.backend.reportingAnalytics "Request dashboard metrics"
            mtdrSpring.backend.reportingAnalytics -> mtdrSpring.backend.persistenceRepositories "Query sprint, work, and time data"
            mtdrSpring.backend.persistenceRepositories -> mtdrSpring.database "Read analytics data"
            mtdrSpring.backend.restControllers -> mtdrSpring.frontend "Return KPIs and charts"
            autoLayout lr
        }

        dynamic mtdrSpring "DynamicDeploymentUpdate" "System deploys a new application version." {
            systemActor -> ciCdPipeline "Triggers build and deployment"
            ciCdPipeline -> ociPlatform "Builds and pushes container images"
            ciCdPipeline -> ociPlatform "Applies Terraform and Kubernetes manifests"
            ociPlatform -> mtdrSpring.frontend "Runs updated frontend container"
            ociPlatform -> mtdrSpring.backend "Runs updated backend container"
            mtdrSpring.backend -> mtdrSpring.database "Uses existing persistent data"
            autoLayout lr
        }

        styles {
            element "Person" {
                shape person
                background #0b6bcb
                color #ffffff
            }
            element "Software System" {
                background #116149
                color #ffffff
            }
            element "Container" {
                background #2b4c7e
                color #ffffff
            }
            element "Component" {
                background #4c6f92
                color #ffffff
            }
            element "Deployment Node" {
                background #f3f4f6
                color #111827
            }
            element "Infrastructure Node" {
                background #dbeafe
                color #111827
            }
            element "External" {
                background #6b7280
                color #ffffff
            }
        }
    }
}
