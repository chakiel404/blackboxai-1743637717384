<!-- Content ends here -->
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Show loading overlay during AJAX requests
        document.addEventListener('DOMContentLoaded', function() {
            const loadingOverlay = document.querySelector('.loading');
            
            // Show loading on form submissions
            document.querySelectorAll('form').forEach(form => {
                form.addEventListener('submit', function() {
                    loadingOverlay.style.display = 'flex';
                });
            });

            // Show loading on AJAX requests
            let originalFetch = window.fetch;
            window.fetch = function() {
                loadingOverlay.style.display = 'flex';
                return originalFetch.apply(this, arguments)
                    .finally(() => {
                        loadingOverlay.style.display = 'none';
                    });
            };
        });

        // Prevent double form submission
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', function(e) {
                if (form.classList.contains('submitted')) {
                    e.preventDefault();
                } else {
                    form.classList.add('submitted');
                    // Re-enable form after 5 seconds in case submission fails
                    setTimeout(() => form.classList.remove('submitted'), 5000);
                }
            });
        });

        // Initialize tooltips
        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });

        // Initialize popovers
        var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });

        // Auto-hide alerts after 5 seconds
        document.querySelectorAll('.alert').forEach(alert => {
            setTimeout(() => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }, 5000);
        });

        // Confirm dangerous actions
        document.querySelectorAll('[data-confirm]').forEach(element => {
            element.addEventListener('click', function(e) {
                if (!confirm(this.dataset.confirm || 'Are you sure?')) {
                    e.preventDefault();
                }
            });
        });

        // Handle file input display
        document.querySelectorAll('.custom-file-input').forEach(input => {
            input.addEventListener('change', function(e) {
                let fileName = e.target.files[0].name;
                let label = e.target.nextElementSibling;
                label.textContent = fileName;
            });
        });

        // Format file sizes
        function formatFileSize(bytes) {
            if (bytes === 0) return '0 Bytes';
            const k = 1024;
            const sizes = ['Bytes', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
        }

        // Format dates
        function formatDate(dateString) {
            const options = { 
                year: 'numeric', 
                month: 'short', 
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            };
            return new Date(dateString).toLocaleDateString('en-US', options);
        }

        // Handle sidebar responsiveness
        function toggleSidebar() {
            const sidebar = document.querySelector('.sidebar');
            const content = document.querySelector('.content');
            
            if (window.innerWidth < 768) {
                sidebar.classList.add('d-none');
                content.classList.remove('ms-sm-auto');
            } else {
                sidebar.classList.remove('d-none');
                content.classList.add('ms-sm-auto');
            }
        }

        // Initial call and event listener for window resize
        window.addEventListener('resize', toggleSidebar);
        toggleSidebar();

        // Add active class to current nav item
        document.querySelectorAll('.nav-link').forEach(link => {
            if (link.getAttribute('href') === window.location.pathname.split('/').pop()) {
                link.classList.add('active');
            }
        });

        // Handle form validation
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', function(e) {
                if (!form.checkValidity()) {
                    e.preventDefault();
                    e.stopPropagation();
                }
                form.classList.add('was-validated');
            });
        });

        // Global AJAX error handler
        window.addEventListener('unhandledrejection', function(event) {
            console.error('Unhandled promise rejection:', event.reason);
            alert('An error occurred. Please try again later.');
        });

        // Handle session timeout
        let sessionTimeout;
        function resetSessionTimeout() {
            clearTimeout(sessionTimeout);
            sessionTimeout = setTimeout(() => {
                alert('Your session has expired. Please login again.');
                window.location.href = 'logout.php';
            }, 30 * 60 * 1000); // 30 minutes
        }

        // Reset timeout on user activity
        ['click', 'keypress', 'scroll', 'mousemove'].forEach(event => {
            document.addEventListener(event, resetSessionTimeout);
        });
        resetSessionTimeout();
    </script>
</body>
</html>