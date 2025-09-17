// Custom JavaScript for Scheduled Task Management System

document.addEventListener('DOMContentLoaded', function() {
    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert:not(.alert-danger)');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Confirm dangerous actions
    const dangerButtons = document.querySelectorAll('.btn-danger[type="submit"]');
    dangerButtons.forEach(function(button) {
        button.addEventListener('click', function(e) {
            if (!button.closest('.modal')) {
                if (!confirm('确定要执行此操作吗？')) {
                    e.preventDefault();
                    return false;
                }
            }
        });
    });

    // Add loading spinner to submit buttons
    const forms = document.querySelectorAll('form');
    forms.forEach(function(form) {
        form.addEventListener('submit', function() {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                const originalText = submitBtn.innerHTML;
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>处理中...';
                submitBtn.disabled = true;
                
                // Re-enable button after 10 seconds as fallback
                setTimeout(function() {
                    submitBtn.innerHTML = originalText;
                    submitBtn.disabled = false;
                }, 10000);
            }
        });
    });

    // Cron expression validation helper
    const cronInput = document.getElementById('cronExpression');
    if (cronInput) {
        cronInput.addEventListener('input', function() {
            validateCronExpression(this.value);
        });
    }

    // Format job data as JSON
    const jobDataTextarea = document.getElementById('jobData');
    if (jobDataTextarea) {
        jobDataTextarea.addEventListener('blur', function() {
            try {
                if (this.value.trim()) {
                    const parsed = JSON.parse(this.value);
                    this.value = JSON.stringify(parsed, null, 2);
                    this.classList.remove('is-invalid');
                }
            } catch (e) {
                this.classList.add('is-invalid');
            }
        });
    }
});

function validateCronExpression(expression) {
    const cronInput = document.getElementById('cronExpression');
    if (!cronInput) return;

    // Basic cron validation (6 or 7 parts for seconds precision)
    const parts = expression.trim().split(/\s+/);
    if (parts.length >= 6 && parts.length <= 7) {
        cronInput.classList.remove('is-invalid');
        cronInput.classList.add('is-valid');
    } else if (expression.trim()) {
        cronInput.classList.remove('is-valid');
        cronInput.classList.add('is-invalid');
    } else {
        cronInput.classList.remove('is-valid', 'is-invalid');
    }
}

// Utility function to refresh page data
function refreshData() {
    location.reload();
}

// Utility function to show toast notifications
function showToast(message, type = 'info') {
    // Create toast container if it doesn't exist
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }

    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-bg-${type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();

    // Remove toast element after it's hidden
    toast.addEventListener('hidden.bs.toast', function() {
        toast.remove();
    });
}