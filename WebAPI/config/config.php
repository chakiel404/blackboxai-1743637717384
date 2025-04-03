<?php
// Error reporting - Only log errors, don't display them
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/../logs/error.log');

// Database configuration
define('DB_TYPE', 'sqlite');
define('DB_PATH', __DIR__ . '/../database/smartapp.db');

// File upload settings
define('MAX_FILE_SIZE', 10 * 1024 * 1024); // 10MB
define('ALLOWED_FILE_TYPES', [
    'pdf' => 'application/pdf',
    'doc' => 'application/msword',
    'docx' => 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'ppt' => 'application/vnd.ms-powerpoint',
    'pptx' => 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'xls' => 'application/vnd.ms-excel',
    'xlsx' => 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'txt' => 'text/plain',
    'jpg' => 'image/jpeg',
    'jpeg' => 'image/jpeg',
    'png' => 'image/png'
]);

// Path configurations
define('UPLOAD_DIR', __DIR__ . '/../uploads');
define('MATERIALS_DIR', UPLOAD_DIR . '/materials');
define('ASSIGNMENTS_DIR', UPLOAD_DIR . '/assignments');

// Create upload directories if they don't exist
if (!file_exists(UPLOAD_DIR)) mkdir(UPLOAD_DIR, 0755, true);
if (!file_exists(MATERIALS_DIR)) mkdir(MATERIALS_DIR, 0755, true);
if (!file_exists(ASSIGNMENTS_DIR)) mkdir(ASSIGNMENTS_DIR, 0755, true);

// API settings
define('JWT_SECRET', 'your-secret-key'); // Change this in production
define('TOKEN_EXPIRY', 24 * 60 * 60); // 24 hours

// Response headers
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}