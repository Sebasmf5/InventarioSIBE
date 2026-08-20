# Documentación JS — Conexión con la API del backend

Esta guía explica, **de manera autocontenida**, los conceptos de JavaScript asíncrono (callbacks, promesas y `async/await`) y **cómo se aplicaron concretamente** en el frontend de InventarioSIBE. Al termine podrás replicar el patrón en un proyecto nuevo sin necesidad de más referencias.

---

## 1. Por qué JavaScript es asíncrono en el navegador

JavaScript es **un solo hilo**: ejecuta una instrucción a la vez, en un único call stack. Si una operación es lenta (p. ej. pedir datos a un servidor en internet, que puede tardar decenas o cientos de milisegundos), el hilo **se quedaría bloqueado** esperando — la interfaz se congelaría, los botones no responderían, la animación dejaría de moverse.

Para evitarlo, el navegador expone **APIs asíncronas** (como `fetch`, `setTimeout`, eventos del DOM) que delegan el trabajo a otros hilos del propio navegador y **devuelven el control al hilo principal de JS inmediatamente**. Cuando el trabajo termina, el navegador "**agenda**" una función nuestro para que se ejecute después, cuando el hilo principal esté libre.

Ese mecanismo de agenda es el **event loop** y la **cola de microtareas/macrotareas**. No necesitas implementarlo; sólo necesitas saber que para reaccionar a "algo que va a pasar en el futuro" usas **callbacks**, **Promesas** o **`async/await`**.

En InventarioSIBE **toda la comunicación con el backend es asíncrona** porque usa `fetch()` contra `http://localhost:8080/api/...`.

---

## 2. Callbacks (la forma clásica)

Un **callback** es, simplemente, una función que pasas como argumento para que **otra función la llame después**, cuando algo ocurra.

```js
// Ejemplo didáctico: callback que se llama cuando expira el timer
setTimeout(function () {
  console.log('Pasaron 2 segundos');
}, 2000);
```

`setTimeout` recibe: (1) el retraso en ms y (2) la función que debe ejecutarse al terminar. JS no se bloquea: agenda el callback y sigue.

En el proyecto **casi no usamos callbacks para HTTP** porque provocan el problema clásico del **callback hell** (anidamiento en cascada). Sí los usamos, en cambio, para **eventos del DOM**, que son "uno a uno" y no se anidan:

- `form.addEventListener('submit', function (e) { ... })` → el callback se ejecuta cuando el usuario envía el formulario.
- `boton.addEventListener('click', function () { ... })` → el callback se ejecuta al clic.
- `buscarInsumo.addEventListener('input', renderListaInsumos)` → cada vez que cambia el texto del buscador, se llama a `renderListaInsumos`.

Los callbacks son perfectos para eventos porque son **disparados por el usuario**, no por una cadena de operaciones. Para cadenas de operaciones (petición → procesar respuesta → otra petición) usamos **promesas**.

---

## 3. Promesas (Promise)

Una **Promesa** es un objeto que representa el resultado **futuro** de una operación asíncrona. Justo al crearla, la operación ya está en curso; el resultado puede llegar en tres estados:

- **pending** — aún no terminó.
- **fulfilled** — terminó bien; hay un valor disponible.
- **rejected** — terminó con error; hay un motivo (error).

Se consume con dos métodos encadenables:

```js
fetch(url)                       // devuelve una Promise<Response>
  .then(function (response) {    // .then se ejecuta si fulfilled
    return response.json();      // response.json() devuelve OTRA Promise
  })
  .then(function (data) {         // este .then recibe lo que resolvió la anterior
    console.log(data);
  })
  .catch(function (error) {      // captura cualquier rechazo de la cadena
    console.error('Falló:', error);
  })
  .finally(function () {          // corre siempre, haya o no error
    console.log('Listo');
  });
```

