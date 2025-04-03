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
    // Verify admin access
    $user = authenticate();
    if (!$user || $user->role !== 'admin') {
        sendError('Unauthorized. Only administrators can manage schedules.', 403);
    }

    switch ($method) {
        case 'GET':
            if (isset($_GET['id'])) {
                getSchedule($db, $_GET['id']);
            } else {
                getSchedules($db);
            }
            break;
            
        case 'POST':
            createSchedule($db);
            break;
            
        case 'PUT':
            if (!isset($_GET['id'])) {
                sendError('Schedule ID is required', 400);
            }
            updateSchedule($db, $_GET['id']);
            break;
            
        case 'DELETE':
            if (!isset($_GET['id'])) {
                sendError('Schedule ID is required', 400);
            }
            deleteSchedule($db, $_GET['id']);
            break;
            
        default:
            sendError('Method not allowed', 405);
    }
} catch (Exception $e) {
    logError('Schedules Error: ' . $e->getMessage());
    sendError('An error occurred while processing your request');
}

function getSchedules($db) {
    try {
        $query = "SELECT sch.*, 
                        s.name as subject_name,
                        c.class_name,
                        CONCAT(t.first_name, ' ', t.last_name) as teacher_name
                 FROM schedules sch
                 JOIN subjects s ON sch.subject_id = s.subject_id
                 JOIN classes c ON sch.class_id = c.class_id
                 JOIN teachers t ON sch.teacher_id = t.teacher_id
                 ORDER BY sch.day_of_week, sch.start_time";
        
        $schedules = $db->fetchAll($query);
        
        sendResponse(['schedules' => $schedules]);
    } catch (Exception $e) {
        logError('Get Schedules Error: ' . $e->getMessage());
        sendError('Failed to fetch schedules');
    }
}

function getSchedule($db, $id) {
    try {
        $query = "SELECT sch.*, 
                        s.name as subject_name,
                        c.class_name,
                        CONCAT(t.first_name, ' ', t.last_name) as teacher_name
                 FROM schedules sch
                 JOIN subjects s ON sch.subject_id = s.subject_id
                 JOIN classes c ON sch.class_id = c.class_id
                 JOIN teachers t ON sch.teacher_id = t.teacher_id
                 WHERE sch.schedule_id = ?";
        
        $schedule = $db->fetch($query, [$id]);
        
        if (!$schedule) {
            sendError('Schedule not found', 404);
        }

        sendResponse(['schedule' => $schedule]);
    } catch (Exception $e) {
        logError('Get Schedule Error: ' . $e->getMessage());
        sendError('Failed to fetch schedule');
    }
}

function createSchedule($db) {
    try {
        $data = getJsonInput();
        
        // Validate required fields
        $requiredFields = ['subject_id', 'class_id', 'teacher_id', 'day_of_week', 'start_time', 'end_time'];
        foreach ($requiredFields as $field) {
            if (!isset($data[$field])) {
                sendError("Field '$field' is required", 400);
            }
        }

        // Validate teacher is assigned to subject
        $teacherSubject = $db->fetch(
            "SELECT * FROM teacher_subjects 
            WHERE teacher_id = ? AND subject_id = ?",
            [$data['teacher_id'], $data['subject_id']]
        );
        
        if (!$teacherSubject) {
            sendError('Teacher is not assigned to this subject', 400);
        }

        // Check for schedule conflicts
        $conflicts = $db->fetchAll(
            "SELECT * FROM schedules 
            WHERE class_id = ? 
            AND day_of_week = ? 
            AND ((start_time <= ? AND end_time > ?) 
                OR (start_time < ? AND end_time >= ?)
                OR (start_time >= ? AND end_time <= ?))",
            [
                $data['class_id'],
                $data['day_of_week'],
                $data['end_time'],
                $data['start_time'],
                $data['end_time'],
                $data['end_time'],
                $data['start_time'],
                $data['end_time']
            ]
        );

        if (!empty($conflicts)) {
            sendError('Schedule conflicts with existing class schedule', 400);
        }

        // Insert schedule
        $scheduleId = $db->insert('schedules', [
            'subject_id' => $data['subject_id'],
            'class_id' => $data['class_id'],
            'teacher_id' => $data['teacher_id'],
            'day_of_week' => $data['day_of_week'],
            'start_time' => $data['start_time'],
            'end_time' => $data['end_time'],
            'academic_year' => getCurrentAcademicYear(),
            'semester' => getCurrentSemester()
        ]);

        // Fetch created schedule
        getSchedule($db, $scheduleId);

    } catch (Exception $e) {
        logError('Create Schedule Error: ' . $e->getMessage());
        sendError('Failed to create schedule');
    }
}

