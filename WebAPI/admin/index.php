<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../core/DB.php';
require_once __DIR__ . '/../core/helpers.php';
require_once 'includes/header.php';

$db = DB::getInstance();

// Get statistics
$stats = $db->fetch("SELECT 
    (SELECT COUNT(*) FROM subjects) as subject_count,
    (SELECT COUNT(*) FROM materials) as material_count,
    (SELECT COUNT(*) FROM quizzes) as quiz_count,
    (SELECT COUNT(*) FROM assignments) as assignment_count,
    (SELECT COUNT(*) FROM users WHERE role = 'guru') as teacher_count,
    (SELECT COUNT(*) FROM users WHERE role = 'siswa') as student_count");

// Get recent activities
$recentMaterials = $db->fetchAll(
    "SELECT m.*, s.name as subject_name, u.full_name as uploaded_by_name 
     FROM materials m 
     JOIN subjects s ON m.subject_id = s.subject_id 
     JOIN users u ON m.uploaded_by = u.user_id 
     ORDER BY m.created_at DESC LIMIT 5"
);

$recentQuizzes = $db->fetchAll(
    "SELECT q.*, s.name as subject_name, u.full_name as created_by_name 
     FROM quizzes q 
     JOIN subjects s ON q.subject_id = s.subject_id 
     JOIN users u ON q.created_by = u.user_id 
     ORDER BY q.created_at DESC LIMIT 5"
);

$recentAssignments = $db->fetchAll(
    "SELECT a.*, s.name as subject_name, u.full_name as created_by_name 
     FROM assignments a 
     JOIN subjects s ON a.subject_id = s.subject_id 
     JOIN users u ON a.created_by = u.user_id 
     ORDER BY a.created_at DESC LIMIT 5"
);
?>

<div class="content">
    <div class="row mb-4">
        <!-- Statistics Cards -->
        <div class="col-xl-2 col-md-4 mb-4">
            <div class="card border-left-primary h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Subjects</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><?php echo $stats['subject_count']; ?></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-book fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-2 col-md-4 mb-4">
            <div class="card border-left-success h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Materials</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><?php echo $stats['material_count']; ?></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-file-alt fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-2 col-md-4 mb-4">
            <div class="card border-left-info h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-info text-uppercase mb-1">Quizzes</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><?php echo $stats['quiz_count']; ?></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-question-circle fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-2 col-md-4 mb-4">
            <div class="card border-left-warning h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Assignments</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><?php echo $stats['assignment_count']; ?></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-tasks fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-2 col-md-4 mb-4">
            <div class="card border-left-primary h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Teachers</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><?php echo $stats['teacher_count']; ?></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-chalkboard-teacher fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-xl-2 col-md-4 mb-4">
            <div class="card border-left-success h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Students</div>
                            <div class="h5 mb-0 font-weight-bold text-gray-800"><?php echo $stats['student_count']; ?></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-user-graduate fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <!-- Recent Materials -->
        <div class="col-lg-4">
            <div class="card mb-4">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold">Recent Materials</h6>
                </div>
                <div class="card-body">
                    <div class="list-group list-group-flush">
                        <?php foreach ($recentMaterials as $material): ?>
                        <div class="list-group-item">
                            <div class="d-flex w-100 justify-content-between">
                                <h6 class="mb-1"><?php echo htmlspecialchars($material['title']); ?></h6>
                                <small class="text-muted"><?php echo date('M d', strtotime($material['created_at'])); ?></small>
                            </div>
                            <p class="mb-1"><?php echo htmlspecialchars($material['subject_name']); ?></p>
                            <small class="text-muted">By <?php echo htmlspecialchars($material['uploaded_by_name']); ?></small>
                        </div>
                        <?php endforeach; ?>
                        <?php if (empty($recentMaterials)): ?>
                        <div class="list-group-item">
                            <p class="mb-0 text-muted">No recent materials</p>
                        </div>
                        <?php endif; ?>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Quizzes -->
        <div class="col-lg-4">
            <div class="card mb-4">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold">Recent Quizzes</h6>
                </div>
                <div class="card-body">
                    <div class="list-group list-group-flush">
                        <?php foreach ($recentQuizzes as $quiz): ?>
                        <div class="list-group-item">
                            <div class="d-flex w-100 justify-content-between">
                                <h6 class="mb-1"><?php echo htmlspecialchars($quiz['title']); ?></h6>
                                <small class="text-muted"><?php echo date('M d', strtotime($quiz['created_at'])); ?></small>
                            </div>
                            <p class="mb-1"><?php echo htmlspecialchars($quiz['subject_name']); ?></p>
                            <small class="text-muted">By <?php echo htmlspecialchars($quiz['created_by_name']); ?></small>
                        </div>
                        <?php endforeach; ?>
                        <?php if (empty($recentQuizzes)): ?>
                        <div class="list-group-item">
                            <p class="mb-0 text-muted">No recent quizzes</p>
                        </div>
                        <?php endif; ?>
                    </div>
                </div>
            </div>
        </div>

        <!-- Recent Assignments -->
        <div class="col-lg-4">
            <div class="card mb-4">
                <div class="card-header">
                    <h6 class="m-0 font-weight-bold">Recent Assignments</h6>
                </div>
                <div class="card-body">
                    <div class="list-group list-group-flush">
                        <?php foreach ($recentAssignments as $assignment): ?>
                        <div class="list-group-item">
                            <div class="d-flex w-100 justify-content-between">
                                <h6 class="mb-1"><?php echo htmlspecialchars($assignment['title']); ?></h6>
                                <small class="text-muted"><?php echo date('M d', strtotime($assignment['created_at'])); ?></small>
                            </div>
                            <p class="mb-1"><?php echo htmlspecialchars($assignment['subject_name']); ?></p>
                            <small class="text-muted">By <?php echo htmlspecialchars($assignment['created_by_name']); ?></small>
                        </div>
                        <?php endforeach; ?>
                        <?php if (empty($recentAssignments)): ?>
                        <div class="list-group-item">
                            <p class="mb-0 text-muted">No recent assignments</p>
                        </div>
                        <?php endif; ?>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
.border-left-primary {
    border-left: 4px solid #4e73df !important;
}
.border-left-success {
    border-left: 4px solid #1cc88a !important;
}
.border-left-info {
    border-left: 4px solid #36b9cc !important;
}
.border-left-warning {
    border-left: 4px solid #f6c23e !important;
}
.card-header {
    background-color: #f8f9fc;
    border-bottom: 1px solid #e3e6f0;
}
.font-weight-bold {
    font-weight: 700 !important;
}
.text-xs {
    font-size: .7rem;
}
.text-gray-300 {
    color: #dddfeb !important;
}
.text-gray-800 {
    color: #5a5c69 !important;
}
</style>

<?php require_once 'includes/footer.php'; ?>