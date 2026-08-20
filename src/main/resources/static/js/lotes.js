/* lotes.js — lotes.html
   Lista todos los lotes con filtros y permite activar/desactivar (baja lógica). */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  const tbody = document.getElementById('tablaLotes');
  let lotes = [];
  let insumos = [];
  let nombreInsumo = {};

  const [respIns, respLot] = await Promise.all([
    authFetch(API_BASE + '/insumos'),
    authFetch(API_BASE + '/lotes')
  ]);
  if (!respIns || !respIns.ok || !respLot || !respLot.ok) {
    mostrarErrorGlobal('No se pudo cargar la lista de lotes.');
    return;
  }
  insumos = await respIns.json();
  lotes = await respLot.json();
  (insumos || []).forEach(function (i) { nombreInsumo[i.id] = i.nombre; });

  // Rellenar el select de insumos
  const selInsumo = document.getElementById('filtroInsumo');
  selInsumo.innerHTML = '<option value="">Todos los insumos</option>' +
    insumos.map(function (i) {
      return '<option value="' + i.id + '">' + escapeHtml(i.nombre) + '</option>';
    }).join('');

  ['filtroLote', 'filtroInsumo', 'filtroSemaforo', 'filtroActivo'].forEach(function (id) {
    document.getElementById(id).addEventListener('input', render);
    document.getElementById(id).addEventListener('change', render);
  });

  function render() {
    const fLote = (document.getElementById('filtroLote').value || '').toLowerCase();
    const fInsumo = document.getElementById('filtroInsumo').value;
    const fSemaforo = document.getElementById('filtroSemaforo').value;
    const fActivo = document.getElementById('filtroActivo').value;

    const filtrados = lotes.filter(function (l) {
      if (fLote && (l.numeroLote || '').toLowerCase().indexOf(fLote) === -1) return false;
      if (fInsumo && l.insumoId !== fInsumo) return false;
      const s = calcularEstadoSemaforo(l);
      if (fSemaforo && s.estado !== fSemaforo) return false;
      if (fActivo === 'activo' && l.activo === false) return false;
      if (fActivo === 'inactivo' && l.activo !== false) return false;
      return true;
    });

    if (!filtrados.length) {
      tbody.innerHTML = '<tr><td colspan="10" class="text-center text-muted py-4">No hay lotes que coincidan.</td></tr>';
      return;
    }

    tbody.innerHTML = filtrados.map(function (l) {
      const s = calcularEstadoSemaforo(l);
      let diasTxt;
      if (s.diasRestantes === null) diasTxt = '–';
      else if (s.diasRestantes < 0) diasTxt = 'Vencido hace ' + Math.abs(s.diasRestantes) + ' d';
      else if (s.diasRestantes === 0) diasTxt = 'Vence hoy';
      else diasTxt = s.diasRestantes + ' d';

      const activoBadge = l.activo === false
        ? '<span class="badge badge-sem badge-inactivo">Inactivo</span>'
        : '<span class="badge badge-sem badge-activo">Activo</span>';

      const toggleBtn = l.activo === false
        ? '<button class="btn btn-sm btn-outline-teal" data-id="' + l.id + '" data-activar="true" title="Habilitar lote"><i class="bi bi-unlock"></i></button>'
        : '<button class="btn btn-sm btn-outline-danger" data-id="' + l.id + '" data-activar="false" title="Deshabilitar lote"><i class="bi bi-lock"></i></button>';

      return '<tr>' +
        '<td><strong>' + escapeHtml(nombreInsumo[l.insumoId] || 'Insumo eliminado') + '</strong></td>' +
        '<td>' + escapeHtml(l.numeroLote) + '</td>' +
        '<td>' + formatFecha(l.fechaVencimiento) + '</td>' +
        '<td class="fw-semibold">' + escapeHtml(diasTxt) + '</td>' +
        '<td>' + l.cantidadInicial + '</td>' +
        '<td>' + l.cantidadActual + '</td>' +
        '<td>' + escapeHtml(l.ubicacion || '–') + '</td>' +
        '<td>' + badgeSemaforoHtml(l) + '</td>' +
        '<td>' + activoBadge + '</td>' +
        '<td class="text-end">' + toggleBtn + '</td>' +
        '</tr>';
    }).join('');

    tbody.querySelectorAll('button[data-id]').forEach(function (btn) {
      btn.addEventListener('click', async function () {
        const id = btn.getAttribute('data-id');
        const activar = btn.getAttribute('data-activar') === 'true';
        const accionTxt = activar ? 'habilitar' : 'deshabilitar';
        if (!confirm('¿Confirmas ' + accionTxt + ' este lote? El stock y los movimientos se conservan.')) return;
        const resp = await authFetch(API_BASE + '/lotes/' + id + '/activo', {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ activo: activar })
        });
        if (!resp) return;
        if (resp.ok) {
          lotes = lotes.map(function (x) {
            return x.id === id ? Object.assign({}, x, { activo: activar }) : x;
          });
          render();
          pintarToast('Lote ' + (activar ? 'habilitado' : 'deshabilitado') + '.');
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
