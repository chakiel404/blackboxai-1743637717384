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
                    getAssignmentSubmissions($db, $_GET['id'], $user);
                } else {
                    getAssignment($db, $_GET['id']);
                }
            } else {
                getAssignments($db);
            }
            break;

        case 'POST':
            if ($action === 'submit' && isset($_GET['id'])) {
                // Students submit assignments
                $user = checkRole(['siswa']);
                submitAssignment($db, $_GET['id'], $user);
            } else {
                // Only teachers can create assignments
                $user = checkRole(['admin', 'guru']);
                createAssignment($db, $user);
            }
            break;

        case 'PUT':
            if ($action === 'grade' && isset($_GET['id'])) {
                // Teachers grade submissions
                $user = checkRole(['admin', 'guru']);
                gradeSubmission($db, $_GET['id'], $user);
            } else {
                // Update assignment
                $user = checkRole(['admin', 'guru']);
                if (!isset($_GET['id'])) {
                    sendError('Assignment ID is required', 400);
                }
                updateAssignment($db, $_GET['id'], $user);
            }
            break;

        case 'DELETE':
            $user = checkRole(['admin', 'guru']);
            if (!isset($_GET['id'])) {
                sendError('Assignment ID is required', 400);
            }
            deleteAssignment($db, $_GET['id'], $user);
            break;

        default:
            sendError('Method not allowed', 405);
    }
} catch (Exception $e) {
    logError('Assignments Error: ' . $e->getMessage());
    sendError('An error occurred while processing your request');
}

function getAssignments($db) {
    try {
        $query = "SELECT a.*, s.name as subject_name, u.full_name as created_by_name,
                        (SELECT COUNT(*) FROM assignment_submissions WHERE assignment_id = a.assignment_id) as submission_count
                 FROM assignments a
                 JOIN subjects s ON a.subject_id = s.subject_id
                 JOIN users u ON a.created_by = u.user_id
                 ORDER BY a.due_date ASC";

        $assignments = $db->fetchAll($query);

        // Format the response
        $formattedAssignments = array_map(function($assignment) {
            return [
                'id' => $assignment['assignment_id'],
                'title' => $assignment['title'],
                'description' => $assignment['description'],
                'subject' => [
                    'id' => $assignment['subject_id'],
                    'name' => $assignment['subject_name']
                ],
                'createdBy' => [
                    'id' => $assignment['created_by'],
                    'name' => $assignment['created_by_name']
                ],
                'dueDate' => $assignment['due_date'],
                'submissionCount' => $assignment['submission_count'],
                'fileName' => $assignment['file_name'],
                'fileSize' => $assignment['file_size'],
                'mimeType' => $assignment['mime_type'],
                'createdAt' => $assignment['created_at']
            ];
        }, $assignments);

        sendResponse(['assignments' => $formattedAssignments]);
    } catch (Exception $e) {
        logError('Get Assignments Error: ' . $e->getMessage());
        sendError('Failed to fetch assignments');
    }
}

function getAssignment($db, $id) {
    try {
        $query = "SELECT a.*, s.name as subject_name, u.full_name as created_by_name
                 FROM assignments a
                 JOIN subjects s ON a.subject_id = s.subject_id
                 JOIN users u ON a.created_by = u.user_id
                 WHERE a.assignment_id = ?";

        $assignment = $db->fetch($query, [$id]);

        if (!$assignment) {
            sendError('Assignment not found', 404);
        }

        // Format the response
        $formattedAssignment = [
            'id' => $assignment['assignment_id'],
            'title' => $assignment['title'],
            'description' => $assignment['description'],
            'subject' => [
                'id' => $assignment['subject_id'],
                'name' => $assignment['subject_name']
            ],
            'createdBy' => [
                'id' => $assignment['created_by'],
                'name' => $assignment['created_by_name']
            ],
            'dueDate' => $assignment['due_date'],
            'fileName' => $assignment['file_name'],
            'fileSize' => $assignment['file_size'],
            'mimeType' => $assignment['mime_type'],
            'createdAt' => $assignment['created_at']
        ];

        sendResponse(['assignment' => $formattedAssignment]);
    } catch (Exception $e) {
        logError('Get Assignment Error: ' . $e->getMessage());
        sendError('Failed to fetch assignment');
    }
}

