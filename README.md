# Campus Placement Portal

Full-stack Campus Internship & Placement Management Portal.

| Layer | Technology |
|---|---|
| Frontend | Angular 20.3.0 (standalone components, Signals), Angular Material 20 (M3), RxJS 7.8 |
| Backend | Spring Boot 4.0.5, Java 21, Spring WebMVC |
| Database | MySQL 8.x (schema managed by Hibernate `ddl-auto=update`; Flyway disabled pending Spring Boot 4.x autoconfig) |
| Auth | JWT (jjwt 0.12.6) + Spring Security 6 + OAuth2 |
| Real-time | STOMP over SockJS (WebSocket) |
| Cache | Caffeine via Spring Cache |

See [`CLAUDE.md`](./CLAUDE.md) for full architectural conventions and [`MAPPING.md`](./MAPPING.md) for the feature → code map.

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
└── Frontend/Placement-portal/          ← Angular project
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

- A default **ADMIN** user — `admin@placementportal.edu` / `Admin@1234` (via `AdminUserSeeder`)
- Five **training modules** (HTML & CSS, JavaScript, Java, Python, Interview Prep) shown on the student Training page (via `TrainingService.seedIfEmpty`)

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
| `EMAIL_API_KEY` | `dev-placeholder-key` | SendGrid API key |

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

Frontend starts on **http://localhost:4200** and proxies API calls to `http://localhost:8081`.

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

## Roles

```
ADMIN > PLACEMENT_OFFICER > STUDENT
EMPLOYER (peer, no hierarchy with academic roles)
```

### Default admin login

A default admin is auto-seeded on first backend boot:

| Field | Value |
|---|---|
| Email | `admin@placementportal.edu` |
| Password | `Admin@1234` |

Change the password from the admin User Management screen before any non-local deployment.

---

## Project Documentation

- **[CLAUDE.md](./CLAUDE.md)** — Conventions, package layout, security rules, RBAC, do/don't list
- **[MAPPING.md](./MAPPING.md)** — Feature → code map for locating endpoints and components
- **[PROJECT_OVERVIEW.txt](./PROJECT_OVERVIEW.txt)** — High-level inventory of screens and endpoints
