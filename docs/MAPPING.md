# MAPPING.md — Feature → Code Map (UI-only)

This document maps every **shipped UI feature** to its frontend code and the backend endpoints it calls. Backend-only features without a UI are intentionally omitted.

Frontend root: `Frontend/Placement-portal/src/app/`
Backend root: `Backend/placement-portal-backend/src/main/java/com/placement/portal/`

---

## Routing

All routes are declared in `app.routes.ts` and lazy-loaded via `loadComponent`. The default fallback (`**`) redirects to `/auth/login`.

| URL | Layout | Component |
|---|---|---|
| `/` | none | `features/landing/landing` |
| `/auth/login` | `auth-layout` | `features/auth/login/login` |
| `/auth/register` | `auth-layout` | `features/auth/register/register` |
| `/student/dashboard` | `student-layout` | `features/student/dashboard/dashboard` |
| `/student/jobs` | `student-layout` | `features/student/job-search/job-search` |
| `/student/drives` | `student-layout` | `features/student/drives/drives` |
| `/student/applications` | `student-layout` | `features/student/applications/applications` |
| `/student/training` | `student-layout` | `features/student/training/training` |
| `/student/resume` | `student-layout` | `features/student/resume/resume` |
| `/student/profile` | `student-layout` | `features/student/profile/profile` |
| `/employer/dashboard` | `employer-layout` | `features/employer/dashboard/dashboard` |
| `/employer/post-job` | `employer-layout` | `features/employer/post-job/post-job` |
| `/employer/applicants` | `employer-layout` | `features/employer/applicants/applicants` |
| `/employer/interviews` | `employer-layout` | `features/employer/interviews/interviews` |
| `/employer/profile` | `employer-layout` | `features/employer/profile/profile` |
| `/officer/dashboard` | `officer-layout` | `features/placement-officer/dashboard/dashboard` |
| `/officer/drives` | `officer-layout` | `features/placement-officer/drives/drives` |
| `/officer/employers` | `officer-layout` | `features/placement-officer/employer-management/employer-management` |
| `/officer/analytics` | `officer-layout` | `features/placement-officer/analytics/analytics` |
| `/officer/students` | `officer-layout` | `features/placement-officer/student-overview/student-overview` |
| `/admin/dashboard` | `admin-layout` | `features/admin/dashboard/dashboard` |
| `/admin/users` | `admin-layout` | `features/admin/user-management/user-management` |
| `/admin/config` | `admin-layout` | `features/admin/system-config/system-config` |
| `/admin/audit-logs` | `admin-layout` | `features/admin/audit-logs/audit-logs` |
| `/admin/training-modules` | `admin-layout` | `features/admin/training-modules/training-modules` |

---

## Feature → Endpoints

### Auth
- `POST /api/auth/login`
- `POST /api/auth/register`

Backend: `controller/AuthController.java` → `service/auth/`

### Student — Profile & Resume
- `GET  /api/students/me`
- `PUT  /api/students/me`
- `GET  /api/resumes/my`
- `POST /api/resumes`
- `PATCH /api/resumes/{id}/primary`
- `DELETE /api/resumes/{id}`

Backend: `StudentController.java`, `ResumeController.java`

### Student — Jobs, Drives, Applications
- `GET  /api/jobs` (paginated, filterable by location/experience)
- `GET  /api/drives`
- `POST /api/applications`
- `GET  /api/applications/my`
- `DELETE /api/applications/{id}`

Backend: `JobController.java`, `PlacementDriveController.java`, `ApplicationController.java`

### Student — Training
- `GET /api/training/modules`

Backend: `TrainingController.java`

### Employer — Profile & Jobs
- `GET /api/employers/me`
- `PUT /api/employers/me`
- `POST /api/jobs`
- `GET  /api/jobs/my`

Backend: `EmployerController.java`, `JobController.java`

### Employer — Applicants & Interviews
- `GET /api/applications/employer`
- `GET /api/applications/job/{jobId}`
- `PATCH /api/applications/{id}/status`
- `GET /api/interviews/employer`
- `POST /api/interviews`

Backend: `ApplicationController.java`, `InterviewController.java`

### Placement Officer — Dashboard & Analytics
- `GET /api/analytics/dashboard`

Backend: `AnalyticsController.java`

### Placement Officer — Drives & Employers
- `GET  /api/drives`
- `POST /api/drives`
- `GET  /api/employers`
- `PUT  /api/employers/{id}/verify`

Backend: `PlacementDriveController.java`, `EmployerController.java`

### Placement Officer — Student Overview
- `GET /api/students` (paginated, filterable by department/batch/placement status)

Backend: `StudentController.java`

### Admin — Users
- `GET  /api/admin/stats`
- `GET  /api/admin/users`
- `POST /api/admin/users`
- `PATCH /api/admin/users/{id}/activate`
- `PATCH /api/admin/users/{id}/deactivate`
- `POST /api/admin/users/{id}/reset-password`

Backend: `AdminController.java`

### Admin — System Config
- `GET /api/admin/configs`
- `PUT /api/admin/configs/{key}`

Backend: `AdminController.java`

### Admin — Audit Logs
- `GET /api/admin/audit-logs`

Backend: `AdminController.java`

### Admin — Training Modules
- `GET    /api/admin/training-modules`
- `POST   /api/admin/training-modules`
- `PUT    /api/admin/training-modules/{id}`
- `DELETE /api/admin/training-modules/{id}`

Backend: `TrainingController.java`

---

## Layout Shells

| File | Used by |
|---|---|
| `layout/auth-layout/auth-layout` | Login, Register |
| `layout/student-layout/student-layout` | All `/student/**` routes |
| `layout/employer-layout/employer-layout` | All `/employer/**` routes |
| `layout/officer-layout/officer-layout` | All `/officer/**` routes |
| `layout/admin-layout/admin-layout` | All `/admin/**` routes |

---

## Shared Components

| File | Purpose |
|---|---|
| `shared/components/navbar/navbar` | Top bar with user menu, logout, theme toggle, notification bell |
| `shared/components/sidebar/sidebar` | Role-specific side navigation |
| `shared/components/status-badge/status-badge` | Color-coded status pills (e.g., SUBMITTED, SHORTLISTED, OFFERED) |
| `shared/components/loading-spinner/loading-spinner` | Centered progress spinner |
| `shared/components/notification-bell/notification-bell` | Badge + dropdown for WebSocket-driven notifications |

---

## Core Services

| File | Purpose |
|---|---|
| `core/services/auth.service` | Login/register, in-memory access token, current-user signal |
| `core/services/api.service` | HTTP wrapper prefixed with `environment.apiUrl` |
| `core/services/notification.service` | In-app notification list + history |
| `core/services/websocket.service` | STOMP over SockJS, JWT auth via CONNECT headers |
| `core/services/theme.service` | Dark/light toggle, persisted to `localStorage` |

---

## Guards & Interceptors

- `core/guards/auth.guard.ts` — requires a logged-in user
- `core/guards/role.guard.ts` — enforces role for the route
- `core/guards/no-auth.guard.ts` — redirects logged-in users away from `/auth/*`
- `core/interceptors/jwt.interceptor.ts` — attaches `Authorization: Bearer <token>`
- `core/interceptors/refresh.interceptor.ts` — refreshes access token on 401
- `core/interceptors/error.interceptor.ts` — centralized HTTP error handling
