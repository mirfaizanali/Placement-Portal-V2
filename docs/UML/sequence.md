sequenceDiagram
    autonumber
    actor Student as Student (User)
    participant UI as Angular Frontend
    participant API as Spring Boot Backend
    participant DB as MySQL Database

    Student->>UI: Click "Apply" on Job Card
    activate UI
    UI->>UI: Intercept with JWT Token
    
    UI->>API: HTTP POST /api/applications (Job ID, Resume ID)
    activate API
    API->>API: Validate Session & CGPA Eligibility
    
    API->>DB: INSERT INTO application (student_id, job_id, status)
    activate DB
    DB-->>API: Confirm Save (Row Created)
    deactivate DB
    
    API-->>UI: Return HTTP 201 Created (Application Object)
    deactivate API
    
    UI-->>Student: Update Status Badge to "SUBMITTED"
    deactivate UI