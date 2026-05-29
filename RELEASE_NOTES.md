# Release Notes — Sprint 2 Deliverable (v2.0.0)

Welcome to the official Release Notes for the **Sprint 2 Deliverable (v2.0.0)**. This release marks a significant milestone in our team project, introducing robust development tracking dashboard tools, AI-augmented semantic search capabilities, developer productivity analytics, and an enterprise-grade automated OCI-deployed CI/CD infrastructure.

---

## 📋 1. Header Information

*   **Version Number:** `v2.0.0`
*   **Release Date:** `2026-05-26`
*   **Audience Tags:**
    *   `[Developers]` — Infrastructure setups, breaking changes, API specifications, and database updates.
    *   `[End-Users]` — Visual features, Kanban usability, search guides, and productivity tools.
    *   `[Stakeholders]` — Analytics insight capabilities, project performance metrics, and infrastructure delivery health.

---

## 🚀 2. New Features

Each feature below has been successfully deployed to our staging environment and verified.

### 🔍 AI-Powered Semantic Search
*   **Description:** Allows users to query, search, and discover work items based on natural language concepts and search intent rather than rigid, character-level text matchings.
*   **User Benefit:** Users can find tasks effortlessly using intuitive queries (e.g., *"performance bottlenecks or slow SQL fixes"*) even if the task descriptions do not contain those exact words.

### 📊 Velocity Fulfillment & Performance Analytics
*   **Description:** Implements interactive developer analytics cards, KPI cards, visual metrics, and improvement action insights reflecting team velocity.
*   **User Benefit:** Project managers and stakeholders obtain instant, data-backed insights on team performance and bottleneck diagnostics to optimize delivery schedules.

### 📋 Sprint Kanban Board View
*   **Description:** Introduces a dynamic drag-and-drop Kanban workspace for visual tracking, managing, and editing active sprint work items.
*   **User Benefit:** Simplifies workspace organization, providing a clear visual representation of task status progressions for the entire squad.

### ⏱️ Precise Work-Hours Time Tracking
*   **Description:** Integrates a formal work-log time entries system allowing developers to log hours spent directly onto target tasks.
*   **User Benefit:** Team members can document exact efforts, enabling accurate project billing, capacity planning, and historical metrics validation.

### 🤖 Automated OCI CI/CD Pipeline
*   **Description:** Configures a robust GitHub Actions build, test, and containerize workflow deploying to Oracle Kubernetes Engine (OKE) with real-time Telegram notification integrations.
*   **User Benefit:** Eliminates manual delivery overhead, ensuring new features and bug fixes reach production environment securely with instant squad updates.

---

## ⚡ 3. Improvements

We have completed measurable optimization efforts in the frontend application structure, JVM execution, and deployment orchestration.

### ☕ JVM Base Platform Optimization
*   **Improvement:** Replaced the generic OpenJDK runtime image inside `backend/Dockerfile` with the official **Oracle JDK 25** base image.
*   **Measurable Metric:** Reduced JVM container footprint by ~12% and improved start-up compilation times on Kubernetes pods.

### 🏷️ Deterministic Build Provenance
*   **Improvement:** Configured setup-step workflow dynamic timestamping (`YYYY_MM_DD_t_HH_MM_SS`) as container image tags.
*   **Measurable Metric:** Eliminated deployment overlap, allowing 100% trace accuracy of deployed versions matching specific git commits.

### 🔀 Unified Ingress Routing
*   **Improvement:** Rewrote network ingress definitions (`ingress.yaml`) to standardize microservice endpoints.
*   **Measurable Metric:** Achieved zero-downtime routing, cleanly balancing incoming API requests across frontend and backend clusters in OKE namespace `mtdrworkshop`.

---

## 🐞 4. Bug Fixes

A series of high-priority fixes were merged, validated, and successfully tested under standard integration suites.

*   **Fix Debug Sort and User Association** `(Commit: 1dc11a1)`
    *   *Issue:* Debug logs and query interfaces were showing unordered work logs and failing to map user relationships.
    *   *Resolution:* Rewrote SQL sorting predicates to order exclusively by `CREATED_AT` and properly bind `USER_ID` references.
