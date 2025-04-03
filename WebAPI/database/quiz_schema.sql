-- Quiz tables
CREATE TABLE quizzes (
    quiz_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    subject_id INT NOT NULL,
    class_id INT NOT NULL,
    teacher_id INT NOT NULL,
    duration_minutes INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    semester VARCHAR(10) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)
);

-- Questions table supporting both multiple choice and essay
CREATE TABLE questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    question_type ENUM('multiple_choice', 'essay') NOT NULL,
    points INT NOT NULL DEFAULT 1,
    answer_key TEXT, -- For essay questions
    order_number INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
);

-- Options for multiple choice questions
CREATE TABLE question_options (
    option_id INT PRIMARY KEY AUTO_INCREMENT,
    question_id INT NOT NULL,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT false,
    order_number INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- Student quiz attempts
CREATE TABLE quiz_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    quiz_id INT NOT NULL,
    student_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    score DECIMAL(5,2),
    status ENUM('in_progress', 'submitted', 'graded') NOT NULL DEFAULT 'in_progress',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);

-- Student answers
CREATE TABLE student_answers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option_id INT, -- For multiple choice
    essay_answer TEXT, -- For essay questions
    points_earned DECIMAL(5,2), -- Points awarded for this answer
    teacher_feedback TEXT, -- Teacher's feedback on essay answers
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id),
    FOREIGN KEY (selected_option_id) REFERENCES question_options(option_id)
);

-- Indexes for better performance
CREATE INDEX idx_quiz_subject ON quizzes(subject_id);
CREATE INDEX idx_quiz_class ON quizzes(class_id);
CREATE INDEX idx_quiz_teacher ON quizzes(teacher_id);
CREATE INDEX idx_question_quiz ON questions(quiz_id);
CREATE INDEX idx_option_question ON question_options(question_id);
CREATE INDEX idx_attempt_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_attempt_student ON quiz_attempts(student_id);
CREATE INDEX idx_answer_attempt ON student_answers(attempt_id);
CREATE INDEX idx_answer_question ON student_answers(question_id);