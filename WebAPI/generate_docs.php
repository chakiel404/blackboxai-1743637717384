<?php
/**
 * API Documentation Generator
 * 
 * This script generates API documentation in Markdown format by analyzing
 * the API endpoint files and extracting documentation from comments.
 */

// Configuration
$config = [
    'api_dir' => __DIR__ . '/api',
    'output_file' => __DIR__ . '/API_DOCUMENTATION.md',
    'title' => 'SmartApp API Documentation',
    'base_url' => 'http://localhost:8000/api'
];

// API endpoint groups
$endpointGroups = [
    'auth' => [
        'title' => 'Authentication',
        'description' => 'Endpoints for user authentication and registration',
        'endpoints' => [
            'login' => [
                'method' => 'POST',
                'path' => '/auth/login',
                'description' => 'Authenticate user and get access token',
                'request' => [
                    'email' => 'string (required) - Email address or NISN/NIP',
                    'password' => 'string (required) - User password',
                    'login_type' => 'string (optional) - Type of login: "email" (default), "nisn", or "nip"'
                ],
                'response' => [
                    'token' => 'string (JWT token)',
                    'user' => [
                        'id' => 'integer',
                        'email' => 'string',
                        'full_name' => 'string',
                        'role' => 'string (admin|guru|siswa)',
                        'nisn' => 'string (for students)',
                        'nip' => 'string (for teachers)'
                    ]
                ]
            ],
            'register' => [
                'method' => 'POST',
                'path' => '/auth/register',
                'description' => 'Register a new user account',
                'request' => [
                    'email' => 'string (required) - Email address',
                    'password' => 'string (required) - Password (min 6 characters)',
                    'full_name' => 'string (required) - Full name',
                    'role' => 'string (required) - User role [guru|siswa]',
                    'nisn' => 'string (required for students) - Student ID number',
                    'nip' => 'string (required for teachers) - Teacher ID number'
                ],
                'response' => [
                    'token' => 'string (JWT token)',
                    'user' => [
                        'id' => 'integer',
                        'email' => 'string',
                        'full_name' => 'string',
                        'role' => 'string',
                        'nisn' => 'string (for students)',
                        'nip' => 'string (for teachers)'
                    ]
                ]
            ]
        ]
    ],
    'subjects' => [
        'title' => 'Subjects',
        'description' => 'Manage educational subjects',
        'endpoints' => []
    ],
    'materials' => [
        'title' => 'Learning Materials',
        'description' => 'Handle educational materials and resources',
        'endpoints' => []
    ],
    'quizzes' => [
        'title' => 'Quizzes',
        'description' => 'Manage quizzes and submissions',
        'endpoints' => []
    ],
    'assignments' => [
        'title' => 'Assignments',
        'description' => 'Handle assignments and submissions',
        'endpoints' => []
    ]
];

// Generate documentation
$doc = "# {$config['title']}\n\n";

// Add introduction
$doc .= "## Introduction\n\n";
$doc .= "This documentation provides information about the SmartApp API endpoints, their request/response formats, ";
$doc .= "and authentication requirements.\n\n";

// Add base URL
$doc .= "## Base URL\n\n";
$doc .= "All API requests should be made to: `{$config['base_url']}`\n\n";

// Add authentication section
$doc .= "## Authentication\n\n";
$doc .= "Most API endpoints require authentication using JWT tokens. Include the token in the Authorization header:\n\n";
$doc .= "```\nAuthorization: Bearer your-token-here\n```\n\n";
$doc .= "### Login Methods\n\n";
$doc .= "Users can log in using:\n";
$doc .= "- Email address (all users)\n";
$doc .= "- NISN (students)\n";
$doc .= "- NIP (teachers)\n\n";

// Add common responses
$doc .= "## Common Response Formats\n\n";
$doc .= "### Success Response\n";
$doc .= "```json\n" . json_encode(['success' => true, 'data' => '...'], JSON_PRETTY_PRINT) . "\n```\n\n";
$doc .= "### Error Response\n";
$doc .= "```json\n" . json_encode(['success' => false, 'error' => 'Error message'], JSON_PRETTY_PRINT) . "\n```\n\n";

