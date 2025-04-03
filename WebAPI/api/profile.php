<?php
require_once __DIR__ . '/../core/helpers.php';
require_once __DIR__ . '/../core/DB.php';

// Get authenticated user
$user = authenticate();
if (!$user) {
    sendError('Unauthorized', 401);
}

// Handle request based on method
switch ($_SERVER['REQUEST_METHOD']) {
    case 'GET':
        handleGetProfile($user);
        break;
    case 'PUT':
        handleUpdateProfile($user);
        break;
    default:
        sendError('Method not allowed', 405);
}

function handleGetProfile($user) {
    global $db;

    try {
        if ($user['role'] === 'siswa') {
            // Get student profile
            $query = "SELECT u.*, s.* FROM users u 
                     JOIN students s ON u.user_id = s.user_id 
                     WHERE u.user_id = ?";
            $profile = $db->fetch($query, [$user['user_id']]);
        } elseif ($user['role'] === 'guru') {
            // Get teacher profile
            $query = "SELECT u.*, t.* FROM users u 
                     JOIN teachers t ON u.user_id = t.user_id 
                     WHERE u.user_id = ?";
            $profile = $db->fetch($query, [$user['user_id']]);
        } else {
            // Get admin profile
            $query = "SELECT * FROM users WHERE user_id = ?";
            $profile = $db->fetch($query, [$user['user_id']]);
        }

        if (!$profile) {
            sendError('Profile not found', 404);
        }

        // Remove sensitive data
        unset($profile['password']);

        sendResponse($profile);
    } catch (Exception $e) {
        logError('Get Profile Error: ' . $e->getMessage());
        sendError('Failed to get profile');
    }
}

function handleUpdateProfile($user) {
    global $db;

    // Get request data
    $data = json_decode(file_get_contents('php://input'), true);
    if (!$data) {
        sendError('Invalid request data');
    }

    try {
        // Start transaction
        $db->beginTransaction();

        // Update common user data
        $userData = [
            'full_name' => sanitizeInput($data['full_name']),
            'email' => filter_var($data['email'], FILTER_SANITIZE_EMAIL)
        ];

        if (!filter_var($userData['email'], FILTER_VALIDATE_EMAIL)) {
            sendError('Invalid email format');
        }

        // Check if email is already used by another user
        $existing = $db->fetch(
            "SELECT user_id FROM users WHERE email = ? AND user_id != ?",
            [$userData['email'], $user['user_id']]
        );
        if ($existing) {
            sendError('Email already in use');
        }

        // Update users table
        $db->update('users', $userData, ['user_id' => $user['user_id']]);

        // Update role-specific data
        if ($user['role'] === 'siswa') {
            $studentData = [
                'gender' => sanitizeInput($data['gender']),
                'birth_date' => sanitizeInput($data['birth_date']),
                'birth_place' => sanitizeInput($data['birth_place']),
                'address' => sanitizeInput($data['address']),
                'phone' => sanitizeInput($data['phone']),
                'class' => sanitizeInput($data['class']),
                'parent_name' => sanitizeInput($data['parent_name']),
                'parent_phone' => sanitizeInput($data['parent_phone'])
            ];

            // Update students table
            $db->update('students', $studentData, ['user_id' => $user['user_id']]);

        } elseif ($user['role'] === 'guru') {
            $teacherData = [
                'gender' => sanitizeInput($data['gender']),
                'birth_date' => sanitizeInput($data['birth_date']),
                'birth_place' => sanitizeInput($data['birth_place']),
                'address' => sanitizeInput($data['address']),
                'phone' => sanitizeInput($data['phone']),
                'education_level' => sanitizeInput($data['education_level']),
                'major' => sanitizeInput($data['major']),
                'join_date' => sanitizeInput($data['join_date'])
            ];

            // Update teachers table
            $db->update('teachers', $teacherData, ['user_id' => $user['user_id']]);
        }

        // Commit transaction
        $db->commit();

        // Get updated profile
        handleGetProfile($user);

    } catch (Exception $e) {
        // Rollback on error
        $db->rollback();
        logError('Update Profile Error: ' . $e->getMessage());
        sendError('Failed to update profile');
    }
}