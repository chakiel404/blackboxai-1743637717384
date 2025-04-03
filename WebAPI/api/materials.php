<?php
// Start output buffering at the very beginning
ob_start();

require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../core/DB.php';
require_once __DIR__ . '/../core/helpers.php';

// Ensure clean output
if (ob_get_length()) ob_clean();

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
                getMaterial($db, $_GET['id']);
            } else {
                getMaterials($db);
            }
            break;
            
        case 'POST':
            // Only teachers and admins can create materials
            $user = checkRole(['admin', 'guru']);
            createMaterial($db, $user);
            break;
            
        case 'PUT':
            $user = checkRole(['admin', 'guru']);
            if (!isset($_GET['id'])) {
                sendError('Material ID is required', 400);
            }
            updateMaterial($db, $_GET['id'], $user);
            break;
            
        case 'DELETE':
            $user = checkRole(['admin', 'guru']);
            if (!isset($_GET['id'])) {
                sendError('Material ID is required', 400);
            }
            deleteMaterial($db, $_GET['id'], $user);
            break;
            
        default:
            sendError('Method not allowed', 405);
    }
} catch (Exception $e) {
    logError('Materials Error: ' . $e->getMessage());
    sendError('An error occurred while processing your request');
}

function getMaterials($db) {
    try {
        $user = authenticate();
        if (!$user) {
            sendError('Unauthorized', 401);
        }

        // Base query
        $query = "SELECT m.*, s.name as subject_name, u.full_name as uploaded_by_name 
                 FROM materials m 
                 JOIN subjects s ON m.subject_id = s.subject_id 
                 JOIN users u ON m.uploaded_by = u.user_id";
        
        $params = [];

        // Filter based on user role
        if ($user->role === 'guru') {
            // Teachers can only see materials for their assigned subjects
            $query .= " JOIN teacher_subjects ts ON s.subject_id = ts.subject_id 
                       WHERE ts.teacher_id = ? AND ts.academic_year = ? AND ts.semester = ?";
            $params = [$user->teacherId, getCurrentAcademicYear(), getCurrentSemester()];
        } 
        elseif ($user->role === 'siswa') {
            // Students can only see materials for their class subjects according to schedule
            $query .= " JOIN schedules sch ON s.subject_id = sch.subject_id 
                       JOIN student_classes sc ON sch.class_id = sc.class_id 
                       WHERE sc.student_id = ? AND sc.academic_year = ? AND sc.semester = ?";
            $params = [$user->studentId, getCurrentAcademicYear(), getCurrentSemester()];
        }
        // Admins can see all materials
        
        $query .= " ORDER BY m.created_at DESC";
        
        $materials = $db->fetchAll($query, $params);
        
        // Format the response
        $formattedMaterials = array_map(function($material) {
            return [
                'id' => $material['material_id'],
                'title' => $material['title'],
                'description' => $material['description'],
                'fileName' => $material['file_name'],
                'fileSize' => $material['file_size'],
                'mimeType' => $material['mime_type'],
                'subject' => [
                    'id' => $material['subject_id'],
                    'name' => $material['subject_name']
                ],
                'uploadedBy' => [
                    'id' => $material['uploaded_by'],
                    'name' => $material['uploaded_by_name']
                ],
                'createdAt' => $material['created_at'],
                'updatedAt' => $material['updated_at']
            ];
        }, $materials);

        sendResponse(['materials' => $formattedMaterials]);
    } catch (Exception $e) {
        logError('Get Materials Error: ' . $e->getMessage());
        sendError('Failed to fetch materials');
    }
}

