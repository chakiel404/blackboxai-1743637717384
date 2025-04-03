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

try {
    switch ($method) {
        case 'GET':
            if (isset($_GET['id'])) {
                getSubject($db, $_GET['id']);
            } else {
                getSubjects($db);
            }
            break;

        case 'POST':
            // Only admin can create subjects
            $user = checkRole(['admin']);
            createSubject($db);
            break;

        case 'PUT':
            $user = checkRole(['admin']);
            if (!isset($_GET['id'])) {
                sendError('Subject ID is required', 400);
            }
            updateSubject($db, $_GET['id']);
            break;

        case 'DELETE':
            $user = checkRole(['admin']);
            if (!isset($_GET['id'])) {
                sendError('Subject ID is required', 400);
            }
            deleteSubject($db, $_GET['id']);
            break;

        default:
            sendError('Method not allowed', 405);
    }
} catch (Exception $e) {
    logError('Subjects Error: ' . $e->getMessage());
    sendError('An error occurred while processing your request');
}

function getSubjects($db) {
    try {
        $query = "SELECT s.*, 
                    (SELECT COUNT(*) FROM materials WHERE subject_id = s.subject_id) as material_count,
                    (SELECT COUNT(*) FROM quizzes WHERE subject_id = s.subject_id) as quiz_count,
                    (SELECT COUNT(*) FROM assignments WHERE subject_id = s.subject_id) as assignment_count
                 FROM subjects s
                 ORDER BY s.name ASC";

        $subjects = $db->fetchAll($query);

        // Format the response
        $formattedSubjects = array_map(function($subject) {
            return [
                'id' => $subject['subject_id'],
                'name' => $subject['name'],
                'description' => $subject['description'],
                'stats' => [
                    'materials' => $subject['material_count'],
                    'quizzes' => $subject['quiz_count'],
                    'assignments' => $subject['assignment_count']
                ],
                'createdAt' => $subject['created_at'],
                'updatedAt' => $subject['updated_at']
            ];
        }, $subjects);

        sendResponse(['subjects' => $formattedSubjects]);
    } catch (Exception $e) {
        logError('Get Subjects Error: ' . $e->getMessage());
        sendError('Failed to fetch subjects');
    }
}

function getSubject($db, $id) {
    try {
        // Get subject details with related content counts
        $query = "SELECT s.*, 
                    (SELECT COUNT(*) FROM materials WHERE subject_id = s.subject_id) as material_count,
                    (SELECT COUNT(*) FROM quizzes WHERE subject_id = s.subject_id) as quiz_count,
                    (SELECT COUNT(*) FROM assignments WHERE subject_id = s.subject_id) as assignment_count
                 FROM subjects s
                 WHERE s.subject_id = ?";

        $subject = $db->fetch($query, [$id]);

        if (!$subject) {
            sendError('Subject not found', 404);
        }

        // Get recent materials
        $materialsQuery = "SELECT m.*, u.full_name as uploaded_by_name
                          FROM materials m
                          JOIN users u ON m.uploaded_by = u.user_id
                          WHERE m.subject_id = ?
                          ORDER BY m.created_at DESC
                          LIMIT 5";
        $materials = $db->fetchAll($materialsQuery, [$id]);

        // Get recent quizzes
        $quizzesQuery = "SELECT q.*, u.full_name as created_by_name
                        FROM quizzes q
                        JOIN users u ON q.created_by = u.user_id
                        WHERE q.subject_id = ?
                        ORDER BY q.created_at DESC
                        LIMIT 5";
        $quizzes = $db->fetchAll($quizzesQuery, [$id]);

        // Get recent assignments
        $assignmentsQuery = "SELECT a.*, u.full_name as created_by_name
                           FROM assignments a
                           JOIN users u ON a.created_by = u.user_id
                           WHERE a.subject_id = ?
                           ORDER BY a.created_at DESC
                           LIMIT 5";
        $assignments = $db->fetchAll($assignmentsQuery, [$id]);

        // Format the response
        $formattedSubject = [
            'id' => $subject['subject_id'],
            'name' => $subject['name'],
            'description' => $subject['description'],
            'stats' => [
                'materials' => $subject['material_count'],
                'quizzes' => $subject['quiz_count'],
                'assignments' => $subject['assignment_count']
            ],
            'recentContent' => [
                'materials' => array_map(function($material) {
                    return [
                        'id' => $material['material_id'],
                        'title' => $material['title'],
                        'uploadedBy' => $material['uploaded_by_name'],
                        'createdAt' => $material['created_at']
                    ];
                }, $materials),
                'quizzes' => array_map(function($quiz) {
                    return [
                        'id' => $quiz['quiz_id'],
                        'title' => $quiz['title'],
                        'createdBy' => $quiz['created_by_name'],
                        'createdAt' => $quiz['created_at']
                    ];
                }, $quizzes),
                'assignments' => array_map(function($assignment) {
                    return [
                        'id' => $assignment['assignment_id'],
                        'title' => $assignment['title'],
                        'createdBy' => $assignment['created_by_name'],
                        'dueDate' => $assignment['due_date']
                    ];
                }, $assignments)
            ],
            'createdAt' => $subject['created_at'],
            'updatedAt' => $subject['updated_at']
        ];

        sendResponse(['subject' => $formattedSubject]);
    } catch (Exception $e) {
        logError('Get Subject Error: ' . $e->getMessage());
        sendError('Failed to fetch subject');
    }
}

