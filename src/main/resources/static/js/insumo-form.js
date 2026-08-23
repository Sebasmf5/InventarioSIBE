/* insumo-form.js — crear/editar insumo (detecta ?id=) */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  const params = parametrosUrl();
  const idEdicion = params.id || null;
  document.getElementById('tituloForm').textContent = idEdicion ? 'Editar insumo' : 'Nuevo insumo';

  const form = document.getElementById('insumoForm');
  const btn = document.getElementById('btnGuardar');
  const spinner = document.getElementById('spinner');

  // Si es edición, precargar
  if (idEdicion) {
    const resp = await authFetch(API_BASE + '/insumos/' + idEdicion);
    if (!resp || !resp.ok) {
      const err = resp ? await parsearErrorApi(resp) : { mensaje: 'No se pudo cargar el insumo.' };
      mostrarErrorGlobal(err.mensaje);
      return;
    }
    const ins = await resp.json();
    document.getElementById('nombre').value = ins.nombre || '';
    document.getElementById('presentacion').value = ins.presentacion || '';
    document.getElementById('unidadMedida').value = ins.unidadMedida || '';
    document.getElementById('stockMinimo').value = ins.stockMinimo;
    document.getElementById('marca').value = ins.marca || '';
    document.getElementById('tipo').value = ins.tipo || 'MEDICAMENTO';
    document.getElementById('registroInvima').value = ins.registroInvima || '';
    document.getElementById('activo').checked = !!ins.activo;
    document.getElementById('unidadesPorCaja').value = ins.unidadesPorCaja || 1;
  }

  form.addEventListener('submit', async function (e) {
    e.preventDefault();
    limpiarErrorForm();

    const payload = {
      nombre: document.getElementById('nombre').value.trim(),
      presentacion: document.getElementById('presentacion').value.trim(),
      unidadMedida: document.getElementById('unidadMedida').value.trim(),
      stockMinimo: parseInt(document.getElementById('stockMinimo').value, 10) || 0,
      activo: document.getElementById('activo').checked,
      marca: document.getElementById('marca').value.trim(),
      tipo: document.getElementById('tipo').value,
      registroInvima: document.getElementById('registroInvima').value.trim() || null,
      unidadesPorCaja: parseInt(document.getElementById('unidadesPorCaja').value, 10) || 1
    };

    btn.disabled = true;
    spinner.classList.remove('d-none');
    try {
      const opts = { method: idEdicion ? 'PUT' : 'POST',
                     headers: { 'Content-Type': 'application/json' },
                     body: JSON.stringify(payload) };
      const url = idEdicion ? API_BASE + '/insumos/' + idEdicion : API_BASE + '/insumos';
      const resp = await authFetch(url, opts);
      if (!resp) return;

      if (resp.ok || resp.status === 201) {
        marcarToast('toast_inventario', idEdicion ? 'Insumo actualizado.' : 'Insumo creado.');
        window.location.href = 'inventario';
        return;
      }
      const err = await parsearErrorApi(resp);
      mostrarErrorForm(err.mensaje, err.campos);
    } finally {
      btn.disabled = false;
      spinner.classList.add('d-none');
    }
  });
});