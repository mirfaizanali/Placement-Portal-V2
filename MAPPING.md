# MAPPING — Campus Placement Portal

End-to-end map of user-facing functionality to the source files that implement
it. Use this as the entry point when you need to find where a feature lives.

> Paths are repo-relative. Backend root: `Backend/placement-portal-backend/`.
> Frontend root: `Frontend/Placement-portal/`.

---

## 1. Overview

| Layer | Tech | Port |
|---|---|---|
| Frontend | Angular 20.3 (standalone components, Signals), Angular Material 20 (M3), RxJS 7.8 | 4200 |
| Backend  | Spring Boot 4.0.5, Java 21, Spring WebMVC, Spring Security 6, JJWT 0.12.6, Spring Cache (Caffeine), STOMP/SockJS WebSocket | 8081 |
| Database | MySQL 8.x (Hibernate `ddl-auto=update`; Flyway disabled — see Known Issues) | 3306 |

---

## 2. Setup & Environment Variables

The backend reads these env vars (with sane defaults so `mvn spring-boot:run`
works out of the box):

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/placement_portal_db?...` | JDBC URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | `root` | DB password |
| `JWT_SECRET` | (committed dev secret — **rotate before prod**) | HS512 base64 key |
| `EMAIL_API_KEY` | `dev-placeholder-key` | SendGrid API key |

Frontend `apiUrl` lives in `Frontend/Placement-portal/src/environments/environment.ts`.

---

## 3. Run Commands

**Backend** (from `Backend/placement-portal-backend/`):
```
./mvnw spring-boot:run        # boot app
./mvnw test                   # run unit tests
./mvnw clean package          # build JAR
```

**Frontend** (from `Frontend/Placement-portal/`):
```
npm install
ng serve                                    # dev server on :4200
ng test --watch=false --browsers=ChromeHeadless   # unit tests
ng build                                    # production build
```

---

## 4. Roles & Permissions

| Role | Source enum | Key permissions | Frontend surface? |
|---|---|---|---|
| STUDENT | `domain/enums/Role.java` | apply to jobs/internships, upload resume, view own apps | yes |
| EMPLOYER | same | post job/internship, view applicants, schedule interview | yes |
| FACULTY_MENTOR | same | view mentees, approve/revise reports | **no UI** — backend endpoints exist but no `/faculty` routes or components |
| PLACEMENT_OFFICER | same | manage drives, verify employers, view analytics | yes |
| ADMIN | same | manage users, system config, audit logs (+ all above) | yes |

Hierarchy is enforced in `config/SecurityConfig.java` via `RoleHierarchyImpl`.
Method-level checks use `@PreAuthorize` on controllers; template-level checks
use the `appHasRole` directive (`shared/directives/has-role.directive.ts`).

---

## 5. Feature → Code Mapping

Each row: **Capability** • **Frontend route → component** • **Backend endpoint** • **Controller → Service → Repository → Entity**

### 5.1 Landing & Authentication

| Capability | Frontend | Backend Endpoint | Controller / Service / Entity |
|---|---|---|---|
| Public landing page | `/` → `features/landing/landing.ts` | — | — |
| Login | `/auth/login` → `features/auth/login/login.ts` | `POST /api/auth/login` | `controller/AuthController.java` → `service/auth/AuthService.java` → `domain/User.java` |
| Register | `/auth/register` → `features/auth/register/register.ts` | `POST /api/auth/register` | same as above; also creates role-specific profile shell |
| Refresh token | (interceptor, automatic) `core/interceptors/refresh.interceptor.ts` | `POST /api/auth/refresh` | `AuthService.refreshWithToken` → `service/auth/RefreshTokenService.java` → `domain/RefreshToken.java` |
| Logout | navbar menu → `core/services/auth.service.ts` | `POST /api/auth/logout` | `AuthController.logout` → `RefreshTokenService.revokeAllUserTokens` |

Frontend session state: `core/services/auth.service.ts` (Signals: `currentUser`, `isAuthenticated`, `userRole`). Access token in-memory; refresh token in httpOnly cookie.

### 5.2 Student — Profile & Resume

| Capability | Frontend | Backend Endpoint | Controller / Service / Entity |
|---|---|---|---|
| View / edit own profile | `/student/profile` → `features/student/profile/profile.ts` | `GET/PUT /api/students/me` | `controller/StudentController.java` → `service/user/StudentService.java` → `repository/StudentProfileRepository.java` → `domain/StudentProfile.java` |
| View own dashboard | `/student/dashboard` → `features/student/dashboard/dashboard.ts` | `GET /api/applications/my` | `ApplicationController` → `ApplicationService` |
| List & upload resumes | `/student/resume` → `features/student/resume/resume.ts` | `GET/POST/PATCH/DELETE /api/resumes/**` | `ResumeController` → `service/resume/ResumeStorageService.java` → `domain/Resume.java` |
| Training & Skill Development | `/student/training` → `features/student/training/training.ts` | `GET /api/training/modules` | `controller/TrainingController.java` → `service/placement/TrainingService.java` → `repository/TrainingModuleRepository.java` → `domain/TrainingModule.java` |

### 5.3 Student — Job Discovery, Drives & Applications

| Capability | Frontend | Backend Endpoint | Controller / Service / Entity |
|---|---|---|---|
| Browse jobs | `/student/jobs` → `features/student/job-search/job-search.ts` | `GET /api/jobs` | `JobController` → `service/placement/JobService.java` → `domain/Job.java` |
| Apply to a job | (same component dialog) | `POST /api/applications` | `ApplicationController` → `service/placement/ApplicationService.java` → `domain/Application.java` |
| Browse drives | `/student/drives` → `features/student/drives/drives.ts` (`StudentDrives`) | `GET /api/drives` | `PlacementDriveController` → `service/placement/PlacementDriveService.java` → `domain/PlacementDrive.java` |
| List own applications | `/student/applications` → `features/student/applications/applications.ts` | `GET /api/applications/my` | same |
| Withdraw application | (applications list action) | `DELETE /api/applications/{id}` | `ApplicationService.withdrawApplication` |

> **Backend-only (no student UI surface today):** `InternshipController` (`/api/internships/**`), `RecommendationController` (`/api/recommendations/**`), and the report endpoints (`ReportService` + `/api/reports/**`) all exist server-side but have no corresponding student route/component. The old `student/internships`, `student/reports`, and `student/recommendations` features were removed.

### 5.4 Faculty Mentor — backend only

There is currently **no frontend surface for faculty**: no `/faculty` route, no `features/faculty/*` components, no faculty layout. The server-side pieces remain in place and are reachable by a faculty-role token via HTTP:

| Capability | Backend Endpoint | Controller / Service |
|---|---|---|
| Faculty self / mentees | `GET /api/faculty/me`, `GET /api/faculty/me/mentees` | `FacultyController` → `service/user/FacultyService.java` |
| Review reports | `GET/PATCH /api/reports/**` | `service/placement/ReportService.java` → `domain/Report.java` |

### 5.5 Employer

| Capability | Frontend | Backend Endpoint | Controller / Service / Entity |
|---|---|---|---|
| Dashboard | `/employer/dashboard` → `features/employer/dashboard/dashboard.ts` | `GET /api/jobs/my`, `GET /api/applications/...` | `JobController`, `ApplicationController` |
| Edit company profile | `/employer/profile` → `features/employer/profile/profile.ts` (`EmployerProfile`) | `GET/PUT /api/employers/me` | `EmployerController` → `service/user/EmployerService.java` → `domain/EmployerProfile.java` |
| Post a job | `/employer/post-job` → `features/employer/post-job/post-job.ts` | `POST /api/jobs` | `JobController.create` → `JobService.createJob` |
| View applicants | `/employer/applicants` → `features/employer/applicants/applicants.ts` | `GET /api/applications/job/{jobId}`, `PATCH /api/applications/{id}/status` | `ApplicationController`, `ApplicationService.updateApplicationStatus` |
| Schedule interview | `/employer/interviews` → `features/employer/interviews/interviews.ts` | `POST/GET/PATCH/DELETE /api/interviews/**` | `InterviewController` → `service/placement/InterviewService.java` → `domain/Interview.java` |

### 5.6 Placement Officer

| Capability | Frontend | Backend Endpoint | Controller / Service |
|---|---|---|---|
| Dashboard / Analytics | `/officer/dashboard`, `/officer/analytics` → `features/placement-officer/dashboard/dashboard.ts`, `analytics/analytics.ts` | `GET /api/analytics/dashboard` | `AnalyticsController` → `service/analytics/AnalyticsService.java` (cached 15 min) |
| Manage drives | `/officer/drives` → `features/placement-officer/drives/drives.ts` | `POST/GET/PATCH /api/drives/**` | `PlacementDriveController` → `service/placement/PlacementDriveService.java` → `domain/PlacementDrive.java` |
| Verify employers | `/officer/employers` → `features/placement-officer/employer-management/employer-management.ts` | `GET /api/employers`, `PUT /api/employers/{id}/verify` | `EmployerController.verify` → `EmployerService.verifyEmployer` |
| Student overview | `/officer/students` → `features/placement-officer/student-overview/student-overview.ts` | `GET /api/students?department=…&batchYear=…&isPlaced=…` | `StudentController` → `StudentService.getAllStudents` |
| Assign faculty mentor | (student overview action) | `PUT /api/students/{id}/mentor` | `StudentService.assignFacultyMentor` |

### 5.7 Admin

| Capability | Frontend | Backend Endpoint | Controller / Service |
|---|---|---|---|
| Dashboard | `/admin/dashboard` → `features/admin/dashboard/dashboard.ts` (`AdminDashboard`) | `GET /api/admin/users?…` | `AdminController` → `service/admin/AdminUserService.java` |
| User management | `/admin/users` → `features/admin/user-management/user-management.ts` | `GET/POST /api/admin/users`, `PATCH /api/admin/users/{id}/(de)activate`, `POST /api/admin/users/{id}/reset-password` | same |
| Training modules CRUD | `/admin/training-modules` → `features/admin/training-modules/training-modules.ts` | `GET/POST/PUT/DELETE /api/admin/training-modules/**` | `AdminController` → `service/placement/TrainingService.java` → `repository/TrainingModuleRepository.java` → `domain/TrainingModule.java` |
| System config | `/admin/config` → `features/admin/system-config/system-config.ts` | `GET/PUT/DELETE /api/admin/configs/**` | `service/admin/SystemConfigService.java` → `domain/SystemConfig.java` |
| Audit logs | `/admin/audit-logs` → `features/admin/audit-logs/audit-logs.ts` | `GET /api/admin/audit-logs` | reads from `repository/AuditLogRepository.java` → `domain/AuditLog.java` (written by `security/AuditLoggingFilter.java`) |
| Default admin seeding | (none — backend startup) | — | `service/admin/AdminUserSeeder.java` (`@PostConstruct`) — idempotently creates `admin@placementportal.edu` / `Admin@1234` if absent |

### 5.8 Notifications (cross-cutting)

| Capability | Frontend | Backend Endpoint | Controller / Service |
|---|---|---|---|
| Notification bell, unread count | `shared/components/notification-bell/notification-bell.ts` + `core/services/notification.service.ts` | `GET /api/notifications?...`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all` | `NotificationController` → `service/notification/NotificationService.java` → `domain/Notification.java` |
| Real-time push | `core/services/websocket.service.ts` (STOMP over SockJS) | STOMP destination `/user/queue/notifications` | `service/notification/WebSocketNotificationService.java`; broker config in `config/WebSocketConfig.java` |
| Email fan-out | (none — backend only) | outbound SendGrid call | `service/notification/EmailNotificationService.java` (WebFlux `WebClient`) |

---

## 6. Cross-Cutting

### Frontend
- **Route definitions:** `src/app/app.routes.ts` (lazy-loaded per feature).
- **Layouts:** `src/app/layout/*` — one shell per role currently exposed (`auth-layout`, `student-layout`, `employer-layout`, `officer-layout`, `admin-layout`). No `faculty-layout` — see §5.4.
- **Guards:** `core/guards/{auth,role,no-auth}.guard.ts`.
- **HTTP interceptors:** `core/interceptors/{jwt,refresh,error}.interceptor.ts`, registered in `src/app/app.config.ts` via `withInterceptors`.
- **Shared UI:** `shared/components/{navbar,sidebar,notification-bell,status-badge,loading-spinner}`.
- **Pipes:** `shared/pipes/{currency-inr,time-ago}.pipe.ts`.
- **Theme:** `core/services/theme.service.ts` (light/dark, persisted to localStorage).

### Backend
- **Security entry point:** `config/SecurityConfig.java` (filter chain, role hierarchy, password encoder).
- **JWT filter:** `security/jwt/JwtAuthenticationFilter.java` (reads bearer token, sets SecurityContext).
- **Audit filter:** `security/AuditLoggingFilter.java`.
- **Rate limiting:** `security/RateLimitingFilter.java` (config in `application.properties` — **see Known Issues, currently the filter is wired but quotas are not enforced**).
- **Global error handler:** `exception/GlobalExceptionHandler.java` (`@ControllerAdvice`).
- **Entity ↔ DTO conversion:** `mapper/EntityMapper.java` (single bean, all conversions).
- **Cache config:** `config/CacheConfig.java` (Caffeine, 1000 entries, 5-min TTL by default; analytics has a longer 15-min TTL).
- **CORS:** `config/CorsConfig.java` (origin from `app.cors.allowed-origins`).
- **Recommendation pipeline:** `MatchCandidateBuilder` loads candidates → `ScoringEngine` (pure, stateless) computes scores → `RecommendationService` sorts & returns.

---

## 7. Known Issues / Tech Debt

Not fixed in the test-cases pass — flagged here for follow-up. File paths point to the locus of each issue.

| # | Issue | Source |
|---|---|---|
| T1 | Flyway disabled; schema is whatever `ddl-auto=update` produces. No version control of DB structure. Re-enable when Spring Boot 4.x Flyway autoconfig lands. | `Backend/.../resources/application.properties` lines 25-31 |
| T2 | Manual in-memory pagination (`subList` over `findAll`). Doesn't scale beyond campus-sized datasets. | `service/user/StudentService.java:93-103`, `service/placement/ApplicationService.java:220-227`, plus similar in `PlacementDriveService`, `InternshipService`, `ReportService`, `NotificationService` |
| T3 | `EmailNotificationService` swallows delivery errors silently — only DB row remains as proof. Add retry queue or delivery confirmation. | `service/notification/EmailNotificationService.java` |
| T4 | SendGrid placeholder key (`dev-placeholder-key`) — email will not actually send until `EMAIL_API_KEY` is set in deployment. | `application.properties` line 63 |
| T5 | Rate-limiting properties (`app.rate-limit.requests-per-minute`) are configured but the filter is not enforcing per-user quotas. | `security/RateLimitingFilter.java` |
| T6 | Refresh-token cookie uses `Secure=true` hardcoded; comment tells devs to manually flip it for HTTP dev. Should read `${SERVER_SSL_ENABLED}`. | `controller/AuthController.java:153, 166` |
| T7 | Soft-delete only (jobs/internships set status to CLOSED rather than DELETE). No GDPR/retention policy. | `service/placement/JobService.java`, `InternshipService.java` |
| T8 | Several backend surfaces have no frontend counterpart: faculty workflows (`/api/faculty/**`, `/api/reports/**`), recommendations (`/api/recommendations/**`), and the standalone internships browser (`/api/internships/**`). Drives are now student-visible; the rest are reachable only by direct HTTP. | `controller/FacultyController.java`, `controller/RecommendationController.java`, `controller/InternshipController.java` |
| T9 | Frontend `ApplicationDto.appliedAt` rendering does not localize timezone — uses raw ISO string in some templates. | various `*.html` |
| T10 | The legacy `PlacementPortalBackendApplicationTests` does a `@SpringBootTest` context load that requires a running MySQL — it will fail in CI without one. Either replace with a slice test or provide a test-profile datasource. | `src/test/java/com/placement/portal/placement_portal_backend/PlacementPortalBackendApplicationTests.java` |

---

## 8. Test Inventory

Tests added in the most recent pass. Each row identifies which bug fix it locks in (bug IDs B1–B6 are described below).

### Backend (`Backend/placement-portal-backend/src/test/java/...`)

| Test class | Locks | What it asserts |
|---|---|---|
| `service/auth/AuthServiceTest` | B2, B6 | Login response includes the user's email; employer registration seeds `companyName` from `fullName`. |
| `service/user/StudentServiceTest` | B1 | `preferredLocations` and `preferredJobTypes` are written to the entity; null fields don't overwrite existing values. |
| `service/placement/ApplicationServiceTest` | B4 | Transitioning an application to ACCEPTED writes `isPlaced=true`, `placedCompany`, and `placementPackage` to the student profile. Also covers REJECTED no-op and CGPA disqualifier. |
| `service/recommendation/ScoringEngineTest` | (regression) | Skill, CGPA (incl. hard disqualifier), experience, preference, recency scoring. |
| `security/jwt/JwtTokenProviderTest` | (regression) | Generate/validate round-trip; tampered, empty, expired tokens rejected. |

### Frontend (`Frontend/Placement-portal/src/app/...`)

| Spec file | Locks | What it asserts |
|---|---|---|
| `core/services/auth.service.spec.ts` | B2 | `login()` stores `email` from the AuthResponse onto `currentUser()`. |
| `features/student/profile/profile.spec.ts` | B1 | Profile form hydrates `preferredLocations`/`preferredJobTypes` from the loaded DTO and sends them in the PUT body. |
| `features/faculty/report-review/report-review.spec.ts` | B3 | Approve flow sends the typed reviewer comment, not a hardcoded empty string. |
| `core/guards/auth.guard.spec.ts` | (regression) | Redirects unauthenticated users to `/auth/login`. |
| `core/guards/role.guard.spec.ts` | (regression) | Blocks users without the required role; passes when no role required. |
| `core/interceptors/jwt.interceptor.spec.ts` | (regression) | Attaches `Authorization: Bearer …` when a token exists. |
| `shared/directives/has-role.directive.spec.ts` | (regression) | Renders only when user role matches (single or array). |

### Bugs fixed in this pass

| ID | Bug | Fix location |
|---|---|---|
| B1 | Student profile `preferredLocations` / `preferredJobTypes` editable but not hydrated from backend and not exposed in response DTO | `dto/response/StudentProfileDto.java`, `mapper/EntityMapper.java`, `core/models/student.model.ts`, `features/student/profile/profile.ts` |
| B2 | `AuthResponse.email` was always empty — frontend `currentUser().email` returned `''` | `dto/response/AuthResponse.java`, `service/auth/AuthService.buildAuthResponse`, `core/models/user.model.ts`, `core/services/auth.service.ts` |
| B3 | Faculty "Approve" always sent `reviewerComments: ''` with no UI to enter a note | `features/faculty/report-review/report-review.{ts,html}` |
| B4 | `StudentProfile.placementPackage` was queried by analytics but never written, so dashboard "average package" was always 0 | `service/placement/ApplicationService.markStudentPlaced` + `resolvePackage` helper |
| B5 | DB credentials and JWT secret committed as literals in `application.properties` | now `${DB_USERNAME:root}`, `${DB_PASSWORD:root}`, `${JWT_SECRET:…}` |
| B6 | New `EmployerProfile` rows created with `companyName=""`, breaking any UI that lists employers before onboarding | `AuthService.createProfileShell` and `AdminUserService.createProfileShell` now seed it from `user.fullName` |
