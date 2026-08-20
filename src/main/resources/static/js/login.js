/* login.js — login.html */

document.addEventListener('DOMContentLoaded', function () {
  // Si ya hay sesión, ir al dashboard
  if (getToken() && getUsuario()) {
    window.location.href = 'dashboard';
    return;
  }

  const form = document.getElementById('loginForm');
  const email = document.getElementById('email');
  const pass = document.getElementById('password');
  const btn = document.getElementById('btnLogin');
  const spinner = document.getElementById('spinner');

  // Toggle mostrar/ocultar contraseña
  const btnToggle = document.getElementById('btnTogglePass');
  const iconToggle = document.getElementById('iconTogglePass');
  btnToggle.addEventListener('click', function () {
    const esPass = pass.type === 'password';
    pass.type = esPass ? 'text' : 'password';
    iconToggle.className = esPass ? 'bi bi-eye-slash' : 'bi bi-eye';
  });

  form.addEventListener('submit', async function (e) {
    e.preventDefault();
    limpiarErrorForm();

    if (!email.value.trim() || !pass.value) {
      mostrarErrorForm('Por favor ingrese correo y contraseña.');
      return;
    }

    btn.disabled = true;
    spinner.classList.remove('d-none');

    try {
      const resp = await fetch(API_BASE + '/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.value.trim(), password: pass.value })
      });

      if (resp.ok) {
        const data = await resp.json();
        guardarSesion(data);
        window.location.href = 'dashboard';
        return;
      }

      // 401 / 400 / 403: mostrar mensaje del backend
      const err = await parsearErrorApi(resp);
      mostrarErrorForm(err.mensaje || 'Credenciales incorrectas.');
    } catch (e) {
      mostrarErrorForm('No se pudo conectar con el servidor. Intente nuevamente.');
    } finally {
      btn.disabled = false;
      spinner.classList.add('d-none');
    }
  });
});