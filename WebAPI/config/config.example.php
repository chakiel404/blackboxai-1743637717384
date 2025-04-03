<?php
// Error reporting
error_reporting(E_ALL);
ini_set('display_errors', 0);

// Database configuration
define('DB_HOST', 'localhost');     // Database host
define('DB_NAME', 'smartapp_db');   // Database name
define('DB_USER', 'root');          // Database username
define('DB_PASS', '');              // Database password
define('DB_CHARSET', 'utf8mb4');    // Database charset

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
define('JWT_SECRET', 'your-secret-key-here');  // Change this in production!
define('TOKEN_EXPIRY', 24 * 60 * 60);         // 24 hours

// Session settings
ini_set('session.cookie_httponly', 1);
ini_set('session.use_only_cookies', 1);
ini_set('session.cookie_secure', 1);
ini_set('session.cookie_samesite', 'Strict');
ini_set('session.gc_maxlifetime', 1800);      // 30 minutes
ini_set('session.cookie_lifetime', 1800);     // 30 minutes

// CORS settings
$allowedOrigins = [
    'http://localhost:8000',
    'http://localhost:3000',
    // Add your production domains here
];

// Set CORS headers
if (isset($_SERVER['HTTP_ORIGIN'])) {
    $origin = $_SERVER['HTTP_ORIGIN'];
    if (in_array($origin, $allowedOrigins)) {
        header('Access-Control-Allow-Origin: ' . $origin);
    }
}

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization');
    header('Access-Control-Max-Age: 86400');  // 24 hours
    exit();
}

// Security headers
header('X-Content-Type-Options: nosniff');
header('X-Frame-Options: SAMEORIGIN');
header('X-XSS-Protection: 1; mode=block');
header('Content-Security-Policy: default-src \'self\'; img-src \'self\' data: https:; style-src \'self\' \'unsafe-inline\' https:; script-src \'self\' \'unsafe-inline\' \'unsafe-eval\' https:; font-src \'self\' https:;');

// Rate limiting settings
define('RATE_LIMIT_REQUESTS', 100);           // Number of requests
define('RATE_LIMIT_WINDOW', 60);              // Time window in seconds

// Cache settings
define('CACHE_ENABLED', true);
define('CACHE_DURATION', 300);                // 5 minutes

// Logging settings
define('LOG_ERRORS', true);
define('LOG_FILE', __DIR__ . '/../log.txt');

// Maintenance mode
define('MAINTENANCE_MODE', false);
define('MAINTENANCE_MESSAGE', 'System is under maintenance. Please try again later.');

// Debug mode (set to false in production)
define('DEBUG_MODE', false);

// Time zone
date_default_timezone_set('Asia/Jakarta');

// Custom error handler
function customErrorHandler($errno, $errstr, $errfile, $errline) {
    if (LOG_ERRORS) {
        $timestamp = date('Y-m-d H:i:s');
        $message = "[$timestamp] Error $errno: $errstr in $errfile on line $errline\n";
        error_log($message, 3, LOG_FILE);
    }

    if (DEBUG_MODE) {
        return false; // Let PHP handle the error
    } else {
        // Return generic error in production
        http_response_code(500);
        if (strpos($_SERVER['HTTP_ACCEPT'], 'application/json') !== false) {
            header('Content-Type: application/json');
            echo json_encode([
                'success' => false,
                'error' => 'An internal server error occurred'
            ]);
        } else {
            require __DIR__ . '/../error.php';
        }
        exit();
    }
}

// Set custom error handler
set_error_handler('customErrorHandler', E_ALL);

// Maintenance mode check
if (MAINTENANCE_MODE && !in_array($_SERVER['REMOTE_ADDR'], ['127.0.0.1', '::1'])) {
    http_response_code(503);
    if (strpos($_SERVER['HTTP_ACCEPT'], 'application/json') !== false) {
        header('Content-Type: application/json');
        echo json_encode([
            'success' => false,
            'error' => MAINTENANCE_MESSAGE
        ]);
    } else {
        echo MAINTENANCE_MESSAGE;
    }
    exit();
}