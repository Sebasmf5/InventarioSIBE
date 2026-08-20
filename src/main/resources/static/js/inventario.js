/* inventario.js — inventario.html */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  const toast = consumirToast('toast_inventario');
  if (toast) pintarToast(toast);

  const tbody = document.getElementById('tablaInsumos');
  let insumos = [];

  const respIns = await authFetch(API_BASE + '/insumos');
  if (!respIns || !respIns.ok) { mostrarErrorGlobal('No se pudo cargar el inventario.'); return; }
  insumos = await respIns.json();

  // Listener de filtros
  ['filtroNombre', 'filtroTipo', 'filtroEstado'].forEach(function (id) {
    document.getElementById(id).addEventListener('input', render);
    document.getElementById(id).addEventListener('change', render);
  });

  // El estado agregado del insumo (peor estado entre sus lotes) lo calcula
  // el backend y llega en ins.estadoInsumo (enum EstadoSemaforo o null).
  // Aquí solo decidimos las dos etiquetas que dependen de datos del propio
  // insumo: "Inactivo" (si ins.activo === false) y "Sin lotes" (si el
  // backend devolvió null porque el insumo no tiene lotes activos).
  function estadoInsumo(ins) {
    if (!ins.activo) return { estado: 'Inactivo', clase: 'badge-inactivo' };
    if (ins.estadoInsumo === null || ins.estadoInsumo === undefined) {
      return { estado: 'Sin lotes', clase: 'badge-agotado' };
    }
    const mapeo = MAPA_ESTADO_SEMAFORO[ins.estadoInsumo] || MAPA_ESTADO_SEMAFORO.AGOTADO;
    return { estado: mapeo.estado, clase: mapeo.clase };
  }

  function render() {
    const fNombre = (document.getElementById('filtroNombre').value || '').toLowerCase();
    const fTipo = document.getElementById('filtroTipo').value;
    const fEstado = document.getElementById('filtroEstado').value;

    const filtrados = insumos.filter(function (i) {
      if (fNombre && (i.nombre || '').toLowerCase().indexOf(fNombre) === -1) return false;
      if (fTipo && i.tipo !== fTipo) return false;
      if (fEstado === 'activo' && !i.activo) return false;
      if (fEstado === 'inactivo' && i.activo) return false;
      return true;
    });

    if (!filtrados.length) {
      tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">No hay insumos que coincidan.</td></tr>';
      return;
    }

    tbody.innerHTML = filtrados.map(function (i) {
      const est = estadoInsumo(i);
      const tipoLabel = i.tipo === 'MEDICAMENTO' ? 'Medicamento' : 'Insumo médico';
      const regInvima = i.registroInvima ? '<br><small class="text-muted">INVIMA: ' + escapeHtml(i.registroInvima) + '</small>' : '';
      return '<tr>' +
        '<td><strong>' + escapeHtml(i.nombre) + '</strong>' + regInvima + '</td>' +
        '<td>' + escapeHtml(i.presentacion) + '</td>' +
        '<td>' + escapeHtml(i.unidadMedida) + '</td>' +
        '<td>' + escapeHtml(i.marca) + '</td>' +
        '<td>' + escapeHtml(tipoLabel) + '</td>' +
        '<td>' + i.stockMinimo + '</td>' +
        '<td><span class="badge badge-sem ' + est.clase + '">' + escapeHtml(est.estado) + '</span></td>' +
        '<td class="text-end text-nowrap">' +
          '<a class="btn btn-sm btn-outline-teal me-1" href="insumo-form?id=' + i.id + '" title="Editar"><i class="bi bi-pencil"></i></a>' +
          '<button class="btn btn-sm btn-outline-danger" data-id="' + i.id + '" data-nombre="' + escapeHtml(i.nombre) + '" title="Desactivar"><i class="bi bi-trash"></i></button>' +
        '</td>' +
        '</tr>';
    }).join('');

    tbody.querySelectorAll('button[data-id]').forEach(function (btn) {
      btn.addEventListener('click', async function () {
        const id = btn.getAttribute('data-id');
        const nombre = btn.getAttribute('data-nombre');
        if (!confirm('¿Desactivar el insumo "' + nombre + '"? Esta es una baja lógica, no se borra el registro.')) return;
        const resp = await authFetch(API_BASE + '/insumos/' + id, { method: 'DELETE' });
        if (!resp) return;
        if (resp.ok || resp.status === 204) {
          insumos = insumos.map(function (x) { return x.id == id ? Object.assign({}, x, { activo: false }) : x; });
          render();
          pintarToast('Insumo desactivado.');
        } else {
          const err = await parsearErrorApi(resp);
          mostrarErrorGlobal(err.mensaje);
        }
      });
    });
  }

  function pintarToast(mensaje) {
    document.getElementById('toastExito').innerHTML =
      '<div class="alert alert-success alert-dismissible fade show" role="alert">' + escapeHtml(mensaje) +
      '<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>';
  }

  render();
});