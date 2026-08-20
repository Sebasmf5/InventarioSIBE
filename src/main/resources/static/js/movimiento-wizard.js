/* movimiento-wizard.js — wizard de 3 pasos para registrar un movimiento */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  const usuarioId = await asegurarUsuarioId();
  if (!usuarioId) {
    mostrarErrorGlobal('No se pudo determinar el usuario logueado. Cierre sesión e intente de nuevo.');
    return;
  }

  let insumos = [];
  let loteSeleccionado = null;
  let insumoSeleccionado = null;

  // Cargar insumos
  const respIns = await authFetch(API_BASE + '/insumos');
  if (!respIns || !respIns.ok) { mostrarErrorGlobal('No se pudo cargar la lista de insumos.'); return; }
  insumos = (await respIns.json()).filter(function (i) { return i.activo; });

  // ---- Paso 1: elegir insumo ----
  const buscar = document.getElementById('buscarInsumo');
  const listaIns = document.getElementById('listaInsumos');
  buscar.addEventListener('input', renderListaInsumos);

  function renderListaInsumos() {
    const q = (buscar.value || '').toLowerCase();
    const items = insumos.filter(function (i) { return !q || (i.nombre || '').toLowerCase().indexOf(q) !== -1; });
    if (!items.length) {
      listaIns.innerHTML = '<div class="text-center text-muted py-4">Sin resultados.</div>';
      return;
    }
    listaIns.innerHTML = items.map(function (i) {
      return '<button type="button" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" data-id="' + i.id + '">' +
        '<div><strong>' + escapeHtml(i.nombre) + '</strong><div class="small text-muted">' +
        escapeHtml(i.presentacion) + ' · ' + escapeHtml(i.marca) + '</div></div>' +
        '<i class="bi bi-chevron-right text-muted"></i></button>';
    }).join('');
    listaIns.querySelectorAll('button[data-id]').forEach(function (b) {
      b.addEventListener('click', function () {
        insumoSeleccionado = insumos.find(function (x) { return x.id === b.getAttribute('data-id'); });
        irAPaso2();
      });
    });
  }
  renderListaInsumos();

  // ---- Paso 2: elegir lote ----
  const listaLotes = document.getElementById('listaLotes');
  document.getElementById('btnVolverPaso1').addEventListener('click', irAPaso1);

  async function irAPaso2() {
    document.getElementById('paso1').classList.add('d-none');
    document.getElementById('paso2').classList.remove('d-none');
    document.getElementById('step1Ind').classList.remove('active');
    document.getElementById('step1Ind').classList.add('done');
    document.getElementById('step2Ind').classList.add('active');

    document.getElementById('loteInsumoNombre').innerHTML =
      '<strong>Insumo:</strong> ' + escapeHtml(insumoSeleccionado.nombre) +
      ' (' + escapeHtml(insumoSeleccionado.presentacion) + ')';
    listaLotes.innerHTML = '<div class="text-center text-muted py-4">Cargando lotes…</div>';

    const resp = await authFetch(API_BASE + '/lotes/por-insumo/' + insumoSeleccionado.id);
    if (!resp || !resp.ok) { listaLotes.innerHTML = '<div class="text-center text-muted py-4">No se pudieron cargar los lotes.</div>'; return; }
    const lotes = await resp.json();
    const disponibles = lotes.filter(function (l) { return l.cantidadActual > 0 && l.activo !== false; });
    if (!disponibles.length) {
      listaLotes.innerHTML = '<div class="text-center text-muted py-4">Este insumo no tiene lotes con stock disponible. Registre un lote primero.</div>';
      return;
    }
    listaLotes.innerHTML = disponibles.map(function (l) {
      return '<button type="button" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" data-id="' + l.id + '">' +
        '<div>' +
          '<div><strong>Lote ' + escapeHtml(l.numeroLote) + '</strong> · vence ' + formatFecha(l.fechaVencimiento) + '</div>' +
          '<div class="small text-muted">Stock actual: ' + l.cantidadActual + ' · ' + escapeHtml(l.ubicacion || '–') + '</div>' +
        '</div>' +
        '<span class="text-nowrap ms-2">' + badgeSemaforoHtml(l) + '</span>' +
      '</button>';
    }).join('');
    listaLotes.querySelectorAll('button[data-id]').forEach(function (b) {
      b.addEventListener('click', function () {
        loteSeleccionado = disponibles.find(function (x) { return x.id === b.getAttribute('data-id'); });
        irAPaso3();
      });
    });
  }

  document.getElementById('btnVolverPaso2').addEventListener('click', irAPaso2);

  // ---- Paso 3: tipo y cantidad ----
  const tipoMov = document.getElementById('tipoMov');
  const cantidad = document.getElementById('cantidad');
  const hint = document.getElementById('hintCantidad');
  const btnConfirmar = document.getElementById('btnConfirmar');
  const spinner = document.getElementById('spinner');

  function irAPaso1() {
    document.getElementById('paso2').classList.add('d-none');
    document.getElementById('paso1').classList.remove('d-none');
    document.getElementById('step2Ind').classList.remove('active');
    document.getElementById('step1Ind').classList.add('active');
    document.getElementById('step1Ind').classList.remove('done');
    limpiarErrorForm();
  }

  function irAPaso3() {
    document.getElementById('paso2').classList.add('d-none');
    document.getElementById('paso3').classList.remove('d-none');
    document.getElementById('step2Ind').classList.remove('active');
    document.getElementById('step2Ind').classList.add('done');
    document.getElementById('step3Ind').classList.add('active');

    document.getElementById('loteSeleccionado').innerHTML =
      '<div><strong>Lote:</strong> ' + escapeHtml(loteSeleccionado.numeroLote) + '</div>' +
      '<div class="small">Vence ' + formatFecha(loteSeleccionado.fechaVencimiento) + ' · Stock actual: ' + loteSeleccionado.cantidadActual + '</div>' +
      '<div class="mt-1">' + badgeSemaforoHtml(loteSeleccionado) + '</div>';

    actualizarLimitesCantidad();
  }

  function actualizarLimitesCantidad() {
    if (tipoMov.value === 'SALIDA') {
      const max = loteSeleccionado ? loteSeleccionado.cantidadActual : 0;
      cantidad.setAttribute('max', String(max));
      cantidad.value = Math.min(parseInt(cantidad.value, 10) || 1, max);
      hint.textContent = 'Máximo disponible: ' + max;
    } else {
      cantidad.removeAttribute('max');
      hint.textContent = 'La entrada aumenta el stock del lote.';
    }
  }
  tipoMov.addEventListener('change', actualizarLimitesCantidad);

  btnConfirmar.addEventListener('click', async function () {
    limpiarErrorForm();
    const cant = parseInt(cantidad.value, 10) || 0;
    if (cant <= 0) { mostrarErrorForm('La cantidad debe ser mayor que cero.'); return; }
    if (tipoMov.value === 'SALIDA' && loteSeleccionado && cant > loteSeleccionado.cantidadActual) {
      mostrarErrorForm('La cantidad de salida excede el stock disponible (' + loteSeleccionado.cantidadActual + ').');
      return;
    }

    const payload = {
      loteId: loteSeleccionado.id,
      usuarioId: usuarioId,
      tipo: tipoMov.value,
      cantidad: cant,
      observacion: document.getElementById('observacion').value.trim() || null
    };

    btnConfirmar.disabled = true;
    spinner.classList.remove('d-none');
    try {
      const resp = await authFetch(API_BASE + '/movimientos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!resp) return;
      if (resp.ok || resp.status === 201) {
        marcarToast('toast_dashboard', 'Movimiento registrado correctamente.');
        window.location.href = 'dashboard';
        return;
      }
      const err = await parsearErrorApi(resp);
      mostrarErrorForm(err.mensaje, err.campos);
    } finally {
      btnConfirmar.disabled = false;
      spinner.classList.add('d-none');
    }
  });
});