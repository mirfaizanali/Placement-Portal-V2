stateDiagram-v2
    [*] --> SUBMITTED : Student Applies

    state SUBMITTED {
        [*] --> PendingReview
    }
    
    SUBMITTED --> WITHDRAWN : Student Withdraws
    SUBMITTED --> REJECTED : Employer Rejects Profile
    SUBMITTED --> SHORTLISTED : Employer Shortlists Profile

    state SHORTLISTED {
        [*] --> VerifiedEligibility
    }
    
    SHORTLISTED --> WITHDRAWN : Student Withdraws
    SHORTLISTED --> REJECTED : Profile Filtered Out
    SHORTLISTED --> INTERVIEWING : Schedule Interview Round 1

    state INTERVIEWING {
        [*] --> RoundScheduled
        RoundScheduled --> RoundExecuted : Date/Time Reached
        RoundExecuted --> RoundEvaluated : Feedback Submitted
        
        state round_check <<choice>>
        RoundEvaluated --> round_check
        
        round_check --> RoundScheduled : Pass [More Rounds Left]
    }

    INTERVIEWING --> REJECTED : Fail Round
    
    state interview_success_check <<choice>>
    INTERVIEWING --> interview_success_check : All Rounds Complete
    
    interview_success_check --> OFFERED : Pass Final Selection
    interview_success_check --> REJECTED : Fail Final Selection

    OFFERED --> [*]
    REJECTED --> [*]
    WITHDRAWN --> [*]