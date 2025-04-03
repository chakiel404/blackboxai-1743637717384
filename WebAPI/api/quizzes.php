<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../core/DB.php';
require_once __DIR__ . '/../core/helpers.php';

// Handle CORS preflight request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$db = DB::getInstance();
$method = $_SERVER['REQUEST_METHOD'];
$action = isset($_GET['action']) ? $_GET['action'] : '';

try {
    switch ($method) {
        case 'GET':
            if (isset($_GET['id'])) {
                if ($action === 'submissions') {
                    // Only teachers can view submissions
                    $user = checkRole(['admin', 'guru']);
                    getQuizSubmissions($db, $_GET['id'], $user);
                } else {
                    getQuiz($db, $_GET['id']);
                }
            } else {
                getQuizzes($db);
            }
            break;

        case 'POST':
            if ($action === 'submit' && isset($_GET['id'])) {
                // Students submit quiz answers
                $user = checkRole(['siswa']);
                submitQuiz($db, $_GET['id'], $user);
            } else {
                // Only teachers can create quizzes
                $user = checkRole(['admin', 'guru']);
                createQuiz($db, $user);
            }
            break;

        case 'PUT':
            $user = checkRole(['admin', 'guru']);
            if (!isset($_GET['id'])) {
                sendError('Quiz ID is required', 400);
            }
            updateQuiz($db, $_GET['id'], $user);
            break;

        case 'DELETE':
            $user = checkRole(['admin', 'guru']);
            if (!isset($_GET['id'])) {
                sendError('Quiz ID is required', 400);
            }
            deleteQuiz($db, $_GET['id'], $user);
            break;

        default:
            sendError('Method not allowed', 405);
    }
} catch (Exception $e) {
    logError('Quizzes Error: ' . $e->getMessage());
    sendError('An error occurred while processing your request');
}

function getQuizzes($db) {
    try {
        $query = "SELECT q.*, s.name as subject_name, u.full_name as created_by_name,
                        (SELECT COUNT(*) FROM quiz_questions WHERE quiz_id = q.quiz_id) as question_count
                 FROM quizzes q
                 JOIN subjects s ON q.subject_id = s.subject_id
                 JOIN users u ON q.created_by = u.user_id
                 ORDER BY q.created_at DESC";

        $quizzes = $db->fetchAll($query);

        // Format the response
        $formattedQuizzes = array_map(function($quiz) {
            return [
                'id' => $quiz['quiz_id'],
                'title' => $quiz['title'],
                'description' => $quiz['description'],
                'subject' => [
                    'id' => $quiz['subject_id'],
                    'name' => $quiz['subject_name']
                ],
                'createdBy' => [
                    'id' => $quiz['created_by'],
                    'name' => $quiz['created_by_name']
                ],
                'questionCount' => $quiz['question_count'],
                'durationMinutes' => $quiz['duration_minutes'],
                'isActive' => (bool)$quiz['is_active'],
                'createdAt' => $quiz['created_at']
            ];
        }, $quizzes);

        sendResponse(['quizzes' => $formattedQuizzes]);
    } catch (Exception $e) {
        logError('Get Quizzes Error: ' . $e->getMessage());
        sendError('Failed to fetch quizzes');
    }
}

function getQuiz($db, $id) {
    try {
        // Get quiz details
        $query = "SELECT q.*, s.name as subject_name, u.full_name as created_by_name
                 FROM quizzes q
                 JOIN subjects s ON q.subject_id = s.subject_id
                 JOIN users u ON q.created_by = u.user_id
                 WHERE q.quiz_id = ?";

        $quiz = $db->fetch($query, [$id]);

        if (!$quiz) {
            sendError('Quiz not found', 404);
        }

        // Get quiz questions
        $questionsQuery = "SELECT * FROM quiz_questions WHERE quiz_id = ?";
        $questions = $db->fetchAll($questionsQuery, [$id]);

        // Format questions
        $formattedQuestions = array_map(function($question) {
            return [
                'id' => $question['question_id'],
                'text' => $question['question_text'],
                'type' => $question['question_type'],
                'options' => json_decode($question['options'], true),
                'points' => $question['points']
            ];
        }, $questions);

        // Format the response
        $formattedQuiz = [
            'id' => $quiz['quiz_id'],
            'title' => $quiz['title'],
            'description' => $quiz['description'],
            'subject' => [
                'id' => $quiz['subject_id'],
                'name' => $quiz['subject_name']
            ],
            'createdBy' => [
                'id' => $quiz['created_by'],
                'name' => $quiz['created_by_name']
            ],
            'questions' => $formattedQuestions,
            'durationMinutes' => $quiz['duration_minutes'],
            'isActive' => (bool)$quiz['is_active'],
            'createdAt' => $quiz['created_at']
        ];

        sendResponse(['quiz' => $formattedQuiz]);
    } catch (Exception $e) {
        logError('Get Quiz Error: ' . $e->getMessage());
        sendError('Failed to fetch quiz');
    }
}