function getMaterial($db, $id) {
    try {
        $query = "SELECT m.*, s.name as subject_name, u.full_name as uploaded_by_name 
                 FROM materials m 
                 JOIN subjects s ON m.subject_id = s.subject_id 
                 JOIN users u ON m.uploaded_by = u.user_id 
                 WHERE m.material_id = ?";
        
        $material = $db->fetch($query, [$id]);
        
        if (!$material) {
            sendError('Material not found', 404);
        }

        // Format the response
        $formattedMaterial = [
            'id' => $material['material_id'],
            'title' => $material['title'],
            'description' => $material['description'],
            'fileName' => $material['file_name'],
            'fileSize' => $material['file_size'],
            'mimeType' => $material['mime_type'],
            'subject' => [
                'id' => $material['subject_id'],
                'name' => $material['subject_name']
            ],
            'uploadedBy' => [
                'id' => $material['uploaded_by'],
                'name' => $material['uploaded_by_name']
            ],
            'createdAt' => $material['created_at'],
            'updatedAt' => $material['updated_at']
        ];

        sendResponse(['material' => $formattedMaterial]);
    } catch (Exception $e) {
        logError('Get Material Error: ' . $e->getMessage());
        sendError('Failed to fetch material');
    }
}

function createMaterial($db, $user) {
    try {
        // Validate file upload
        if (!isset($_FILES['file'])) {
            sendError('No file uploaded', 400);
        }

        // Validate other required fields
        if (!isset($_POST['title']) || !isset($_POST['subject_id'])) {
            sendError('Title and subject are required', 400);
        }

        $title = sanitizeInput($_POST['title']);
        $description = isset($_POST['description']) ? sanitizeInput($_POST['description']) : '';
        $subjectId = filter_var($_POST['subject_id'], FILTER_VALIDATE_INT);

        // Validate subject exists
        $subject = $db->fetch("SELECT subject_id FROM subjects WHERE subject_id = ?", [$subjectId]);
        if (!$subject) {
            sendError('Invalid subject', 400);
        }

        // If user is a teacher, verify they are assigned to this subject and have it scheduled
        if ($user->role === 'guru') {
            // Check if teacher is assigned to this subject in current academic year and semester
            $teacherSubject = $db->fetch(
                "SELECT ts.* FROM teacher_subjects ts
                WHERE ts.teacher_id = ? 
                AND ts.subject_id = ?
                AND ts.academic_year = ?
                AND ts.semester = ?",
                [
                    $user->teacherId, 
                    $subjectId,
                    getCurrentAcademicYear(),
                    getCurrentSemester()
                ]
            );
            
            if (!$teacherSubject) {
                sendError('You are not assigned to teach this subject in the current academic year/semester', 403);
            }

            // Check if the subject is in teacher's schedule
            $schedule = $db->fetch(
                "SELECT s.* FROM schedules s
                WHERE s.teacher_id = ? 
                AND s.subject_id = ?
                AND s.academic_year = ?
                AND s.semester = ?",
                [
                    $user->teacherId, 
                    $subjectId,
                    getCurrentAcademicYear(),
                    getCurrentSemester()
                ]
            );

            if (!$schedule) {
                sendError('This subject is not in your current teaching schedule', 403);
            }
        }

        // Handle file upload
        $uploadResult = handleFileUpload($_FILES['file'], MATERIALS_DIR);
        if (!$uploadResult['success']) {
            sendError($uploadResult['message'], 400);
        }

        // Begin transaction
        $db->beginTransaction();

        // Insert material record
        $materialId = $db->insert('materials', [
            'title' => $title,
            'description' => $description,
            'file_path' => $uploadResult['filePath'],
            'file_name' => $uploadResult['fileName'],
            'file_size' => $uploadResult['fileSize'],
            'mime_type' => $uploadResult['mimeType'],
            'subject_id' => $subjectId,
            'uploaded_by' => $user->userId
        ]);

        // Commit transaction
        $db->commit();

        // Fetch the created material with related data
        $query = "SELECT m.*, s.name as subject_name, u.full_name as uploaded_by_name 
                 FROM materials m 
                 JOIN subjects s ON m.subject_id = s.subject_id 
                 JOIN users u ON m.uploaded_by = u.user_id 
                 WHERE m.material_id = ?";
        
        $material = $db->fetch($query, [$materialId]);

        sendResponse([
            'message' => 'Material created successfully',
            'material' => [
                'id' => $material['material_id'],
                'title' => $material['title'],
                'description' => $material['description'],
                'fileName' => $material['file_name'],
                'fileSize' => $material['file_size'],
                'mimeType' => $material['mime_type'],
                'subject' => [
                    'id' => $material['subject_id'],
                    'name' => $material['subject_name']
                ],
                'uploadedBy' => [
                    'id' => $material['uploaded_by'],
                    'name' => $material['uploaded_by_name']
                ],
                'createdAt' => $material['created_at']
            ]
        ], 201);

    } catch (Exception $e) {
        // Rollback transaction on error
        $db->rollBack();
        logError('Create Material Error: ' . $e->getMessage());
        sendError('Failed to create material');
    }
}

