<?php
/**
 * SmartApp API Test Script
 * 
 * This script helps test the API endpoints during development.
 * DO NOT USE IN PRODUCTION!
 */

require_once __DIR__ . '/config/config.php';
require_once __DIR__ . '/core/helpers.php';

// Test configuration
$config = [
    'base_url' => 'http://localhost:8000',
    'admin_credentials' => [
        'email' => 'admin@smartapp.com',
        'password' => 'admin123'
    ],
    'teacher_credentials' => [
        'email' => 'john.doe@smartapp.com',
        'password' => 'teacher123'
    ],
    'student_credentials' => [
        'email' => 'student1@smartapp.com',
        'password' => 'student123'
    ]
];

// Store tokens
$tokens = [];

// Test cases
$tests = [
    // Authentication tests
    'auth' => [
        [
            'name' => 'Admin Login',
            'endpoint' => '/api/auth/login',
            'method' => 'POST',
            'data' => $config['admin_credentials'],
            'expected_status' => 200
        ],
        [
            'name' => 'Teacher Login',
            'endpoint' => '/api/auth/login',
            'method' => 'POST',
            'data' => $config['teacher_credentials'],
            'expected_status' => 200
        ],
        [
            'name' => 'Student Login',
            'endpoint' => '/api/auth/login',
            'method' => 'POST',
            'data' => $config['student_credentials'],
            'expected_status' => 200
        ],
        [
            'name' => 'Invalid Login',
            'endpoint' => '/api/auth/login',
            'method' => 'POST',
            'data' => ['email' => 'invalid@example.com', 'password' => 'wrong'],
            'expected_status' => 401
        ]
    ],

    // Subjects tests
    'subjects' => [
        [
            'name' => 'List Subjects',
            'endpoint' => '/api/subjects',
            'method' => 'GET',
            'auth' => 'admin',
            'expected_status' => 200
        ],
        [
            'name' => 'Create Subject (Admin)',
            'endpoint' => '/api/subjects',
            'method' => 'POST',
            'auth' => 'admin',
            'data' => [
                'name' => 'Test Subject',
                'description' => 'Test Description'
            ],
            'expected_status' => 201
        ],
        [
            'name' => 'Create Subject (Teacher - Should Fail)',
            'endpoint' => '/api/subjects',
            'method' => 'POST',
            'auth' => 'teacher',
            'data' => [
                'name' => 'Test Subject',
                'description' => 'Test Description'
            ],
            'expected_status' => 403
        ]
    ],

    // Materials tests
    'materials' => [
        [
            'name' => 'List Materials',
            'endpoint' => '/api/materials',
            'method' => 'GET',
            'auth' => 'student',
            'expected_status' => 200
        ],
        [
            'name' => 'Upload Material (Teacher)',
            'endpoint' => '/api/materials',
            'method' => 'POST',
            'auth' => 'teacher',
            'multipart' => true,
            'data' => [
                'title' => 'Test Material',
                'description' => 'Test Description',
                'subject_id' => 1,
                'file' => [
                    'name' => 'test.pdf',
                    'type' => 'application/pdf',
                    'content' => 'Test content'
                ]
            ],
            'expected_status' => 201
        ]
    ],

    // Quizzes tests
    'quizzes' => [
        [
            'name' => 'List Quizzes',
            'endpoint' => '/api/quizzes',
            'method' => 'GET',
            'auth' => 'student',
            'expected_status' => 200
        ],
        [
            'name' => 'Create Quiz (Teacher)',
            'endpoint' => '/api/quizzes',
            'method' => 'POST',
            'auth' => 'teacher',
            'data' => [
                'title' => 'Test Quiz',
                'description' => 'Test Description',
                'subject_id' => 1,
                'questions' => [
                    [
                        'text' => 'Test Question',
                        'type' => 'multiple_choice',
                        'options' => ['A', 'B', 'C', 'D'],
                        'correct_answer' => 'A'
                    ]
                ]
            ],
            'expected_status' => 201
        ]
    ],

    // Assignments tests
    'assignments' => [
        [
            'name' => 'List Assignments',
            'endpoint' => '/api/assignments',
            'method' => 'GET',
            'auth' => 'student',
            'expected_status' => 200
        ],
        [
            'name' => 'Create Assignment (Teacher)',
            'endpoint' => '/api/assignments',
            'method' => 'POST',
            'auth' => 'teacher',
            'multipart' => true,
            'data' => [
                'title' => 'Test Assignment',
                'description' => 'Test Description',
                'subject_id' => 1,
                'due_date' => date('Y-m-d H:i:s', strtotime('+1 week')),
                'file' => [
                    'name' => 'test.pdf',
                    'type' => 'application/pdf',
                    'content' => 'Test content'
                ]
            ],
            'expected_status' => 201
        ]
    ]
];

// Function to make HTTP requests
function makeRequest($url, $method = 'GET', $data = null, $headers = [], $multipart = false) {
    $ch = curl_init();
    
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
    
    if ($data) {
        if ($multipart) {
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        } else {
            curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
            $headers[] = 'Content-Type: application/json';
        }
    }
    
    if (!empty($headers)) {
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    }
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    
    curl_close($ch);
    
    return [
        'response' => json_decode($response, true),
        'status' => $httpCode
    ];
}

// Run tests
echo "Starting API Tests...\n\n";

foreach ($tests as $group => $groupTests) {
    echo "Testing $group endpoints:\n";
    echo str_repeat('-', 80) . "\n";
    
    foreach ($groupTests as $test) {
        echo "  {$test['name']}... ";
        
        $url = $config['base_url'] . $test['endpoint'];
        $headers = [];
        
        // Add authentication token if required
        if (isset($test['auth'])) {
            if (!isset($tokens[$test['auth']])) {
                // Get token first
                $credentials = $config["{$test['auth']}_credentials"];
                $loginResponse = makeRequest(
                    $config['base_url'] . '/api/auth/login',
                    'POST',
                    $credentials
                );
                if ($loginResponse['status'] === 200 && isset($loginResponse['response']['data']['token'])) {
                    $tokens[$test['auth']] = $loginResponse['response']['data']['token'];
                }
            }
            
            if (isset($tokens[$test['auth']])) {
                $headers[] = 'Authorization: Bearer ' . $tokens[$test['auth']];
            }
        }
        
        // Make request
        $response = makeRequest(
            $url,
            $test['method'],
            isset($test['data']) ? $test['data'] : null,
            $headers,
            isset($test['multipart']) ? $test['multipart'] : false
        );
        
        // Check result
        if ($response['status'] === $test['expected_status']) {
            echo "\033[32mPASSED\033[0m\n";
        } else {
            echo "\033[31mFAILED\033[0m\n";
            echo "    Expected status {$test['expected_status']}, got {$response['status']}\n";
            if (isset($response['response']['error'])) {
                echo "    Error: {$response['response']['error']}\n";
            }
        }
    }
    
    echo "\n";
}

echo "Tests completed.\n";