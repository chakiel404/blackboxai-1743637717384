<?php
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../core/DB.php';
require_once __DIR__ . '/../core/helpers.php';
require_once 'includes/header.php';

$db = DB::getInstance();

// Get all subjects with their content counts
$query = "SELECT s.*, 
            (SELECT COUNT(*) FROM materials WHERE subject_id = s.subject_id) as material_count,
            (SELECT COUNT(*) FROM quizzes WHERE subject_id = s.subject_id) as quiz_count,
            (SELECT COUNT(*) FROM assignments WHERE subject_id = s.subject_id) as assignment_count
         FROM subjects s
         ORDER BY s.name ASC";

$subjects = $db->fetchAll($query);
?>

<div class="content">
    <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pb-2 mb-3">
        <h2>Manage Subjects</h2>
        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addSubjectModal">
            <i class="fas fa-plus me-2"></i>Add Subject
        </button>
    </div>

    <div class="card">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Description</th>
                            <th class="text-center">Materials</th>
                            <th class="text-center">Quizzes</th>
                            <th class="text-center">Assignments</th>
                            <th>Created At</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($subjects as $subject): ?>
                        <tr>
                            <td><?php echo $subject['subject_id']; ?></td>
                            <td><?php echo htmlspecialchars($subject['name']); ?></td>
                            <td><?php echo htmlspecialchars($subject['description']); ?></td>
                            <td class="text-center">
                                <span class="badge bg-info">
                                    <?php echo $subject['material_count']; ?>
                                </span>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-warning">
                                    <?php echo $subject['quiz_count']; ?>
                                </span>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-success">
                                    <?php echo $subject['assignment_count']; ?>
                                </span>
                            </td>
                            <td><?php echo date('Y-m-d H:i', strtotime($subject['created_at'])); ?></td>
                            <td>
                                <div class="btn-group">
                                    <button type="button" 
                                            class="btn btn-sm btn-primary" 
                                            onclick="editSubject(<?php echo $subject['subject_id']; ?>)"
                                            data-bs-toggle="tooltip"
                                            title="Edit Subject">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <?php if ($subject['material_count'] == 0 && 
                                            $subject['quiz_count'] == 0 && 
                                            $subject['assignment_count'] == 0): ?>
                                        <button type="button" 
                                                class="btn btn-sm btn-danger" 
                                                onclick="deleteSubject(<?php echo $subject['subject_id']; ?>)"
                                                data-bs-toggle="tooltip"
                                                title="Delete Subject">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    <?php else: ?>
                                        <button type="button" 
                                                class="btn btn-sm btn-danger" 
                                                disabled
                                                data-bs-toggle="tooltip"
                                                title="Cannot delete subject with existing content">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    <?php endif; ?>
                                </div>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                        <?php if (empty($subjects)): ?>
                        <tr>
                            <td colspan="8" class="text-center">No subjects found</td>
                        </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<!-- Add Subject Modal -->
<div class="modal fade" id="addSubjectModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Add New Subject</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form id="addSubjectForm" action="subject_actions.php" method="POST" class="needs-validation" novalidate>
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="name" class="form-label">Subject Name</label>
                        <input type="text" 
                               class="form-control" 
                               id="name" 
                               name="name" 
                               required 
                               minlength="2" 
                               maxlength="255">
                        <div class="invalid-feedback">
                            Please enter a subject name (2-255 characters)
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="description" class="form-label">Description</label>
                        <textarea class="form-control" 
                                  id="description" 
                                  name="description" 
                                  rows="3"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary" name="action" value="add">
                        <i class="fas fa-plus me-2"></i>Add Subject
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Edit Subject Modal -->
<div class="modal fade" id="editSubjectModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Edit Subject</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form id="editSubjectForm" action="subject_actions.php" method="POST" class="needs-validation" novalidate>
                <input type="hidden" name="subject_id" id="edit_subject_id">
                <div class="modal-body">
                    <div class="mb-3">
                        <label for="edit_name" class="form-label">Subject Name</label>
                        <input type="text" 
                               class="form-control" 
                               id="edit_name" 
                               name="name" 
                               required 
                               minlength="2" 
                               maxlength="255">
                        <div class="invalid-feedback">
                            Please enter a subject name (2-255 characters)
                        </div>
                    </div>
                    <div class="mb-3">
                        <label for="edit_description" class="form-label">Description</label>
                        <textarea class="form-control" 
                                  id="edit_description" 
                                  name="description" 
                                  rows="3"></textarea>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary" name="action" value="edit">
                        <i class="fas fa-save me-2"></i>Save Changes
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Delete Subject Modal -->
<div class="modal fade" id="deleteSubjectModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Delete Subject</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p>Are you sure you want to delete this subject? This action cannot be undone.</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <form action="subject_actions.php" method="POST" style="display: inline;">
                    <input type="hidden" name="subject_id" id="delete_subject_id">
                    <button type="submit" class="btn btn-danger" name="action" value="delete">
                        <i class="fas fa-trash me-2"></i>Delete Subject
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    function editSubject(id) {
        // Fetch subject details using API
        fetch(`../api/subjects/${id}`)
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    document.getElementById('edit_subject_id').value = id;
                    document.getElementById('edit_name').value = data.subject.name;
                    document.getElementById('edit_description').value = data.subject.description;
                    new bootstrap.Modal(document.getElementById('editSubjectModal')).show();
                } else {
                    alert('Failed to fetch subject details: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to fetch subject details');
            });
    }

    function deleteSubject(id) {
        document.getElementById('delete_subject_id').value = id;
        new bootstrap.Modal(document.getElementById('deleteSubjectModal')).show();
    }
</script>

<?php require_once 'includes/footer.php'; ?>