# Referencia detallada del frontend JS — InventarioSIBE

Documentación **archivo por archivo, función por función** del JavaScript del frontend. A diferencia de `frontend-js-api.md` (que es un tutorial de async/await), este documento es una **referencia** para consultar qué hace cada función, qué recibe y qué devuelve.

## Índice

1. [Arquitectura general](#1-arquitectura-general)
2. [`js/common.js` — utilidades compartidas](#2-jscommonjs--utilidades-compartidas)
3. [`js/login.js` — inicio de sesión](#3-jsloginjs--inicio-de-sesión)
4. [`js/dashboard.js` — panel de control](#4-jsdashboardjs--panel-de-control)
5. [`js/inventario.js` — gestión de insumos](#5-jsinventariojs--gestión-de-insumos)
6. [`js/insumo-form.js` — crear/editar insumo](#6-jsinsumo-formjs--creareditar-insumo)
7. [`js/lote-form.js` — registrar lote](#7-jslote-formjs--registrar-lote)
8. [`js/movimiento-wizard.js` — asistente de movimiento](#8-jsmovimiento-wizardjs--asistente-de-movimiento)
9. [`js/usuarios.js` — gestión de usuarios](#9-jsusuariosjs--gestión-de-usuarios)
10. [`js/lotes.js` — gestión de lotes y toggle activo](#10-jslotesjs--gestión-de-lotes-y-toggle-activo)
11. [Patrones transversales](#11-patrones-transversales)

---

## 1. Arquitectura general

```
static/
├─ index.html              → redirige a /login o /dashboard según localStorage
├─ login.html              + js/login.js
├─ dashboard.html          + js/dashboard.js
├─ inventario.html         + js/inventario.js
├─ insumo-form.html        + js/insumo-form.js
├─ lote-form.html          + js/lote-form.js
├─ movimiento-wizard.html  + js/movimiento-wizard.js
├─ usuarios.html           + js/usuarios.js
├─ lotes.html              + js/lotes.js
├─ css/custom.css
└─ js/common.js            ← compartido por TODAS las páginas autenticadas
```

**Convención**: cada página `X.html` carga **siempre primero** `common.js` y luego `X.js`:

```html
<script src="js/common.js"></script>
<script src="js/dashboard.js"></script>
```

**Flujo típico de cualquier página autenticada**:

```
DOMContentLoaded
   ↓
requireAuth()  ──si no hay token──→  redirige a /login (return null)
   ↓
renderNavbar(usuario)  →  inyecta el sidebar lateral
   ↓
authFetch(API_BASE + '/recurso')  →  GET/POST al backend con JWT
   ↓
await resp.json()  →  pintar tabla / rellenar formulario
```

---

## 2. `js/common.js` — utilidades compartidas

Este archivo **no define ninguna página**: expone funciones y constantes que usan todos los demás JS. Es el único archivo compartido.

### 2.1 Constantes globales

```js
const API_BASE = '/api';
const MAPA_ESTADO_SEMAFORO = {
  VERDE:    { estado: 'Vigente',    clase: 'badge-vigente'    },
  AMARILLO: { estado: 'Por vencer', clase: 'badge-por-vencer' },
  ROJO:     { estado: 'Crítico',    clase: 'badge-critico'    },
  AGOTADO:  { estado: 'Agotado',    clase: 'badge-agotado'    }
};
const CLAVES = { TOKEN: 'inv_token', USUARIO: 'inv_usuario' };
```

| Constante | Significado |
|---|---|
| `API_BASE` | Prefijo de todos los endpoints REST. Todas las llamadas son `API_BASE + '/insumos'`, etc. |
| `MAPA_ESTADO_SEMAFORO` | Mapea el **enum `EstadoSemaforo`** que el backend devuelve en cada `LoteResponseDTO` (`VERDE`/`AMARILLO`/`ROJO`/`AGOTADO`) a su etiqueta legible y clase CSS para pintar el badge. **El cálculo de umbrales se hace en el backend** (ver §2.5). |
| `CLAVES` | Nombres de las dos claves de `localStorage` donde se guarda la sesión. Centralizados para evitar errores de tipeo. |

---

### 2.2 Sesión y autenticación

#### `getToken(): string | null`

Devuelve el JWT almacenado en `localStorage` bajo la clave `inv_token`.

```js
function getToken() {
  return localStorage.getItem(CLAVES.TOKEN);
}
```

- **Parámetros**: ninguno.
- **Retorno**: `string` (el token) o `null` si no hay sesión.
- **Uso**: internamente en `authFetch`; raramente se llama directo desde una página.

---

#### `getUsuario(): object | null`

Devuelve el objeto usuario guardado en `localStorage` bajo `inv_usuario`, ya parseado desde JSON.

```js
function getUsuario() {
  const raw = localStorage.getItem(CLAVES.USUARIO);
  try { return raw ? JSON.parse(raw) : null; }
  catch (e) { return null; }
}
```

- **Parámetros**: ninguno.
- **Retorno**: `{ id, nombre, email, rol }` o `null`.
- **Por qué try/catch**: si por corrupción de datos el JSON queda malformado, devuelve `null` en vez de tirar la página.
- **Uso**: en `requireAuth`, `renderNavbar`, `asegurarUsuarioId`.

---

#### `guardarSesion(loginResponse: object): void`

Persiste la sesión tras un login exitoso. Recibe el cuerpo de la respuesta de `POST /api/auth/login`.

```js
function guardarSesion(loginResponse) {
  localStorage.setItem(CLAVES.TOKEN, loginResponse.token);
  localStorage.setItem(CLAVES.USUARIO, JSON.stringify({
    id: loginResponse.id || null,
    nombre: loginResponse.nombre,
    email: loginResponse.email,
    rol: loginResponse.rol
  }));
}
```

- **Parámetro `loginResponse`**: `{ token, id, nombre, email, rol }` (lo que devuelve `LoginResponseDTO`).
- **Retorno**: nada.
- **Por qué solo guarda 4 campos del usuario**: el `LoginResponseDTO` del backend puede traer más, pero solo nos interesa el mínimo para pintar el sidebar y autorizar.
- **Uso**: solo en `login.js`.

---

#### `requireAuth(): object | null`

**Portero de cada página autenticada**. Si no hay token o usuario válido, redirige a `/login` y devuelve `null`; si hay sesión, devuelve el usuario.

```js
function requireAuth() {
  const token = getToken();
  const usuario = getUsuario();
  if (!token || !usuario) {
    window.location.href = 'login';
    return null;
  }
  return usuario;
}
```

- **Retorno**: el usuario (`{ id, nombre, email, rol }`) o `null` (y en ese caso ya navegó a login).
- **Patón de uso obligatorio**:

```js
const usuario = requireAuth();
if (!usuario) return;       // ← SIEMPRE este guard, si no la página sigue ejecutándose
renderNavbar(usuario);
```

- **Por qué el `if (!usuario) return`**: `window.location.href = 'login'` **no detiene el JS** inmediatamente (la navegación es asíncrona). Sin el `return`, el código siguiente ejecutaría con `usuario === null` y rompería.

---

#### `requerirRol(rolEsperado: string): object | null`

Igual que `requireAuth`, pero además verifica el **rol**. Si el rol no coincide, redirige a `/dashboard`.

```js
function requerirRol(rolEsperado) {
  const usuario = requireAuth();           // primero, ¿hay sesión?
  if (!usuario) return null;
  if (usuario.rol !== rolEsperado) {       // luego, ¿tiene el rol?
    window.location.href = 'dashboard';
    return null;
  }
  return usuario;
}
```

- **Parámetro `rolEsperado`**: `'SUPERVISOR'` o `'ENFERMERIA'`.
- **Uso**: en `usuarios.js` (página exclusiva de SUPERVISOR).
- **Doble defensa**: la UI se oculta aquí, **y** el backend refuerza con `@PreAuthorize("hasRole('SUPERVISOR')")`. Alguien podría llamar a `/api/usuarios` con `curl` sin la página.

---

#### `logout(): void`

Borra las dos claves de sesión y redirige al login.

```js
function logout() {
  localStorage.removeItem(CLAVES.TOKEN);
  localStorage.removeItem(CLAVES.USUARIO);
  window.location.href = 'login';
}
```

- **Uso**: botón "Cerrar sesión" del sidebar (definido en `renderNavbar`), y en `authFetch` cuando llega un `401` (token caducado).

---

### 2.3 Llamadas HTTP

#### `authFetch(url, options?): Promise<Response> | null`

**La envoltura central de todas las peticiones HTTP autenticadas.** Añade automáticamente la cabecera `Authorization: Bearer <token>` y maneja 401 y errores de red.

```js
async function authFetch(url, options = {}) {
  const token = getToken();
  if (!token) { logout(); return null; }                       // ① sin sesión

  const headers = Object.assign({}, options.headers || {});   // ② copia headers
  headers['Authorization'] = 'Bearer ' + token;

  if (options.body && typeof options.body === 'string'
      && !headers['Content-Type']) {                          // ③ Content-Type
    headers['Content-Type'] = 'application/json';
  }

  let resp;
  try {
    resp = await fetch(url, Object.assign({}, options, { headers }));  // ④ fetch
  } catch (e) {
    mostrarErrorGlobal('No se pudo conectar con el servidor.');
    return null;                                              // ⑤ red caída
  }

  if (resp.status === 401) { logout(); return null; }         // ⑥ token expirado

  return resp;                                                // ⑦ Response lista
}
```

- **Parámetros**:
  - `url` — ruta relativa, normalmente `API_BASE + '/insumos'`.
  - `options` — mismo objeto que el segundo argumento de `fetch`: `{ method, body, headers }`.
- **Retorno**: una `Promise<Response>` resuelta, o `null` en dos casos especiales:
  - no había token → redirigió a login,
  - falló la red → mostró error global.
- **Patrón de uso SIEMPRE**:

```js
const resp = await authFetch(API_BASE + '/insumos');
if (!resp || !resp.ok) { /* mostrar error */ return; }
const data = await resp.json();
```

- **Por qué `!resp`**: `fetch` rechaza su Promesa solo si falla la **red** (DNS, conexión rechazada). Un `404` o `401` es una Response exitosa. `authFetch` devuelve `null` solo en los dos casos especiales; por eso se comprueba `!resp` antes de `resp.ok`.
- **Por qué `Object.assign({}, options.headers || {})`:** copia sin mutar el objeto original del llamador (buenas prácticas: nunca mutar argumentos).

---

#### `getUsuarioId(): string | null`

Devuelve el `id` del usuario logueado, leído del objeto en localStorage.

```js
function getUsuarioId() {
  const u = getUsuario();
  return u ? u.id : null;
}
```

- **Uso**: en formularios que necesitan enviar `usuarioId` al backend (lote-form, movimiento-wizard).

---

#### `asegurarUsuarioId(): Promise<string | null>`

Devuelve el id del usuario logueado, **y si por compatibilidad hacia atrás no está en localStorage**, lo resuelve consultando `GET /api/usuarios` por email (sólo SUPERVISOR puede).

```js
async function asegurarUsuarioId() {
  const u = getUsuario();
  if (!u) return null;
  if (u.id) return u.id;                          // caso normal
  // Fallback: usuarios con sesión vieja sin id
  try {
    const resp = await authFetch(API_BASE + '/usuarios');
    if (!resp || !resp.ok) return null;
    const usuarios = await resp.json();
    const encontrado = (usuarios || []).find(x => x.email === u.email);
    if (encontrado) {
      u.id = encontrado.id;
      localStorage.setItem(CLAVES.USUARIO, JSON.stringify(u));  // actualiza
      return encontrado.id;
    }
  } catch (e) { /* ignorar */ }
  return null;
}
```

- **Retorno**: el UUID del usuario o `null`.
- **Efecto secundario**: si lo resolvió vía API, **actualiza localStorage** para no volver a consultarlo.
- **Uso**: en `lote-form.js` y `movimiento-wizard.js`, porque el backend exige `usuarioId` en el DTO.

---

### 2.4 Navbar / Sidebar

#### `renderNavbar(usuario: object): void`

Inyecta en `<div id="navbar"></div>` el HTML del sidebar lateral fijo. El sidebar se construye **en JS** (no en HTML estático) para poder marcar el enlace activo según la URL actual.

```js
function renderNavbar(usuario) {
  const nav = document.getElementById('navbar');
  if (!nav) return;
  const esSupervisor = usuario && usuario.rol === 'SUPERVISOR';
  let pathActual = window.location.pathname.split('/').pop() || 'dashboard';
  pathActual = pathActual.replace(/\.html$/, '');
  // ... construye items, linksHtml, y el innerHTML final
}
```

- **Parámetro `usuario`**: el objeto devuelto por `requireAuth()`.
- **Lógica de "ruta activa"**: compara el path actual (sin extensión) con el `href` de cada item; si coincide, añade la clase `active`.
- **Items del menú**:

| href | label | icon | visible para |
|---|---|---|---|
| `dashboard` | Dashboard | `bi-speedometer2` | todos |
| `inventario` | Inventario | `bi-boxes` | todos |
| `lotes` | Lotes | `bi-layers` | todos |
| `lote-form` | Registrar lote | `bi-plus-circle` | todos |
| `movimiento-wizard` | Registrar movimiento | `bi-arrow-left-right` | todos |
| `usuarios` | Usuarios | `bi-people` | solo SUPERVISOR |

- **Sidebar responsive**: en móvil (`<992px`) el sidebar se oculta con `transform: translateX(-100%)` y se abre con el botón hamburger (`.app-sidebar-toggle`); el backdrop oscuro cierra al hacer clic.
- **Listeners que registra**:
  - `btnLogout` → `logout()`.
  - `sidebarToggle` → `abrirSidebar()`.
  - `sidebarBackdrop` → `cerrarSidebar()`.

---

### 2.5 Semáforo (mapeo del enum del backend)

> **Fuente única de verdad**: el cálculo de umbrales (verde/amarillo/rojo) lo hace el **backend** con `Lote.calcularEstadoSemaforo(ConfiguracionSemaforo, LocalDate)` y los valores configurables de la tabla `configuracion_semaforo`. El `LoteResponseDTO` ya trae `estado` (enum `EstadoSemaforo`) y `diasRestantes`. El front **solo mapea** el enum a etiqueta legible + clase CSS para pintar.
>
> Antes los umbrales estaban **duplicados y hardcodeados** en `SEMAFORO_UMBRALES`; si un admin cambiaba la configuración en la DB, el front seguía usando 90/30. Ahora esa duplicación se eliminó.

#### `calcularEstadoSemaforo(lote): { estado, clase, diasRestantes }`

```js
function calcularEstadoSemaforo(lote) {
  const codigo = lote.estado || 'AGOTADO';
  const mapeo = MAPA_ESTADO_SEMAFORO[codigo] || MAPA_ESTADO_SEMAFORO.AGOTADO;
  return {
    estado: mapeo.estado,
    clase: mapeo.clase,
    diasRestantes: (lote.diasRestantes === undefined ? null : lote.diasRestantes)
  };
}
```

- **Parámetro `lote`**: un `LoteResponseDTO` con `estado` (enum string: `VERDE`/`AMARILLO`/`ROJO`/`AGOTADO`) y `diasRestantes` (`Long` o `null` cuando está agotado).
- **Retorno**:

| campo | tipo | valores |
|---|---|---|
| `estado` | string | `'Vigente'` / `'Por vencer'` / `'Crítico'` / `'Agotado'` |
| `clase` | string | clase CSS para el badge (`badge-vigente`, etc.) |
| `diasRestantes` | number\|null | días hasta vencer; `null` si está agotado |

- **Por qué sigue existiendo esta función** (aunque ya no calcula): mantiene la **misma firma** de retorno que tenían los callers, así `dashboard.js`, `inventario.js`, `lotes.js` y `movimiento-wizard.js` no cambiaron. Solo cambió la implementación interna: antes computaba con umbrales hardcodeados, ahora mapea el enum del backend.
- **Uso**: dashboard, inventario, lotes, movimiento-wizard.

---

#### `severidadSemaforo(estado): number`

Convierte el estado en un número para comparar cuál es el "peor" estado entre los lotes de un insumo. Mayor = más urgente.

```js
function severidadSemaforo(estado) {
  switch (estado) {
    case 'Crítico':     return 3;
    case 'Por vencer':  return 2;
    case 'Vigente':     return 1;
    case 'Agotado':     return 0;
    default:            return -1;
  }
}
```

- **Uso**: en `inventario.js` para calcular el estado de un **insumo** a partir de sus lotes (el insumo hereda el peor estado de sus lotes).

---

#### `badgeSemaforoHtml(lote): string`

Devuelve el HTML de un `<span class="badge">` con el color y los días restantes.

```js
function badgeSemaforoHtml(lote) {
  const s = calcularEstadoSemaforo(lote);
  let extra = '';
  if (s.diasRestantes !== null) {
    if (s.diasRestantes < 0)      extra = ' · vencido hace ' + Math.abs(s.diasRestantes) + ' d';
    else if (s.diasRestantes === 0) extra = ' · vence hoy';
    else                          extra = ' · ' + s.diasRestantes + ' d';
  }
  return '<span class="badge badge-sem ' + s.clase + '">'
       + escapeHtml(s.estado) + escapeHtml(extra) + '</span>';
}
```

- **Retorno**: un string HTML ya listo para inyectar con `innerHTML`.
- **Ejemplo de salida**: `<span class="badge badge-sem badge-critico">Crítico · 12 d</span>`.
- **Por qué `escapeHtml`**: aunque el estado y los días son valores internos, se escapan por defensa ante futuros cambios.

---

### 2.6 Utilidades de fecha y texto

#### `formatFecha(fecha): string`

Formatea una fecha ISO (`'2025-12-31'` o `'2025-12-31T10:00:00'`) al formato legible `'31/12/2025'`.

```js
function formatFecha(fecha) {
  if (!fecha) return '';
  const dia = (fecha || '').split('T')[0];
  const partes = dia.split('-');
  if (partes.length !== 3) return fecha;
  return partes[2] + '/' + partes[1] + '/' + partes[0];
}
```

- **Retorno**: string vacío si no hay fecha; el original si no parsea; `dd/mm/yyyy` si todo bien.
- **Uso**: en todas las tablas que muestran vencimientos.

---

#### `escapeHtml(s): string`

Escapa los 5 caracteres peligrosos para HTML (`& < > " '`) para evitar **XSS** al inectar texto del backend con `innerHTML`.

```js
function escapeHtml(s) {
  if (s === null || s === undefined) return '';
  return String(s).replace(/[&<>"']/g, function (c) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
  });
}
```

- **Regla de oro**: cualquier texto que venga del backend (nombres de insumos, observaciones, etc.) **debe** pasarse por `escapeHtml` antes de concatenarse en un string HTML. Ejemplo:

```js
// BIEN:
'<td>' + escapeHtml(insumo.nombre) + '</td>'

// MAL (XSS si el nombre contiene <script>):
'<td>' + insumo.nombre + '</td>'
```

---

#### `parametrosUrl(): object`

Lee los query params de la URL actual en un objeto plano.

```js
function parametrosUrl() {
  const params = new URLSearchParams(window.location.search);
  const o = {};
  params.forEach(function (v, k) { o[k] = v; });
  return o;
}
```

- **Ejemplo**: URL `/insumo-form?id=abc-123` → `{ id: 'abc-123' }`.
- **Uso**: en `insumo-form.js` para detectar edición (`?id=...`).

---

### 2.7 Manejo de errores del backend

El `GlobalExceptionHandler` de Spring devuelve errores en este formato:

```json
// 404 / 409 (regla de negocio)
{ "timestamp": "...", "status": 404, "mensaje": "...", "campos": null }

// 400 (validación de campos)
{ "timestamp": "...", "status": 400, "mensaje": "Validación de campos fallida",
  "campos": { "nombre": "no debe estar vacío", "email": "debe ser un correo válido" } }
```

#### `parsearErrorApi(resp): Promise<{ mensaje, campos }>`

Extrae `{ mensaje, campos }` del cuerpo de error.

```js
async function parsearErrorApi(resp) {
  let data = null;
  try { data = await resp.json(); } catch (e) { /* cuerpo no JSON */ }
  if (!data) return { mensaje: 'Error inesperado (HTTP ' + resp.status + ')', campos: null };
  return {
    mensaje: data.mensaje || ('Error HTTP ' + resp.status),
    campos: data.campos || null
  };
}
```

- **Parámetro `resp`**: el objeto `Response` devuelto por `authFetch` (cuando no fue `ok`).
- **Retorno**: `{ mensaje: string, campos: object|null }`.
- **Por qué try/catch en `resp.json()`**: si el backend responde un error 500 con cuerpo HTML (proxy, gateway), `json()` fallaría; en ese caso devolvemos un mensaje genérico.
- **Uso**: en todos los formularios, dentro del `try`:

```js
const err = await parsearErrorApi(resp);
mostrarErrorForm(err.mensaje, err.campos);
```

---

#### `mostrarErrorForm(mensaje, campos?): void`

Pinta una alerta Bootstrap `alert-danger` dentro del contenedor `#formError`. Si hay `campos` (validación), los lista debajo.

```js
function mostrarErrorForm(mensaje, campos) {
  const cont = document.getElementById('formError');
  if (!cont) { alert(mensaje + (campos ? '\n' + JSON.stringify(campos) : '')); return; }
  let html = '<div class="alert alert-danger alert-dismissible fade show" role="alert"><div>'
           + escapeHtml(mensaje) + '</div>';
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
```

- **Parámetros**:
  - `mensaje` — texto principal del error.
  - `campos` (opcional) — objeto `{ campo: detalle }` que viene del backend en errores 400 de validación.
- **Fallback**: si no existe `#formError` en la página, hace un `alert()` nativo.
- **Uso**: en todos los formularios (login, insumo-form, lote-form, movimiento-wizard, usuarios).

---

#### `limpiarErrorForm(): void`

Vacía el contenedor `#formError`. Se llama al inicio de cada submit para borrar errores anteriores.

```js
function limpiarErrorForm() {
  const cont = document.getElementById('formError');
  if (cont) cont.innerHTML = '';
}
```

---

#### `mostrarErrorGlobal(mensaje): void`

Pinta una alerta en `#errorGlobal` (a nivel de página completa, no de formulario). Si no existe, hace `console.error`.

```js
function mostrarErrorGlobal(mensaje) {
  let cont = document.getElementById('errorGlobal');
  if (cont) {
    cont.innerHTML = '<div class="alert alert-danger" role="alert">' + escapeHtml(mensaje) + '</div>';
  } else {
    console.error(mensaje);
  }
}
```

- **Uso**: errores de carga de página (fallo al pedir lista, contexto caído), no de validación de formulario.

---

### 2.8 Toasts entre páginas

Como la navegación es con `window.location.href` (no es SPA), al cambiar de página **se pierde el contexto JS**. Para mostrar "Lote registrado" en el dashboard tras venir del formulario, se usa `sessionStorage` (efímero: se borra al cerrar la pestaña).

#### `marcarToast(clave, mensaje): void`

Guarda un mensaje en `sessionStorage` bajo una clave, para que la página destino lo lea.

```js
function marcarToast(clave, mensaje) {
  sessionStorage.setItem(clave, mensaje);
}
```

- **Convención de claves**: `toast_dashboard`, `toast_inventario` — indican en qué página se debe consumir.
- **Uso**: antes de `window.location.href = 'dashboard'`.

---

#### `consumirToast(clave): string | null`

Lee y borra el mensaje guardado. Es **consumir** (no solo leer): después de leerlo lo elimina para que no se vuelva a mostrar en recargas.

```js
function consumirToast(clave) {
  const m = sessionStorage.getItem(clave);
  if (m) sessionStorage.removeItem(clave);
  return m;
}
```

- **Uso**: al inicio del `DOMContentLoaded` de la página destino:

```js
const toast = consumirToast('toast_dashboard');
if (toast) {
  document.getElementById('toastExito').innerHTML = '<div class="alert alert-success ...">' + ...;
}
```

---

## 3. `js/login.js` — inicio de sesión

Página: `login.html`. **No requiere sesión previa** (es la puerta de entrada).

### Flujo completo

```js
document.addEventListener('DOMContentLoaded', function () {
  // 1. Si ya hay sesión, saltar al dashboard
  if (getToken() && getUsuario()) {
    window.location.href = 'dashboard';
    return;
  }
  // 2. Referencias al DOM
  const form = document.getElementById('loginForm');
  // ... email, pass, btn, spinner, btnToggle, iconToggle
  // 3. Toggle mostrar/ocultar contraseña
  btnToggle.addEventListener('click', function () { ... });
  // 4. Submit → POST /auth/login
  form.addEventListener('submit', async function (e) { ... });
});
```

### Método principal: submit del formulario

```js
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
    const resp = await fetch(API_BASE + '/auth/login', {     // ← fetch DIRECTO, no authFetch
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value.trim(), password: pass.value })
    });

    if (resp.ok) {
      const data = await resp.json();
      guardarSesion(data);                                   // persiste token + usuario
      window.location.href = 'dashboard';
      return;
    }
    const err = await parsearErrorApi(resp);
    mostrarErrorForm(err.mensaje || 'Credenciales incorrectas.');
  } catch (e) {
    mostrarErrorForm('No se pudo conectar con el servidor. Intente nuevamente.');
  } finally {
    btn.disabled = false;
    spinner.classList.add('d-none');
  }
});
```

**Notas clave**:
- Login usa `fetch` **directo** (no `authFetch`) porque aún no hay token que añadir.
- `finally` **siempre** rehabilita el botón y oculta el spinner, incluso si falló.
- `guardarSesion(data)` se llama solo en el `resp.ok` — el backend responde 200 con el token.

### Toggle de contraseña

```js
btnToggle.addEventListener('click', function () {
  const esPass = pass.type === 'password';
  pass.type = esPass ? 'text' : 'password';
  iconToggle.className = esPass ? 'bi bi-eye-slash' : 'bi bi-eye';
});
```

Cambia el `type` del input entre `password` y `text`, y el ícono entre `bi-eye` y `bi-eye-slash`.

---

## 4. `js/dashboard.js` — panel de control

Página: `dashboard.html`.

### Flujo

```js
document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  // 1. Toast opcional (dejado por otra página vía sessionStorage)
  const toast = consumirToast('toast_dashboard');
  if (toast) { /* pintar alert success */ }

  // 2. Pedir insumos y lotes EN PARALELO (Promise.all)
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

  // 3. Mapa insumoId → nombre
  const nombreInsumo = {};
  insumos.forEach(i => { nombreInsumo[i.id] = i.nombre; });

  // 4. Conteos por estado (solo lotes ACTIVOS)
  const conteo = { Vigente: 0, 'Por vencer': 0, 'Crítico': 0, Agotado: 0 };
  const criticos = [];
  lotes
    .filter(l => l.activo !== false)            // ← excluye lotes inactivos
    .forEach(function (lote) {
      const s = calcularEstadoSemaforo(lote);
      conteo[s.estado] = (conteo[s.estado] || 0) + 1;
      if (s.estado === 'Crítico') criticos.push({ lote, estado: s });
    });

  // 5. Pintar las 4 tarjetas
  document.getElementById('cntVigente').textContent = conteo['Vigente'];
  // ... cntPorVencer, cntCritico, cntAgotado

  // 6. Ordenar críticos por menor cantidad de días (vencidos primero)
  criticos.sort((a, b) => {
    const da = a.estado.diasRestantes === null ? Infinity : a.estado.diasRestantes;
    const db = b.estado.diasRestantes === null ? Infinity : b.estado.diasRestantes;
    return da - db;
  });

  // 7. Pintar tabla de urgentes
  tbody.innerHTML = criticos.map(c => { ... }).join('');
});
```

**Notas**:
- `Promise.all` lanza las dos peticiones a la vez — reduce el tiempo de carga.
- `nombreInsumo[l.insumoId]` resuelve el nombre del insumo en cada fila crítica (los lotes solo guardan el `insumoId`).
- El filtro `l.activo !== false` hace que un lote deshabilitado **no cuente** en las tarjetas ni en la tabla.

---

## 5. `js/inventario.js` — gestión de insumos

Página: `inventario.html`. Lista, filtra y desactiva insumos.

### Estructura

```js
document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth(); if (!usuario) return;
  renderNavbar(usuario);

  const tbody = document.getElementById('tablaInsumos');
  let insumos = [];
  let lotes = [];

  // 1. Cargar insumos + lotes en paralelo
  const [respIns, respLot] = await Promise.all([
    authFetch(API_BASE + '/insumos'),
    authFetch(API_BASE + '/lotes')
  ]);
  insumos = await respIns.json();
  lotes = (respLot && respLot.ok) ? await respLot.json() : [];

  // 2. Listeners de los 3 filtros (nombre, tipo, estado)
  ['filtroNombre', 'filtroTipo', 'filtroEstado'].forEach(id => {
    document.getElementById(id).addEventListener('input', render);
    document.getElementById(id).addEventListener('change', render);
  });

  // 3. estadoInsumo(ins): calcula el "peor" estado entre los lotes del insumo
  function estadoInsumo(ins) { ... }

  // 4. render(): filtra y pinta la tabla
  function render() { ... }

  render();   // pintado inicial
});
```

### `estadoInsumo(ins): { estado, clase }`

El estado del **insumo** es el **peor** estado entre sus lotes (Crítico > Por vencer > Vigente > Agotado). Si no tiene lotes, "Sin lotes". Si el insumo está inactivo, "Inactivo" predomina.

```js
function estadoInsumo(ins) {
  // Solo se consideran lotes ACTIVOS del insumo
  const lotesIns = lotes.filter(l => l.insumoId === ins.id && l.activo !== false);
  if (!lotesIns.length) return { estado: 'Sin lotes', clase: 'badge-agotado' };

  let peor = { estado: 'Agotado', sev: 0 };
  lotesIns.forEach(l => {
    const s = calcularEstadoSemaforo(l);
    const sev = severidadSemaforo(s.estado);
    if (sev > peor.sev) peor = { estado: s.estado, sev, clase: s.clase };
  });
  // ... normaliza
  if (!ins.activo) return { estado: 'Inactivo', clase: 'badge-inactivo' };
  return { estado: peor.estado, clase: peor.clase };
}
```

### `render()`

Filtra `insumos` según los 3 inputs (`filtroNombre`, `filtroTipo`, `filtroEstado`) y pinta la tabla. Cada fila incluye dos botones de acción:

- **Editar**: enlace `<a href="insumo-form?id={id}">`.
- **Desactivar**: botón que llama a `DELETE /api/insumos/{id}` (baja lógica en el backend).

```js
tbody.querySelectorAll('button[data-id]').forEach(btn => {
  btn.addEventListener('click', async function () {
    const id = btn.getAttribute('data-id');
    if (!confirm('¿Desactivar el insumo "..."?')) return;
    const resp = await authFetch(API_BASE + '/insumos/' + id, { method: 'DELETE' });
    if (resp.ok || resp.status === 204) {
      insumos = insumos.map(x => x.id == id ? { ...x, activo: false } : x);  // actualiza local
      render();
      pintarToast('Insumo desactivado.');
    } else {
      const err = await parsearErrorApi(resp);
      mostrarErrorGlobal(err.mensaje);
    }
  });
});
```

**Optimización**: tras desactivar, **no recarga la página** — actualiza el array local y re-renderiza. La baja lógica es inmediata en la UI.

---

## 6. `js/insumo-form.js` — crear/editar insumo

Página: `insumo-form.html`. **Detecta** si es edición por la presencia de `?id=` en la URL.

### Flujo

```js
document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth(); if (!usuario) return;
  renderNavbar(usuario);

  const params = parametrosUrl();
  const idEdicion = params.id || null;
  document.getElementById('tituloForm').textContent = idEdicion ? 'Editar insumo' : 'Nuevo insumo';

  // 1. Si edición → GET /insumos/{id} y precargar el formulario
  if (idEdicion) {
    const resp = await authFetch(API_BASE + '/insumos/' + idEdicion);
    // ... rellenar todos los campos
  }

  // 2. Submit → POST (crear) o PUT (editar)
  form.addEventListener('submit', async function (e) {
    e.preventDefault();
    limpiarErrorForm();

    const payload = { ... };   // leer valores del DOM
    const opts = {
      method: idEdicion ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    };
    const url = idEdicion ? API_BASE + '/insumos/' + idEdicion : API_BASE + '/insumos';

    try {
      const resp = await authFetch(url, opts);
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
```

**Notas**:
- `parseInt(valor, 10) || 0` para `stockMinimo`: si el input está vacío o no es número, usa 0 (el backend rechazaría con validación).
- `registroInvima` es opcional → se envía `null` si está vacío.
- El **mismo** script sirve para crear y editar, diferenciado solo por `?id=`.

---

## 7. `js/lote-form.js` — registrar lote

Página: `lote-form.html`. Registra un lote nuevo + genera automáticamente un movimiento de ENTRADA (lo hace el backend).

### Flujo

```js
document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth(); if (!usuario) return;
  renderNavbar(usuario);

  // 1. Resolver el usuarioId (requerido por el DTO)
  const usuarioId = await asegurarUsuarioId();
  if (!usuarioId) {
    mostrarErrorGlobal('No se pudo determinar el usuario logueado.');
    return;
  }

  // 2. Bloquear fechas pasadas en el input date
  const hoy = new Date().toISOString().split('T')[0];
  document.getElementById('fechaVencimiento').setAttribute('min', hoy);

  // 3. Cargar insumos en el <select>, solo los activos
  const respIns = await authFetch(API_BASE + '/insumos');
  const insumos = await respIns.json();
  const activos = insumos.filter(i => i.activo);
  selectIns.innerHTML = '<option value="">Seleccione...</option>' +
    activos.map(i => '<option value="' + i.id + '">' + escapeHtml(i.nombre + ' · ' + i.presentacion) + '</option>').join('');

  // 4. Submit → POST /lotes
  form.addEventListener('submit', async function (e) {
    e.preventDefault();
    const payload = {
      insumoId: selectIns.value,
      usuarioId: usuarioId,
      numeroLote: ...,
      fechaVencimiento: ...,
      cantidadInicial: parseInt(..., 10) || 0,
      observacion: ... || null,
      ubicacion: ...
    };
    // ... POST + marcarToast('toast_dashboard', 'Lote registrado...') + redirigir
  });
});
```

**Notas**:
- `setAttribute('min', hoy)` impite seleccionar una fecha de vencimiento ya pasada.
- El backend, al recibir el lote, **crea también** el `Movimiento` de ENTRADA con la cantidad inicial (ver `LoteService.registrarLote`).

---

## 8. `js/movimiento-wizard.js` — asistente de movimiento

Página: `movimiento-wizard.html`. Asistente de **3 pasos**: insumo → lote → tipo/cantidad.

### Estado interno

```js
let insumos = [];
let loteSeleccionado = null;
let insumoSeleccionado = null;
```

### Paso 1: elegir insumo

```js
const buscar = document.getElementById('buscarInsumo');
buscar.addEventListener('input', renderListaInsumos);

function renderListaInsumos() {
  const q = (buscar.value || '').toLowerCase();
  const items = insumos.filter(i => !q || i.nombre.toLowerCase().includes(q));
  listaIns.innerHTML = items.map(i =>
    '<button data-id="' + i.id + '">' + ... + '</button>'
  ).join('');
  listaIns.querySelectorAll('button[data-id]').forEach(b => {
    b.addEventListener('click', () => {
      insumoSeleccionado = insumos.find(x => x.id === b.getAttribute('data-id'));
      irAPaso2();
    });
  });
}
```

### Paso 2: elegir lote

```js
async function irAPaso2() {
  // actualizar clases de los indicadores (step1Ind → done, step2Ind → active)
  const resp = await authFetch(API_BASE + '/lotes/por-insumo/' + insumoSeleccionado.id);
  const lotes = await resp.json();
  // SOLO lotes con stock Y activos
  const disponibles = lotes.filter(l => l.cantidadActual > 0 && l.activo !== false);
  // pintar lista, click → irAPaso3
}
```

### Paso 3: tipo y cantidad

```js
function irAPaso3() {
  // pintar info del lote + badge semáforo
  actualizarLimitesCantidad();
}

function actualizarLimitesCantidad() {
  if (tipoMov.value === 'SALIDA') {
    const max = loteSeleccionado.cantidadActual;
    cantidad.setAttribute('max', String(max));            // limita el input
    cantidad.value = Math.min(parseInt(cantidad.value, 10) || 1, max);
    hint.textContent = 'Máximo disponible: ' + max;
  } else {
    cantidad.removeAttribute('max');
    hint.textContent = 'La entrada aumenta el stock del lote.';
  }
}
```

### Confirmar

```js
btnConfirmar.addEventListener('click', async function () {
  const cant = parseInt(cantidad.value, 10) || 0;
  if (cant <= 0) { mostrarErrorForm('La cantidad debe ser mayor que cero.'); return; }
  if (tipoMov.value === 'SALIDA' && cant > loteSeleccionado.cantidadActual) {
    mostrarErrorForm('La cantidad de salida excede el stock disponible.');
    return;
  }
  const payload = { loteId, usuarioId, tipo, cantidad: cant, observacion };
  // ... POST /movimientos + marcarToast + redirigir
});
```

**Notas**:
- Las validaciones de cantidad **se hacen en el cliente** (UX) **y** el backend las repite (seguridad).
- `irAPaso1` / `irAPaso2` / `irAPaso3` manejan las transiciones de visibilidad (`.d-none`) y los indicadores del wizard.

---

## 9. `js/usuarios.js` — gestión de usuarios

Página: `usuarios.html`. **Exclusiva SUPERVISOR**.

### Estructura

```js
document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requerirRol('SUPERVISOR');   // ← portero con rol
  if (!usuario) return;
  renderNavbar(usuario);

  const modal = new bootstrap.Modal(document.getElementById('modalUsuario'));

  async function cargar() {
    const resp = await authFetch(API_BASE + '/usuarios');
    usuarios = await resp.json();
    tbody.innerHTML = usuarios.map(u => { ... }).join('');
  }
  cargar();

  // Botón "Nuevo usuario" → abre modal vacío
  document.getElementById('btnNuevo').addEventListener('click', () => { ... });

  // Botón "Guardar" → POST /usuarios
  btnGuardar.addEventListener('click', async function () {
    const payload = { nombre, email, password, rol, activo };
    if (!payload.nombre || !payload.email || !payload.password) {
      mostrarErrorForm('Complete todos los campos obligatorios.');
      return;
    }
    // ... POST + recargar lista + toast
  });
});
```

**Notas**:
- `new bootstrap.Modal(modalEl)` instancia el modal de Bootstrap 5 por JS (no requiere `data-bs-toggle`).
- Tras guardar exitosamente, se llama `await cargar()` para refrescar la lista sin recargar la página.

---

## 10. `js/lotes.js` — gestión de lotes y toggle activo

Página: `lotes.html` (nueva). Lista todos los lotes y permite **habilitar/deshabilitar** (baja lógica, sin tocar stock ni trazabilidad).

### Flujo

```js
document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth(); if (!usuario) return;
  renderNavbar(usuario);

  // 1. Cargar insumos + lotes en paralelo (para resolver nombres)
  const [respIns, respLot] = await Promise.all([
    authFetch(API_BASE + '/insumos'),
    authFetch(API_BASE + '/lotes')
  ]);
  insumos = await respIns.json();
  lotes = await respLot.json();
  // Mapa insumoId → nombre
  insumos.forEach(i => { nombreInsumo[i.id] = i.nombre; });

  // 2. Rellenar <select> de insumos para el filtro
  selInsumo.innerHTML = '<option value="">Todos los insumos</option>' +
    insumos.map(i => '<option value="' + i.id + '">' + escapeHtml(i.nombre) + '</option>').join('');

  // 3. Listeners de los 4 filtros
  ['filtroLote', 'filtroInsumo', 'filtroSemaforo', 'filtroActivo'].forEach(id => {
    document.getElementById(id).addEventListener('input', render);
    document.getElementById(id).addEventListener('change', render);
  });

  // 4. render(): filtra y pinta
  function render() { ... }

  render();
});
```

### `render()`

Filtra por número de lote, insumo, estado semáforo y estado activo. Cada fila tiene un botón toggle:

- Si el lote está **activo** → botón rojo `bi-lock` (deshabilitar).
- Si está **inactivo** → botón teal `bi-unlock` (habilitar).

```js
const toggleBtn = l.activo === false
  ? '<button data-id="' + l.id + '" data-activar="true"><i class="bi bi-unlock"></i></button>'
  : '<button data-id="' + l.id + '" data-activar="false"><i class="bi bi-lock"></i></button>';
```

### Handler del toggle

```js
tbody.querySelectorAll('button[data-id]').forEach(btn => {
  btn.addEventListener('click', async function () {
    const id = btn.getAttribute('data-id');
    const activar = btn.getAttribute('data-activar') === 'true';
    if (!confirm('¿Confirmas ' + (activar ? 'habilitar' : 'deshabilitar') + ' este lote?')) return;

    const resp = await authFetch(API_BASE + '/lotes/' + id + '/activo', {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ activo: activar })
    });
    if (resp.ok) {
      lotes = lotes.map(x => x.id === id ? { ...x, activo: activar } : x);  // actualiza local
      render();
      pintarToast('Lote ' + (activar ? 'habilitado' : 'deshabilitado') + '.');
    } else {
      const err = await parsearErrorApi(resp);
      mostrarErrorGlobal(err.mensaje);
    }
  });
});
```

**Endpoint usado**: `PATCH /api/lotes/{id}/activo` con body `{ "activo": true|false }`.

**Reglas de negocio**:
- El stock (`cantidadActual`) **no se toca**.
- Los movimientos históricos **se conservan**.
- Un lote inactivo queda excluido del dashboard, de la tabla de inventario y del wizard de salidas.

---

## 11. Patrones transversales

### 11.1 Patrón obligatorio de cada página autenticada

```js
document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();          // ① portero
  if (!usuario) return;                    // ② guard incondicional
  renderNavbar(usuario);                   // ③ sidebar

  // ... resto de la lógica
});
```

**Nunca** omitir el `if (!usuario) return;` — la redirección a login no detiene el JS.

### 11.2 Patrón de llamada HTTP

```js
const resp = await authFetch(API_BASE + '/recurso', { method: 'POST', ... });
if (!resp) return;                         // sin sesión o red caída
if (!resp.ok) {
  const err = await parsearErrorApi(resp);
  mostrarErrorForm(err.mensaje, err.campos);
  return;
}
const data = await resp.json();
```

### 11.3 Patrón de submit de formulario

```js
form.addEventListener('submit', async function (e) {
  e.preventDefault();                      // ← no recargar la página
  limpiarErrorForm();                      // borra errores anteriores

  btn.disabled = true;
  spinner.classList.remove('d-none');
  try {
    // ... authFetch + manejar respuesta
    if (resp.ok || resp.status === 201) {
      marcarToast('toast_X', 'Mensaje.');
      window.location.href = 'X';
      return;
    }
    const err = await parsearErrorApi(resp);
    mostrarErrorForm(err.mensaje, err.campos);
  } finally {
    btn.disabled = false;                  // ← SIEMPRE, aun si falló
    spinner.classList.add('d-none');
  }
});
```

### 11.4 Patrón de pintar tabla con `innerHTML`

```js
tbody.innerHTML = items.map(function (item) {
  return '<tr>' +
    '<td>' + escapeHtml(item.nombre) + '</td>' +     // ← escapeHtml SIEMPRE
    '<td>' + formatFecha(item.fecha) + '</td>' +
    '</tr>';
}).join('');
```

**Reglas**:
- Todo texto del backend → `escapeHtml(...)`.
- Fechas → `formatFecha(...)`.
- Badges de semáforo → `badgeSemaforoHtml(lote)` (ya devuelve el HTML).
- Al final, registrar listeners con `querySelectorAll('button[data-id]').forEach(...)`.

### 11.5 Patrón de optimismo en bajas lógicas

Tras desactivar/habilitar, **no** se recarga la página:

```js
insumos = insumos.map(x => x.id == id ? { ...x, activo: false } : x);
render();
pintarToast('...');
```

Se actualiza el array local y se re-renderiza. Más rápido y sin parpadeo.

### 11.6 Checklist para añadir una pantalla nueva

1. Crear `static/mipagina.html` con `<div id="navbar"></div>` y `<div class="container app-main">…</div>`.
2. Crear `static/js/mipagina.js` que empiece con `requireAuth()` + `renderNavbar(usuario)`.
3. **Todas** las llamadas HTTP por `authFetch(...)`, nunca `fetch()` directo.
4. Programar `submit`/`click` con `async function`, `try/catch/finally`, mostrar errores con `mostrarErrorForm`, éxitos con `marcarToast`.
5. Registrar la ruta limpia en `WebConfig.java` y permitir el path en `SecurityConfig.java`.
6. Añadir el enlace en el array `items` de `renderNavbar` (en `common.js`) si debe aparecer en el sidebar.

---

## Resumen de endpoints consumidos

| Endpoint | Consumido por | Método JS |
|---|---|---|
| `POST /api/auth/login` | login.js | `fetch` directo |
| `GET /api/insumos` | dashboard, inventario, lote-form, movimiento-wizard, lotes | `authFetch` |
| `GET /api/insumos/{id}` | insumo-form | `authFetch` |
| `POST /api/insumos` | insumo-form | `authFetch` |
| `PUT /api/insumos/{id}` | insumo-form | `authFetch` |
| `DELETE /api/insumos/{id}` | inventario | `authFetch` |
| `GET /api/lotes` | dashboard, inventario, lotes | `authFetch` |
| `GET /api/lotes/por-insumo/{id}` | movimiento-wizard | `authFetch` |
| `POST /api/lotes` | lote-form | `authFetch` |
| `PATCH /api/lotes/{id}/activo` | lotes | `authFetch` |
| `POST /api/movimientos` | movimiento-wizard | `authFetch` |
| `GET /api/usuarios` | usuarios, asegurarUsuarioId | `authFetch` |
| `POST /api/usuarios` | usuarios | `authFetch` |

**Endpoints disponibles en backend pero aún no consumidos por el front**:

| Endpoint | Pendiente de |
|---|---|
| `GET /api/lotes/{id}` | pantalla de detalle de lote (futura) |
| `GET /api/movimientos/por-lote/{loteId}` | pantalla de bitácora/historial de movimientos (futura) |
| `GET /api/usuarios/{id}` | edición de usuario (futura, requiere `PUT` en backend) |
