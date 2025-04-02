<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SmartApp API Documentation</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .endpoint {
            border-left: 4px solid #007bff;
            margin-bottom: 20px;
            padding-left: 15px;
        }
        .method {
            font-weight: bold;
            padding: 3px 8px;
            border-radius: 4px;
            margin-right: 10px;
        }
        .get { background-color: #61affe; color: white; }
        .post { background-color: #49cc90; color: white; }
        .put { background-color: #fca130; color: white; }
        .delete { background-color: #f93e3e; color: white; }
        pre {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 4px;
        }
        .nav-link.active {
            background-color: #007bff !important;
            color: white !important;
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container">
            <a class="navbar-brand" href="#">SmartApp API</a>
        </div>
    </nav>

    <div class="container my-5">
        <div class="row">
            <div class="col-md-3">
                <div class="list-group" id="list-tab" role="tablist">
                    <a class="list-group-item list-group-item-action active" href="#authentication">Authentication</a>
                    <a class="list-group-item list-group-item-action" href="#subjects">Subjects</a>
                    <a class="list-group-item list-group-item-action" href="#materials">Materials</a>
                    <a class="list-group-item list-group-item-action" href="#quizzes">Quizzes</a>
                    <a class="list-group-item list-group-item-action" href="#assignments">Assignments</a>
                </div>
            </div>

            <div class="col-md-9">
                <h1>API Documentation</h1>
                <p class="lead">Welcome to the SmartApp API documentation. This API provides endpoints for managing educational content including subjects, materials, quizzes, and assignments.</p>

                <div class="alert alert-info">
                    <strong>Base URL:</strong> <code><?php echo $_SERVER['HTTP_HOST']; ?>/api</code>
                </div>

                <section id="authentication" class="mb-5">
                    <h2>Authentication</h2>
                    <p>All API requests require authentication using JWT tokens. Include the token in the Authorization header:</p>
                    <pre>Authorization: Bearer your-token-here</pre>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/auth/login</h4>
                        <p>Authenticate user and get access token.</p>
                        <strong>Request Body:</strong>
                        <pre>{
    "email": "user@example.com",
    "password": "password123"
}</pre>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/auth/register</h4>
                        <p>Register a new user account.</p>
                        <strong>Request Body:</strong>
                        <pre>{
    "full_name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "siswa|guru|admin"
}</pre>
                    </div>
                </section>

                <section id="subjects" class="mb-5">
                    <h2>Subjects</h2>
                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/subjects</h4>
                        <p>Get list of all subjects.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/subjects/{id}</h4>
                        <p>Get detailed information about a specific subject.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/subjects</h4>
                        <p>Create a new subject (Admin only).</p>
                        <strong>Request Body:</strong>
                        <pre>{
    "name": "Mathematics",
    "description": "Advanced mathematics course"
}</pre>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method put">PUT</span>/subjects/{id}</h4>
                        <p>Update an existing subject (Admin only).</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method delete">DELETE</span>/subjects/{id}</h4>
                        <p>Delete a subject (Admin only).</p>
                    </div>
                </section>

                <section id="materials" class="mb-5">
                    <h2>Materials</h2>
                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/materials</h4>
                        <p>Get list of all learning materials.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/materials/{id}</h4>
                        <p>Get detailed information about a specific material.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/materials</h4>
                        <p>Upload a new learning material (Teacher/Admin only).</p>
                        <strong>Request Body (multipart/form-data):</strong>
                        <pre>title: "Chapter 1: Introduction"
description: "Introduction to the course"
subject_id: 1
file: [FILE]</pre>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method delete">DELETE</span>/materials/{id}</h4>
                        <p>Delete a learning material (Teacher/Admin only).</p>
                    </div>
                </section>

                <section id="quizzes" class="mb-5">
                    <h2>Quizzes</h2>
                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/quizzes</h4>
                        <p>Get list of all quizzes.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/quizzes/{id}</h4>
                        <p>Get detailed information about a specific quiz.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/quizzes</h4>
                        <p>Create a new quiz (Teacher/Admin only).</p>
                        <strong>Request Body:</strong>
                        <pre>{
    "title": "Chapter 1 Quiz",
    "description": "Test your knowledge",
    "subject_id": 1,
    "duration_minutes": 60,
    "questions": [
        {
            "text": "What is 2+2?",
            "type": "multiple_choice",
            "options": ["3", "4", "5", "6"],
            "correct_answer": "4",
            "points": 1
        }
    ]
}</pre>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/quizzes/{id}/submit</h4>
                        <p>Submit answers for a quiz (Student only).</p>
                        <strong>Request Body:</strong>
                        <pre>{
    "answers": {
        "1": "4",
        "2": "True"
    },
    "start_time": "2023-01-01 10:00:00"
}</pre>
                    </div>
                </section>

                <section id="assignments" class="mb-5">
                    <h2>Assignments</h2>
                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/assignments</h4>
                        <p>Get list of all assignments.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method get">GET</span>/assignments/{id}</h4>
                        <p>Get detailed information about a specific assignment.</p>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/assignments</h4>
                        <p>Create a new assignment (Teacher/Admin only).</p>
                        <strong>Request Body (multipart/form-data):</strong>
                        <pre>title: "Assignment 1"
description: "Complete the exercises"
subject_id: 1
due_date: "2023-12-31 23:59:59"
file: [FILE]</pre>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method post">POST</span>/assignments/{id}/submit</h4>
                        <p>Submit work for an assignment (Student only).</p>
                        <strong>Request Body (multipart/form-data):</strong>
                        <pre>file: [FILE]</pre>
                    </div>

                    <div class="endpoint">
                        <h4><span class="method put">PUT</span>/assignments/{id}/grade</h4>
                        <p>Grade a submitted assignment (Teacher/Admin only).</p>
                        <strong>Request Body:</strong>
                        <pre>{
    "grade": 85,
    "feedback": "Good work!"
}</pre>
                    </div>
                </section>
            </div>
        </div>
    </div>

    <footer class="bg-light py-3 mt-5">
        <div class="container text-center">
            <p class="mb-0">SmartApp API Documentation &copy; <?php echo date('Y'); ?></p>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Smooth scroll to sections
        document.querySelectorAll('.list-group-item').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const section = document.querySelector(this.getAttribute('href'));
                section.scrollIntoView({ behavior: 'smooth' });
                
                // Update active state
                document.querySelectorAll('.list-group-item').forEach(item => {
                    item.classList.remove('active');
                });
                this.classList.add('active');
            });
        });

        // Update active menu item on scroll
        window.addEventListener('scroll', function() {
            const sections = document.querySelectorAll('section');
            let current = '';

            sections.forEach(section => {
                const sectionTop = section.offsetTop;
                if (pageYOffset >= sectionTop - 60) {
                    current = section.getAttribute('id');
                }
            });

            document.querySelectorAll('.list-group-item').forEach(item => {
                item.classList.remove('active');
                if (item.getAttribute('href').substring(1) === current) {
                    item.classList.add('active');
                }
            });
        });
    </script>
</body>
</html>