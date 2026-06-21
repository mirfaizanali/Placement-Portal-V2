graph LR
    %% Actors Configuration
    subgraph Actors
        Student((Student))
        Employer((Employer))
        Officer((Placement Officer))
        Admin((Admin))
    end

    %% System Boundary
    subgraph Campus Placement Portal System
        %% Student Use Cases
        UC1(Manage Profile & Resumes)
        UC2(Browse & Apply to Jobs)
        UC3(Track & Withdraw Applications)
        UC4(Access Training Resources)
        
        %% Employer Use Cases
        UC5(Update Company Profile)
        UC6(Post New Job Openings)
        UC7(Review Applicants & Change Status)
        UC8(Schedule Interview Rounds)
        
        %% Officer Use Cases
        UC9(Create & Manage Drives)
        UC10(Verify Employers)
        UC11(View Student Insights)
        UC12(Monitor Placement Analytics)
        
        %% Admin Use Cases
        UC13(User CRUD & PW Reset)
        UC14(Edit System Configurations)
        UC15(Manage Training Catalog)
        UC16(Review Audit Logs)
    end

    %% Student Connections
    Student --> UC1
    Student --> UC2
    Student --> UC3
    Student --> UC4

    %% Employer Connections
    Employer --> UC5
    Employer --> UC6
    Employer --> UC7
    Employer --> UC8

    %% Officer Connections
    Officer --> UC9
    Officer --> UC10
    Officer --> UC11
    Officer --> UC12

    %% Admin Connections
    Admin --> UC13
    Admin --> UC14
    Admin --> UC15
    Admin --> UC16