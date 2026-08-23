/* movimientos.js — movimientos.html
 *
 * Esta pantalla tiene DOS modos:
 *
 *   1. Panel general (sin ?loteId= en la URL):
 *      Muestra TODOS los movimientos del sistema.
 *      Llama a: GET /api/movimientos
 *
 *   2. Vista por lote (con ?loteId=xxx en la URL):
 *      Muestra los movimientos de UN lote específico.
 *      Llama a: GET /api/movimientos/por-lote/{loteId}
 *      Además muestra info del lote en el encabezado.
 *
 * ═══════════════════════════════════════════════════════════════
 * INSTRUCCIONES: completa las partes marcadas con "TODO".
 * Las variables ya están declaradas con los nombres que debes usar.
 * Sigue los comentarios paso a paso.
 * ═══════════════════════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', async function () {

  /* ═══ 1. AUTENTICACIÓN ═══
   * Llama a requireAuth() para verificar que hay sesión.
   * Si no hay, redirige a login y detén el script.
   * Luego inyecta el navbar con renderNavbar(usuario).
   *
   * Variables que necesitas:
   *   const usuario = ...
   *   if (!usuario) return;
   *   renderNavbar(usuario);
   */

  // TODO: const usuario = requireAuth();
  // TODO: if (!usuario) return;
  // TODO: renderNavbar(usuario);


  /* ═══ 2. DETECTAR MODO (panel general vs vista por lote) ═══
   * Lee los query params de la URL con parametrosUrl().
   * Si hay ?loteId=xxx, estás en modo "vista por lote".
   * Si no, estás en modo "panel general".
   *
   * Variables que necesitas:
   *   const params = parametrosUrl();    // { loteId: 'xxx' } o {}
   *   const loteId = params.loteId || null;  // string (UUID) o null
   *   const esVistaPorLote = loteId !== null;   // boolean
   *
   * Si es vista por lote:
   *   - Cambia el título a "Bitácora del lote"
   *   - Cambia el subtítulo
   *   - Oculta el filtro de lote (no tiene sentido filtrar por lote si ya estás en uno)
   *
   * Elementos del DOM que ya existen en el HTML:
   *   document.getElementById('tituloPagina')    → <h2>
   *   document.getElementById('subtituloPagina')  → <p>
   *   document.getElementById('filtroLote')       → <select> (ocúltalo con .closest('.col-6').classList.add('d-none'))
   */

  // TODO: const params = parametrosUrl();
  // TODO: const loteId = params.loteId || null;
  // TODO: const esVistaPorLote = loteId !== null;

  // TODO: if (esVistaPorLote) {
  //   document.getElementById('tituloPagina').textContent = 'Bitácora del lote';
  //   document.getElementById('subtituloPagina').textContent = 'Movimientos del lote seleccionado';
  //   // Ocultar el filtro de lote (ya estás viendo uno específico)
  //   document.getElementById('filtroLote').closest('.col-6, .col-md-3').classList.add('d-none');
  // }


  /* ═══ 3. VARIABLES DE ESTADO ═══
   * Declara aquí las variables que vas a usar en toda la función.
   *
   *   let movimientos = [];    → array de MovimientoResponseDTO que llega del backend
   *   const tbody = document.getElementById('tablaMovimientos');  → el <tbody> donde pintas las filas
   */

  // TODO: let movimientos = [];
  // TODO: const tbody = document.getElementById('tablaMovimientos');


  /* ═══ 4. CARGAR DATOS DEL BACKEND ═══
   * Haz la petición HTTP según el modo:
   *
   *   Si esVistaPorLote:
   *     GET /api/movimientos/por-lote/{loteId}
   *     Además, carga la info del lote para el encabezado:
   *     GET /api/lotes/{loteId}
   *     Puedes hacer ambas en paralelo con Promise.all.
   *
   *   Si NO esVistaPorLote (panel general):
   *     GET /api/movimientos
   *     También carga la lista de insumos para el filtro:
   *     GET /api/insumos
   *     Puedes hacer ambas en paralelo con Promise.all.
   *
   * Usa authFetch para todas las llamadas.
   * Recuerda el patrón: if (!resp || !resp.ok) { mostrarErrorGlobal('...'); return; }
   * Luego: const datos = await resp.json();
   *
   * Para el panel general, rellena el <select id="filtroLote"> con los
   * números de lote únicos que aparezcan en los movimientos.
   *
   * Campos que trae cada MovimientoResponseDTO:
   *   .id            → UUID
   *   .loteId        → UUID
   *   .usuarioId     → UUID
   *   .tipo          → "ENTRADA" o "SALIDA"
   *   .cantidad      → int (unidades)
   *   .fecha         → string ISO (ej. "2026-08-22T15:30:00")
   *   .observacion   → string o null
   *   .nombreUsuario → string (ej. "Enfermera Ana")  ← ya resuelto por el backend
   *   .numeroLote    → string (ej. "L-001")           ← ya resuelto por el backend
   *   .nombreInsumo  → string (ej. "Acetaminofén")    ← ya resuelto por el backend
   *   .unidadesPorCaja → int
   *   .cajas         → int
   *   .unidadesSueltas → int
   *   .cantidadFormateada → string (ej. "5 cajas y 3 unidades")  ← ya calculado por el backend
   */

  // TODO: aquí va tu código de carga de datos


  /* ═══ 5. REGISTRAR LISTENERS DE FILTROS ═══
   * Los filtros en el HTML tienen estos IDs:
   *   'filtroInsumo'  → <input type="text">  (buscar por nombre de insumo)
   *   'filtroTipo'    → <select>             (ENTRADA / SALIDA / todos)
   *   'filtroLote'    → <select>             (número de lote / todos)
   *   'filtroUsuario' → <select>             (nombre de usuario / todos)
   *
   * Para cada filtro, registra un listener que llame a render():
   *   document.getElementById('filtroInsumo').addEventListener('input', render);
   *   document.getElementById('filtroTipo').addEventListener('change', render);
   *   etc.
   *
   * Si es vista por lote, el filtroLote está oculto, no lo registres.
   */

  // TODO: registrar listeners


  /* ═══ 6. FUNCIÓN render() ═══
   * Esta función filtra el array `movimientos` según los valores de los
   * filtros y pinta la tabla.
   *
   * Pasos:
   *   a) Leer los valores de los 4 filtros:
   *      const fInsumo = (document.getElementById('filtroInsumo').value || '').toLowerCase();
   *      const fTipo = document.getElementById('filtroTipo').value;
   *      const fLote = document.getElementById('filtroLote').value;
   *      const fUsuario = document.getElementById('filtroUsuario').value;
   *
   *   b) Filtrar el array `movimientos`:
   *      - Si fInsumo no está vacío, filtra los que incluyan ese texto en .nombreInsumo
   *      - Si fTipo no está vacío, filtra los que .tipo === fTipo
   *      - Si fLote no está vacío, filtra los que .numeroLote === fLote
   *      - Si fUsuario no está vacío, filtra los que .nombreUsuario === fUsuario
   *
   *   c) Si no hay resultados, mostrar:
   *      tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No hay movimientos que coincidan.</td></tr>';
   *      return;
   *
   *   d) Ordenar por fecha descendente (más reciente primero):
   *      movimientos.sort(function(a, b) {
   *        return (b.fecha || '').localeCompare(a.fecha || '');
   *      });
   *
   *   e) Construir el HTML de cada fila con .map() y escapeHtml:
   *
   *      Columnas: Fecha | Tipo (badge) | Insumo | Lote | Cantidad | Usuario | Observación
   *
   *      Para la columna Tipo:
   *        - ENTRADA → badge verde: '<span class="badge badge-sem badge-activo">Entrada</span>'
   *        - SALIDA  → badge rojo: '<span class="badge badge-sem badge-critico">Salida</span>'
   *
   *      Para la columna Cantidad:
   *        - Muestra .cantidadFormateada (ej. "5 cajas y 3 unidades")
   *        - Si quieres, añade el total entre paréntesis: "(53 u)"
   *
   *      Para la columna Fecha:
   *        - La fecha viene como "2026-08-22T15:30:00"
   *        - Puedes usar formatFecha(m.fecha) para la fecha (dd/mm/yyyy)
   *        - Para la hora, extrae la parte después de la 'T' y recorta a HH:MM
   *
   *      Para la columna Observación:
   *        - Si .observacion es null, muestra '–' en gris
   *        - Si no, muestra escapeHtml(m.observacion)
   *
   *   f) Asignar a tbody.innerHTML el resultado del .map().join('')
   */

  // TODO: function render() { ... }


  /* ═══ 7. LLAMAR render() POR PRIMERA VEZ ═══
   * Después de cargar los datos y registrar los listeners,
   * llama a render() para pintar la tabla inicial.
   */

  // TODO: render();


  /* ═══ 8. (OPCIONAL) FUNCIÓN pintarToast ═══
   * Si quieres mostrar un mensaje de éxito (ej. al volver de otra página):
   *
   * function pintarToast(mensaje) {
   *   document.getElementById('toastExito').innerHTML =
   *     '<div class="alert alert-success alert-dismissible fade show" role="alert">'
   *     + escapeHtml(mensaje) +
   *     '<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>';
   * }
   *
   * Y para consumir un toast dejado por otra página:
   *   const toast = consumirToast('toast_movimientos');
   *   if (toast) pintarToast(toast);
   */

  // TODO: (opcional) toast
});