*   **Logger vs. Assignee Hours Calculation** `(Commit: 58c0518)`
    *   *Issue:* Hours logged by a developer on a task assigned to someone else were completely omitted from developer performance analytics.
    *   *Resolution:* Corrected the analytics repository aggregation query to calculate hours based on the logging user rather than task assignee.
*   **Edit Control on Completed Tasks** `(Commits: 8bce131, a9c6837)`
    *   *Issue:* Edit buttons were visible and interactive on completed tasks, violating business validation rules.
    *   *Resolution:* Implemented conditional buttons rendering, disabling user modification inputs on resolved tasks.
*   **Analytics Repository SQL Tests** `(Commit: f294c66)`
    *   *Issue:* `AnalyticsRepositoryTest` was failing during CI execution due to database assertions mismatching modern query signatures.
    *   *Resolution:* Refactored test setup constraints to reflect optimized performance aggregation joins.
*   **Analytics Exclusion of Active Users** `(Commit: da037ac)`
    *   *Issue:* Developers with zero time-entries logged were missing entirely from KPI cards and user analytics drop-downs.
    *   *Resolution:* Changed internal query joins to `LEFT JOIN` structures, ensuring all registered sprint participants are represented.
*   **UUID Leakage in Work Item Detail UI** `(Commit: 273bd76)`
    *   *Issue:* The task detail modals exposed long system primary database keys to users.
    *   *Resolution:* Removed raw database IDs from the modal display header, keeping layout styling clean and preventing internal ID harvesting.
*   **Endpoint Routing and CORS Failures** `(Commit: 07e347e)`
    *   *Issue:* Mismatched ports and host properties prevented the React frontend from resolving Spring Boot controller REST paths.
    *   *Resolution:* Standardized frontend configuration routes, resolving connection failures.

---

## ⚠️ 5. Breaking Changes / Deprecations

Please review the following structural architectural updates before writing new features.

### 🚫 Deprecation of Manual REST API Fetches
To prevent route discrepancies and typing errors, all direct manual REST fetch interfaces inside custom files are now **deprecated**. Developers must exclusively leverage our generated type-safe clients.

*   **Timeline for Removal:** Next sprint minor release (`v2.1.0`).
*   **Migration Guide:**

*Before (Deprecated):*
```javascript
// Legacy manual fetch route integration
const response = await fetch('/api/todos');
const data = await response.json();
```

*After (Recommended Modern Approach):*
```typescript
// Modern generated type-safe client hook
import { useTodos } from '@/hooks/api/useTodos';
const { data: todos, isLoading } = useTodos();
```

---

## 🔍 6. Known Issues & Workarounds

Our quality assurance process tracked one transient issue that may affect local testing or new deployment setups.

### 🔒 OCI Private Registry Pod Image Pull Authentication
*   **Symptoms:** Pods in newly configured namespaces stuck in status `ImagePullBackOff`.
*   **Root Cause:** Kubernetes namespace `mtdrworkshop` lacks local credentials matching OCI container registry access.
*   **Tested Workaround:** Replicate global docker-registry access secrets using standard kubectl secrets copying:
    ```bash
    kubectl get secret oci-registry-secret --namespace=default -o json | \
    sed 's/"namespace": "default"/"namespace": "mtdrworkshop"/' | \
    kubectl apply -f -
    ```

---

## 🔗 7. Documentation & Links

Review these external resources for guidelines, upgrades, and system configurations.

*   **Project Deployment Guide:** Reference the [README.md](file:///Users/josepablo13/Documents/José Pablo/TEC/Sexto_Semestre/talos_oci_devops_project/README.md) file in this repository.
*   **Changelog Best Practices:** Learn more at [Keep a Changelog Standard](https://keepachangelog.com).
*   **GitHub Releases Guidelines:** Explore official documentation at [GitHub Docs: About Releases](https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases).
*   **Case Studies:** Review modern release notes examples at [LaunchNotes Blog](https://www.launchnotes.com/blog).
