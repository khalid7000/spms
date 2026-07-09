# Employee Portfolio & Goals Module - Design Document

## Overview
Tracks employee achievements against strategy initiatives, organized by title-specific categories and personal goals. Supports annual evaluation cycle with mutual-approval goal-setting workflow and AI-assisted suggestion engine.

## Architecture Overview

### 1. Data Model

#### A. Core Entities

**EmployeeTitle** (Predefined with default categories)
- `id`: UUID
- `title_name`: String (Faculty, Manager, Director, Staff)
- `department_id`: Foreign key to Department
- `created_at`, `updated_at`

**PortfolioCategory** (Title-specific evaluation dimensions)
- `id`: UUID
- `title_id`: Foreign key to EmployeeTitle
- `category_name`: String (Teaching, Research, Service, etc.)
- `description`: String
- `sort_order`: Integer
- `is_system_default`: Boolean (Faculty Teaching = true, etc.)
- `created_at`, `updated_at`

**CategoryRankLabel** (Custom 1-5 rating labels per category)
- `id`: UUID
- `category_id`: Foreign key to PortfolioCategory
- `rank`: Integer (1-5)
- `label`: String (e.g., "Excellent", "Good", "Acceptable")
- `description`: String

**CategoryCriteria** (Standards/rubric for each category)
- `id`: UUID
- `category_id`: Foreign key to PortfolioCategory
- `criteria_name`: String
- `description`: String
- `sort_order`: Integer

