graph TD
    %% Styling Classes for Architecture Layers
    classDef actor fill:#eaeaea,stroke:#333,stroke-width:2px;
    classDef frontend fill:#eef2ff,stroke:#4f46e5,stroke-width:2px;
    classDef interceptor fill:#fef3c7,stroke:#d97706,stroke-width:1px,stroke-dasharray: 5 5;
    classDef backend fill:#ecfdf5,stroke:#059669,stroke-width:2px;
    classDef database fill:#fff1f2,stroke:#e11d48,stroke-width:2px;

    %% --- NODES DECLARATION ---
    
    %% Actors
    Emp((Employer Actor)):::actor

    %% Angular Frontend Layer
    Layout[employer-layout.component.ts]:::frontend
    Comp[post-job.component.ts]:::frontend
    ApiSvc[api.service.ts]:::frontend
    JwtInt[jwt.interceptor.ts]:::interceptor
    Bell[notification-bell.component.ts]:::frontend

    %% Spring Boot Backend Layer
    SecFilter[Spring Security Filter Chain]:::interceptor
    JobCtrl[JobController.java]:::backend
    JobSvc[JobService.java]:::backend
    AuditSvc[AuditLogService.java]:::backend
    WSSvc[WebSocketNotificationService.java]:::backend

    %% Database Tier
    DB[(MySQL Database)]:::database

    %% --- DETAILED INTERACTION LINKS ---

    %% Frontend local interaction
    Emp -- "1: fillForm(title, minCgpa, deadline)" --> Layout
    Layout -- "1.1: instantiate" --> Comp
    Comp -- "1.2: validate & submit()" --> Comp
    
    %% Outbound HTTP Lifecycle
    Comp -- "2: post('/api/jobs', body)" --> ApiSvc
    ApiSvc -- "2.1: intercept(req)" --> JwtInt
    JwtInt -- "2.2: append Header ('Authorization: Bearer')" --> ApiSvc
    
    %% Server-Side Ingestion & Security
    ApiSvc -- "3: HTTP POST /api/jobs" --> SecFilter
    SecFilter -- "3.1: authorizeRole('EMPLOYER')" --> JobCtrl
    JobCtrl -- "3.2: createJob(dto)" --> JobSvc
    
    %% Persistence & Side Effects
    JobSvc -- "3.3: save(jobEntity)" --> DB
    JobSvc -- "3.4: logAction('CREATE', 'JOB_POST')" --> AuditSvc
    AuditSvc -- "3.5: INSERT INTO audit_log" --> DB
    
    %% Real-time Notification Loop
    JobSvc -- "4: dispatchJobAlert(jobId)" --> WSSvc
    WSSvc -- "4.1: STOMP Broadcast (/user/queue/notifications)" --> Bell
    Bell -- "4.2: Reactive Signal UI Update (Increment Badge)" --> Emp