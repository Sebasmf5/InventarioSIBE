/* InventarioSIBE — common.js
   Compartido por todas las páginas autenticadas:
   - lectura del JWT desde localStorage
   - requireAuth() que redirige a login.html si no hay token válido
   - authFetch(url, options) añade automáticamente Authorization: Bearer <token>
   - renderNavbar(usuario)
   - utilidades de semáforo, fechas y errores del backend */

const API_BASE = '/api';

// Umbrales del semáforo: el cálculo lo hace el backend (Lote.calcularEstadoSemaforo
// usando ConfiguracionSemaforo de la DB). El front solo mapea el enum EstadoSemaforo
// a etiqueta legible + clase CSS para pintar el badge.
const MAPA_ESTADO_SEMAFORO = {
  VERDE:    { estado: 'Vigente',    clase: 'badge-vigente'    },
  AMARILLO: { estado: 'Por vencer', clase: 'badge-por-vencer' },
  ROJO:     { estado: 'Crítico',    clase: 'badge-critico'    },
  AGOTADO:  { estado: 'Agotado',    clase: 'badge-agotado'    }
};

const CLAVES = {
  TOKEN: 'inv_token',
  USUARIO: 'inv_usuario'
};

/* ---------- Autenticación ---------- */

function getToken() {
  return localStorage.getItem(CLAVES.TOKEN);
}

function getUsuario() {
  const raw = localStorage.getItem(CLAVES.USUARIO);
  try { return raw ? JSON.parse(raw) : null; }
  catch (e) { return null; }
}

function guardarSesion(loginResponse) {
  localStorage.setItem(CLAVES.TOKEN, loginResponse.token);
  localStorage.setItem(CLAVES.USUARIO, JSON.stringify({
    id: loginResponse.id || null,
    nombre: loginResponse.nombre,
    email: loginResponse.email,
    rol: loginResponse.rol
  }));
}

function requireAuth() {
  const token = getToken();
  const usuario = getUsuario();
  if (!token || !usuario) {
    window.location.href = 'login';
    return null;
  }
  return usuario;
}

function requerirRol(rolEsperado) {
  const usuario = requireAuth();
  if (!usuario) return null;
  if (usuario.rol !== rolEsperado) {
    window.location.href = 'dashboard';
    return null;
  }
  return usuario;
}

function logout() {
  localStorage.removeItem(CLAVES.TOKEN);
  localStorage.removeItem(CLAVES.USUARIO);
  window.location.href = 'login';
}

/* fetch con Authorization. En 401 cierra sesión. */
async function authFetch(url, options = {}) {
  const token = getToken();
  if (!token) { logout(); return null; }
  const headers = Object.assign({}, options.headers || {});
  headers['Authorization'] = 'Bearer ' + token;
  if (options.body && typeof options.body === 'string' && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }
  let resp;
  try {
    resp = await fetch(url, Object.assign({}, options, { headers }));
  } catch (e) {
    mostrarErrorGlobal('No se pudo conectar con el servidor.');
    return null;
  }
  if (resp.status === 401) { logout(); return null; }
  return resp;
}

/* Devuelve el id del usuario logueado. Si no está (login antiguo sin id),
   intenta resolverlo vía GET /api/usuarios filtrando por email (sólo SUPERVISOR). */
function getUsuarioId() {
  const u = getUsuario();
  return u ? u.id : null;
}

async function asegurarUsuarioId() {
  const u = getUsuario();
  if (!u) return null;
  if (u.id) return u.id;
  // Fallback: buscar por email en /api/usuarios (sólo SUPERVISOR podrá)
  try {
    const resp = await authFetch(API_BASE + '/usuarios');
    if (!resp || !resp.ok) return null;
    const usuarios = await resp.json();
    const encontrado = (usuarios || []).find(function (x) { return x.email === u.email; });
    if (encontrado) {
      u.id = encontrado.id;
      localStorage.setItem(CLAVES.USUARIO, JSON.stringify(u));
      return encontrado.id;
    }
  } catch (e) { /* ignorar */ }
  return null;
}

