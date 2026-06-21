# Campus Placement Portal

Full-stack Campus Internship & Placement Management Portal.

| Layer | Technology |
|---|---|
| Frontend | Angular 20.3.0 (standalone components, Signals), Angular Material 20 (M3), RxJS 7.8 |
| Backend | Spring Boot 4.0.5, Java 21, Spring WebMVC |
| Database | MySQL 8.x (schema managed by Hibernate `ddl-auto=update`) |
| Auth | JWT (jjwt 0.12.6) + Spring Security 6 |
| Real-time | STOMP over SockJS (WebSocket) |
| Cache | Caffeine via Spring Cache |

See [`MAPPING.md`](./MAPPING.md) for the feature → code map and [`PROJECT_OVERVIEW.txt`](./PROJECT_OVERVIEW.txt) for the screen/endpoint inventory.

---

## Prerequisites

- **JDK 21**
- **Node.js 20+** and **npm 10+**
- **MySQL 8.x** running on `localhost:3306`
- **Angular CLI 20** (`npm install -g @angular/cli`)
- **Maven 3.9+** (or use the bundled `mvnw` wrapper)

---

## Repository Structure

```
SIH-main/
├── Backend/placement-portal-backend/   ← Spring Boot project
├── Frontend/Placement-portal/          ← Angular project
└── docs/                               ← this folder
```

---

## 1. Database Setup

Create the database (default credentials are `root` / `root`):

```sql
CREATE DATABASE placement_portal_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Schema is auto-created on first backend boot via Hibernate `ddl-auto=update`.

On first boot the backend also seeds:

- A default **ADMIN** user — `admin@placementportal.edu` / `Admin@1234`
- Five **training modules** (HTML & CSS, JavaScript, Java, Python, Interview Prep) shown on the student Training page

Both seeders are idempotent — restarting the backend will not duplicate rows.

---

## 2. Backend Setup

```bash
cd Backend/placement-portal-backend
./mvnw spring-boot:run
```

Backend starts on **http://localhost:8081**.

### Environment variables (optional — defaults work for local dev)

| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/placement_portal_db?...` | JDBC URL |
| `DB_USERNAME` | `root` | |
| `DB_PASSWORD` | `root` | |
| `JWT_SECRET` | dev placeholder | **Override in prod.** 512-bit base64 |

### Useful commands

```bash
./mvnw test                  # run unit tests
./mvnw clean package         # build JAR
```

---

## 3. Frontend Setup

```bash
cd Frontend/Placement-portal
npm install
ng serve
```

Frontend starts on **http://localhost:4200**.

API and WebSocket URLs are configured in `src/environments/environment.ts`:

```ts
apiUrl: 'http://localhost:8081'
wsUrl:  'ws://localhost:8081/ws'
```

### Useful commands

```bash
ng build                     # production build → dist/
ng test                      # unit tests
```

---

## 4. Default Ports

| Service | Port |
|---|---|
| Frontend (Angular dev server) | 4200 |
| Backend (Spring Boot) | 8081 |
| MySQL | 3306 |

CORS allows only `http://localhost:4200` in development.

---

## Roles (UI-facing)

```
ADMIN > PLACEMENT_OFFICER > STUDENT
EMPLOYER (peer, no hierarchy with academic roles)
```

Each role lands in its own layout shell after login.

### Default admin login

A default admin is auto-seeded on first backend boot:

| Field | Value |
|---|---|
| Email | `admin@placementportal.edu` |
| Password | `Admin@1234` |

Change the password from the admin User Management screen before any non-local deployment.

---

## Implemented UI Features (at a glance)

| Role | Screens |
|---|---|
| Public | Landing page |
| Auth | Login, Register |
| Student | Dashboard, Job Search, Drives, Applications, Training, Resume, Profile |
| Employer | Dashboard, Post Job, Applicants, Interviews, Profile |
| Placement Officer | Dashboard, Drives, Employer Management, Analytics, Student Overview |
| Admin | Dashboard, User Management, System Config, Audit Logs, Training Modules |

Full screen-by-screen detail lives in [`PROJECT_OVERVIEW.txt`](./PROJECT_OVERVIEW.txt). Code locations are in [`MAPPING.md`](./MAPPING.md).
