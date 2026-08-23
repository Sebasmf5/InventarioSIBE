/* dashboard.js — dashboard.html */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  // Toast de redirección (opcional, desde otras páginas)
  const toast = consumirToast('toast_dashboard');
  if (toast) {
    document.getElementById('toastExito').innerHTML =
      '<div class="alert alert-success alert-dismissible fade show" role="alert">' +
      escapeHtml(toast) +
      '<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>';
  }

  const [respIns, respLot] = await Promise.all([
    authFetch(API_BASE + '/insumos'),
    authFetch(API_BASE + '/lotes')
  ]);

  if (!respIns || !respIns.ok || !respLot || !respLot.ok) {
    mostrarErrorGlobal('No se pudo cargar la información del dashboard.');
    return;
  }

  const insumos = await respIns.json();
  const lotes = await respLot.json();

  // Mapa insumoId -> nombre
  const nombreInsumo = {};
  (insumos || []).forEach(function (i) { nombreInsumo[i.id] = i.nombre; });

  // Conteos por estado
  const conteo = { Vigente: 0, 'Por vencer': 0, 'Crítico': 0, Agotado: 0 };
  const criticos = [];

  (lotes || []).filter(function (lote) { return lote.activo !== false; }).forEach(function (lote) {
    const s = calcularEstadoSemaforo(lote);
    conteo[s.estado] = (conteo[s.estado] || 0) + 1;
    if (s.estado === 'Crítico') {
      criticos.push({ lote: lote, estado: s });
    }
  });

  document.getElementById('cntVigente').textContent = conteo['Vigente'];
  document.getElementById('cntPorVencer').textContent = conteo['Por vencer'];
  document.getElementById('cntCritico').textContent = conteo['Crítico'];
  document.getElementById('cntAgotado').textContent = conteo['Agotado'];

  // Ordenar críticos por menor cantidad de días restantes (ascendente, incluye negativos = vencidos)
  criticos.sort(function (a, b) {
    const da = a.estado.diasRestantes === null ? Infinity : a.estado.diasRestantes;
    const db = b.estado.diasRestantes === null ? Infinity : b.estado.diasRestantes;
    return da - db;
  });

  const tbody = document.getElementById('tablaUrgentes');
  if (!criticos.length) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No hay lotes críticos. Todo en orden.</td></tr>';
    return;
  }

  tbody.innerHTML = criticos.map(function (c) {
    const l = c.lote;
    const diasTxt = c.estado.diasRestantes === null ? '–'
      : (c.estado.diasRestantes < 0 ? 'Vencido hace ' + Math.abs(c.estado.diasRestantes) + ' d'
         : c.estado.diasRestantes === 0 ? 'Vence hoy'
         : c.estado.diasRestantes + ' d');
    return '<tr>' +
      '<td>' + escapeHtml(nombreInsumo[l.insumoId] || 'Insumo eliminado') + '</td>' +
      '<td>' + escapeHtml(l.numeroLote) + '</td>' +
      '<td>' + formatFecha(l.fechaVencimiento) + '</td>' +
      '<td class="fw-semibold">' + escapeHtml(diasTxt) + '</td>' +
      '<td>' + escapeHtml(l.stockFormateado || (l.cantidadActual + ' u')) + '</td>' +
      '<td>' + escapeHtml(l.ubicacion || '–') + '</td>' +
      '<td>' + badgeSemaforoHtml(l) + '</td>' +
      '</tr>';
  }).join('');
});