-- Certificates table
CREATE TABLE certificates (
    certificate_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    type ENUM('quiz', 'assignment') NOT NULL,
    reference_id INT NOT NULL, -- quiz_id or assignment_id
    title VARCHAR(255) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    subject_name VARCHAR(255) NOT NULL,
    class_name VARCHAR(255) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    grade CHAR(1) NOT NULL,
    completion_date DATETIME NOT NULL,
    teacher_name VARCHAR(255) NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    semester VARCHAR(10) NOT NULL,
    certificate_number VARCHAR(50) NOT NULL UNIQUE,
    qr_code VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    INDEX idx_cert_student (student_id),
    INDEX idx_cert_type_ref (type, reference_id)
);

-- Rankings table for subjects
CREATE TABLE subject_rankings (
    ranking_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_id INT NOT NULL,
    class_id INT NOT NULL,
    student_id INT NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    semester VARCHAR(10) NOT NULL,
    total_score DECIMAL(5,2) NOT NULL,
    average_score DECIMAL(5,2) NOT NULL,
    rank_in_class INT NOT NULL,
    rank_in_subject INT NOT NULL,
    total_quizzes INT NOT NULL,
    total_assignments INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    UNIQUE KEY unique_subject_ranking (subject_id, class_id, student_id, academic_year, semester)
);