Puntos clave:
- `then`, `catch` y `finally` **devuelven una nueva Promesa**, por eso se encadenan con `.`.
- Si dentro de un `then` retornas **otra Promesa**, la cadena **espera** a esa Promesa. Esto resuelve el callback hell: cada paso va en un `then` plano, no anidado.
- `Promise.all([p1, p2])` devuelve una Promesa que se resuelve cuando **todas** se resuelven (o rechaza si una falla). Útil para lanzar peticiones en paralelo.

En el proyecto usamos `Promise.all` en `dashboard.js` y `inventario.js` para pedir insumos y lotes **al mismo tiempo** (no uno después del otro), reduciendo el tiempo de carga:

```js
const [respIns, respLot] = await Promise.all([
  authFetch(API_BASE + '/insumos'),
  authFetch(API_BASE + '/lotes')
]);
```

---

## 4. async / await (la forma moderna sobre Promesas)

`async/await` **no reemplaza** a las Promesas: **las usa por debajo**. Es **azúcar sintáctico** para escribir código asíncrono con la misma forma que escribirías código síncrono.

Reglas:
- Una función marcada con `async` **siempre devuelve una Promesa**. Si dentro retornas un valor, JS lo envuelve automáticamente en `Promise.resolve(valor)`.
- Dentro de una función `async`, puedes usar `await` delante de **cualquier Promesa**. La función se **"pausa"** en esa línea **sin bloquear el hilo**: cede el control al event loop, y **reanuda** cuando la Promesa se resuelve, entregándote el valor (o lanzando el error si fue rechazada).
- `try / catch` funciona con `await` igual que en código síncrono. Con `.then()` tenías `.catch()`; con `await` usas `try/catch`.

Ejemplo canónico (login.js):

```js
form.addEventListener('submit', async function (e) {   // ① función async
  e.preventDefault();
  try {
    const resp = await fetch(API_BASE + '/auth/login', {  // ② pausa hasta recibir la respuesta
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (resp.ok) {
      const data = await resp.json();   // ③ pausa hasta parsear el JSON
      guardarSesion(data);
      window.location.href = 'dashboard';
      return;
    }

    const err = await parsearErrorApi(resp);   // otra espera
    mostrarErrorForm(err.mensaje);
  } catch (e) {
    mostrarErrorForm('No se pudo conectar con el servidor.');
  } finally {
    btn.disabled = false;           // corre siempre
    spinner.classList.add('d-none');
  }
});
```

Equivalente con Promesas puras, que es exactamente lo que JS ejecuta internamente:

```js
form.addEventListener('submit', function (e) {
  e.preventDefault();
  fetch(API_BASE + '/auth/login', {...})
    .then(function (resp) {
      if (resp.ok) return resp.json().then(function (data) {
        guardarSesion(data);
        window.location.href = 'dashboard';
      });
      return parsearErrorApi(resp).then(function (err) { mostrarErrorForm(err.mensaje); });
    })
    .catch(function (e) { mostrarErrorForm('No se pudo conectar.'); });
});
```

Se ve claramente por qué el equipo eligió `async/await`: el orden de lectura coincide con el orden de ejecución, los errores se manejan con un único `try/catch` y no hay anidamiento.

---

## 5. fetch() — la API HTTP nativa del navegador

`fetch(url, options)` devuelve una Promesa de un objeto `Response`. **`fetch` no lanza error por códigos 4xx/5xx** — sólo rechaza su Promesa si **falla la red** (DNS, conexión rechazada, etc.). Un `404` o `401` se considera una respuesta correcta desde el punto de vista de la solicitud.

Por eso **siempre**:
1. Comprobar `response.ok` (true si el status está entre 200 y 299) o `response.status`.
2. Llamar a `.json()` (que también es asíncrona, devuelve otra Promesa) para obtener el cuerpo.

```js
const resp = await fetch('/api/insumos');
if (!resp.ok) { /* manejar error con resp.status y await resp.json() */ }
const datos = await resp.json();
```

---

## 6. El contrato HTTP del backend (resumen)

