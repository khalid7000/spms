# Employee Portfolio Module - Implementation Guide

## Overview
The Employee Portfolio & Goals module is now fully implemented with complete backend and frontend components. This guide covers deployment, integration, and next steps.

## What Has Been Built

### Backend (Java/Spring Boot)

#### Database Migrations
- **V34**: `employee_title` - Employee title management
- **V35**: `portfolio_category`, `category_rank_label`, `category_criteria` - Category system with rubrics
- **V36**: `employee_goal` - Goals with state machine workflow
- **V37**: `portfolio_entry` - Achievement/portfolio entries
- **V38**: Default categories seeding (Faculty: Teaching/Research/Service, Manager: Strategic Thinker/Lead by Example/Business & Financial Acumen)

#### Domain Entities
- `EmployeeTitle` - Maps to employee title (Faculty, Manager, Director, Staff)
- `PortfolioCategory` - Evaluation category (Teaching, Research, etc.)
- `CategoryRankLabel` - 1-5 rating labels (e.g., "5 = Excellent")
- `CategoryCriteria` - Standards/rubric items
- `EmployeeGoal` - Annual goals with state machine
  - States: DRAFT → LEADER_SUBMITTED → EMPLOYEE_REVIEW → DEPLOYED/EMPLOYEE_SUBMITTED → ARCHIVED
  - Supports AI-suggested improvements
- `PortfolioEntry` - Individual achievement records

#### Repositories (6 interfaces)
- `EmployeeTitleRepository`
- `PortfolioCategoryRepository`
- `CategoryRankLabelRepository`
- `CategoryCriteriaRepository`
- `EmployeeGoalRepository`
- `PortfolioEntryRepository`

#### Services (4 core services)
1. **PortfolioCategoryService** - Category management + rank labels + criteria
2. **EmployeeGoalService** - Goal creation, workflow state transitions, AI suggestions
3. **PortfolioEntryService** - Achievement logging, portfolio queries, summary statistics
4. **PortfolioAiService** - AI suggestion generation using existing AI engine

#### REST Controllers (3 endpoints)
1. **PortfolioCategoryController** - `/api/portfolio/categories/*`
   - Category CRUD, rank label management, criteria management
   
2. **EmployeeGoalController** - `/api/portfolio/goals/*`
   - Goal CRUD, workflow transitions, AI suggestions
   - Endpoints for leader and employee interactions
   
3. **PortfolioEntryController** - `/api/portfolio/entries/*`
   - Achievement logging, portfolio queries, statistics

### Frontend (React/Vite)

#### API Client
- `spms-client/src/api/portfolio.js` - Complete API wrapper for all portfolio operations

#### Pages/Components

1. **Admin Pages**
   - `CategoryManagementPage.jsx` - Full category management UI
     - Title selection
     - Category CRUD with inline editing
     - Drawer-based rank label management (1-5 scale with custom labels)
     - Criteria management with reordering
   
   - `GoalSettingPage.jsx` - Leader goal creation workflow
     - Team member selection
     - Goal creation with strength/weakness notes
     - AI suggestion integration
     - Goal state tracking and workflow

2. **Employee Pages**
   - `AchievementLoggingPage.jsx` - Achievement logging interface
     - Initiative selection from strategy
     - Multi-select categorization
     - Optional goal linking
     - Self-assessment ratings (1-5)
     - Evidence attachment URL
     - Portfolio summary dashboard

## Integration Checklist

### Backend Integration

- [ ] Add Lombok dependency `vlmihalcea-hibernate-types` for JSONB support
```xml
<dependency>
    <groupId>com.vladmihalcea</groupId>
    <artifactId>hibernate-types-60</artifactId>
    <version>2.20.0</version>
</dependency>
```

- [ ] Apply database migrations V34-V38
  ```bash
  ./mvnw flyway:migrate
  ```

- [ ] Ensure `AuditService` and `AppUserRepository` are available (should exist)

- [ ] Add portfolio routes to security configuration (if using Spring Security)
  - Admin routes: `/api/portfolio/categories/*` (ADMIN)
  - Goal routes: `/api/portfolio/goals/*` (isAuthenticated)
  - Entry routes: `/api/portfolio/entries/*` (isAuthenticated)

- [ ] Verify `RestTemplate` bean exists for AI service calls (used by PortfolioAiService)

- [ ] Configure AI engine URL in application.yml:
  ```yaml
  ai:
    engine:
      url: http://localhost:8080/api/ai
    enabled: true
  ```

### Frontend Integration

- [ ] Add portfolio API client import to existing layouts/navigation

