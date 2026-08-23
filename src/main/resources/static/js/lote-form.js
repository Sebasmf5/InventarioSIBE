/* lote-form.js — registrar lote nuevo (POST /api/lotes) */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  // Asegurar el id del usuario logueado (para usuarioId)
  const usuarioId = await asegurarUsuarioId();
  if (!usuarioId) {
    mostrarErrorGlobal('No se pudo determinar el usuario logueado. Cierre sesión e intente de nuevo.');
    return;
  }

  // Deshabilitar fechas pasadas
  const hoy = new Date().toISOString().split('T')[0];
  document.getElementById('fechaVencimiento').setAttribute('min', hoy);

  // Cargar insumos en el dropdown
  const selectIns = document.getElementById('insumoId');
  const respIns = await authFetch(API_BASE + '/insumos');
  if (!respIns || !respIns.ok) {
    selectIns.innerHTML = '<option value="">No hay insumos disponibles</option>';
    mostrarErrorGlobal('No se pudo cargar la lista de insumos.');
    return;
  }
  const insumos = await respIns.json();
  const activos = insumos.filter(function (i) { return i.activo; });
  if (!activos.length) {
    selectIns.innerHTML = '<option value="">No hay insumos activos. Cree uno primero.</option>';
    selectIns.disabled = true;
    return;
  }
  // Mapa insumoId -> insumo (para leer unidadesPorCaja al calcular el total)
  const insumosMap = {};
  activos.forEach(function (i) { insumosMap[i.id] = i; });

  selectIns.innerHTML = '<option value="">Seleccione un insumo…</option>' +
    activos.map(function (i) {
      return '<option value="' + i.id + '">' + escapeHtml(i.nombre + ' · ' + i.presentacion) + '</option>';
    }).join('');

  // Elementos para el cálculo de cajas + unidades sueltas → total
  const cajasInput = document.getElementById('cajas');
  const unidadesSueltasInput = document.getElementById('unidadesSueltas');
  const factorInfo = document.getElementById('factorInfo');
  const totalInfo = document.getElementById('totalInfo');

  function actualizarFactorYTotal() {
    const ins = insumosMap[selectIns.value];
    const factor = ins ? ins.unidadesPorCaja : 1;
    factorInfo.textContent = ins
      ? 'Este insumo tiene ' + factor + ' unidades por caja.'
      : '';
    actualizarTotal();
  }

  function actualizarTotal() {
    const ins = insumosMap[selectIns.value];
    const factor = ins ? ins.unidadesPorCaja : 1;
    const cajas = parseInt(cajasInput.value, 10) || 0;
    const sueltas = parseInt(unidadesSueltasInput.value, 10) || 0;
    const total = cajas * factor + sueltas;
    totalInfo.textContent = 'Total: ' + total + ' unidades';
  }

  selectIns.addEventListener('change', actualizarFactorYTotal);
  cajasInput.addEventListener('input', actualizarTotal);
  unidadesSueltasInput.addEventListener('input', actualizarTotal);

  const form = document.getElementById('loteForm');
  const btn = document.getElementById('btnGuardar');
  const spinner = document.getElementById('spinner');

  form.addEventListener('submit', async function (e) {
    e.preventDefault();
    limpiarErrorForm();

    // Calcular cantidadInicial en unidades a partir de cajas + sueltas
    const ins = insumosMap[selectIns.value];
    const unidadesPorCaja = ins ? ins.unidadesPorCaja : 1;
    const cajas = parseInt(cajasInput.value, 10) || 0;
    const unidadesSueltas = parseInt(unidadesSueltasInput.value, 10) || 0;
    const cantidadInicial = cajas * unidadesPorCaja + unidadesSueltas;

    if (cantidadInicial <= 0) {
      mostrarErrorForm('Ingrese al menos una caja o unidad.');
      return;
    }

    const payload = {
      insumoId: selectIns.value,
      usuarioId: usuarioId,
      numeroLote: document.getElementById('numeroLote').value.trim(),
      fechaVencimiento: document.getElementById('fechaVencimiento').value,
      cantidadInicial: cantidadInicial,
      observacion: document.getElementById('observacion').value.trim() || null,
      ubicacion: document.getElementById('ubicacion').value.trim()
    };

    btn.disabled = true;
    spinner.classList.remove('d-none');
    try {
      const resp = await authFetch(API_BASE + '/lotes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!resp) return;
      if (resp.ok || resp.status === 201) {
        marcarToast('toast_dashboard', 'Lote registrado y entrada de stock generada.');
        window.location.href = 'dashboard';
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