function createQuiz($db, $user) {
    try {
        $data = getJsonInput();

        // Validate required fields
        if (!isset($data['title']) || !isset($data['subject_id']) || !isset($data['questions'])) {
            sendError('Title, subject, and questions are required', 400);
        }

        // Validate subject exists
        $subject = $db->fetch("SELECT subject_id FROM subjects WHERE subject_id = ?", [$data['subject_id']]);
        if (!$subject) {
            sendError('Invalid subject', 400);
        }

        // Begin transaction
        $db->beginTransaction();

        // Insert quiz
        $quizId = $db->insert('quizzes', [
            'title' => sanitizeInput($data['title']),
            'description' => isset($data['description']) ? sanitizeInput($data['description']) : '',
            'subject_id' => $data['subject_id'],
            'created_by' => $user->userId,
            'duration_minutes' => isset($data['duration_minutes']) ? $data['duration_minutes'] : 60,
            'is_active' => isset($data['is_active']) ? $data['is_active'] : true
        ]);

        // Insert questions
        foreach ($data['questions'] as $question) {
            if (!isset($question['text']) || !isset($question['type'])) {
                throw new Exception('Invalid question format');
            }

            $db->insert('quiz_questions', [
                'quiz_id' => $quizId,
                'question_text' => sanitizeInput($question['text']),
                'question_type' => $question['type'],
                'options' => isset($question['options']) ? json_encode($question['options']) : null,
                'correct_answer' => sanitizeInput($question['correct_answer']),
                'points' => isset($question['points']) ? $question['points'] : 1
            ]);
        }

        // Commit transaction
        $db->commit();

        // Fetch the created quiz
        $query = "SELECT q.*, s.name as subject_name, u.full_name as created_by_name
                 FROM quizzes q
                 JOIN subjects s ON q.subject_id = s.subject_id
                 JOIN users u ON q.created_by = u.user_id
                 WHERE q.quiz_id = ?";

        $quiz = $db->fetch($query, [$quizId]);

        sendResponse([
            'message' => 'Quiz created successfully',
            'quiz' => [
                'id' => $quiz['quiz_id'],
                'title' => $quiz['title'],
                'description' => $quiz['description'],
                'subject' => [
                    'id' => $quiz['subject_id'],
                    'name' => $quiz['subject_name']
                ],
                'createdBy' => [
                    'id' => $quiz['created_by'],
                    'name' => $quiz['created_by_name']
                ],
                'durationMinutes' => $quiz['duration_minutes'],
                'isActive' => (bool)$quiz['is_active'],
                'createdAt' => $quiz['created_at']
            ]
        ], 201);

    } catch (Exception $e) {
        $db->rollBack();
        logError('Create Quiz Error: ' . $e->getMessage());
        sendError('Failed to create quiz');
    }
}

function submitQuiz($db, $quizId, $user) {
    try {
        $data = getJsonInput();

        // Validate quiz exists and is active
        $quiz = $db->fetch(
            "SELECT * FROM quizzes WHERE quiz_id = ? AND is_active = 1",
            [$quizId]
        );

        if (!$quiz) {
            sendError('Quiz not found or inactive', 404);
        }

        // Check if student has already submitted this quiz
        $existingSubmission = $db->fetch(
            "SELECT * FROM quiz_submissions WHERE quiz_id = ? AND student_id = ? AND status = 'completed'",
            [$quizId, $user->userId]
        );

        if ($existingSubmission) {
            sendError('Quiz already submitted', 400);
        }

        // Get quiz questions
        $questions = $db->fetchAll(
            "SELECT * FROM quiz_questions WHERE quiz_id = ?",
            [$quizId]
        );

        // Calculate score
        $totalPoints = 0;
        $earnedPoints = 0;
        $answers = [];

        foreach ($questions as $question) {
            $totalPoints += $question['points'];
            $studentAnswer = isset($data['answers'][$question['question_id']]) 
                ? $data['answers'][$question['question_id']] 
                : null;

            $answers[$question['question_id']] = [
                'student_answer' => $studentAnswer,
                'correct_answer' => $question['correct_answer'],
                'points' => $question['points']
            ];

            if ($question['question_type'] === 'essay') {
                // Essay questions need manual grading
                continue;
            }

            if ($studentAnswer === $question['correct_answer']) {
                $earnedPoints += $question['points'];
            }
        }

        $score = ($totalPoints > 0) ? ($earnedPoints / $totalPoints) * 100 : 0;

        // Record submission
        $submissionId = $db->insert('quiz_submissions', [
            'quiz_id' => $quizId,
            'student_id' => $user->userId,
            'start_time' => date('Y-m-d H:i:s', strtotime($data['start_time'])),
            'end_time' => date('Y-m-d H:i:s'),
            'score' => $score,
            'answers' => json_encode($answers),
            'status' => 'completed'
        ]);

        sendResponse([
            'message' => 'Quiz submitted successfully',
            'submission' => [
                'id' => $submissionId,
                'score' => $score,
                'totalPoints' => $totalPoints,
                'earnedPoints' => $earnedPoints,
                'submittedAt' => date('Y-m-d H:i:s')
            ]
        ]);

    } catch (Exception $e) {
        logError('Submit Quiz Error: ' . $e->getMessage());
        sendError('Failed to submit quiz');
    }
}

