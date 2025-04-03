<?php
session_start();

// Check if user is logged in and is admin
if (!isset($_SESSION['user']) || $_SESSION['user']['role'] !== 'admin') {
    header('Location: login.php');
    exit();
}

// Get current page for navigation highlighting
$currentPage = basename($_SERVER['PHP_SELF'], '.php');
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo ucfirst($currentPage); ?> - SmartApp Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .sidebar {
            min-height: 100vh;
            background-color: #343a40;
        }
        .sidebar .nav-link {
            color: #fff;
            padding: 1rem;
            display: flex;
            align-items: center;
            transition: all 0.3s ease;
        }
        .sidebar .nav-link i {
            margin-right: 10px;
            width: 20px;
            text-align: center;
        }
        .sidebar .nav-link:hover {
            background-color: rgba(255,255,255,0.1);
            padding-left: 1.5rem;
        }
        .sidebar .nav-link.active {
            background-color: #007bff;
            border-radius: 4px;
        }
        .content {
            padding: 20px;
        }
        .btn-circle {
            width: 30px;
            height: 30px;
            padding: 6px 0px;
            border-radius: 15px;
            text-align: center;
            font-size: 12px;
            line-height: 1.42857;
        }
        .admin-header {
            background-color: #fff;
            border-bottom: 1px solid #dee2e6;
            padding: 1rem;
            margin-bottom: 2rem;
        }
        .admin-header h1 {
            margin: 0;
            font-size: 1.5rem;
        }
        .table th {
            background-color: #f8f9fa;
        }
        .modal-header {
            background-color: #f8f9fa;
        }
        .badge {
            font-size: 0.875rem;
        }
        .loading {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(255,255,255,0.8);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 9999;
            display: none;
        }
    </style>
</head>
<body>
    <!-- Loading Overlay -->
    <div class="loading">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
        </div>
    </div>

    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <nav class="col-md-2 d-none d-md-block sidebar">
                <div class="position-sticky">
                    <div class="py-4 px-3 mb-4 bg-dark">
                        <div class="text-white">
                            <h5>SmartApp Admin</h5>
                            <p class="mb-0"><?php echo htmlspecialchars($_SESSION['user']['full_name']); ?></p>
                        </div>
                    </div>
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link <?php echo $currentPage === 'subjects' ? 'active' : ''; ?>" href="subjects.php">
                                <i class="fas fa-book"></i>
                                Subjects
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link <?php echo $currentPage === 'materials' ? 'active' : ''; ?>" href="materials.php">
                                <i class="fas fa-file-alt"></i>
                                Materials
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link <?php echo $currentPage === 'quizzes' ? 'active' : ''; ?>" href="quizzes.php">
                                <i class="fas fa-question-circle"></i>
                                Quizzes
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link <?php echo $currentPage === 'assignments' ? 'active' : ''; ?>" href="assignments.php">
                                <i class="fas fa-tasks"></i>
                                Assignments
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link <?php echo $currentPage === 'users' ? 'active' : ''; ?>" href="users.php">
                                <i class="fas fa-users"></i>
                                Users
                            </a>
                        </li>
                        <li class="nav-item mt-4">
                            <a class="nav-link text-danger" href="logout.php" onclick="return confirm('Are you sure you want to logout?')">
                                <i class="fas fa-sign-out-alt"></i>
                                Logout
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>

            <!-- Main content -->
            <main class="col-md-10 ms-sm-auto px-md-4">
                <!-- Page header -->
                <div class="admin-header d-flex justify-content-between align-items-center">
                    <h1><?php echo ucfirst($currentPage); ?></h1>
                    <nav aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="subjects.php">Dashboard</a></li>
                            <li class="breadcrumb-item active" aria-current="page"><?php echo ucfirst($currentPage); ?></li>
                        </ol>
                    </nav>
                </div>

                <?php if (isset($_SESSION['message'])): ?>
                    <div class="alert alert-<?php echo $_SESSION['message_type']; ?> alert-dismissible fade show" role="alert">
                        <?php 
                            echo $_SESSION['message'];
                            unset($_SESSION['message']);
                            unset($_SESSION['message_type']);
                        ?>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                <?php endif; ?>

                <!-- Content starts here -->