-- Rankings table for classes
CREATE TABLE class_rankings (
    ranking_id INT PRIMARY KEY AUTO_INCREMENT,
    class_id INT NOT NULL,
    student_id INT NOT NULL,
    academic_year VARCHAR(9) NOT NULL,
    semester VARCHAR(10) NOT NULL,
    total_score DECIMAL(5,2) NOT NULL,
    average_score DECIMAL(5,2) NOT NULL,
    rank_in_class INT NOT NULL,
    total_subjects INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(class_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    UNIQUE KEY unique_class_ranking (class_id, student_id, academic_year, semester)
);

-- Achievement badges
CREATE TABLE achievement_badges (
    badge_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon_url VARCHAR(255) NOT NULL,
    criteria_type ENUM('score', 'rank', 'completion') NOT NULL,
    criteria_value INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student achievements
CREATE TABLE student_achievements (
    achievement_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    badge_id INT NOT NULL,
    subject_id INT,
    class_id INT,
    earned_date DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (badge_id) REFERENCES achievement_badges(badge_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id)
);

-- Stored procedure to update rankings
DELIMITER //
CREATE PROCEDURE update_rankings(
    IN p_academic_year VARCHAR(9),
    IN p_semester VARCHAR(10)
)
BEGIN
    -- Update subject rankings
    INSERT INTO subject_rankings (
        subject_id, class_id, student_id, academic_year, semester,
        total_score, average_score, rank_in_class, rank_in_subject,
        total_quizzes, total_assignments
    )
    SELECT 
        s.subject_id,
        c.class_id,
        st.student_id,
        p_academic_year,
        p_semester,
        SUM(COALESCE(q.score, 0) + COALESCE(a.score, 0)) as total_score,
        AVG(COALESCE(q.score, 0) + COALESCE(a.score, 0)) as average_score,
        RANK() OVER (PARTITION BY s.subject_id, c.class_id 
                     ORDER BY AVG(COALESCE(q.score, 0) + COALESCE(a.score, 0)) DESC) as rank_in_class,
        RANK() OVER (PARTITION BY s.subject_id 
                     ORDER BY AVG(COALESCE(q.score, 0) + COALESCE(a.score, 0)) DESC) as rank_in_subject,
        COUNT(DISTINCT q.quiz_id) as total_quizzes,
        COUNT(DISTINCT a.assignment_id) as total_assignments
    FROM students st
    JOIN classes c ON st.class_id = c.class_id
    JOIN subjects s ON 1=1
    LEFT JOIN quiz_attempts q ON q.student_id = st.student_id AND q.subject_id = s.subject_id
    LEFT JOIN assignment_submissions a ON a.student_id = st.student_id AND a.subject_id = s.subject_id
    WHERE q.academic_year = p_academic_year
    AND q.semester = p_semester
    GROUP BY s.subject_id, c.class_id, st.student_id
    ON DUPLICATE KEY UPDATE
        total_score = VALUES(total_score),
        average_score = VALUES(average_score),
        rank_in_class = VALUES(rank_in_class),
        rank_in_subject = VALUES(rank_in_subject),
        total_quizzes = VALUES(total_quizzes),
        total_assignments = VALUES(total_assignments),
        updated_at = CURRENT_TIMESTAMP;

    -- Update class rankings
    INSERT INTO class_rankings (
        class_id, student_id, academic_year, semester,
        total_score, average_score, rank_in_class, total_subjects
    )
    SELECT 
        c.class_id,
        st.student_id,
        p_academic_year,
        p_semester,
        SUM(sr.total_score) as total_score,
        AVG(sr.average_score) as average_score,
        RANK() OVER (PARTITION BY c.class_id 
                     ORDER BY AVG(sr.average_score) DESC) as rank_in_class,
        COUNT(DISTINCT sr.subject_id) as total_subjects
    FROM students st
    JOIN classes c ON st.class_id = c.class_id
    JOIN subject_rankings sr ON sr.student_id = st.student_id
    WHERE sr.academic_year = p_academic_year
    AND sr.semester = p_semester
    GROUP BY c.class_id, st.student_id
    ON DUPLICATE KEY UPDATE
        total_score = VALUES(total_score),
        average_score = VALUES(average_score),
        rank_in_class = VALUES(rank_in_class),
        total_subjects = VALUES(total_subjects),
        updated_at = CURRENT_TIMESTAMP;
END //
DELIMITER ;

-- Trigger to generate certificates
DELIMITER //
CREATE TRIGGER after_quiz_graded
AFTER UPDATE ON quiz_attempts
FOR EACH ROW
BEGIN
    IF NEW.status = 'graded' AND OLD.status != 'graded' THEN
        INSERT INTO certificates (
            student_id, type, reference_id, title, student_name,
            subject_name, class_name, score, grade, completion_date,
            teacher_name, academic_year, semester, certificate_number, qr_code
        )
        SELECT 
            NEW.student_id,
            'quiz',
            NEW.quiz_id,
            q.title,
            s.full_name,
            sub.name,
            c.class_name,
            NEW.score,
            CASE 
                WHEN NEW.score >= 90 THEN 'A'
                WHEN NEW.score >= 80 THEN 'B'
                WHEN NEW.score >= 70 THEN 'C'
                WHEN NEW.score >= 60 THEN 'D'
                ELSE 'E'
            END,
            NOW(),
            t.full_name,
            q.academic_year,
            q.semester,
            CONCAT('QZ-', q.academic_year, '-', q.semester, '-', NEW.quiz_id, '-', NEW.student_id),
            CONCAT('https://smartapp.com/verify/', SHA2(CONCAT(NEW.quiz_id, '-', NEW.student_id, '-', NEW.score), 256))
        FROM quizzes q
        JOIN students s ON s.student_id = NEW.student_id
        JOIN subjects sub ON sub.subject_id = q.subject_id
        JOIN classes c ON c.class_id = q.class_id
        JOIN teachers t ON t.teacher_id = q.teacher_id
        WHERE q.quiz_id = NEW.quiz_id;
    END IF;
END //
DELIMITER ;

-- Trigger to generate certificates for assignments
DELIMITER //
CREATE TRIGGER after_assignment_graded
AFTER UPDATE ON assignment_submissions
FOR EACH ROW
BEGIN
    IF NEW.status = 'graded' AND OLD.status != 'graded' THEN
        INSERT INTO certificates (
            student_id, type, reference_id, title, student_name,
            subject_name, class_name, score, grade, completion_date,
            teacher_name, academic_year, semester, certificate_number, qr_code
        )
        SELECT 
            NEW.student_id,
            'assignment',
            NEW.assignment_id,
            a.title,
            s.full_name,
            sub.name,
            c.class_name,
            NEW.score,
            CASE 
                WHEN NEW.score >= 90 THEN 'A'
                WHEN NEW.score >= 80 THEN 'B'
                WHEN NEW.score >= 70 THEN 'C'
                WHEN NEW.score >= 60 THEN 'D'
                ELSE 'E'
            END,
            NOW(),
            t.full_name,
            a.academic_year,
            a.semester,
            CONCAT('AS-', a.academic_year, '-', a.semester, '-', NEW.assignment_id, '-', NEW.student_id),
            CONCAT('https://smartapp.com/verify/', SHA2(CONCAT(NEW.assignment_id, '-', NEW.student_id, '-', NEW.score), 256))
        FROM assignments a
        JOIN students s ON s.student_id = NEW.student_id
        JOIN subjects sub ON sub.subject_id = a.subject_id
        JOIN classes c ON c.class_id = a.class_id
        JOIN teachers t ON t.teacher_id = a.teacher_id
        WHERE a.assignment_id = NEW.assignment_id;
    END IF;
END //
DELIMITER ;