function createSubject($db) {
    try {
        $data = getJsonInput();

        // Validate required fields
        if (!isset($data['name'])) {
            sendError('Subject name is required', 400);
        }

        $name = sanitizeInput($data['name']);
        $description = isset($data['description']) ? sanitizeInput($data['description']) : '';

        // Check if subject with same name exists
        $existing = $db->fetch("SELECT subject_id FROM subjects WHERE name = ?", [$name]);
        if ($existing) {
            sendError('Subject with this name already exists', 409);
        }

        // Insert subject
        $subjectId = $db->insert('subjects', [
            'name' => $name,
            'description' => $description
        ]);

        // Fetch created subject
        $subject = $db->fetch(
            "SELECT * FROM subjects WHERE subject_id = ?",
            [$subjectId]
        );

        sendResponse([
            'message' => 'Subject created successfully',
            'subject' => [
                'id' => $subject['subject_id'],
                'name' => $subject['name'],
                'description' => $subject['description'],
                'createdAt' => $subject['created_at']
            ]
        ], 201);

    } catch (Exception $e) {
        logError('Create Subject Error: ' . $e->getMessage());
        sendError('Failed to create subject');
    }
}

function updateSubject($db, $id) {
    try {
        // Check if subject exists
        $subject = $db->fetch(
            "SELECT * FROM subjects WHERE subject_id = ?",
            [$id]
        );

        if (!$subject) {
            sendError('Subject not found', 404);
        }

        $data = getJsonInput();
        $updates = [];

        // Update name if provided
        if (isset($data['name'])) {
            $name = sanitizeInput($data['name']);
            // Check if another subject has this name
            $existing = $db->fetch(
                "SELECT subject_id FROM subjects WHERE name = ? AND subject_id != ?",
                [$name, $id]
            );
            if ($existing) {
                sendError('Another subject with this name already exists', 409);
            }
            $updates['name'] = $name;
        }

        // Update description if provided
        if (isset($data['description'])) {
            $updates['description'] = sanitizeInput($data['description']);
        }

        if (empty($updates)) {
            sendError('No updates provided', 400);
        }

        // Update subject
        $db->update('subjects', $updates, 'subject_id = ?', [$id]);

        // Fetch updated subject
        $updatedSubject = $db->fetch(
            "SELECT * FROM subjects WHERE subject_id = ?",
            [$id]
        );

        sendResponse([
            'message' => 'Subject updated successfully',
            'subject' => [
                'id' => $updatedSubject['subject_id'],
                'name' => $updatedSubject['name'],
                'description' => $updatedSubject['description'],
                'updatedAt' => $updatedSubject['updated_at']
            ]
        ]);

    } catch (Exception $e) {
        logError('Update Subject Error: ' . $e->getMessage());
        sendError('Failed to update subject');
    }
}

function deleteSubject($db, $id) {
    try {
        // Check if subject exists
        $subject = $db->fetch(
            "SELECT * FROM subjects WHERE subject_id = ?",
            [$id]
        );

        if (!$subject) {
            sendError('Subject not found', 404);
        }

        // Check if subject has any related content
        $contentCounts = $db->fetch(
            "SELECT 
                (SELECT COUNT(*) FROM materials WHERE subject_id = ?) as material_count,
                (SELECT COUNT(*) FROM quizzes WHERE subject_id = ?) as quiz_count,
                (SELECT COUNT(*) FROM assignments WHERE subject_id = ?) as assignment_count",
            [$id, $id, $id]
        );

        if ($contentCounts['material_count'] > 0 || 
            $contentCounts['quiz_count'] > 0 || 
            $contentCounts['assignment_count'] > 0) {
            sendError('Cannot delete subject with existing content. Remove all related materials, quizzes, and assignments first.', 400);
        }

        // Delete subject
        $db->delete('subjects', 'subject_id = ?', [$id]);

        sendResponse(['message' => 'Subject deleted successfully']);

    } catch (Exception $e) {
        logError('Delete Subject Error: ' . $e->getMessage());
        sendError('Failed to delete subject');
    }
}