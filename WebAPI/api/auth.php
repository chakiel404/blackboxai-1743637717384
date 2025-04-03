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
$action = isset($_GET['action']) ? $_GET['action'] : '';

try {
    switch ($method) {
        case 'POST':
            switch ($action) {
                case 'login':
                    handleLogin($db);
                    break;
                case 'register':
                    handleRegister($db);
                    break;
                default:
                    sendError('Invalid action', 404);
            }
            break;
        default:
            sendError('Method not allowed', 405);
    }
} catch (Exception $e) {
    logError('Auth Error: ' . $e->getMessage());
    sendError('An error occurred while processing your request');
}

function handleLogin($db) {
    $data = getJsonInput();
    
    // Validate required fields
    if (!isset($data['email']) || !isset($data['password'])) {
        sendError('Email and password are required');
    }

    $email = filter_var($data['email'], FILTER_SANITIZE_EMAIL);
    $password = $data['password'];

    // Get user from database
    try {
        $stmt = $db->query(
            "SELECT user_id, email, password, full_name, role FROM users WHERE email = ?",
            [$email]
        );
        $user = $stmt->fetch();

        if (!$user) {
            sendError('Invalid email or password', 401);
        }

        // Verify password
        if (!password_verify($password, $user['password'])) {
            sendError('Invalid email or password', 401);
        }

        // Generate token
        $token = generateToken($user['user_id'], $user['role']);

        // Return user data and token
        $response = [
            'token' => $token,
            'user' => [
                'id' => $user['user_id'],
                'email' => $user['email'],
                'full_name' => $user['full_name'],
                'role' => $user['role']
            ]
        ];

        // Add role-specific IDs
        if ($user['role'] === 'siswa') {
            $response['user']['nisn'] = $user['nisn'];
        } elseif ($user['role'] === 'guru') {
            $response['user']['nip'] = $user['nip'];
        }

        sendResponse($response);

    } catch (Exception $e) {
        logError('Login Error: ' . $e->getMessage());
        sendError('Failed to process login');
    }
}

function handleRegister($db) {
    $data = getJsonInput();
    
    // Validate required fields
    if (!isset($data['email']) || !isset($data['password']) || 
        !isset($data['full_name']) || !isset($data['role'])) {
        sendError('All fields are required');
    }

    // Sanitize and validate input
    $email = filter_var($data['email'], FILTER_SANITIZE_EMAIL);
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        sendError('Invalid email format');
    }

    $password = $data['password'];
    if (strlen($password) < 6) {
        sendError('Password must be at least 6 characters long');
    }

    $fullName = sanitizeInput($data['full_name']);
    if (empty($fullName)) {
        sendError('Full name is required');
    }

    $role = strtolower($data['role']);
    if (!in_array($role, ['admin', 'guru', 'siswa'])) {
        sendError('Invalid role');
    }

    // Check if email already exists
    try {
        $stmt = $db->query("SELECT user_id FROM users WHERE email = ?", [$email]);
        if ($stmt->fetch()) {
            sendError('Email already registered', 409);
        }

        // Hash password
        $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

        // Insert new user
        $userId = $db->insert('users', [
            'email' => $email,
            'password' => $hashedPassword,
            'full_name' => $fullName,
            'role' => $role
        ]);

        // Generate token
        $token = generateToken($userId, $role);

        // Return user data and token
        sendResponse([
            'token' => $token,
            'user' => [
                'id' => $userId,
                'email' => $email,
                'full_name' => $fullName,
                'role' => $role
            ]
        ], 201);

    } catch (Exception $e) {
        logError('Registration Error: ' . $e->getMessage());
        sendError('Failed to register user');
    }
}

function validatePassword($password) {
    // At least 6 characters long
    if (strlen($password) < 6) {
        return false;
    }

    // Contains at least one number
    if (!preg_match('/[0-9]/', $password)) {
        return false;
    }

    // Contains at least one letter
    if (!preg_match('/[a-zA-Z]/', $password)) {
        return false;
    }

    return true;
}