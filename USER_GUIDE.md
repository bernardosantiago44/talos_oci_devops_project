# End-User Guide: Task Management & Team Analytics (v2.0.0)

Welcome to the official User Guide for the **Talos Task Management Application**. This guide is designed to help you quickly master the new features delivered in Sprint 2, including our Kanban Board workspace, AI-powered semantic search engine, precise time logging, and developer analytics.

---

## 🧭 Document Metadata & Environment Stamp
*   **Applies to Software Version:** `v2.0.0`
*   **Operating Systems Supported:** macOS, Windows 10/11, Linux (Ubuntu/Debian)
*   **Browser Dependencies:** Google Chrome (v110+), Mozilla Firefox (v110+), Apple Safari (v16+), Microsoft Edge (v110+)
*   **Target User Roles:** Project Contributors (Developers), Project Managers (Stakeholders), and Administrators
*   **Required Initial State:** A valid user account and network access to your company’s internal staging port.

---

## 🗂️ Table of Contents

1.  [⚡ Quick Start Guide (Your First Task Win)](#-1-quick-start-guide-your-first-task-win)
2.  [📋 How to Manage Tasks Using the Kanban Board](#-2-how-to-manage-tasks-using-the-kanban-board)
3.  [🔍 How to Find Tasks Using Natural Language AI Search](#-3-how-to-find-tasks-using-natural-language-ai-search)
4.  [⏱️ How to Log Time and Review Team Analytics](#-4-how-to-log-time-and-review-team-analytics)
5.  [🛠️ Troubleshooting Common Action Failures](#-5-troubleshooting-common-action-failures)
6.  [🌐 Global Accessibility & Device Responsiveness](#-6-global-accessibility-device-responsiveness)
7.  [📖 Glossary of Terminology](#-7-glossary-of-terminology)
8.  [📝 Feedback, Ownership & Document Lifecycle](#-8-feedback-ownership-document-lifecycle)

---

## <a name="quick-start"></a>⚡ 1. Quick Start Guide (Your First Task Win)

If you are new to the platform, this section helps you log in, create a task, and view it on the active Kanban board in under two minutes.

### Steps to Create and View Your First Task

1.  Open your browser and navigate to the team portal address.
2.  Type your user credentials on the login screen, then press the `[Enter]` key on your keyboard to enter the workspace.
3.  Click the **Create Task** button located in the top-right corner of the main dashboard navigation toolbar.
4.  In the pop-up form, enter **"Review documentation layout"** in the **Title** field, select **High** from the **Priority** drop-down menu, and click the **Submit** button.
5.  Look at the leftmost column on your screen labeled **To Do**; your newly created task card will be displayed at the top of the list.

> [!NOTE]
> **First Win Achieved:** You have successfully registered and verified your first work item on the collaborative board!

---

## <a name="kanban-board"></a>📋 2. How to Manage Tasks Using the Kanban Board

The collaborative Kanban board represents active team workflows visually. It is divided into vertical columns representing progressive phases of task completion: **To Do**, **In Progress**, and **Done**.

### Visual Board Layout

Below is a visual outline of how your active board space looks when logged into the dashboard:

```
+------------------+  +------------------+  +------------------+
|      TO DO       |  |   IN PROGRESS    |  |       DONE       |
+------------------+  +------------------+  +------------------+
| [Task Card #101] |  | [Task Card #94 ] |  | [Task Card #82 ] |
| Title: Docs      |  | Title: Fix SQL   |  | Title: Ingress   |
| Priority: High   |  | Priority: Low    |  | Priority: High   |
| [Log Time] [Del] |  | [Log Time] [Del] |  |                  |
+------------------+  +------------------+  +------------------+
| [Create Task  +] |  |                  |  |                  |
+------------------+  +------------------+  +------------------+
```

### Steps to Advance a Task's Status

1.  Locate the target task card in the **To Do** column.
2.  Click and hold the left button of your mouse on the task card title.
3.  Drag the card horizontally over to the middle column labeled **In Progress**.
4.  Release the mouse button; the background server will instantly save your progress and update all active team members' screens.

### Steps to Complete a Task

1.  Locate your active task card inside the **In Progress** column.
2.  Drag and drop the card into the **Done** column.

> [!IMPORTANT]
> **Edit Control Rules:** To protect project audit history, once a task card enters the **Done** column, its **Edit** button is dynamically hidden. If you need to make changes to a completed task, you must first drag the card back into the **In Progress** column.

---

## <a name="ai-search"></a>🔍 3. How to Find Tasks Using Natural Language AI Search

Unlike standard search systems that require you to guess exact keyword spellings, our new **Semantic Search Panel** uses artificial intelligence to interpret the conceptual meaning behind your queries.

### Visual Search Interface

```
+------------------------------------------------------------------+
|  (Sparkles) Describe what you're looking for...                  |
|  [ critical bugs in the login flow                             ] |  [ Search ]
+------------------------------------------------------------------+
|  Powered by AI semantic embeddings — search by meaning           |
+------------------------------------------------------------------+
|  3 results for "critical bugs in the login flow"   [AI-Powered]  |
|                                                                  |
|  +------------------------------------------------------------+  |
|  | Authentication API Mismatch                  (95% Match)   |  |
|  | Title: Backend routes reject valid credentials             |  |
|  | [IN_PROGRESS] [HIGH] [BUG]                            [->] |  |
|  +------------------------------------------------------------+  |
+------------------------------------------------------------------+
```

### Steps to Query Your Workspace Using AI

1.  Click on the **Semantic Search** tab located in the dashboard sidebar menu.
2.  Click inside the search text area containing the prompt: *"Describe what you're looking for..."*
3.  Type a conceptual query in plain language (e.g., *"slow database operations that block queries"*).
4.  Click the **Search** button or press the `[Enter]` key.
5.  Review the ranked result list. Each result card displays a green or blue badge showing a relevance percentage (e.g., **95% Match**), illustrating how closely the task matches your search intent.
6.  Click on any result card to open its detailed metadata viewer card.

> [!TIP]
> **Pro-Tip:** You do not need to use structured codes. Queries like *"stuff the designer told us to fix in the header style"* will successfully return relevant tasks matching layout updates!

---

## <a name="time-logs"></a>⏱️ 4. How to Log Time and Review Team Analytics

Accurate time records allow the system to calculate sprint velocity and provide team managers with visibility on project timelines.

### Steps to Record Your Daily Worked Hours

1.  Locate the task card you worked on inside the **Kanban Board** workspace.
2.  Click the small **Log Time** button at the bottom of the card.
3.  In the pop-up modal labeled **Record Hours**, click inside the **Hours Worked** field.
4.  Type the precise number of hours spent (e.g., **2.5**).
5.  Click the **Save Time Entry** button.

### Steps to Review Team Productivity Metrics

1.  Click the **Developer Analytics** tab located on the primary side navigation panel.
2.  At the top of the page, review the **Velocity Fulfillment Card**. This displays the active team performance percentage, illustrating planned versus actually completed items.
3.  To inspect individual contributions, click on the **Team Member Dropdown** menu and select a colleague's name.
4.  View the dynamically generated bar charts displaying hours logged per week and active workload balances.

---

## <a name="troubleshooting"></a>🛠️ 5. Troubleshooting Common Action Failures

If you encounter unexpected behaviors or interface errors, reference these solutions before contacting technical support.

### Common User Failures & Solutions

#### 1. The search results show a warning: "AI Service Unavailable — Falling back to local search"
*   **Why it happens:** The connection to our backend semantic AI embedding service was briefly interrupted or is experiencing high network load.
*   **What to do:** The system automatically switches to keyword-matching search. You can continue searching using exact keywords (e.g., *"SQL"* or *"Login"*), or wait 30 seconds and click the **Search** button again.

#### 2. The task Edit button is missing
*   **Why it happens:** You are trying to modify a completed work item in the **Done** column.
*   **What to do:** Drag the task card back into the **In Progress** column. The **Edit** button will reappear immediately.

#### 3. "Validation Error: Hours must be positive and non-zero"
*   **Why it happens:** You typed a negative number, zero, or text in the **Hours Worked** input field.
*   **What to do:** Click inside the **Hours Worked** field, clear your input, type a valid decimal number greater than zero (e.g., **1.5**), and click **Save Time Entry**.

#### 4. The dashboard displays an alert: "Unable to sync updates with server"
*   **Why it happens:** Your browser lost its internet connection, or your user authorization token has expired.
*   **What to do:**
    1. Check your internet connection status.
    2. Save any open draft notes, click the **Log Out** button in the dashboard footer, close your browser tab, open a new tab, and sign back in.

---

## <a name="accessibility"></a>🌐 6. Global Accessibility & Device Responsiveness

We strive to make our workspace accessible to all team members, regardless of their location, physical abilities, or hardware constraints.

### Keyboard Navigation Quick-Key Reference
For team members navigating the interface without a mouse or using screen readers, use these keyboard inputs:

*   `[Tab]` — Moves your focus forward to the next interactive card, button, or input field.
*   `[Shift]` + `[Tab]` — Moves your focus backward to the previous interactive element.
*   `[Spacebar]` or `[Enter]` — Activates the highlighted button, opens cards, or submits active forms.
*   `[Escape]` — Closes pop-up modals, search result viewers, or active dropdown menus.

### Accessible Diagrams Alt-Text Index
*   **Kanban Wireframe Schema:** Illustrates three vertical columns (To Do, In Progress, Done) containing cards that can be moved using drag-and-drop actions.
*   **AI Search Panel Layout:** Represents the top semantic input query bar with an active AI-Powered results card layout, displaying relevance scores.

### Responsive Breakpoint Adjustments
Our interface layout adapts smoothly across different screens:
*   **Desktop Monitors:** Full Kanban layout showing three columns side-by-side with secondary analytics widgets and menus fully expanded.
*   **Tablet Devices:** Collapses sidebar navigation into a tap-to-expand menu, adjusting tables to slide horizontally.
*   **Mobile Screens:** Stacks vertical board columns sequentially. Swiping left or right navigates between columns, preventing text truncation.

---

## <a name="glossary"></a>📖 7. Glossary of Terminology

To keep our communication clear, here is a glossary explaining key terms used inside our portal:

*   **Task / Work Item:** An individual unit of work representing a bug fix, new feature implementation, or task.
*   **Kanban Board:** A visual project management workspace displaying cards across sequential progress columns.
*   **AI Semantic Search:** A search method that utilizes artificial intelligence to find tasks based on natural meaning rather than literal keyword matches.
*   **Relevance Match Score:** A percentage showing how closely a search result corresponds to the conceptual intent of your query.
*   **Velocity Fulfillment:** A project metric calculated by comparing planned task estimates against actually completed tasks.
*   **Work Log:** An entry recording the specific hours spent by a team member on a task.

---

## <a name="feedback"></a>📝 8. Feedback, Ownership & Document Lifecycle

### Document Ownership
*   **Assigned Document Owner:** Technical Product Manager (PM)
*   **Audit Schedule:** Audited, updated, and validated at the conclusion of every two-week sprint cycle.

### Help Us Improve
Your feedback helps us make our documentation better! Please participate in our quick, anonymous survey below:

**Was this user guide article helpful?**
*   `[ ]` **Yes, absolutely** — I found what I was looking for.
*   `[ ]` **No, not quite** — Some instructions were confusing.

**Please share any comments or suggestions:**
`[                                                                            ]`
`[                                                                            ]`
*(Click [Submit Feedback] to send your response directly to the Product team)*