| Método | URL | Body | Respuesta 2xx | Respuesta error |
|---|---|---|---|---|
| POST | `/api/auth/login` | `{email,password}` | `{token,email,nombre,rol,id}` | 401/400 |
| GET | `/api/insumos` | – | `[InsumoResponseDTO]` | – |
| POST | `/api/insumos` | `InsumoRequestDTO` | 201 `InsumoResponseDTO` | 400 |
| PUT | `/api/insumos/{id}` | `InsumoRequestDTO` | `InsumoResponseDTO` | 404/400 |
| DELETE | `/api/insumos/{id}` | – | 204 | 409 |
| GET | `/api/insumos/{id}` | – | `InsumoResponseDTO` | 404 |
| POST | `/api/lotes` | `RegistrarLoteRequestDTO` | 201 `LoteResponseDTO` | 400 |
| GET | `/api/lotes` | – | `[LoteResponseDTO]` | – |
| GET | `/api/lotes/por-insumo/{id}` | – | `[LoteResponseDTO]` | – |
| POST | `/api/movimientos` | `MovimientoRequestDTO` | 201 `MovimientoResponseDTO` | 400/409 |
| GET | `/api/movimientos/por-lote/{id}` | – | `[MovimientoResponseDTO]` | – |
| GET | `/api/usuarios` | – | `[UsuarioResponseDTO]` | – (sólo SUPERVISOR) |
| POST | `/api/usuarios` | `UsuarioRequestDTO` | 201 `UsuarioResponseDTO` | 400 |

Todos los endpoints excepto `/api/auth/**` requieren la cabecera `Authorization: Bearer <token>`.

### Formato de error del `GlobalExceptionHandler`

```json
// 404 / 409 / 400 (regla de negocio)
{ "timestamp": "...", "status": 404, "mensaje": "...", "campos": null }

// 400 (validación de campos)
{ "timestamp": "...", "status": 400, "mensaje": "Validación de campos fallida",
  "campos": { "nombre": "no debe estar vacío", "email": "debe ser un correo válido" } }
```

**El frontend nunca debe dejar un error silencioso**: el `mensaje` (y, si existen, los pares de `campos`) se muestran en cada formulario.

---

## 7. La envoltura `authFetch` — por qué y cómo

Todos los endpoints de negocio requieren JWT. Si cada llamada repitiese `headers: { Authorization: 'Bearer ' + token }`, se duplicaría lógica y sería fácil olvidarla. Por eso el común la encapsula en `common.js`:

```js
async function authFetch(url, options = {}) {
  const token = getToken();
  if (!token) { logout(); return null; }            // sin sesión → a login

  // 1) Copia las cabeceras que pedía el llamador, sin mutar el original
  const headers = Object.assign({}, options.headers || {});
  headers['Authorization'] = 'Bearer ' + token;

  // 2) Si el cuerpo es un string (un JSON serializado) y no se indicó Content-Type, añádelo
  if (options.body && typeof options.body === 'string' && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  // 3) Petición con try/catch por si falla la red (fetch rechaza, no devuelve Response)
  let resp;
  try {
    resp = await fetch(url, Object.assign({}, options, { headers }));
  } catch (e) {
    mostrarErrorGlobal('No se pudo conectar con el servidor.');
    return null;
  }

  // 4) Si el backend responde 401, el token caducó: cerrar sesión
  if (resp.status === 401) { logout(); return null; }

  // 5) Si todo bien, devolver la Response para que el llamador la procese
  return resp;
}
```

Patrón de uso en cualquier página:

```js
const resp = await authFetch(API_BASE + '/insumos');   // siempre await → siempre async
if (!resp || !resp.ok) { /* mostrar error */ return; }
const insumos = await resp.json();
```

`authFetch` devuelve `null` en dos casos especiales: (a) no había token y redirigió a login; (b) falló la red. Por eso se comprueba `if (!resp ...)` antes de tocar `resp.json()`.

---

## 8. Obtener y guardar el JWT (login.js)

Al enviar el formulario de login:

1. Se llama a `POST /api/auth/login` con `{email, password}`.
2. Si responde 200, el body trae `{token, id, email, nombre, rol}`.
3. Se guarda en `localStorage` con `guardarSesion(...)` (definida en `common.js`).
4. Se redirige a `dashboard`.