// Add endpoints documentation
foreach ($endpointGroups as $group => $info) {
    $doc .= "## {$info['title']}\n\n";
    $doc .= "{$info['description']}\n\n";
    
    foreach ($info['endpoints'] as $name => $endpoint) {
        $doc .= "### {$endpoint['method']} {$endpoint['path']}\n\n";
        $doc .= "{$endpoint['description']}\n\n";
        
        // Request
        $doc .= "#### Request\n\n";
        $doc .= "```json\n" . json_encode($endpoint['request'], JSON_PRETTY_PRINT) . "\n```\n\n";
        
        // Response
        $doc .= "#### Response\n\n";
        $doc .= "```json\n" . json_encode($endpoint['response'], JSON_PRETTY_PRINT) . "\n```\n\n";
    }
}

// Add file upload section
$doc .= "## File Uploads\n\n";
$doc .= "For endpoints that accept file uploads (materials, assignments), use multipart/form-data format.\n\n";
$doc .= "### File Upload Limits\n\n";
$doc .= "- Maximum file size: 10MB\n";
$doc .= "- Allowed file types: pdf, doc, docx, ppt, pptx, xls, xlsx, txt, jpg, jpeg, png\n\n";

// Add error codes
$doc .= "## Error Codes\n\n";
$doc .= "| Code | Description |\n";
$doc .= "|------|-------------|\n";
$doc .= "| 400  | Bad Request - Invalid input parameters |\n";
$doc .= "| 401  | Unauthorized - Authentication required |\n";
$doc .= "| 403  | Forbidden - Insufficient permissions |\n";
$doc .= "| 404  | Not Found - Resource not found |\n";
$doc .= "| 422  | Unprocessable Entity - Validation failed |\n";
$doc .= "| 500  | Internal Server Error |\n\n";

// Add rate limiting section
$doc .= "## Rate Limiting\n\n";
$doc .= "API requests are limited to 100 requests per minute per IP address. The following headers are included in the response:\n\n";
$doc .= "```\nX-RateLimit-Limit: 100\nX-RateLimit-Remaining: 99\nX-RateLimit-Reset: 1623456789\n```\n\n";

// Add examples section
$doc .= "## Examples\n\n";

// Login examples
$doc .= "### Login Examples\n\n";

// Email login
$doc .= "#### Login with Email\n\n";
$doc .= "```bash\n";
$doc .= "curl -X POST {$config['base_url']}/auth/login \\\n";
$doc .= "  -H 'Content-Type: application/json' \\\n";
$doc .= "  -d '{\n";
$doc .= "    \"email\": \"teacher@example.com\",\n";
$doc .= "    \"password\": \"password123\"\n";
$doc .= "  }'\n";
$doc .= "```\n\n";

// NISN login
$doc .= "#### Login with NISN (Students)\n\n";
$doc .= "```bash\n";
$doc .= "curl -X POST {$config['base_url']}/auth/login \\\n";
$doc .= "  -H 'Content-Type: application/json' \\\n";
$doc .= "  -d '{\n";
$doc .= "    \"email\": \"123456789\",\n";
$doc .= "    \"password\": \"password123\",\n";
$doc .= "    \"login_type\": \"nisn\"\n";
$doc .= "  }'\n";
$doc .= "```\n\n";

// NIP login
$doc .= "#### Login with NIP (Teachers)\n\n";
$doc .= "```bash\n";
$doc .= "curl -X POST {$config['base_url']}/auth/login \\\n";
$doc .= "  -H 'Content-Type: application/json' \\\n";
$doc .= "  -d '{\n";
$doc .= "    \"email\": \"987654321\",\n";
$doc .= "    \"password\": \"password123\",\n";
$doc .= "    \"login_type\": \"nip\"\n";
$doc .= "  }'\n";
$doc .= "```\n\n";

// Upload material example
$doc .= "### Upload Material Example\n\n";
$doc .= "```bash\n";
$doc .= "curl -X POST {$config['base_url']}/materials \\\n";
$doc .= "  -H 'Authorization: Bearer your-token-here' \\\n";
$doc .= "  -F 'title=Example Material' \\\n";
$doc .= "  -F 'description=Example description' \\\n";
$doc .= "  -F 'subject_id=1' \\\n";
$doc .= "  -F 'file=@/path/to/file.pdf'\n";
$doc .= "```\n\n";

// Save documentation
file_put_contents($config['output_file'], $doc);
echo "Documentation generated successfully at: {$config['output_file']}\n";
