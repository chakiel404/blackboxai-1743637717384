<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../core/DB.php';

try {
    // Create database connection without selecting a database
    $dsn = "mysql:host=" . DB_HOST . ";charset=" . DB_CHARSET;
    $options = [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ];
    $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);

    echo "Connected to MySQL server successfully.\n\n";

    // Create database if it doesn't exist
    $sql = "CREATE DATABASE IF NOT EXISTS " . DB_NAME . " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
    $pdo->exec($sql);
    echo "Database '" . DB_NAME . "' created or already exists.\n\n";

    // Select the database
    $pdo->exec("USE " . DB_NAME);

    // Read and execute schema.sql
    echo "Creating database schema...\n";
    $schema = file_get_contents(__DIR__ . '/schema.sql');
    $pdo->exec($schema);
    echo "Database schema created successfully.\n\n";

    // Read and execute init_data.sql
    echo "Loading initial data...\n";
    $initData = file_get_contents(__DIR__ . '/init_data.sql');
    $pdo->exec($initData);
    echo "Initial data loaded successfully.\n\n";

    // Create upload directories
    $uploadDirs = [
        __DIR__ . '/../uploads',
        __DIR__ . '/../uploads/materials',
        __DIR__ . '/../uploads/assignments'
    ];

    foreach ($uploadDirs as $dir) {
        if (!file_exists($dir)) {
            if (mkdir($dir, 0755, true)) {
                echo "Created directory: $dir\n";
            } else {
                echo "Failed to create directory: $dir\n";
            }
        } else {
            echo "Directory already exists: $dir\n";
        }
    }

    // Create sample files for materials and assignments
    $sampleFiles = [
        '/uploads/materials/algebra_intro.pdf' => 'Sample algebra introduction content',
        '/uploads/materials/newton_laws.pdf' => 'Sample Newton\'s laws content',
        '/uploads/materials/periodic_table.pdf' => 'Sample periodic table content',
        '/uploads/materials/cell_biology.pdf' => 'Sample cell biology content',
        '/uploads/materials/programming_basics.pdf' => 'Sample programming basics content',
        '/uploads/assignments/student1_algebra.pdf' => 'Sample student 1 algebra homework',
        '/uploads/assignments/student2_physics.pdf' => 'Sample student 2 physics lab report',
        '/uploads/assignments/student3_chemistry.pdf' => 'Sample student 3 chemistry research'
    ];

    foreach ($sampleFiles as $file => $content) {
        $fullPath = __DIR__ . '/..' . $file;
        if (!file_exists($fullPath)) {
            if (file_put_contents($fullPath, $content)) {
                echo "Created sample file: $file\n";
            } else {
                echo "Failed to create sample file: $file\n";
            }
        } else {
            echo "Sample file already exists: $file\n";
        }
    }

    echo "\nDatabase initialization completed successfully!\n";
    echo "You can now log in with the following credentials:\n\n";
    echo "Admin:\n";
    echo "Email: admin@smartapp.com\n";
    echo "Password: admin123\n\n";
    echo "Teacher:\n";
    echo "Email: john.doe@smartapp.com\n";
    echo "Password: teacher123\n\n";
    echo "Student:\n";
    echo "Email: student1@smartapp.com\n";
    echo "Password: student123\n";

} catch (PDOException $e) {
    die("Database Error: " . $e->getMessage() . "\n");
} catch (Exception $e) {
    die("Error: " . $e->getMessage() . "\n");
}

function executeMultipleQueries($pdo, $sql) {
    $queries = explode(';', $sql);
    foreach ($queries as $query) {
        $query = trim($query);
        if (!empty($query)) {
            try {
                $pdo->exec($query);
            } catch (PDOException $e) {
                echo "Error executing query: " . $query . "\n";
                echo "Error message: " . $e->getMessage() . "\n\n";
            }
        }
    }
}