```js
const resp = await fetch(API_BASE + '/auth/login', {         // ← sin authFetch: login no necesita token
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
if (resp.ok) {
  const data = await resp.json();
  guardarSesion(data);
  window.location.href = 'dashboard';
  return;
}
const err = await parsearErrorApi(resp);
mostrarErrorForm(err.mensaje);
```

`localStorage` es un almacén del navegador **persistente entre recargas y pestañas**. Guardamos dos claves:

| Clave | Contenido |
|---|---|
| `inv_token` | el JWT |
| `inv_usuario` | `{id, nombre, email, rol}` serializado como JSON |

`sessionStorage` se usa sólo para "toasts" de una sola lectura (ver §12).

---

## 9. Proteger páginas: `requireAuth` y `requerirRol`

Cada página autenticada empieza su `DOMContentLoaded` con:

```js
const usuario = requireAuth();    // si no hay token → redirige a login y devuelve null
if (!usuario) return;
renderNavbar(usuario);
```

`requireAuth` se implementa así:

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

Para páginas exclusivas de SUPERVISOR (como `usuarios.js`) se usa `requerirRol('SUPERVISOR')`, que hace lo mismo pero además redirige a `dashboard` si el rol no coincide — **doble defensa**: la UI se oculta y el backend refuerza con `@PreAuthorize("hasRole('SUPERVISOR')")`.

> **No basta con ocultar enlaces:** la validación del rol en JS es de UX, la seguridad real está en el backend. Alguien podría llamar a `/api/usuarios` con `curl` sin la página.

---

## 10. Patrón completo: GET, pintar tabla, manejar errores

El dashboard es el ejemplo más completo. Flujo:

1. `requireAuth()`.
2. `renderNavbar(usuario)` inyecta el sidebar.
3. **`Promise.all`** para pedir `/insumos` y `/lotes` en paralelo.
4. Comprobar `resp.ok` en ambas.
5. `await resp.json()` para obtener los arrays.
6. Cálculo del semáforo **en el cliente** (ver §11) con `calcularEstadoSemaforo`.
7. Ordenar los críticos por `diasRestantes` ascendente.
8. Construir el HTML de cada fila con `escapeHtml` (evita XSS) y `badgeSemaforoHtml`.
9. Insertar con `tbody.innerHTML = ...`.

```js
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
```

---

## 11. Patrón completo: POST con validación de campos

`lote-form.js` registra un lote. Pasos:

1. `requireAuth()`.
2. `asegurarUsuarioId()` — devuelve el `id` del usuario logueado (campo `usuarioId` obligatorio del DTO).
3. Cargar insumos en un `<select>` con `GET /api/insumos`, filtrando los activos.
4. Al submitir, **construir el payload** con los valores actuales del DOM.
5. `authFetch(url, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })`.
6. Si `2xx` → redirigir al dashboard con un toast.
7. Si no → `parsearErrorApi(resp)` y mostrar en `#formError`.

```js
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
  mostrarErrorForm(err.mensaje, err.campos);   // pinta mensaje + lista de campos
} finally {
  btn.disabled = false;
  spinner.classList.add('d-none');
}
```

`parsearErrorApi` espera el formato del `GlobalExceptionHandler` y devuelve `{mensaje, campos}`:

```js
async function parsearErrorApi(resp) {
  let data = null;
  try { data = await resp.json(); } catch (e) { /* cuerpo no JSON */ }
  if (!data) return { mensaje: 'Error inesperado (HTTP ' + resp.status + ')', campos: null };
  return { mensaje: data.mensaje || ('Error HTTP ' + resp.status), campos: data.campos || null };
}
```

`mostrarErrorForm` pinta una alerta Bootstrap dentro de `#formError`; si hay `campos` (validación 400) los lista debajo del mensaje.

---

## 12. Toasts de "operación exitosa" entre páginas

Como la navegación es con `window.location.href` (no es SPA), al cambiar de página **se pierde el contexto JS**. Para mostrar "Lote registrado" en el dashboard tras venir del formulario:

- Antes de navegar, la página de origen hace `marcarToast('toast_dashboard', '...')` que guarda el mensaje en `sessionStorage`.
- Al cargar el dashboard, `consumirToast('toast_dashboard')` lee y borra la clave; si hay mensaje, lo pinta como alert verde.

`sessionStorage` se diferencia de `localStorage` en que **se borra al cerrar la pestaña**: perfecto para mensajes efímeros.

---

## 13. Semáforo — cálculo en el cliente

El backend devuelve `fechaVencimiento` y `cantidadActual` crudos; el **color** se calcula en JS (porque es una regla de presentación). La función está en `common.js`:

```js
const SEMAFORO_UMBRALES = { VERDE: 90, AMARILLO: 30 };

function calcularEstadoSemaforo(lote) {
  if (lote.cantidadActual <= 0) return { estado:'Agotado', clase:'badge-agotado', diasRestantes: null };

  const hoy = new Date(); hoy.setHours(0,0,0,0);
  const venc = parsearFechaLocal(lote.fechaVencimiento);     // 'YYYY-MM-DD' → Date local
  const dias = Math.floor((venc.getTime() - hoy.getTime()) / 86400000);

  if      (dias > SEMAFORO_UMBRALES.VERDE)   return { estado:'Vigente',    clase:'badge-vigente',    diasRestantes: dias };
  else if (dias > SEMAFORO_UMBRALES.AMARILLO) return { estado:'Por vencer', clase:'badge-por-vencer', diasRestantes: dias };
  else                                      return { estado:'Crítico',    clase:'badge-critico',    diasRestantes: dias };
}
```

Los umbrales (`90`/`30`) están centralizados en `SEMAFORO_UMBRALES` porque el backend los configura en `configuracion_semaforo` y **en el futuro podrían consultarse por API**. Cambiar un solo objeto y todo el frontend se actualiza.

`parsearFechaLocal` construye el `Date` con `new Date(año, mes-1, día)` (no con `new Date('YYYY-MM-DD')`, que en algunos navegadores se interpreta como UTC y desplaza un día según huso horario).

---

## 14. Estructura de archivos y responsabilidades

```
static/
├─ index.html              redirige a login o dashboard según localStorage
├─ login.html + js/login.js     form → POST /auth/login → guarda JWT → va a dashboard
├─ dashboard.html + js/dashboard.js   GET /insumos + /lotes → 4 tarjetas + lista urgentes
├─ inventario.html + js/inventario.js   CRUD insumos (listar, filtrar, desactivar)
├─ insumo-form.html + js/insumo-form.js  crear/editar (?id=) insumo
├─ lote-form.html + js/lote-form.js      POST /lotes
├─ movimiento-wizard.html + js/movimiento-wizard.js  3 pasos: insumo → lote → tipo/cantidad → POST /movimientos
├─ usuarios.html + js/usuarios.js  SUPERVISOR: lista + modal de alta (POST /usuarios)
├─ css/custom.css          paleta teal + badges semáforo + layout sidebar
└─ js/common.js            auth, authFetch, renderNavbar, semáforo, errores, escapeHtml
```

**Convención de nomenclatura**: cada página `X.html` tiene su `js/X.js`. `common.js` es el único compartido y se carga **antes** del JS de la página en cada HTML:

```html
<script src="js/common.js"></script>
<script src="js/dashboard.js"></script>
```

---

## 15. Checklist para añadir una nueva pantalla

1. Crear `static/mipagina.html` con `<div id="navbar"></div>` y `<div class="container app-main">…</div>`.
2. Crear `static/js/mipagina.js` que empiece con `requireAuth()` (o `requerirRol('SUPERVISOR')`) y `renderNavbar(usuario)`.
3. **Todas** las llamadas HTTP por `authFetch(...)`, nunca `fetch()` directo.
4. Programar el `submit`/`click` con `async function`, `try/catch/finally`, mostrar errores con `mostrarErrorForm` y éxitos con `marcarToast`.
5. Exportar las rutas limpias en `WebConfig.java` y permitir el path en `SecurityConfig`.
6. Añadir el enlace en el array `items` de `renderNavbar` si debe aparecer en el sidebar.