function updateMaterial($db, $id, $user) {
    try {
        // Check if material exists and user has permission
        $material = $db->fetch(
            "SELECT * FROM materials WHERE material_id = ?", 
            [$id]
        );

        if (!$material) {
            sendError('Material not found', 404);
        }

        // Only the uploader or admin can update
        if ($user->role !== 'admin' && $material['uploaded_by'] !== $user->userId) {
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
            $subjectId = filter_var($data['subject_id'], FILTER_VALIDATE_INT);
            $subject = $db->fetch("SELECT subject_id FROM subjects WHERE subject_id = ?", [$subjectId]);
            if (!$subject) {
                sendError('Invalid subject', 400);
            }
            $updates['subject_id'] = $subjectId;
        }

        if (empty($updates)) {
            sendError('No updates provided', 400);
        }

        // Update the material
        $db->update('materials', $updates, 'material_id = ?', [$id]);

        // Fetch updated material
        $query = "SELECT m.*, s.name as subject_name, u.full_name as uploaded_by_name 
                 FROM materials m 
                 JOIN subjects s ON m.subject_id = s.subject_id 
                 JOIN users u ON m.uploaded_by = u.user_id 
                 WHERE m.material_id = ?";
        
        $updatedMaterial = $db->fetch($query, [$id]);

        sendResponse([
            'message' => 'Material updated successfully',
            'material' => [
                'id' => $updatedMaterial['material_id'],
                'title' => $updatedMaterial['title'],
                'description' => $updatedMaterial['description'],
                'fileName' => $updatedMaterial['file_name'],
                'fileSize' => $updatedMaterial['file_size'],
                'mimeType' => $updatedMaterial['mime_type'],
                'subject' => [
                    'id' => $updatedMaterial['subject_id'],
                    'name' => $updatedMaterial['subject_name']
                ],
                'uploadedBy' => [
                    'id' => $updatedMaterial['uploaded_by'],
                    'name' => $updatedMaterial['uploaded_by_name']
                ],
                'updatedAt' => $updatedMaterial['updated_at']
            ]
        ]);

    } catch (Exception $e) {
        logError('Update Material Error: ' . $e->getMessage());
        sendError('Failed to update material');
    }
}

function deleteMaterial($db, $id, $user) {
    try {
        // Check if material exists and user has permission
        $material = $db->fetch(
            "SELECT * FROM materials WHERE material_id = ?", 
            [$id]
        );

        if (!$material) {
            sendError('Material not found', 404);
        }

        // Only the uploader or admin can delete
        if ($user->role !== 'admin' && $material['uploaded_by'] !== $user->userId) {
            sendError('Unauthorized', 403);
        }

        // Begin transaction
        $db->beginTransaction();

        // Delete the file
        if (file_exists($material['file_path'])) {
            unlink($material['file_path']);
        }

        // Delete the database record
        $db->delete('materials', 'material_id = ?', [$id]);

        // Commit transaction
        $db->commit();

        sendResponse(['message' => 'Material deleted successfully']);

    } catch (Exception $e) {
        // Rollback transaction on error
        $db->rollBack();
        logError('Delete Material Error: ' . $e->getMessage());
        sendError('Failed to delete material');
    }
}