function updateSchedule($db, $id) {
    try {
        // Check if schedule exists
        $schedule = $db->fetch(
            "SELECT * FROM schedules WHERE schedule_id = ?", 
            [$id]
        );

        if (!$schedule) {
            sendError('Schedule not found', 404);
        }

        $data = getJsonInput();
        $updates = [];

        // Update fields if provided
        $allowedFields = ['subject_id', 'class_id', 'teacher_id', 'day_of_week', 'start_time', 'end_time'];
        foreach ($allowedFields as $field) {
            if (isset($data[$field])) {
                $updates[$field] = $data[$field];
            }
        }

        if (!empty($updates)) {
            // If teacher or subject changed, validate teacher assignment
            if (isset($updates['teacher_id']) || isset($updates['subject_id'])) {
                $teacherId = $updates['teacher_id'] ?? $schedule['teacher_id'];
                $subjectId = $updates['subject_id'] ?? $schedule['subject_id'];
                
                $teacherSubject = $db->fetch(
                    "SELECT * FROM teacher_subjects 
                    WHERE teacher_id = ? AND subject_id = ?",
                    [$teacherId, $subjectId]
                );
                
                if (!$teacherSubject) {
                    sendError('Teacher is not assigned to this subject', 400);
                }
            }

            // Check for schedule conflicts
            if (isset($updates['class_id']) || isset($updates['day_of_week']) || 
                isset($updates['start_time']) || isset($updates['end_time'])) {
                
                $classId = $updates['class_id'] ?? $schedule['class_id'];
                $dayOfWeek = $updates['day_of_week'] ?? $schedule['day_of_week'];
                $startTime = $updates['start_time'] ?? $schedule['start_time'];
                $endTime = $updates['end_time'] ?? $schedule['end_time'];

                $conflicts = $db->fetchAll(
                    "SELECT * FROM schedules 
                    WHERE schedule_id != ? 
                    AND class_id = ? 
                    AND day_of_week = ? 
                    AND ((start_time <= ? AND end_time > ?) 
                        OR (start_time < ? AND end_time >= ?)
                        OR (start_time >= ? AND end_time <= ?))",
                    [
                        $id,
                        $classId,
                        $dayOfWeek,
                        $endTime,
                        $startTime,
                        $endTime,
                        $endTime,
                        $startTime,
                        $endTime
                    ]
                );

                if (!empty($conflicts)) {
                    sendError('Schedule conflicts with existing class schedule', 400);
                }
            }

            // Update the schedule
            $db->update('schedules', $updates, ['schedule_id' => $id]);
        }

        // Fetch updated schedule
        getSchedule($db, $id);

    } catch (Exception $e) {
        logError('Update Schedule Error: ' . $e->getMessage());
        sendError('Failed to update schedule');
    }
}

function deleteSchedule($db, $id) {
    try {
        // Check if schedule exists
        $schedule = $db->fetch(
            "SELECT * FROM schedules WHERE schedule_id = ?", 
            [$id]
        );

        if (!$schedule) {
            sendError('Schedule not found', 404);
        }

        // Delete the schedule
        $db->delete('schedules', ['schedule_id' => $id]);

        sendResponse(['message' => 'Schedule deleted successfully']);

    } catch (Exception $e) {
        logError('Delete Schedule Error: ' . $e->getMessage());
        sendError('Failed to delete schedule');
    }
}