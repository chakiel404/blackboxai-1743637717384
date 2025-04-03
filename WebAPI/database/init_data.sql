-- Insert admin user (password: admin123)
INSERT INTO users (email, password, full_name, role) VALUES 
('admin@smartapp.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', 'admin');

-- Insert test teachers (password: teacher123)
INSERT INTO users (email, password, full_name, role) VALUES 
('john.doe@smartapp.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John Doe', 'guru'),
('jane.smith@smartapp.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane Smith', 'guru');

-- Insert teacher details
INSERT INTO teachers (user_id, nip, gender, birth_date, birth_place, address, phone, education_level, major, join_date) VALUES
((SELECT user_id FROM users WHERE email = 'john.doe@smartapp.com'), 
 '198701012015041001', 'L', '1987-01-01', 'Jakarta', 'Jl. Pendidikan No. 1, Jakarta', '081234567890', 'S2', 'Matematika', '2015-04-01'),
((SELECT user_id FROM users WHERE email = 'jane.smith@smartapp.com'),
 '198803042016052002', 'P', '1988-03-04', 'Bandung', 'Jl. Guru No. 2, Bandung', '081234567891', 'S1', 'Fisika', '2016-05-01');

-- Insert test students (password: student123)
INSERT INTO users (email, password, full_name, role) VALUES 
('student1@smartapp.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Student One', 'siswa'),
('student2@smartapp.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Student Two', 'siswa'),
('student3@smartapp.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Student Three', 'siswa');

-- Insert student details
INSERT INTO students (user_id, nisn, class, gender, birth_date, birth_place, address, phone, parent_name, parent_phone) VALUES
((SELECT user_id FROM users WHERE email = 'student1@smartapp.com'),
 '0051234567', 'X-IPA-1', 'L', '2005-05-15', 'Jakarta', 'Jl. Pelajar No. 1, Jakarta', '081234567892', 'Parent One', '081234567893'),
((SELECT user_id FROM users WHERE email = 'student2@smartapp.com'),
 '0067891234', 'X-IPA-2', 'P', '2006-07-20', 'Surabaya', 'Jl. Siswa No. 2, Surabaya', '081234567894', 'Parent Two', '081234567895'),
((SELECT user_id FROM users WHERE email = 'student3@smartapp.com'),
 '0089123456', 'X-IPS-1', 'L', '2008-03-10', 'Bandung', 'Jl. Murid No. 3, Bandung', '081234567896', 'Parent Three', '081234567897');

-- Insert subjects
INSERT INTO subjects (name, description) VALUES 
('Mathematics', 'Basic and advanced mathematics concepts'),
('Physics', 'Study of matter, energy, and their interactions'),
('Chemistry', 'Study of substances and their properties'),
('Biology', 'Study of living organisms'),
('Computer Science', 'Study of computers and programming');

-- Assign teachers to subjects
INSERT INTO teacher_subjects (teacher_id, subject_id, academic_year, semester) VALUES
((SELECT teacher_id FROM teachers WHERE nip = '198701012015041001'), 
 (SELECT subject_id FROM subjects WHERE name = 'Mathematics'), '2023/2024', '1'),
((SELECT teacher_id FROM teachers WHERE nip = '198701012015041001'), 
 (SELECT subject_id FROM subjects WHERE name = 'Computer Science'), '2023/2024', '1'),
((SELECT teacher_id FROM teachers WHERE nip = '198803042016052002'), 
 (SELECT subject_id FROM subjects WHERE name = 'Physics'), '2023/2024', '1');

-- Insert sample materials
INSERT INTO materials (title, description, file_path, file_name, subject_id, uploaded_by) VALUES
('Introduction to Algebra', 'Basic algebraic concepts', '/uploads/materials/algebra_intro.pdf', 'algebra_intro.pdf',
 (SELECT subject_id FROM subjects WHERE name = 'Mathematics'),
 (SELECT user_id FROM users WHERE email = 'john.doe@smartapp.com')),
('Newton''s Laws', 'Introduction to Newton''s laws of motion', '/uploads/materials/newton_laws.pdf', 'newton_laws.pdf',
 (SELECT subject_id FROM subjects WHERE name = 'Physics'),
 (SELECT user_id FROM users WHERE email = 'jane.smith@smartapp.com'));

-- Insert sample quizzes
INSERT INTO quizzes (title, description, subject_id, created_by, duration_minutes) VALUES
('Algebra Quiz 1', 'Basic algebra operations', 
 (SELECT subject_id FROM subjects WHERE name = 'Mathematics'),
 (SELECT user_id FROM users WHERE email = 'john.doe@smartapp.com'),
 60),
('Physics Quiz 1', 'Newton''s laws applications',
 (SELECT subject_id FROM subjects WHERE name = 'Physics'),
 (SELECT user_id FROM users WHERE email = 'jane.smith@smartapp.com'),
 45);

-- Insert sample assignments
INSERT INTO assignments (title, description, subject_id, created_by, due_date) VALUES
('Algebra Homework 1', 'Practice problems on basic algebra',
 (SELECT subject_id FROM subjects WHERE name = 'Mathematics'),
 (SELECT user_id FROM users WHERE email = 'john.doe@smartapp.com'),
 DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY)),
('Physics Lab Report', 'Report on Newton''s laws experiment',
 (SELECT subject_id FROM subjects WHERE name = 'Physics'),
 (SELECT user_id FROM users WHERE email = 'jane.smith@smartapp.com'),
 DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY));

-- Insert sample quiz questions
INSERT INTO quiz_questions (quiz_id, question_text, question_type, options, correct_answer, points) VALUES
((SELECT quiz_id FROM quizzes WHERE title = 'Algebra Quiz 1'),
 'Solve for x: 2x + 5 = 13', 'multiple_choice',
 '["4", "5", "6", "7"]', '4', 2),
((SELECT quiz_id FROM quizzes WHERE title = 'Physics Quiz 1'),
 'What is Newton''s first law?', 'essay',
 NULL, 'An object at rest stays at rest, and an object in motion stays in motion...', 5);

-- Insert sample quiz submissions
INSERT INTO quiz_submissions (quiz_id, student_id, answers, score) VALUES
((SELECT quiz_id FROM quizzes WHERE title = 'Algebra Quiz 1'),
 (SELECT student_id FROM students WHERE nisn = '0051234567'),
 '{"1": "4"}', 100),
((SELECT quiz_id FROM quizzes WHERE title = 'Physics Quiz 1'),
 (SELECT student_id FROM students WHERE nisn = '0067891234'),
 '{"1": "An object continues its state of rest or motion unless acted upon by an external force"}', 90);

-- Insert sample assignment submissions
INSERT INTO assignment_submissions (assignment_id, student_id, file_path, file_name, grade, feedback, status) VALUES
((SELECT assignment_id FROM assignments WHERE title = 'Algebra Homework 1'),
 (SELECT student_id FROM students WHERE nisn = '0051234567'),
 '/uploads/assignments/student1_algebra.pdf', 'student1_algebra.pdf',
 95, 'Excellent work!', 'graded'),
((SELECT assignment_id FROM assignments WHERE title = 'Physics Lab Report'),
 (SELECT student_id FROM students WHERE nisn = '0067891234'),
 '/uploads/assignments/student2_physics.pdf', 'student2_physics.pdf',
 88, 'Good analysis, but could improve on data presentation', 'graded');