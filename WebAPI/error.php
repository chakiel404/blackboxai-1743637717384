<?php
$httpCode = $_SERVER['REDIRECT_STATUS'] ?? 404;
$title = 'Error';
$message = 'An error occurred';

switch ($httpCode) {
    case 400:
        $title = '400 Bad Request';
        $message = 'The request could not be understood by the server due to malformed syntax.';
        break;
    case 401:
        $title = '401 Unauthorized';
        $message = 'Authentication is required and has failed or has not yet been provided.';
        break;
    case 403:
        $title = '403 Forbidden';
        $message = 'You do not have permission to access this resource.';
        break;
    case 404:
        $title = '404 Not Found';
        $message = 'The requested resource could not be found on this server.';
        break;
    case 405:
        $title = '405 Method Not Allowed';
        $message = 'The method specified in the request is not allowed for the resource.';
        break;
    case 500:
        $title = '500 Internal Server Error';
        $message = 'The server encountered an unexpected condition that prevented it from fulfilling the request.';
        break;
    case 503:
        $title = '503 Service Unavailable';
        $message = 'The server is currently unable to handle the request due to temporary overloading or maintenance.';
        break;
}

// Check if request accepts JSON
if (isset($_SERVER['HTTP_ACCEPT']) && strpos($_SERVER['HTTP_ACCEPT'], 'application/json') !== false) {
    header('Content-Type: application/json');
    echo json_encode([
        'success' => false,
        'error' => [
            'code' => $httpCode,
            'message' => $message
        ]
    ]);
    exit;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo htmlspecialchars($title); ?> - SmartApp API</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .error-container {
            text-align: center;
            padding: 40px;
            background: white;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
            max-width: 500px;
            width: 90%;
        }
        .error-code {
            font-size: 72px;
            font-weight: bold;
            color: #dc3545;
            margin-bottom: 20px;
        }
        .error-icon {
            font-size: 48px;
            color: #dc3545;
            margin-bottom: 20px;
        }
        .error-message {
            color: #6c757d;
            margin-bottom: 30px;
        }
        .back-link {
            color: #007bff;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        .back-link:hover {
            color: #0056b3;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-container">
            <div class="error-code"><?php echo $httpCode; ?></div>
            <div class="error-icon">
                <?php if ($httpCode == 404): ?>
                    <i class="fas fa-search"></i>
                <?php elseif ($httpCode == 403): ?>
                    <i class="fas fa-lock"></i>
                <?php elseif ($httpCode == 500): ?>
                    <i class="fas fa-exclamation-triangle"></i>
                <?php else: ?>
                    <i class="fas fa-exclamation-circle"></i>
                <?php endif; ?>
            </div>
            <h1 class="h4 mb-4"><?php echo htmlspecialchars($title); ?></h1>
            <p class="error-message"><?php echo htmlspecialchars($message); ?></p>
            <a href="/" class="back-link">
                <i class="fas fa-arrow-left"></i>
                Back to Documentation
            </a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>