function getQuizSubmissions($db, $quizId, $user) {
    try {
        // Verify quiz exists and user has permission
        $quiz = $db->fetch(
            "SELECT * FROM quizzes WHERE quiz_id = ?",
            [$quizId]
        );

        if (!$quiz) {
            sendError('Quiz not found', 404);
        }

        if ($user->role !== 'admin' && $quiz['created_by'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        // Get submissions with student details
        $query = "SELECT qs.*, u.full_name as student_name
                 FROM quiz_submissions qs
                 JOIN users u ON qs.student_id = u.user_id
                 WHERE qs.quiz_id = ?
                 ORDER BY qs.created_at DESC";

        $submissions = $db->fetchAll($query, [$quizId]);

        // Format submissions
        $formattedSubmissions = array_map(function($submission) {
            return [
                'id' => $submission['submission_id'],
                'student' => [
                    'id' => $submission['student_id'],
                    'name' => $submission['student_name']
                ],
                'score' => $submission['score'],
                'startTime' => $submission['start_time'],
                'endTime' => $submission['end_time'],
                'status' => $submission['status'],
                'answers' => json_decode($submission['answers'], true),
                'submittedAt' => $submission['created_at']
            ];
        }, $submissions);

        sendResponse(['submissions' => $formattedSubmissions]);

    } catch (Exception $e) {
        logError('Get Quiz Submissions Error: ' . $e->getMessage());
        sendError('Failed to fetch quiz submissions');
    }
}

function updateQuiz($db, $id, $user) {
    try {
        // Check if quiz exists and user has permission
        $quiz = $db->fetch(
            "SELECT * FROM quizzes WHERE quiz_id = ?",
            [$id]
        );

        if (!$quiz) {
            sendError('Quiz not found', 404);
        }

        if ($user->role !== 'admin' && $quiz['created_by'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        $data = getJsonInput();
        $updates = [];

        // Update basic info
        if (isset($data['title'])) {
            $updates['title'] = sanitizeInput($data['title']);
        }
        if (isset($data['description'])) {
            $updates['description'] = sanitizeInput($data['description']);
        }
        if (isset($data['subject_id'])) {
            $subject = $db->fetch("SELECT subject_id FROM subjects WHERE subject_id = ?", [$data['subject_id']]);
            if (!$subject) {
                sendError('Invalid subject', 400);
            }
            $updates['subject_id'] = $data['subject_id'];
        }
        if (isset($data['duration_minutes'])) {
            $updates['duration_minutes'] = $data['duration_minutes'];
        }
        if (isset($data['is_active'])) {
            $updates['is_active'] = $data['is_active'];
        }

        // Begin transaction
        $db->beginTransaction();

        // Update quiz
        if (!empty($updates)) {
            $db->update('quizzes', $updates, 'quiz_id = ?', [$id]);
        }

        // Update questions if provided
        if (isset($data['questions'])) {
            // Delete existing questions
            $db->delete('quiz_questions', 'quiz_id = ?', [$id]);

            // Insert new questions
            foreach ($data['questions'] as $question) {
                $db->insert('quiz_questions', [
                    'quiz_id' => $id,
                    'question_text' => sanitizeInput($question['text']),
                    'question_type' => $question['type'],
                    'options' => isset($question['options']) ? json_encode($question['options']) : null,
                    'correct_answer' => sanitizeInput($question['correct_answer']),
                    'points' => isset($question['points']) ? $question['points'] : 1
                ]);
            }
        }

        // Commit transaction
        $db->commit();

        sendResponse(['message' => 'Quiz updated successfully']);

    } catch (Exception $e) {
        $db->rollBack();
        logError('Update Quiz Error: ' . $e->getMessage());
        sendError('Failed to update quiz');
    }
}

function deleteQuiz($db, $id, $user) {
    try {
        // Check if quiz exists and user has permission
        $quiz = $db->fetch(
            "SELECT * FROM quizzes WHERE quiz_id = ?",
            [$id]
        );

        if (!$quiz) {
            sendError('Quiz not found', 404);
        }

        if ($user->role !== 'admin' && $quiz['created_by'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        // Begin transaction
        $db->beginTransaction();

        // Delete questions and submissions (cascade delete will handle this)
        $db->delete('quizzes', 'quiz_id = ?', [$id]);

        // Commit transaction
        $db->commit();

        sendResponse(['message' => 'Quiz deleted successfully']);

    } catch (Exception $e) {
        $db->rollBack();
        logError('Delete Quiz Error: ' . $e->getMessage());
        sendError('Failed to delete quiz');
    }
}