function createAssignment($db, $user) {
    try {
        // Validate required fields
        if (!isset($_POST['title']) || !isset($_POST['subject_id']) || !isset($_POST['due_date'])) {
            sendError('Title, subject, and due date are required', 400);
        }

        // Validate subject exists
        $subject = $db->fetch("SELECT subject_id FROM subjects WHERE subject_id = ?", [$_POST['subject_id']]);
        if (!$subject) {
            sendError('Invalid subject', 400);
        }

        // Handle file upload if present
        $filePath = null;
        $fileName = null;
        $fileSize = null;
        $mimeType = null;

        if (isset($_FILES['file'])) {
            $uploadResult = handleFileUpload($_FILES['file'], ASSIGNMENTS_DIR);
            if (!$uploadResult['success']) {
                sendError($uploadResult['message'], 400);
            }
            $filePath = $uploadResult['filePath'];
            $fileName = $uploadResult['fileName'];
            $fileSize = $uploadResult['fileSize'];
            $mimeType = $uploadResult['mimeType'];
        }

        // Insert assignment
        $assignmentId = $db->insert('assignments', [
            'title' => sanitizeInput($_POST['title']),
            'description' => isset($_POST['description']) ? sanitizeInput($_POST['description']) : '',
            'subject_id' => $_POST['subject_id'],
            'created_by' => $user->userId,
            'due_date' => $_POST['due_date'],
            'file_path' => $filePath,
            'file_name' => $fileName,
            'file_size' => $fileSize,
            'mime_type' => $mimeType
        ]);

        // Fetch created assignment
        $query = "SELECT a.*, s.name as subject_name, u.full_name as created_by_name
                 FROM assignments a
                 JOIN subjects s ON a.subject_id = s.subject_id
                 JOIN users u ON a.created_by = u.user_id
                 WHERE a.assignment_id = ?";

        $assignment = $db->fetch($query, [$assignmentId]);

        sendResponse([
            'message' => 'Assignment created successfully',
            'assignment' => [
                'id' => $assignment['assignment_id'],
                'title' => $assignment['title'],
                'description' => $assignment['description'],
                'subject' => [
                    'id' => $assignment['subject_id'],
                    'name' => $assignment['subject_name']
                ],
                'createdBy' => [
                    'id' => $assignment['created_by'],
                    'name' => $assignment['created_by_name']
                ],
                'dueDate' => $assignment['due_date'],
                'fileName' => $assignment['file_name'],
                'createdAt' => $assignment['created_at']
            ]
        ], 201);

    } catch (Exception $e) {
        logError('Create Assignment Error: ' . $e->getMessage());
        sendError('Failed to create assignment');
    }
}