- [ ] Add new pages to routing configuration:
  - Admin: `/admin/portfolio/categories` → `CategoryManagementPage`
  - Admin: `/admin/portfolio/goals` → `GoalSettingPage`
  - Member: `/portfolio/achievements` → `AchievementLoggingPage`

- [ ] Update admin navigation menu to include:
  - "Portfolio Categories" → CategoryManagementPage
  - "Goal Setting" → GoalSettingPage

- [ ] Update member navigation to include:
  - "My Achievements" → AchievementLoggingPage
  - "Portfolio" → Portfolio dashboard

- [ ] Add portfolio API import to main layout:
  ```javascript
  import * as api from '../../api/portfolio'
  ```

- [ ] Install any missing dependencies (all Ant Design components should already be available):
  - `dayjs` - For date handling
  - `@tanstack/react-query` - For queries/mutations

## Data Model Relationships

```
EmployeeTitle (1) ←→ (many) PortfolioCategory
EmployeeTitle (1) ←→ (many) AppUser (via user.title)

PortfolioCategory (1) ←→ (many) CategoryRankLabel (system tracks 1-5)
PortfolioCategory (1) ←→ (many) CategoryCriteria

AppUser (1) ←→ (many) EmployeeGoal (as employee)
AppUser (1) ←→ (many) EmployeeGoal (as leader)
AcademicYear (1) ←→ (many) EmployeeGoal
EmployeeGoal (1) ←→ (many) PortfolioEntry

AppUser (1) ←→ (many) PortfolioEntry (as employee)
Initiative (1) ←→ (many) PortfolioEntry
PortfolioCategory (1) ←→ (many) PortfolioEntry
EmployeeGoal (1) ←→ (many) PortfolioEntry (optional)
```

## API Endpoints Reference

### Category Management (Admin)
```
POST   /api/portfolio/categories
GET    /api/portfolio/categories/{id}
GET    /api/portfolio/titles/{titleId}/categories
PUT    /api/portfolio/categories/{id}
DELETE /api/portfolio/categories/{id}

POST   /api/portfolio/categories/{categoryId}/rank-labels
GET    /api/portfolio/categories/{categoryId}/rank-labels
PUT    /api/portfolio/rank-labels/{id}
DELETE /api/portfolio/rank-labels/{id}

POST   /api/portfolio/categories/{categoryId}/criteria
GET    /api/portfolio/categories/{categoryId}/criteria
PUT    /api/portfolio/criteria/{id}
DELETE /api/portfolio/criteria/{id}
PUT    /api/portfolio/categories/{categoryId}/criteria/reorder
```

### Goal Management
```
POST   /api/portfolio/goals
GET    /api/portfolio/goals/{id}
GET    /api/portfolio/goals/my-academic-year/{academicYearId}
GET    /api/portfolio/goals/team/{academicYearId}
PUT    /api/portfolio/goals/{id}
PUT    /api/portfolio/goals/{id}/submit-for-review (Leader submits for employee review)
PUT    /api/portfolio/goals/{id}/employee-review (Employee starts review)
PUT    /api/portfolio/goals/{id}/employee-accept (Employee accepts - DEPLOYED)
PUT    /api/portfolio/goals/{id}/employee-request-changes (Employee requests changes)
PUT    /api/portfolio/goals/{id}/archive
GET    /api/portfolio/goals/{id}/ai-suggestions
PUT    /api/portfolio/goals/reorder
```

### Achievement/Portfolio Management
```
POST   /api/portfolio/entries (Log achievement)
GET    /api/portfolio/entries/{id}
GET    /api/portfolio/entries/my-portfolio
GET    /api/portfolio/entries/employee/{employeeId}
GET    /api/portfolio/entries/my-portfolio/by-category/{categoryId}
GET    /api/portfolio/entries/my-portfolio/by-goal/{goalId}
GET    /api/portfolio/entries/my-portfolio/date-range
PUT    /api/portfolio/entries/{id}
DELETE /api/portfolio/entries/{id}
PUT    /api/portfolio/entries/{entryId}/link-goal/{goalId}
GET    /api/portfolio/entries/my-portfolio/summary
GET    /api/portfolio/entries/employee/{employeeId}/summary
```

## Workflow - Goal Setting Process

1. **Leader Creates Goal** (DRAFT state)
   - Specifies goal title, description, category
   - Notes employee strengths and weaknesses
   - Can regenerate AI suggestions based on weaknesses

2. **Leader Submits** (LEADER_SUBMITTED)
   - Changes state from DRAFT → LEADER_SUBMITTED
   - Sends to employee for review

