document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('.login-form');
    const usernameInput = form.querySelector('input[name="username"]');
    const passwordInput = form.querySelector('input[name="password"]');
    const clientError = document.getElementById('client-error');
    const serverError = document.getElementById('server-error');

    form.addEventListener('submit', function(e) {
        // Скрываем старые сообщения
        clientError.style.display = 'none';
        clientError.textContent = '';

        if (serverError) {
            serverError.style.display = 'none';
        }

        const errors = [];

        if (!usernameInput.value.trim() || !passwordInput.value.trim()) {
            errors.push('⚠️ Please fill out all fields');
        }

        if (errors.length > 0) {
            e.preventDefault();
            clientError.textContent = errors.join(' ');
            clientError.style.display = 'block';
        }
    });
});