/* ---------- Navbar ---------- */

function renderNavbar(usuario) {
  const nav = document.getElementById('navbar');
  if (!nav) return;
  const esSupervisor = usuario && usuario.rol === 'SUPERVISOR';
  // pathActual sin extensión para comparar contra rutas limpias
  let pathActual = window.location.pathname.split('/').pop() || 'dashboard';
  pathActual = pathActual.replace(/\.html$/, '');

  const items = [
    { href: 'dashboard',          label: 'Dashboard',          icon: 'bi-speedometer2' },
    { href: 'inventario',         label: 'Inventario',          icon: 'bi-boxes' },
    { href: 'lotes',              label: 'Lotes',               icon: 'bi-layers' },
    { href: 'lote-form',          label: 'Registrar lote',       icon: 'bi-plus-circle' },
    { href: 'movimiento-wizard',  label: 'Registrar movimiento', icon: 'bi-arrow-left-right' }
  ];
  if (esSupervisor) items.push({ href: 'usuarios', label: 'Usuarios', icon: 'bi-people' });

  let linksHtml = '';
  items.forEach(function (it) {
    const active = it.href === pathActual ? ' active' : '';
    linksHtml += '<li class="nav-item">' +
      '<a class="nav-link' + active + '" href="' + it.href + '">' +
      '<i class="bi ' + it.icon + ' me-2"></i>' + escapeHtml(it.label) + '</a></li>';
  });

  nav.innerHTML =
    '<button class="app-sidebar-toggle d-lg-none" id="sidebarToggle" type="button" aria-label="Abrir menú">' +
      '<i class="bi bi-list"></i></button>' +
    '<aside class="app-sidebar" id="sidebarEl">' +
      '<div class="sidebar-header">' +
        '<i class="bi bi-boxes sidebar-logo"></i>' +
        '<span class="sidebar-brand">InventarioSIBE</span>' +
      '</div>' +
      '<nav class="sidebar-nav"><ul class="list-unstyled mb-0">' + linksHtml + '</ul></nav>' +
      '<div class="sidebar-footer">' +
        '<div class="sidebar-user">' +
          '<i class="bi bi-person-circle me-2"></i>' +
          '<div>' +
            '<div class="sidebar-user-name">' + escapeHtml(usuario ? usuario.nombre : '') + '</div>' +
            '<span class="badge bg-light text-teal">' + escapeHtml(usuario ? usuario.rol : '') + '</span>' +
          '</div>' +
        '</div>' +
        '<button class="btn btn-sm btn-outline-light w-100 mt-3" id="btnLogout" type="button">' +
          '<i class="bi bi-box-arrow-right me-1"></i> Cerrar sesión</button>' +
      '</div>' +
      '<div class="sidebar-backdrop d-lg-none" id="sidebarBackdrop"></div>' +
    '</aside>';

  const btnLogout = document.getElementById('btnLogout');
  if (btnLogout) btnLogout.addEventListener('click', logout);

  // Toggle sidebar en móvil
  const toggle = document.getElementById('sidebarToggle');
  const sidebar = document.getElementById('sidebarEl');
  const backdrop = document.getElementById('sidebarBackdrop');
  function abrirSidebar()  { sidebar.classList.add('show'); if (backdrop) backdrop.classList.add('show'); }
  function cerrarSidebar() { sidebar.classList.remove('show'); if (backdrop) backdrop.classList.remove('show'); }
  if (toggle)   toggle.addEventListener('click', abrirSidebar);
  if (backdrop) backdrop.addEventListener('click', cerrarSidebar);
}

/* ---------- Semáforo ---------- */

/* Recibe un LoteResponseDTO (con `estado` y `diasRestantes` calculados por el backend)
   y devuelve { estado, clase, diasRestantes } listo para pintar. */