Con esto tendrás una página consistente con el resto del sistema.

---

## 16. Rutas limpias: cómo se hicieron

Spring Boot sirve por defecto `static/login.html` en la URL `/login.html` (con la extensión). Para que la URL del navegador sea `/login` (sin `.html`) se añadieron dos piezas:

**1. `WebConfig.java`** — registra un *forward* (no redirect) por cada ruta limpia:

```java
registry.addViewController("/login").setViewName("forward:/login.html");
```

El `forward` es **interno**: el navegador pide `/login`, Spring **reenvía** la misma petición a `/login.html` y sirve el archivo **sin cambiar** la URL visible. No hay segunda petición del cliente. Es la forma estándar y simple con HTML estáticos servidos por Spring Boot.

**2. `SecurityConfig.java`** — se añadieron explícitamente las rutas limpias (y las con `.html`) en `permitAll()` para que el filtro JWT no las bloqueara:

```java
.requestMatchers("/login", "/login.html", "/dashboard", "/dashboard.html", …).permitAll()
```

**3. El frontend** usa **sólo rutas limpias** en todos los `href` y `window.location.href` (sin extensión):
- en HTML: `<a href="insumo-form?id=…">`, `<a href="dashboard">`
- en JS: `window.location.href = 'dashboard'`, `window.location.replace('login')`

La coincidencia de "ruta activa" en el sidebar compara el path sin extensión:

```js
let pathActual = window.location.pathname.split('/').pop() || 'dashboard';
pathActual = pathActual.replace(/\.html$/, '');
```

Resultado: el usuario siempre ve `http://servidor/login`, `/dashboard`, `/inventario`, etc., y los archivos físicos siguen siendo `.html` servidos tal cual por Spring.

---

## 17. Errores comunes a evitar

| Error | Por qué pasa | Cómo evitarlo |
|---|---|---|
| Llamar `await` fuera de `async` | `await` sólo existe dentro de funciones marcadas `async` | Marcar el callback del `addEventListener` con `async function` |
| Olvidar `await` devanto de `resp.json()` | `resp.json()` devuelve una Promesa, no el objeto | Siempre `const data = await resp.json();` |
| Tratar un 404 como excepción | `fetch` no lanza para 4xx/5xx | Comprobar `resp.ok` o `resp.status` antes |
| Mezclar callbacks y await | Difícil de leer y propenso a bugs | Elegir `async/await` para HTTP y `addEventListener` para DOM |
| Inyectar HTML del backend sin escapar | Riesgo XSS | Usar siempre `escapeHtml(...)` al construir filas |
| Olvidar `Content-Type: application/json` | El backend rechaza con 415 | `authFetch` lo añade automáticamente si el body es string |
|_Dejar un error silencioso_ | El usuario no sabe qué pasó | `parsearErrorApi` + `mostrarErrorForm` siempre |

---

## 18. Resumen ejecutivo

- **Callbacks** → para **eventos del DOM** (`addEventListener`). No para cadenas HTTP.
- **Promesas** → `fetch` las devuelve; `Promise.all` para paralelizar; `.then/.catch` para encadenar (legible pero anidado).
- **`async/await`** → **forma elegida en todo el proyecto** para HTTP. Mismo flujo que síncrono, `try/catch` único, sin callback hell.
- **`authFetch`** → envoltura reutilizable que añade el `Authorization: Bearer` y maneja 401/red caída.
- **`requireAuth` / `requerirRol`** → porteros de cada página; sin token válido, redirigen a login.
- **`parsearErrorApi` + `mostrarErrorForm`** → garantizan que **ningún error quede sin mostrar**.
- **`SEMAFORO_UMBRALES`** → único punto donde cambiar los umbrales del semáforo calculado en el cliente.

Con este patrón y la checklist del §15 puedes añadir cualquier pantalla nueva sin repetir boilerplate.