function submitAssignment($db, $assignmentId, $user) {
    try {
        // Check if assignment exists and is not past due
        $assignment = $db->fetch(
            "SELECT * FROM assignments WHERE assignment_id = ? AND due_date > NOW()",
            [$assignmentId]
        );

        if (!$assignment) {
            sendError('Assignment not found or past due date', 404);
        }

        // Validate file upload
        if (!isset($_FILES['file'])) {
            sendError('No file uploaded', 400);
        }

        // Handle file upload
        $uploadResult = handleFileUpload($_FILES['file'], ASSIGNMENTS_DIR . '/submissions');
        if (!$uploadResult['success']) {
            sendError($uploadResult['message'], 400);
        }

        // Check for existing submission
        $existingSubmission = $db->fetch(
            "SELECT * FROM assignment_submissions WHERE assignment_id = ? AND student_id = ?",
            [$assignmentId, $user->userId]
        );

        if ($existingSubmission) {
            // Update existing submission
            $db->update('assignment_submissions', [
                'file_path' => $uploadResult['filePath'],
                'file_name' => $uploadResult['fileName'],
                'file_size' => $uploadResult['fileSize'],
                'mime_type' => $uploadResult['mimeType'],
                'submission_date' => date('Y-m-d H:i:s'),
                'status' => 'submitted'
            ], 'submission_id = ?', [$existingSubmission['submission_id']]);

            $submissionId = $existingSubmission['submission_id'];
        } else {
            // Create new submission
            $submissionId = $db->insert('assignment_submissions', [
                'assignment_id' => $assignmentId,
                'student_id' => $user->userId,
                'file_path' => $uploadResult['filePath'],
                'file_name' => $uploadResult['fileName'],
                'file_size' => $uploadResult['fileSize'],
                'mime_type' => $uploadResult['mimeType'],
                'submission_date' => date('Y-m-d H:i:s'),
                'status' => 'submitted'
            ]);
        }

        sendResponse([
            'message' => 'Assignment submitted successfully',
            'submission' => [
                'id' => $submissionId,
                'fileName' => $uploadResult['fileName'],
                'submittedAt' => date('Y-m-d H:i:s')
            ]
        ]);

    } catch (Exception $e) {
        logError('Submit Assignment Error: ' . $e->getMessage());
        sendError('Failed to submit assignment');
    }
}

