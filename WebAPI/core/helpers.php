<?php
require_once __DIR__ . '/../config/config.php';

/**
 * Send a JSON response with proper headers
 */
function sendResponse($data, $statusCode = 200) {
    // Start output buffering
    ob_start();
    
    // Clear any previous output
    if (ob_get_length()) ob_clean();
    
    // Set headers
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-cache, must-revalidate');
    http_response_code($statusCode);
    
    // Prepare response
    $response = [
        'success' => $statusCode >= 200 && $statusCode < 300,
        'data' => $data
    ];
    
    // Ensure proper JSON encoding with pretty print for debugging
    $jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
    
    if ($jsonResponse === false) {
        // Log JSON encoding error
        error_log('JSON encoding error: ' . json_last_error_msg());
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'error' => 'Internal server error: ' . json_last_error_msg()
        ], JSON_PRETTY_PRINT);
    } else {
        echo $jsonResponse;
    }
    
    // End output buffering and flush
    ob_end_flush();
    exit();
}

/**
 * Send an error response with proper headers
 */
function sendError($message, $statusCode = 400) {
    // Start output buffering
    ob_start();
    
    // Clear any previous output
    if (ob_get_length()) ob_clean();
    
    // Set headers
    header('Content-Type: application/json; charset=utf-8');
    header('Cache-Control: no-cache, must-revalidate');
    http_response_code($statusCode);
    
    // Prepare error response
    $response = [
        'success' => false,
        'error' => $message
    ];
    
    // Ensure proper JSON encoding with pretty print for debugging
    $jsonResponse = json_encode($response, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
    
    if ($jsonResponse === false) {
        // Log JSON encoding error
        error_log('JSON encoding error: ' . json_last_error_msg());
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'error' => 'Internal server error: ' . json_last_error_msg()
        ], JSON_PRETTY_PRINT);
    } else {
        echo $jsonResponse;
    }
    
    // End output buffering and flush
    ob_end_flush();
    exit();
}

/**
 * Log error messages to file
 */
function logError($message, $context = []) {
    $logFile = __DIR__ . '/../log.txt';
    $timestamp = date('Y-m-d H:i:s');
    $contextStr = !empty($context) ? ' Context: ' . json_encode($context) : '';
    $logMessage = "[$timestamp] $message$contextStr\n";
    file_put_contents($logFile, $logMessage, FILE_APPEND);
}

/**
 * Sanitize input data
 */
function sanitizeInput($data) {
    if (is_array($data)) {
        return array_map('sanitizeInput', $data);
    }
    return htmlspecialchars(trim($data), ENT_QUOTES, 'UTF-8');
}

/**
 * Validate file upload
 */
function validateFile($file, $allowedTypes = ALLOWED_FILE_TYPES) {
    if (!isset($file['error']) || is_array($file['error'])) {
        return ['valid' => false, 'message' => 'Invalid file parameters'];
    }

    switch ($file['error']) {
        case UPLOAD_ERR_OK:
            break;
        case UPLOAD_ERR_INI_SIZE:
        case UPLOAD_ERR_FORM_SIZE:
            return ['valid' => false, 'message' => 'File too large'];
        case UPLOAD_ERR_PARTIAL:
            return ['valid' => false, 'message' => 'File upload was partial'];
        case UPLOAD_ERR_NO_FILE:
            return ['valid' => false, 'message' => 'No file was uploaded'];
        default:
            return ['valid' => false, 'message' => 'Unknown upload error'];
    }

    if ($file['size'] > MAX_FILE_SIZE) {
        return ['valid' => false, 'message' => 'File size exceeds limit'];
    }

    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mimeType = $finfo->file($file['tmp_name']);

    if (!in_array($mimeType, $allowedTypes)) {
        return ['valid' => false, 'message' => 'Invalid file type'];
    }

    return ['valid' => true, 'message' => ''];
}

/**
 * Handle file upload
 */
function handleFileUpload($file, $targetDir, $allowedTypes = ALLOWED_FILE_TYPES) {
    $validation = validateFile($file, $allowedTypes);
    if (!$validation['valid']) {
        return ['success' => false, 'message' => $validation['message']];
    }

    if (!file_exists($targetDir)) {
        mkdir($targetDir, 0755, true);
    }

    $fileExtension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    $fileName = uniqid() . '.' . $fileExtension;
    $targetPath = $targetDir . '/' . $fileName;

    if (!move_uploaded_file($file['tmp_name'], $targetPath)) {
        return ['success' => false, 'message' => 'Failed to move uploaded file'];
    }

    return [
        'success' => true,
        'fileName' => $fileName,
        'filePath' => $targetPath,
        'fileSize' => $file['size'],
        'mimeType' => $file['type']
    ];
}

/**
 * Generate JWT token
 */
function generateToken($userId, $role) {
    $issuedAt = time();
    $expirationTime = $issuedAt + TOKEN_EXPIRY;

    $payload = [
        'iat' => $issuedAt,
        'exp' => $expirationTime,
        'userId' => $userId,
        'role' => $role
    ];

    return jwt_encode($payload, JWT_SECRET);
}

/**
 * Verify JWT token
 */
function verifyToken($token) {
    try {
        $payload = jwt_decode($token, JWT_SECRET);
        
        if ($payload->exp < time()) {
            return ['valid' => false, 'message' => 'Token has expired'];
        }

        return ['valid' => true, 'payload' => $payload];
    } catch (Exception $e) {
        return ['valid' => false, 'message' => 'Invalid token'];
    }
}

/**
 * Check if user has required role
 */
function checkRole($requiredRoles) {
    $headers = getallheaders();
    if (!isset($headers['Authorization'])) {
        sendError('No authorization token provided', 401);
    }

    $token = str_replace('Bearer ', '', $headers['Authorization']);
    $verification = verifyToken($token);

    if (!$verification['valid']) {
        sendError($verification['message'], 401);
    }

    if (!in_array($verification['payload']->role, (array)$requiredRoles)) {
        sendError('Unauthorized access', 403);
    }

    return $verification['payload'];
}

/**
 * Get request body as JSON
 */
function getJsonInput() {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);

    if (json_last_error() !== JSON_ERROR_NONE) {
        sendError('Invalid JSON input');
    }

    return $data;
}

/**
 * Format date for API response
 */
function formatDate($date) {
    return date('Y-m-d H:i:s', strtotime($date));
}

/**
 * Generate random string
 */
function generateRandomString($length = 10) {
    return bin2hex(random_bytes($length));
}

/**
 * Simple JWT encode function (for demonstration)
 * In production, use a proper JWT library
 */
function jwt_encode($payload, $secret) {
    $header = json_encode(['typ' => 'JWT', 'alg' => 'HS256']);
    $payload = json_encode($payload);
    
    $base64UrlHeader = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($header));
    $base64UrlPayload = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($payload));
    
    $signature = hash_hmac('sha256', $base64UrlHeader . "." . $base64UrlPayload, $secret, true);
    $base64UrlSignature = str_replace(['+', '/', '='], ['-', '_', ''], base64_encode($signature));
    
    return $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;
}

/**
 * Simple JWT decode function (for demonstration)
 * In production, use a proper JWT library
 */
function jwt_decode($token, $secret) {
    $tokenParts = explode('.', $token);
    if (count($tokenParts) != 3) {
        throw new Exception('Invalid token format');
    }

    $payload = base64_decode(str_replace(['-', '_'], ['+', '/'], $tokenParts[1]));
    return json_decode($payload);
}