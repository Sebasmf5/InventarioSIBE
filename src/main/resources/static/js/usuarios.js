/* usuarios.js — usuarios.html (solo SUPERVISOR) */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requerirRol('SUPERVISOR');
  if (!usuario) return;
  renderNavbar(usuario);

  const tbody = document.getElementById('tablaUsuarios');
  const modalEl = document.getElementById('modalUsuario');
  const modal = new bootstrap.Modal(modalEl);
  const btnGuardar = document.getElementById('btnGuardar');

  let usuarios = [];

  async function cargar() {
    tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">Cargando…</td></tr>';
    const resp = await authFetch(API_BASE + '/usuarios');
    if (!resp || !resp.ok) { mostrarErrorGlobal('No se pudo cargar la lista de usuarios.'); return; }
    usuarios = await resp.json();
    if (!usuarios.length) {
      tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">No hay usuarios.</td></tr>';
      return;
    }
    tbody.innerHTML = usuarios.map(function (u) {
      const estadoBadge = u.activo
        ? '<span class="badge badge-sem badge-activo">Activo</span>'
        : '<span class="badge badge-sem badge-inactivo">Inactivo</span>';
      const rolLabel = u.rol === 'SUPERVISOR' ? 'Supervisor' : 'Enfermería';
      return '<tr>' +
        '<td><strong>' + escapeHtml(u.nombre) + '</strong></td>' +
        '<td>' + escapeHtml(u.email) + '</td>' +
        '<td><span class="badge bg-light text-teal">' + escapeHtml(rolLabel) + '</span></td>' +
        '<td>' + estadoBadge + '</td>' +
        '</tr>';
    }).join('');
  }
  cargar();

  document.getElementById('btnNuevo').addEventListener('click', function () {
    document.getElementById('usuarioForm').reset();
    document.getElementById('uActivo').checked = true;
    document.getElementById('uRol').value = 'ENFERMERIA';
    limpiarErrorForm();
    document.getElementById('modalTitulo').textContent = 'Nuevo usuario';
    modal.show();
  });

  btnGuardar.addEventListener('click', async function () {
    limpiarErrorForm();
    const payload = {
      nombre: document.getElementById('uNombre').value.trim(),
      email: document.getElementById('uEmail').value.trim(),
      password: document.getElementById('uPassword').value,
      rol: document.getElementById('uRol').value,
      activo: document.getElementById('uActivo').checked
    };
    if (!payload.nombre || !payload.email || !payload.password) {
      mostrarErrorForm('Complete todos los campos obligatorios.');
      return;
    }
    btnGuardar.disabled = true;
    try {
      const resp = await authFetch(API_BASE + '/usuarios', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!resp) return;
      if (resp.ok || resp.status === 201) {
        modal.hide();
        await cargar();
        document.getElementById('toastExito').innerHTML =
          '<div class="alert alert-success alert-dismissible fade show" role="alert">Usuario creado.' +
          '<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>';
        return;
      }
      const err = await parsearErrorApi(resp);
      mostrarErrorForm(err.mensaje, err.campos);
    } finally {
      btnGuardar.disabled = false;
    }
  });
});