function gradeSubmission($db, $submissionId, $user) {
    try {
        $data = getJsonInput();

        // Validate required fields
        if (!isset($data['grade']) || !isset($data['feedback'])) {
            sendError('Grade and feedback are required', 400);
        }

        // Validate grade
        $grade = filter_var($data['grade'], FILTER_VALIDATE_INT);
        if ($grade === false || $grade < 0 || $grade > 100) {
            sendError('Grade must be between 0 and 100', 400);
        }

        // Get submission and check permissions
        $query = "SELECT s.*, a.created_by as teacher_id 
                 FROM assignment_submissions s
                 JOIN assignments a ON s.assignment_id = a.assignment_id
                 WHERE s.submission_id = ?";

        $submission = $db->fetch($query, [$submissionId]);

        if (!$submission) {
            sendError('Submission not found', 404);
        }

        if ($user->role !== 'admin' && $submission['teacher_id'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        // Update submission
        $db->update('assignment_submissions', [
            'grade' => $grade,
            'feedback' => sanitizeInput($data['feedback']),
            'status' => 'graded'
        ], 'submission_id = ?', [$submissionId]);

        sendResponse([
            'message' => 'Submission graded successfully',
            'grade' => [
                'submissionId' => $submissionId,
                'grade' => $grade,
                'feedback' => $data['feedback'],
                'gradedAt' => date('Y-m-d H:i:s')
            ]
        ]);

    } catch (Exception $e) {
        logError('Grade Submission Error: ' . $e->getMessage());
        sendError('Failed to grade submission');
    }
}

function getAssignmentSubmissions($db, $assignmentId, $user) {
    try {
        // Verify assignment exists and user has permission
        $assignment = $db->fetch(
            "SELECT * FROM assignments WHERE assignment_id = ?",
            [$assignmentId]
        );

        if (!$assignment) {
            sendError('Assignment not found', 404);
        }

        if ($user->role !== 'admin' && $assignment['created_by'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        // Get submissions with student details
        $query = "SELECT s.*, u.full_name as student_name
                 FROM assignment_submissions s
                 JOIN users u ON s.student_id = u.user_id
                 WHERE s.assignment_id = ?
                 ORDER BY s.submission_date DESC";

        $submissions = $db->fetchAll($query, [$assignmentId]);

        // Format submissions
        $formattedSubmissions = array_map(function($submission) {
            return [
                'id' => $submission['submission_id'],
                'student' => [
                    'id' => $submission['student_id'],
                    'name' => $submission['student_name']
                ],
                'fileName' => $submission['file_name'],
                'fileSize' => $submission['file_size'],
                'submissionDate' => $submission['submission_date'],
                'status' => $submission['status'],
                'grade' => $submission['grade'],
                'feedback' => $submission['feedback']
            ];
        }, $submissions);

        sendResponse(['submissions' => $formattedSubmissions]);

    } catch (Exception $e) {
        logError('Get Assignment Submissions Error: ' . $e->getMessage());
        sendError('Failed to fetch submissions');
    }
}

function updateAssignment($db, $id, $user) {
    try {
        // Check if assignment exists and user has permission
        $assignment = $db->fetch(
            "SELECT * FROM assignments WHERE assignment_id = ?",
            [$id]
        );

        if (!$assignment) {
            sendError('Assignment not found', 404);
        }

        if ($user->role !== 'admin' && $assignment['created_by'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        $updates = [];

        // Update basic info from POST data
        if (isset($_POST['title'])) {
            $updates['title'] = sanitizeInput($_POST['title']);
        }
        if (isset($_POST['description'])) {
            $updates['description'] = sanitizeInput($_POST['description']);
        }
        if (isset($_POST['subject_id'])) {
            $subject = $db->fetch("SELECT subject_id FROM subjects WHERE subject_id = ?", [$_POST['subject_id']]);
            if (!$subject) {
                sendError('Invalid subject', 400);
            }
            $updates['subject_id'] = $_POST['subject_id'];
        }
        if (isset($_POST['due_date'])) {
            $updates['due_date'] = $_POST['due_date'];
        }

        // Handle file update if present
        if (isset($_FILES['file'])) {
            $uploadResult = handleFileUpload($_FILES['file'], ASSIGNMENTS_DIR);
            if (!$uploadResult['success']) {
                sendError($uploadResult['message'], 400);
            }

            // Delete old file if exists
            if ($assignment['file_path'] && file_exists($assignment['file_path'])) {
                unlink($assignment['file_path']);
            }

            $updates['file_path'] = $uploadResult['filePath'];
            $updates['file_name'] = $uploadResult['fileName'];
            $updates['file_size'] = $uploadResult['fileSize'];
            $updates['mime_type'] = $uploadResult['mimeType'];
        }

        if (empty($updates)) {
            sendError('No updates provided', 400);
        }

        // Update assignment
        $db->update('assignments', $updates, 'assignment_id = ?', [$id]);

        sendResponse(['message' => 'Assignment updated successfully']);

    } catch (Exception $e) {
        logError('Update Assignment Error: ' . $e->getMessage());
        sendError('Failed to update assignment');
    }
}

function deleteAssignment($db, $id, $user) {
    try {
        // Check if assignment exists and user has permission
        $assignment = $db->fetch(
            "SELECT * FROM assignments WHERE assignment_id = ?",
            [$id]
        );

        if (!$assignment) {
            sendError('Assignment not found', 404);
        }

        if ($user->role !== 'admin' && $assignment['created_by'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        // Begin transaction
        $db->beginTransaction();

        // Get all submission files
        $submissions = $db->fetchAll(
            "SELECT file_path FROM assignment_submissions WHERE assignment_id = ?",
            [$id]
        );

        // Delete submission files
        foreach ($submissions as $submission) {
            if ($submission['file_path'] && file_exists($submission['file_path'])) {
                unlink($submission['file_path']);
            }
        }

        // Delete assignment file
        if ($assignment['file_path'] && file_exists($assignment['file_path'])) {
            unlink($assignment['file_path']);
        }

        // Delete assignment (cascade will handle submissions)
        $db->delete('assignments', 'assignment_id = ?', [$id]);

        // Commit transaction
        $db->commit();

        sendResponse(['message' => 'Assignment deleted successfully']);

    } catch (Exception $e) {
        $db->rollBack();
        logError('Delete Assignment Error: ' . $e->getMessage());
        sendError('Failed to delete assignment');
    }
}