**EmployeeGoal** (Annual goals with mutual approval workflow)
- `id`: UUID
- `employee_id`: Foreign key to AppUser
- `academic_year_id`: Foreign key to AcademicYear
- `goal_title`: String
- `description`: String (from leader)
- `category_id`: Foreign key to PortfolioCategory (which evaluation category)
- `measurement_kpi_id`: Foreign key to Measurement (links to strategy KPI)
- `state`: ENUM (DRAFT, LEADER_SUBMITTED, EMPLOYEE_REVIEW, EMPLOYEE_SUBMITTED, DEPLOYED, ARCHIVED)
- `leader_id`: Foreign key to AppUser (employee's direct manager)
- `leader_strengths`: Text (optional, filled by leader)
- `leader_weaknesses`: Text (optional, filled by leader)
- `leader_submitted_at`: Timestamp
- `employee_accepted_at`: Timestamp
- `ai_suggested_improvements`: JSON (array of AI suggestions, see 1.E)
- `sort_order`: Integer
- `created_at`, `updated_at`

**PortfolioEntry** (Single achievement logged by employee)
- `id`: UUID
- `employee_id`: Foreign key to AppUser
- `initiative_id`: Foreign key to Initiative (strategy initiative)
- `category_id`: Foreign key to PortfolioCategory (which category this achievement relates to)
- `goal_id`: Foreign key to EmployeeGoal (optional, links to goal if applicable)
- `achievement_title`: String
- `achievement_description`: Text
- `category_rating`: Integer (1-5, self-assessed)
- `evidence_url`: String (optional link/file)
- `logged_date`: Date (when achievement occurred)
- `created_at`: Timestamp (when submitted)

#### B. Relationship Summary

```
EmployeeTitle (1) ←→ (many) PortfolioCategory
EmployeeTitle (1) ←→ (many) Employee
PortfolioCategory (1) ←→ (many) CategoryRankLabel
PortfolioCategory (1) ←→ (many) CategoryCriteria
PortfolioCategory (1) ←→ (many) EmployeeGoal
EmployeeGoal (many) ←→ (1) AppUser (employee)
EmployeeGoal (many) ←→ (1) AppUser (leader)
EmployeeGoal (many) ←→ (1) AcademicYear
PortfolioEntry (many) ←→ (1) AppUser (employee)
PortfolioEntry (many) ←→ (1) Initiative
PortfolioEntry (many) ←→ (1) PortfolioCategory
PortfolioEntry (many) ←→ (1) EmployeeGoal (optional)
```

### 2. Goal-Setting Workflow States

```
DRAFT (Leader creates/edits goal)
    ↓
    LEADER_SUBMITTED (Leader submits for employee review)
    ↓
    EMPLOYEE_REVIEW (Employee reviewing, can comment/ask for changes)
    ├→ EMPLOYEE_SUBMITTED (Employee submits back for leader reconsideration)
    │   ↓
    │   (Returns to DRAFT with leader feedback, cycle repeats)
    │
    └→ DEPLOYED (Employee accepts, goal is active for this academic year)
    
DEPLOYED
    ↓
    ARCHIVED (At end of academic year)
```

### 3. API Endpoints

#### Portfolio Categories (Admin)
- `POST /api/portfolio/categories` - Create category
- `GET /api/portfolio/categories?titleId=X` - List categories for title
- `PUT /api/portfolio/categories/{id}` - Update category
- `DELETE /api/portfolio/categories/{id}` - Delete category
- `POST /api/portfolio/categories/{id}/rank-labels` - Add rank label
- `PUT /api/portfolio/categories/{id}/rank-labels/{rankId}` - Update rank label
- `POST /api/portfolio/categories/{id}/criteria` - Add criteria

#### Goals (Leader & Employee)
- `POST /api/portfolio/goals` - Leader creates goal for employee
- `GET /api/portfolio/goals/my-academic-year/{academicYearId}` - Get employee's goals for year
- `GET /api/portfolio/goals/team/{academicYearId}` - Get leader's team goals
- `GET /api/portfolio/goals/{id}` - Get goal with AI suggestions
- `PUT /api/portfolio/goals/{id}` - Leader updates goal (when in DRAFT/EMPLOYEE_SUBMITTED)
- `PUT /api/portfolio/goals/{id}/submit` - Leader submits for employee review
- `PUT /api/portfolio/goals/{id}/employee-accept` - Employee accepts goal
- `PUT /api/portfolio/goals/{id}/employee-submit-changes` - Employee requests changes
- `GET /api/portfolio/goals/{id}/ai-suggestions` - Trigger AI suggestions based on weaknesses

#### Achievements (Employee)
- `POST /api/portfolio/entries` - Log achievement (auto-accept)
- `GET /api/portfolio/entries/my-portfolio` - Get employee's portfolio
- `GET /api/portfolio/entries/employee/{empId}` - Get employee portfolio (manager/HR)
- `GET /api/portfolio/entries/by-category/{categoryId}` - Filter by category
- `GET /api/portfolio/entries/by-goal/{goalId}` - Get entries linked to goal
- `PUT /api/portfolio/entries/{id}` - Update entry (if no approval workflow)
- `DELETE /api/portfolio/entries/{id}` - Delete entry

#### Portfolio Dashboard
- `GET /api/portfolio/dashboard` - Employee's portfolio summary
- `GET /api/portfolio/dashboard/evaluation/{academicYearId}` - Evaluation summary for year

### 4. Frontend Components

#### Admin Management Pages
- **CategoryManagementPage**: CRUD categories, rank labels, criteria
- **TitleConfigPage**: Manage title-category mappings

#### Leader Pages
- **GoalSettingPage**: Create/edit goals for team members
  - AI suggestion panel (shows improvement suggestions)
  - Drag-to-sort goals by priority
  - State badge indicating review status
- **TeamPortfolioPage**: View team members' portfolios
- **GoalReviewPage**: Monitor goal acceptance status

#### Employee Pages
- **GoalReviewPage**: Review goals from leader
  - Compare against suggested improvements
  - Accept/request-changes workflow
- **PortfolioAchievementPage**: Log achievements against initiatives
  - Multi-select: Initiative + Category + Goal
  - Evidence attachment
  - Category rating 1-5
- **PortfolioPage**: Personal portfolio dashboard
  - Achievements grouped by category
  - Goal progress tracking
  - Summary statistics (by category, by goal)

### 5. Database Migrations

- `V31__create_employee_title.sql` - Employee titles table
- `V32__create_portfolio_category.sql` - Categories + rank labels + criteria
- `V33__create_employee_goal.sql` - Goal-setting workflow
- `V34__create_portfolio_entry.sql` - Achievement entries
- `V35__seed_default_categories.sql` - Faculty/Manager categories

### 6. Integration Points

**With Existing Strategy System**:
1. When logging achievement against Initiative → user also tags with Category/Goal
2. Measurement/Achievement already exists; Portfolio adds evaluation context
3. Permission model: Employee can see own + manager can see team

**With AI Engine**:
1. Goal creation endpoint triggers AI to suggest improvements based on leader's strengths/weaknesses notes
2. AI response: array of suggested goals with rationale
3. Leader accepts/rejects/edits suggestions before submitting to employee

**With Annual Evaluation**:
1. Annual evaluation references DEPLOYED goals for that academic year
2. Portfolio entries rolled up into evaluation summary
3. HR can export evaluation with goals + achievements

## Implementation Phases

### Phase 1: Core Data Model & Admin APIs
- Create database migrations
- EmployeeTitle, PortfolioCategory, CategoryRankLabel, CategoryCriteria entities
- Admin REST endpoints for category management
- Seed default faculty/manager categories

### Phase 2: Goal-Setting Workflow
- EmployeeGoal entity with state machine
- Leader endpoints: create, edit, submit goals
- Employee endpoints: review, accept, request-changes
- Goal workflow state transitions

### Phase 3: Achievement Tracking
- PortfolioEntry entity
- Enhanced achievement logging (with category/goal tagging)
- Portfolio entry APIs

### Phase 4: Frontend - Admin & Leader UIs
- Category management page
- Goal setting page (leader)
- Team portfolio view

### Phase 5: Frontend - Employee UIs
- Goal review page
- Achievement logging with multi-select
- Personal portfolio dashboard

### Phase 6: AI Integration & Reports
- AI suggestion endpoint integration
- Evaluation summary reports
- Annual rollup functionality