function calcularEstadoSemaforo(lote) {
  const codigo = lote.estado || 'AGOTADO';
  const mapeo = MAPA_ESTADO_SEMAFORO[codigo] || MAPA_ESTADO_SEMAFORO.AGOTADO;
  return {
    estado: mapeo.estado,
    clase: mapeo.clase,
    diasRestantes: (lote.diasRestantes === undefined ? null : lote.diasRestantes)
  };
}

function badgeSemaforoHtml(lote) {
  const s = calcularEstadoSemaforo(lote);
  let extra = '';
  if (s.diasRestantes !== null) {
    if (s.diasRestantes < 0)      extra = ' · vencido hace ' + Math.abs(s.diasRestantes) + ' d';
    else if (s.diasRestantes === 0) extra = ' · vence hoy';
    else                          extra = ' · ' + s.diasRestantes + ' d';
  }
  return '<span class="badge badge-sem ' + s.clase + '">' + escapeHtml(s.estado) + escapeHtml(extra) + '</span>';
}

/* ---------- Utilidades ---------- */

function formatFecha(fecha) {
  if (!fecha) return '';
  const dia = (fecha || '').split('T')[0];
  const partes = dia.split('-');
  if (partes.length !== 3) return fecha;
  return partes[2] + '/' + partes[1] + '/' + partes[0];
}

function escapeHtml(s) {
  if (s === null || s === undefined) return '';
  return String(s).replace(/[&<>"']/g, function (c) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
  });
}

function parametrosUrl() {
  const params = new URLSearchParams(window.location.search);
  const o = {};
  params.forEach(function (v, k) { o[k] = v; });
  return o;
}

/* ---------- Errores del backend ---------- */

/* Extrae { mensaje, campos } del cuerpo de error del GlobalExceptionHandler */
async function parsearErrorApi(resp) {
  let data = null;
  try { data = await resp.json(); } catch (e) { /* cuerpo no JSON */ }
  if (!data) return { mensaje: 'Error inesperado (HTTP ' + resp.status + ')', campos: null };
  return {
    mensaje: data.mensaje || ('Error HTTP ' + resp.status),
    campos: data.campos || null
  };
}

/* Pinta el error en un contenedor #formError (forma de alerta).
   Si hay campos (validación), los lista debajo del mensaje. */
function mostrarErrorForm(mensaje, campos) {
  const cont = document.getElementById('formError');
  if (!cont) { alert(mensaje + (campos ? '\n' + JSON.stringify(campos) : '')); return; }
  let html = '<div class="alert alert-danger alert-dismissible fade show" role="alert"><div>' +
    escapeHtml(mensaje) + '</div>';
  if (campos && typeof campos === 'object' && Object.keys(campos).length) {
    html += '<ul class="mb-0 mt-2 small">';
    Object.keys(campos).forEach(function (k) {
      html += '<li><strong>' + escapeHtml(k) + ':</strong> ' + escapeHtml(campos[k]) + '</li>';
    });
    html += '</ul>';
  }
  html += '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button></div>';
  cont.innerHTML = html;
}

function limpiarErrorForm() {
  const cont = document.getElementById('formError');
  if (cont) cont.innerHTML = '';
}

/* Error global (página completa) cuando no hay contenedor específico */
function mostrarErrorGlobal(mensaje) {
  let cont = document.getElementById('errorGlobal');
  if (cont) {
    cont.innerHTML = '<div class="alert alert-danger" role="alert">' + escapeHtml(mensaje) + '</div>';
  } else {
    console.error(mensaje);
  }
}

/* Toast sencillo opcional (usado en redirecciones exitosas) */
function marcarToast(clave, mensaje) {
  sessionStorage.setItem(clave, mensaje);
}
function consumirToast(clave) {
  const m = sessionStorage.getItem(clave);
  if (m) sessionStorage.removeItem(clave);
  return m;
}