3. **Employee Reviews** (EMPLOYEE_REVIEW)
   - Can accept goal (→ DEPLOYED - goal is active)
   - Can request changes (→ EMPLOYEE_SUBMITTED - back to leader)

4. **Mutual Approval Loop**
   - If changes requested: Leader edits and resubmits
   - Repeats until employee accepts

5. **Deployed**
   - Goal is active for the academic year
   - Employee can log achievements against this goal
   - Employee can view progress

6. **Archived**
   - After academic year ends
   - Historical record maintained

## Workflow - Achievement Logging

1. **Employee Logs Achievement**
   - Selects initiative from department strategy
   - Chooses evaluation category
   - Optionally links to deployed goal
   - Enters title, description, self-assessment rating (1-5)
   - Adds evidence URL (optional)
   - Saves (auto-accepted, no approval needed)

2. **Achievements Accumulate**
   - Can be filtered by category, goal, date range
   - Portfolio summary shows totals and average rating
   - Used for annual evaluation

## Key Design Decisions

1. **No Approval for Achievements** - Employee-logged achievements auto-accept
2. **AI Suggestions at Goal Creation** - Based on leader's strength/weakness notes
3. **Multi-level Rating Scale** - Categories have 1-5 scale with customizable labels per rating
4. **Goal State Machine** - Enforces workflow: DRAFT → LEADER_SUBMITTED → EMPLOYEE_REVIEW → DEPLOYED/EMPLOYEE_SUBMITTED → ARCHIVED
5. **Optional Goal Linking** - Achievements can optionally link to goals, allowing flexible logging
6. **Portfolio Visibility** - Self + Manager + HR can see portfolio
7. **Audit Trail** - All actions logged via existing AuditService

## Testing Scenarios

### Scenario 1: Admin Setup
1. Login as admin
2. Go to "Portfolio Categories"
3. Select "Faculty" title
4. Verify default categories: Teaching, Research, Service
5. Verify each has rank labels and criteria
6. Edit a criteria name
7. Test reordering criteria

### Scenario 2: Leader Goal Setting
1. Login as manager/director
2. Go to "Goal Setting"
3. Select academic year and employee
4. Create new goal with strengths/weaknesses
5. Click "AI Suggestions"
6. Review suggested goals
7. Submit goal for employee review

### Scenario 3: Employee Goal Review
1. Login as employee with assigned goals
2. Go to "My Goals"
3. See pending goal for review
4. Accept or request changes
5. If accepted, goal becomes DEPLOYED

### Scenario 4: Achievement Logging
1. Login as employee
2. Go to "My Achievements"
3. Log achievement against initiative
4. Link to deployed goal
5. Enter self-assessment rating
6. Add evidence URL
7. Verify appears in portfolio

## Performance Considerations

- **Index Strategy**: Frequently queried fields indexed:
  - `employee_goal.employee_id`, `leader_id`, `academic_year_id`, `state`
  - `portfolio_entry.employee_id`, `category_id`, `logged_date`

- **Pagination**: All list endpoints support pagination
  - Default: 10 items per page

- **Lazy Loading**: All relationships use LAZY fetching to avoid N+1 queries

- **JSONB Storage**: AI suggestions stored as JSONB for efficient querying

## Future Enhancements

1. **Batch Imports** - Excel import for annual goal setup
2. **Export Reports** - PDF/Excel export of portfolio for evaluation
3. **Email Notifications** - Notify when goals need review
4. **Dashboard Analytics** - Department-level portfolio analytics
5. **Bulk Operations** - Archive multiple goals at once
6. **Goal Templates** - Reusable goal templates by title/category
7. **360 Feedback** - Peer/self/manager assessment integration
8. **Portfolio Themes** - Group achievements by theme/project
9. **Progress Tracking** - Milestone tracking within goals
10. **Mobile App** - Achievement logging on mobile devices

## Support & Documentation

- **Code Comments**: All service methods have JavaDoc
- **API Documentation**: Add Swagger/OpenAPI annotations if desired
- **Frontend Components**: JSX files include inline comments
- **Database Schema**: Clear table/column naming conventions

## Deployment Notes

1. **Database**: Run migrations in order V34-V38
2. **Classpath**: Ensure `hibernate-types` JAR is on classpath for JSONB support
3. **Security**: Configure role-based access control for endpoints
4. **AI Service**: Ensure AI engine URL is accessible (or disable if not available)
5. **Frontend Build**: Run `npm build` to compile React components

---

**Module Status**: ✅ COMPLETE
**Backend**: Fully implemented with all entities, services, controllers
**Frontend**: Fully implemented with admin, leader, and employee UIs
**Ready for**: Integration testing, UI refinement, production deployment
