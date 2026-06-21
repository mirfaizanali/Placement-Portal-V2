classDiagram
    direction LR

    %% User & Security Module
    class User {
        +Long id
        +String email
        +Role role
    }
    class AuditLog {
        +Long id
        +String action
        +DateTime timestamp
    }

    %% Profiles
    class Student {
        +Long id
        +String rollNumber
        +String department
    }
    class Employer {
        +Long id
        +String companyName
        +Boolean isVerified
    }

    %% Resumes & Training
    class Resume {
        +Long id
        +String filePath
        +Boolean isPrimary
    }
    class TrainingModule {
        +Long id
        +String title
    }

    %% Core Placement Workflow
    class PlacementDrive {
        +Long id
        +String title
        +Date date
    }
    class Job {
        +Long id
        +String title
        +Double minCgpa
    }
    class Application {
        +Long id
        +String status
    }
    class Interview {
        +Long id
        +DateTime dateTime
    }
    
    class SystemConfig {
        +String key
        +String value
    }

    %% Relationships
    User "1" -- "1" Student : profiles
    User "1" -- "1" Employer : profiles
    User "1" --> "*" AuditLog : generates
    
    Student "1" *-- "*" Resume : uploads
    Student "1" <-- "*" Application : submits
    Employer "1" *-- "*" Job : posts
    
    PlacementDrive "1" *-- "*" Job : hosts
    Job "1" *-- "*" Application : receives
    Application "1" *-- "*" Interview : schedules