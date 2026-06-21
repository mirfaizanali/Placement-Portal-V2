-- Training & Skill Development catalog.
-- Read by all authenticated students at /api/training/modules.
-- Managed by admins at /api/admin/training-modules.

CREATE TABLE training_modules (
    id             CHAR(36)      NOT NULL PRIMARY KEY,
    title          VARCHAR(120)  NOT NULL,
    description    VARCHAR(1000) NOT NULL,
    learning_link  VARCHAR(500)  NOT NULL,
    icon_name      VARCHAR(60)   NOT NULL,
    display_order  INT           NOT NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NULL,
    INDEX idx_training_modules_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO training_modules (id, title, description, learning_link, icon_name, display_order)
VALUES
    (UUID(), 'HTML & CSS Basics',
     'Learn the structural and styling foundations of every web page. You will build pages with semantic HTML and style them with modern CSS, covering selectors, the box model, flexbox and responsive layouts. A must-have skill for any frontend or full-stack interview.',
     'https://www.w3schools.com/html/', 'code', 1),

    (UUID(), 'JavaScript Basics',
     'Master the language that powers interactivity on the web. MDN''s official guide walks through variables, control flow, functions, objects, the DOM and async patterns. Strong JavaScript fundamentals open doors to web, mobile and Node.js roles.',
     'https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide', 'javascript', 2),

    (UUID(), 'Java Fundamentals',
     'Get hands-on with one of the most in-demand languages for campus placements. Covers syntax, OOP, collections, exception handling and basic I/O. Java fluency is expected at most service-based and product companies hiring freshers.',
     'https://www.w3schools.com/java/', 'coffee', 3),

    (UUID(), 'Python Basics',
     'Pick up Python — the go-to language for scripting, data, and automation. Learn data types, control flow, functions, file handling, and standard libraries. Python is the easiest second language to add to your resume and unlocks data-science roles.',
     'https://www.w3schools.com/python/', 'terminal', 4),

    (UUID(), 'Communication Skills / Interview Preparation',
     'Technical skill alone doesn''t land offers — clear communication does. Walk through common HR and behavioural questions, structure your answers using STAR, and learn how to talk about projects with impact. Pair this with the technical modules for a complete placement-ready toolkit.',
     'https://www.w3schools.com/howto/howto_interview.asp', 'record_voice_over', 5);
