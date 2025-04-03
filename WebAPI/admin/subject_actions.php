<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../core/DB.php';
require_once __DIR__ . '/../core/helpers.php';

session_start();

// Check if user is logged in and is admin
if (!isset($_SESSION['user']) || $_SESSION['user']['role'] !== 'admin') {
    header('Location: login.php');
    exit();
}

$db = DB::getInstance();

try {
    if (!isset($_POST['action'])) {
        throw new Exception('No action specified');
    }

    switch ($_POST['action']) {
        case 'add':
            handleAdd($db);
            break;
        case 'edit':
            handleEdit($db);
            break;
        case 'delete':
            handleDelete($db);
            break;
        default:
            throw new Exception('Invalid action');
    }
} catch (Exception $e) {
    $_SESSION['message'] = 'Error: ' . $e->getMessage();
    $_SESSION['message_type'] = 'danger';
    header('Location: subjects.php');
    exit();
}

function handleAdd($db) {
    // Validate input
    if (!isset($_POST['name']) || empty(trim($_POST['name']))) {
        throw new Exception('Subject name is required');
    }

    $name = sanitizeInput($_POST['name']);
    $description = isset($_POST['description']) ? sanitizeInput($_POST['description']) : '';

    // Check if subject with same name exists
    $existing = $db->fetch(
        "SELECT subject_id FROM subjects WHERE name = ?",
        [$name]
    );

    if ($existing) {
        throw new Exception('A subject with this name already exists');
    }

    // Insert new subject
    try {
        $db->insert('subjects', [
            'name' => $name,
            'description' => $description
        ]);

        $_SESSION['message'] = 'Subject added successfully';
        $_SESSION['message_type'] = 'success';
    } catch (Exception $e) {
        logError('Add Subject Error: ' . $e->getMessage());
        throw new Exception('Failed to add subject');
    }

    header('Location: subjects.php');
    exit();
}

function handleEdit($db) {
    // Validate input
    if (!isset($_POST['subject_id']) || !isset($_POST['name']) || empty(trim($_POST['name']))) {
        throw new Exception('Subject ID and name are required');
    }

    $subjectId = filter_var($_POST['subject_id'], FILTER_VALIDATE_INT);
    if (!$subjectId) {
        throw new Exception('Invalid subject ID');
    }

    $name = sanitizeInput($_POST['name']);
    $description = isset($_POST['description']) ? sanitizeInput($_POST['description']) : '';

    // Check if subject exists
    $subject = $db->fetch(
        "SELECT subject_id FROM subjects WHERE subject_id = ?",
        [$subjectId]
    );

    if (!$subject) {
        throw new Exception('Subject not found');
    }

    // Check if another subject has this name
    $existing = $db->fetch(
        "SELECT subject_id FROM subjects WHERE name = ? AND subject_id != ?",
        [$name, $subjectId]
    );

    if ($existing) {
        throw new Exception('Another subject with this name already exists');
    }

    // Update subject
    try {
        $db->update('subjects', [
            'name' => $name,
            'description' => $description
        ], 'subject_id = ?', [$subjectId]);

        $_SESSION['message'] = 'Subject updated successfully';
        $_SESSION['message_type'] = 'success';
    } catch (Exception $e) {
        logError('Edit Subject Error: ' . $e->getMessage());
        throw new Exception('Failed to update subject');
    }

    header('Location: subjects.php');
    exit();
}

function handleDelete($db) {
    // Validate input
    if (!isset($_POST['subject_id'])) {
        throw new Exception('Subject ID is required');
    }

    $subjectId = filter_var($_POST['subject_id'], FILTER_VALIDATE_INT);
    if (!$subjectId) {
        throw new Exception('Invalid subject ID');
    }

    // Check if subject exists
    $subject = $db->fetch(
        "SELECT subject_id FROM subjects WHERE subject_id = ?",
        [$subjectId]
    );

    if (!$subject) {
        throw new Exception('Subject not found');
    }

    // Check if subject has any related content
    $contentCounts = $db->fetch(
        "SELECT 
            (SELECT COUNT(*) FROM materials WHERE subject_id = ?) as material_count,
            (SELECT COUNT(*) FROM quizzes WHERE subject_id = ?) as quiz_count,
            (SELECT COUNT(*) FROM assignments WHERE subject_id = ?) as assignment_count",
        [$subjectId, $subjectId, $subjectId]
    );

    if ($contentCounts['material_count'] > 0 || 
        $contentCounts['quiz_count'] > 0 || 
        $contentCounts['assignment_count'] > 0) {
        throw new Exception('Cannot delete subject with existing content. Remove all related materials, quizzes, and assignments first.');
    }

    // Delete subject
    try {
        $db->delete('subjects', 'subject_id = ?', [$subjectId]);

        $_SESSION['message'] = 'Subject deleted successfully';
        $_SESSION['message_type'] = 'success';
    } catch (Exception $e) {
        logError('Delete Subject Error: ' . $e->getMessage());
        throw new Exception('Failed to delete subject');
    }

    header('Location: subjects.php');
    exit();
}

// Helper function to validate and sanitize input
function sanitizeInput($input) {
    $input = trim($input);
    $input = stripslashes($input);
    $input = htmlspecialchars($input, ENT_QUOTES, 'UTF-8');
    return $input;
}