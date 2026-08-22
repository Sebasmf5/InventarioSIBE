# Documento maestro — Principios HTML del frontend de InventarioSIBE

Esta guía explica, **desde cero**, todos los principios HTML que se usaron en el frontend de InventarioSIBE. No es teoría genérica: cada concepto se ilustra con **tu código real**, archivo por archivo. El objetivo es que entiendas el rompecabezas completo: por qué cada etiqueta está ahí, qué hace el navegador con ella y cómo se conecta con el JS y el backend.

## Índice

1. [El modelo mental: ¿qué es HTML?](#1-el-modelo-mental-qué-es-html)
2. [La estructura obligatoria de toda página](#2-la-estructura-obligatoria-de-toda-página)
3. [El `<head>`: metadatos y recursos externos](#3-el-head-metadatos-y-recursos-externos)
4. [El `<body>`: el contenido visible](#4-el-body-el-contenido-visible)
5. [Etiquetas semánticas: ¿por qué `<main>` y no `<div>`?](#5-etiquetas-semánticas-por-qué-main-y-no-div)
6. [El atributo `id`: identificar elementos únicos](#6-el-atributo-id-identificar-elementos-únicos)
7. [El atributo `class`: estilos y Bootstrap](#7-el-atributo-class-estilos-y-bootstrap)
8. [Enlaces `<a href>`: la navegación nativa del navegador](#8-enlaces-a-href-la-navegación-nativa-del-navegador)
9. [Formularios: la forma nativa de enviar datos](#9-formularios-la-forma-nativa-de-enviar-datos)
10. [Inputs: los distintos tipos](#10-inputs-los-distintos-tipos)
11. [Tablas: estructura para datos en filas y columnas](#11-tablas-estructura-para-datos-en-filas-y-columnas)
12. [Contenedores y layout: divs anidados](#12-contenedores-y-layout-divs-anidados)
13. [El orden de los `<script>`: por qué importa](#13-el-orden-de-los-script-por-qué-importa)
14. [Cómo el HTML se conecta con el JS](#14-cómo-el-html-se-conecta-con-el-js)
15. [Cómo el HTML se conecta con el backend](#15-cómo-el-html-se-conecta-con-el-backend)
16. [El rompecabezas completo: flujo de una página](#16-el-rompecabezas-completo-flujo-de-una-página)

---

## 1. El modelo mental: ¿qué es HTML?

HTML (HyperText Markup Language) **no es programación**: es **marcado**. Tu escribes texto y le pones **etiquetas** (tags) alrededor para decir "esto es un título", "esto es un párrafo", "esto es un enlace". El navegador lee esas etiquetas y construye una representación interna llamada **DOM** (Document Object Model) — un árbol de nodos que el navegador pinta en pantalla y que el JavaScript puede manipular.

```html
<h1>Hola</h1>     ← etiqueta de apertura + contenido + etiqueta de cierre
```

- `<h1>` → etiqueta de apertura
- `Hola` → contenido
- `</h1>` → etiqueta de cierre (con la `/`)

Algunas etiquetas no tienen contenido ni cierre, se auto-cer

```html
<input type="text" />     ← etiqueta vacía (self-closing)
```

**La regla de oro**: HTML describe **estructura y significado**, no apariencia. La apariencia la da el CSS. El comportamiento lo da el JS. HTML solo dice "esto es un botón" o "esto es una tabla".

---

## 2. La estructura obligatoria de toda página

Toda página HTML del proyecto empieza con el mismo esqueleto:

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <!-- metadatos: título, CSS, iconos -->
</head>
<body>
  <!-- contenido visible -->
</body>
</html>
```

Ejemplo real de `inventario.html:1-11`:

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Inventario · InventarioSIBE</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet" />
  <link href="css/custom.css" rel="stylesheet" />
</head>
<body>
  ...
</body>
</html>
```

Qué hace cada pieza:

| Pieza | Para qué sirve |
|---|---|
| `<!DOCTYPE html>` | Le dice al navegador "usa HTML5" (el estándar moderno). Sin esto, el navegador entra en "modo rarito" (quirks mode) y renderiza distinto. |
| `<html lang="es">` | El contenedor raíz. `lang="es"` ayuda a lectores de pantalla y a buscadores (accesibilidad + SEO). |
| `<head>` | Metadatos **no visibles**: título de la pestaña, codificación, CSS, iconos. El navegador los lee antes de pintar. |
| `<body>` | El contenido **visible** que el usuario ve. |

---

## 3. El `<head>`: metadatos y recursos externos

El `<head>` no se ve en pantalla, pero prepara todo lo que la página necesita.

### 3.1 `<meta charset>` y `<meta viewport>`

```html
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
```

- **`charset="UTF-8"`**: la codificación de caracteres. Permite tildes, ñ, emojis. Sin esto, los acentos se ven como simbolos raros.
- **`viewport`**: **clave para responsive**. Le dice al navegador "adapta el ancho al dispositivo". Sin esto, en móvil la página se ve como una versión miniatura de desktop. Es lo que hace que tu inventario se vea bien en celular.

### 3.2 `<title>`

```html
<title>Inventario · InventarioSIBE</title>
```

El texto que aparece en la pestaña del navegador. También lo usan los buscadores. Cada página tiene el suyo.

### 3.3 `<link>` para CSS

```html
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
<link href="css/custom.css" rel="stylesheet" />
```

`<link>` trae recursos externos. Con `rel="stylesheet"` trae CSS. El **orden importa**: `custom.css` va **después** de Bootstrap para poder sobreescribir sus estilos (el último en cargarse gana si tienen la misma especificidad).

---

## 4. El `<body>`: el contenido visible

Aquí va lo que el usuario ve. En las páginas internas de InventarioSIBE hay un patrón de layout fijo:

```html
<body>
  <div class="app-layout">
    <div id="navbar"></div>
    <main class="app-main">
      <div class="container">
        <!-- ... contenido específico de la página ... -->
      </div>
    </main>
  </div>

  <script src="js/common.js"></script>
  <script src="js/pagina.js"></script>
</body>
```

Tres capas de anidamiento, cada una con un propósito:

```
body
└─ div.app-layout        ← wrapper flexbox (sidebar | contenido)
   ├─ div#navbar         ← aquí inyecta common.js el menú lateral
   └─ main.app-main      ← área de contenido (flex: 1, min-width: 0)
      └─ div.container   ← Bootstrap: centra y limita el ancho
         └─ ... contenido de la página ...
```

- `.app-layout` es `display: flex` → pone al navbar y al main lado a lado.
- `#navbar` tiene ancho fijo (250px) y fondo teal-dark → reserva el espacio del sidebar **antes** de que JS lo inyecte (anti-FOUC).
- `.app-main` tiene `flex: 1 1 0; min-width: 0` → ocupa el resto del ancho sin desbordarse.
- `.container` es de Bootstrap → centra el contenido y le pone un `max-width` para que las líneas no sean demasiado largas en pantallas grandes.

---

## 5. Etiquetas semánticas: ¿por qué `<main>` y no `<div>`?

Podrías usar `<div>` para todo, pero HTML5 trajo etiquetas **semánticas** que describen el rol del contenido:

| Etiqueta | Significado | Dónde se usa |
|---|---|---|
| `<main>` | contenido principal de la página (uno por página) | envoltura del área de contenido |
| `<header>` | encabezado de sección | dentro de cards o secciones |
| `<nav>` | navegación | el sidebar inyectado por `common.js` usa `<nav class="sidebar-nav">` |
| `<aside>` | contenido complementario | el sidebar usa `<aside class="app-sidebar">` |
| `<section>` | sección genérica | agrupar contenido |
| `<article>` | contenido autónomo | (no usado aquí) |

**Por qué importa**:
- **Accesibilidad**: lectores de pantalla (personas ciegas) navegan por semántica. Un `<main>` les dice "aquí está el contenido principal".
- **SEO**: Google entiende mejor la estructura.
- **Mantenibilidad**: al leer `<main>`, `<nav>`, `<aside>`, sabes qué hace cada bloque sin leer el contenido.

En `inventario.html:15`:

```html
<main class="app-main">
  <div class="container">
    ...
  </div>
</main>
```

`<main>` dice "esto es el contenido principal de la página". Un `<div>` no diría nada.

---

## 5. El atributo `id`: identificar elementos únicos

El `id` identifica **un solo elemento** en toda la página. Debe ser único (no puedes tener dos elementos con el mismo `id`).

```html
<div id="navbar"></div>
<div id="errorGlobal"></div>
<div id="toastExito"></div>
<div id="formError"></div>
<form id="loginForm">...</form>
<input id="email" />
```

**Para qué sirve el `id`**:
1. **El JS lo localiza**: `document.getElementById('email')` → encuentra el input.
2. **El `<label for="email">`** se asocia al input con ese id (al clicar el label, el input toma foco).
3. **Anclas**: `#seccion` en la URL salta al elemento con ese id.

En el proyecto, los `id` son el puente entre HTML y JS. Sin `id="tablaInsumos"`, el JS no podría encontrar la tabla para inyectar las filas.

---

## 7. El atributo `class`: estilos y Bootstrap

`class` identifica **grupos de elementos** que comparten estilo. Un elemento puede tener varias clases separadas por espacio:

```html
<input type="text" id="filtroNombre" class="form-control" />
<button class="btn btn-teal w-100 py-2 fw-semibold">Ingresar</button>
```

En el proyecto, las clases cumplen dos roles:

### 7.1 Clases de Bootstrap

`form-control`, `btn`, `btn-teal`, `card`, `row`, `col-12`, `mb-3`, `w-100`... son clases del framework Bootstrap que aplican estilos predefinidos (tamaños, espaciados, colores, grid).

### 7.2 Clases propias (`custom.css`)

`app-layout`, `app-main`, `app-card`, `btn-teal`, `login-card`, `stat-card`, `badge-sem`, `badge-vigente`... las define `custom.css` para la paleta teal y el layout.

### 7.3 Clases que el JS añade/quita dinámicamente

El JS usa clases para mostrar/ocultar elementos:

```js
document.getElementById('paso2').classList.add('d-none');      // oculta
document.getElementById('paso3').classList.remove('d-none');   // muestra
sidebar.classList.add('show');                                  // abre sidebar en móvil
```

`d-none` es una clase de Bootstrap que aplica `display: none`. Añadirla/ quitarla es cómo el wizard cambia de paso.

---

## 8. Enlaces `<a href>`: la navegación nativa del navegador

Esta es la pieza más importante de entender. Un enlace `<a>` (anchor) con `href` es una **instrucción de navegación** que el navegador ejecuta **sin que tú programes nada**.

### 8.1 La anatomía

```html
<a href="insumo-form?id=abc-123" class="btn btn-sm btn-outline-teal" title="Editar">
  <i class="bi bi-pencil"></i>
</a>
```

| Atributo | Para qué |
|---|---|
| `href` | **la URL a la que navegar** — es lo único indispensable |
| `class` | estilo (Bootstrap le pone clase `btn-*` para que parezca botón) |
| `title` | tooltip al pasar el mouse (accesibilidad) |

El contenido entre `<a>` y `</a>` (aquí un icono `<i>`) es lo clickeable.

### 8.2 Qué pasa al hacer clic

El navegador, **por defecto**, hace lo siguiente:

1. Lee el `href`.
2. Hace una petición **GET** al servidor pidiendo esa URL.
3. **Reemplaza** la página actual por la nueva.
4. La URL visible cambia a la del `href`.

**No hay JS involucrado**. Es comportamiento nativo del navegador, como lo es que un botón sea clickeable. Tú solo construyes el `href` con el id correcto; el navegador hace el resto.

### 8.3 Por qué el botón "Editar" es un `<a>` y no un `<button>`

```html
<!-- EDITAR: es un enlace (navega) -->
<a href="insumo-form?id=abc-123" class="btn btn-sm btn-outline-teal">
  <i class="bi bi-pencil"></i>
</a>

<!-- DESACTIVAR: es un botón (no navega, hace una baja lógica) -->
<button class="btn btn-sm btn-outline-danger" data-id="abc-123" data-nombre="Acetaminofén">
  <i class="bi bi-trash"></i>
</button>
```

**Regla de decisión**:
- ¿La acción **va a otra página**? → `<a href="...">` (el navegador navega solo).
- ¿La acción **se queda en la misma página** y hace algo (fetch, mostrar modal, etc.)? → `<button>` + JS.

El enlace "Editar" va al formulario de edición → es `<a>`. El botón "Desactivar" se queda en la página y hace un `DELETE` por fetch → es `<button>`.

### 8.4 Query params: pasar datos por la URL

```html
<a href="insumo-form?id=abc-123">
```

La parte después de `?` se llama **query string**: `id=abc-123`. Es la forma de **pasar datos a una página** sin JS ni formularios. La página destino lee esos params con:

```js
const params = parametrosUrl();   // { id: 'abc-123' }
const idEdicion = params.id;
```

Así el formulario sabe que está en modo edición (porque hay `?id=`) y qué insumo cargar.

### 8.5 Ventajas del `<a>` sobre un `<button>` con JS

- **Clic medio / ctrl+clic** → abre en pestaña nueva (nativo del navegador).
- **Clic derecho → "Copiar enlace"** → obtienes la URL directa.
- **No requiere JS** → si JS falla, el enlace igual navega.
- **Accesibilidad**: lectores de pantalla lo reconocen como navegación.

---

## 9. Formularios: la forma nativa de enviar datos

El formulario es la forma nativa de HTML para **enviar datos al servidor**. Aunque aquí lo interceptamos con JS, la estructura sigue siendo la de HTML.

### 9.1 Anatomía

```html
<form id="loginForm" novalidate>
  <div class="mb-3">
    <label for="email" class="form-label">Correo electrónico</label>
    <input type="email" id="email" name="email" class="form-control" required />
  </div>
  <button type="submit" class="btn btn-teal">Ingresar</button>
</form>
```

| Parte | Para qué |
|---|---|
| `<form>` | contenedor de campos relacionados |
| `id="loginForm"` | para que el JS lo localice y escuche su `submit` |
| `novalidate` | **desactiva la validación nativa del navegador** (la hacemos en JS/backend) |
| `<label for="email">` | texto asociado al input `id="email"` (al clicar el label, el input toma foco — accesibilidad) |
| `<input name="email">` | `name` es el nombre del campo si se enviara sin JS (aquí no se usa porque enviamos JSON por JS, pero es buena práctica) |
| `required` | atributo de validación nativa (lo usa el navegador si no hubiera `novalidate`) |
| `<button type="submit">` | al pulsarlo, dispara el evento `submit` del formulario |

### 9.2 Cómo se intercepta con JS

Sin JS, el formulario al pulsar "Ingresar" haría una petición GET/POST nativa y **recargaría la página**. Aquí no queremos eso: queremos enviar los datos por `fetch` sin recargar. Por eso:

```js
form.addEventListener('submit', async function (e) {
  e.preventDefault();    // ← DETIENE el comportamiento nativo (recargar)
  // ... aquí hacemos el fetch manual ...
});
```

`e.preventDefault()` es la línea mágica: le dice al navegador "no hagas lo que harías por defecto, yo me encargo". A partir de ahí, el JS lee los valores de los inputs, construye un JSON y lo envía con `fetch`.

### 9.3 El botón "Cancelar" vs el botón "Guardar"

```html
<a href="inventario" class="btn btn-outline-secondary">Cancelar</a>
<button type="submit" class="btn btn-teal">Guardar</button>
```

- **Cancelar** es un `<a href="inventario">` → navega de vuelta a la lista, sin enviar nada.
- **Guardar** es un `<button type="submit">` → dispara el `submit` del form (que el JS intercepta).

La diferencia entre `type="submit"` (dispara submit) y `type="button"` (no hace nada por sí mismo, solo JS) es crucial.

---

## 10. Inputs: los distintos tipos

Cada tipo de `<input>` le dice al navegador qué clase de dato esperas, y el navegador aplica validación, teclado y apariencia distintos.

| `type` | Para qué | Ejemplo del proyecto |
|---|---|---|
| `text` | texto libre | nombre, marca, observación |
| `email` | correo (valida formato, teclado con @) | login |
| `password` | contraseña (oculta caracteres) | login, usuarios |
| `number` | números (flechas, min/max) | cantidad, stockMinimo |
| `date` | fecha (calendario) | fechaVencimiento |
| `checkbox` | casilla (sí/no) | activo |

Ejemplos:

```html
<input type="email" id="email" required />              ← valida formato de correo
<input type="number" id="stockMinimo" min="0" value="0" />  ← no permite negativos
<input type="date" id="fechaVencimiento" required />    ← muestra calendario
<input type="checkbox" id="activo" checked />           ← casilla, marcada por defecto
```

El `type` afecta:
- **Validación**: `type="email"` rechaza "foo" (sin @).
- **Teclado móvil**: `type="email"` muestra teclado con @; `type="number"` muestra solo dígitos.
- **Apariencia**: `type="date"` muestra calendario; `type="checkbox"` muestra casilla.

---

## 11. Tablas: estructura para datos en filas y columnas

La tabla del inventario (`inventario.html:56-74`):

```html
<div class="table-responsive">
  <table class="table table-hover align-middle mb-0">
    <thead>
      <tr>
        <th>Nombre</th>
        <th>Presentación</th>
        <!-- ... más <th> ... -->
      </tr>
    </thead>
    <tbody id="tablaInsumos">
      <tr><td colspan="8" class="text-center text-muted py-4">Cargando…</td></tr>
    </tbody>
  </table>
</div>
```

Anatomía:

| Etiqueta | Rol |
|---|---|
| `<table>` | contenedor |
| `<thead>` | cabecera (la fila de títulos) |
| `<tbody>` | cuerpo (las filas de datos) |
| `<tr>` | table row (una fila) |
| `<th>` | table header (celda de cabecera, en negrita) |
| `<td>` | table data (celda de datos) |
| `colspan="8"` | esta celda ocupa el lugar de 8 columnas (para el mensaje "Cargando…") |

**Por qué `tbody` tiene `id`**: el JS inyecta las filas dentro del `<tbody>`. Sin `id="tablaInsumos"`, el JS no sabría dónde ponerlas.

**Por qué `table-responsive`**: en móvil, las tablas anchas se desbordan. Esta clase de Bootstrap envuelve la tabla en un contenedor con `overflow-x: auto` → aparece scroll horizontal solo cuando hace falta.

### Cómo se inyectan las filas (en JS)

El `<tbody>` arranca con una sola fila placeholder "Cargando…". El JS, tras recibir los datos del backend, reemplaza su contenido:

```js
tbody.innerHTML = filtrados.map(function (i) {
  return '<tr>' +
    '<td>' + escapeHtml(i.nombre) + '</td>' +
    '<td>' + escapeHtml(i.presentacion) + '</td>' +
    // ...
    '</tr>';
}).join('');
```

El JS **construye strings HTML** y los inyecta con `innerHTML`. El navegador parsea ese string y crea los nodos del DOM al vuelo. Es como si escribieras el HTML a mano, pero dinámico.

---

## 12. Contenedores y layout: divs anidados

HTML se estructura anidando contenedores. Cada `<div>` es una caja que contiene otras cajas:

```
div.app-layout                  (display: flex → sidebar | main lado a lado)
├─ div#navbar                   (ancho fijo 250px)
└─ main.app-main                (flex: 1, ocupa el resto)
   └─ div.container             (Bootstrap: centra y limita ancho)
      └─ div.card.app-card      (tarjeta con sombra)
         └─ div.card-body       (padding interior)
            └─ ... filtros + tabla ...
```

Cada nivel tiene un propósito:
- `.app-layout` → layout general (flexbox).
- `.container` → centrado y `max-width` (Bootstrap).
- `.card` → tarjeta visual (sombra, borde redondeado).
- `.card-body` → padding interior de la tarjeta.

**La regla de anidamiento**: cada capa debe tener **un** propósito claro. No anides 5 divs sin razón — si todos hacen lo mismo, es "divitis" (un anti-patrón).

---

## 1. El orden de los `<script>`: por qué importa

Al final de cada HTML:

```html
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="js/common.js"></script>
<script src="js/inventario.js"></script>
</body>
```

Tres reglas se cumplen aquí:

### Regla 1: los scripts van al **final del `<body>`**

Si pusieras un script en el `<head>`, el navegador lo ejecutaría **antes de que existan los elementos del `<body>`**. Entonces `document.getElementById('email')` devolvería `null` (el input todavía no existe) y el script rompería.

Al ponerlos al final, el navegador primero pinta todo el HTML y **luego** ejecuta el JS. Así, cuando el JS busca elementos, ya existen.

### Regla 2: `common.js` **antes** que el JS de la página

`inventario.js` usa funciones de `common.js` (`requireAuth`, `renderNavbar`, `authFetch`...). Si `inventario.js` se cargara primero, esas funciones no estarían definidas todavía → error `ReferenceError`.

### Regla 3: Bootstrap JS antes que `common.js`

`common.js` usa `bootstrap.Modal` (en `usuarios.js` se instancia un modal). Bootstrap debe estar cargado primero para que la clase `bootstrap` exista cuando se ejecute `common.js` (o el JS de la página que la use).

**Orden seguro**:

```
1. Bootstrap JS  (define window.bootstrap)
2. common.js     (define requireAuth, authFetch, renderNavbar, etc.)
3. pagina.js     (usa los dos anteriores)
```

---

## 14. Cómo el HTML se conecta con el JS

El HTML y el JS se conectan por tres vías:

### 14.1 El JS busca elementos por `id`

```html
<input id="email" />
```
```js
const email = document.getElementById('email');   // encuentra el input
```

`getElementById` es la forma más rápida y directa. El `id` es el puente.

### 14.2 El JS escucha eventos del formulario

```html
<form id="loginForm">...</form>
```
```js
form.addEventListener('submit', function (e) { ... });   // escucha el submit
```

El JS registra un callback que se ejecuta cuando el usuario envía el formulario. El navegador se encarga de disparar el evento.

### 14.3 El JS inyecta contenido dinámico

```html
<tbody id="tablaInsumos">
  <tr><td>Cargando…</td></tr>
</tbody>
```
```js
tbody.innerHTML = '<tr><td>Acetaminofén</td><td>...</td></tr>';   // reemplaza el contenido
```

El `<tbody>` arranca con un placeholder. El JS reemplaza su `innerHTML` con las filas reales una vez que tiene los datos del backend.

---

## 15. Cómo el HTML se conecta con el backend

El HTML **no llama al backend directamente**. El backend sirve el archivo HTML cuando el navegador lo pide. La conexión ocurre así:

### 15.1 El navegador pide la página (GET)

Cuando el usuario navega a `/inventario`:

```
1. navegador: GET /inventario HTTP/1.1
2. Spring (WebConfig): forward → /inventario.html
3. Spring sirve el archivo estático
4. navegador: pinta el HTML, ejecuta los scripts
```

### 15.2 El JS pide datos al backend (fetch)

Una vez pintado el HTML, el JS (ya en el navegador) hace peticiones HTTP al backend:

```js
const resp = await authFetch(API_BASE + '/insumos');   // GET /api/insumos
const insumos = await resp.json();
```

Esta petición es **independiente** de la que sirvió la página. El navegador hace dos peticiones:
1. `GET /inventario` → trae el HTML.
2. `GET /api/insumos` → trae los datos (JSON).

### 15.3 El backend devuelve JSON, no HTML

Los endpoints `/api/*` devuelven **JSON** (datos estructurados), no HTML. El JS recibe el JSON, lo procesa y construye el HTML dinámicamente (innerHTML, inyectar filas, etc.).

```
backend /api/insumos → [
  { id:"abc", nombre:"Acetaminofén", presentacion:"Tableta", ... },
  { id:"def", nombre:"Ibuprofeno", presentacion:"Jarbe", ... }
]
```

El JS transforma ese JSON en filas de tabla, badges, botones...

### 15.4 El JS envía datos al backend (POST/PUT/DELETE)

Cuando el usuario guarda un formulario, el JS lee los inputs, construye un JSON y lo envía:

```js
const payload = { nombre: "Acetaminofén", presentacion: "Tableta", ... };
const resp = await authFetch(API_BASE + '/insumos', {
  method: 'POST',
  body: JSON.stringify(payload)
});
```

El backend procesa, guarda en DB y responde con el recurso creado (201) o un error (400).

---

## 16. El rompecabezas completo: flujo de una página

Vamos a juntar todo. Flujo completo de `/inventario` desde que el usuario escribe la URL hasta que ve la tabla:

```
┌──────────────────────────────────────────────────────────────┐
│ 1. USUARIO escribe /inventario en el navegador               │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│ 2. NAVEGADOR envía GET /inventario                           │
│    "dame la página inventario"                                │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│ 3. SPRING (WebConfig.java) recibe /inventario                │
│    forward → /inventario.html (sirve el archivo sin cambiar   │
│    la URL visible)                                            │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────┐
│ 4. NAVEGADOR recibe el HTML, lo pinta:                │
│    - <head>: carga Bootstrap CSS, custom.css          │
│    - <body>: pinta el layout vacío                    │
│      (app-layout, #navbar vacío, .app-main)           │
│    - #navbar ya tiene 250px y fondo teal (placeholder)│
│    - <tbody id="tablaInsumos"> muestra "Cargando…"    │
└───────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────┐
│ 5. NAVEGADOR ejecuta los <script> al final del body:  │
│    a. bootstrap.bundle.min.js → define window.bootstrap│
│    b. common.js → define requireAuth, authFetch, ...  │
│    c. inventario.js → arranca el DOMContentLoaded     │
└───────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────┐
│ 6. inventario.js ejecuta:                             │
│    a. requireAuth() → ¿hay token? sí → devuelve usuario│
│    b. renderNavbar(usuario) → inyecta el sidebar      │
│       dentro de #navbar (el placeholder se "llena")    │
│    c. authFetch('/api/insumos') → petición al backend  │
│       con header Authorization: Bearer <token>         │
└───────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────┐
│ 7. BACKEND recibe GET /api/insumos:                   │
│    - JwtAuthenticationFilter valida el token          │
│    - InsumoController.listar() → InsumoService.listar()│
│    - Devuelve [InsumoResponseDTO, ...] como JSON       │
└───────────────────────────────────────────────────────┘
                              ↓
\ red≤)
│ 8. inventario.js recibe el JSON, lo procesa:          │
│    - filtra por los <select> e <input> de filtros     │
│    - construye HTML de las filas con escapeHtml       │
│    - tbody.innerHTML = '<tr>...<td>Acetaminofén</td>' │
│    - El navegador pinta las filas en pantalla          │
└───────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────┐
│ 9. USUARIO ve la tabla con todos los insumos          │
│    Puede:                                             │
│    - clic en lápiz (<a href="insumo-form?id=...">) →   │
│      navegador navega al formulario (paso 1 de nuevo) │
│    - clic en basura (<button data-id="...">) → JS     │
│      hace fetch DELETE y refresca la tabla             │
└───────────────────────────────────────────────────────┘
```

---

## Resumen: los 10 principios que debes recordar

1. **HTML describe estructura y significado**, no apariencia ni comportamiento. La apariencia la da CSS, el comportamiento lo da JS.
2. **La estructura obligatoria**: `<!DOCTYPE html>` + `<html lang="es">` + `<head>` + `<body>`.
3. **El `<head>`** prepara la página: título, CSS, viewport para responsive.
4. **`<a href="...">`** es navegación nativa: el navegador navega solo, sin JS.
5. **`<button>`** es acción local: no navega, requiere JS para hacer algo.
6. **El `id`** identifica elementos únicos para que el JS los encuentre y el `<label>` se asocie.
7. **El `class`** agrupa elementos para estilos (CSS/Bootstrap) y para mostrar/ocultar (`d-none`).
8. **Los scripts van al final del `<body>`** y en orden: Bootstrap → common.js → página.js.
9. **El navegador pide el HTML (GET)**; luego **el JS pide los datos (fetch)**. Son dos peticiones separadas.
10. **El backend sirve HTML para las páginas y JSON para los datos**. El JS transforma el JSON en HTML dinámico.

---

## Glosario rápido

| Término | Significado |
|---|---|
| **HTML** | lenguaje de marcado que describe la estructura de una página |
| **DOM** | el árbol de nodos que el navegador construye a partir del HTML |
| **Etiqueta / tag** | palabra clave como `<div>`, `<a>`, `<input>` que describe un elemento |
| **Atributo** | información extra en la etiqueta de apertura: `id`, `class`, `href`, `type`, `required` |
| **Elemento** | una etiqueta completa con su contenido: `<p>texto</p>` |
| **`<head>`** | metadatos no visibles (título, CSS, charset) |
| **`<body>`** | contenido visible de la página |
| **`id`** | identificador único de un elemento |
| **`class`** | etiquetas de estilo (puede haber varias) |
| **`href`** | URL a la que navega un enlace `<a>` |
| **`<form>`** | contenedor de campos para enviar datos |
| **`<input>`** | campo de entrada (text, email, number, date, checkbox) |
| **`<table>`** | estructura de datos en filas y columnas |
| **`<thead>` / `<tbody>`** | cabecera / cuerpo de una tabla |
| **`<tr>` / `<th>` / `<td>`** | fila / celda de cabecera / celda de datos |
| **`<script>`** | carga y ejecuta JavaScript |
| **`<link rel="stylesheet">`** | carga CSS externo |
| **Query string** | parte después de `?` en una URL: `?id=abc-123` |
| **`e.preventDefault()`** | detiene el comportamiento nativo de un evento (p. ej. que un form recargue la página) |
| **`innerHTML`** | propiedad para leer o reemplazar el contenido HTML de un elemento |
| **`addEventListener`** | registra una función que se ejecuta cuando ocurre un evento (click, submit, input) |

---

## Dónde ver cada principio en el código real

| Principio | Dónde verlo |
|---|---|
| Estructura obligatoria | `inventario.html:1-11` |
| `<head>` con CSS y viewport | `inventario.html:3-9` |
| Layout Flexbox con `#navbar` + `.app-main` | `inventario.html:12-16` |
| `<a href>` para navegación | `inventario.js:69` (botón editar), `inventario.html:22` (nuevo insumo) |
| `<button>` con `data-id` + JS | `inventario.js:70` (botón desactivar) |
| `<form>` con `novalidate` | `login.html:26`, `insumo-form.html:29` |
| `<input type="email">` | `login.html:31` |
| `<input type="number" min="0">` | `insumo-form.html:52` |
| `<input type="date">` | `lote-form.html:46` |
| `<input type="checkbox">` | `insumo-form.html:64` |
| `<table>` con `thead`/`tbody` | `inventario.html:56-74` |
| `<tbody id="...">` para inyección JS | `inventario.html:70` |
| `colspan` para fila placeholder | `inventario.html:71` |
| Orden de scripts | `inventario.html:81-83` |
| Query param `?id=` | `inventario.js:69` (emisor), `insumo-form.js:8-9` (receptor) |
| `e.preventDefault()` en form | `login.js:26`, `insumo-form.js:36` |
| `innerHTML` para inyectar filas | `inventario.js:64-73` |

---

Este documento cubre los principios HTML que se usaron en el frontend de InventarioSIBE. Revisa el código real de cada archivo y cruza cada principio con su ejemplo. El rompecabezas se arma cuando entiendes que cada etiqueta está ahí por una razón: el `<a>` navega, el `<button>` dispara JS, el `id` es el puente con el JS, el `class` es el puente con el CSS, el `<form>` agrupa inputs, el `<tbody id>` es donde el JS inyecta las filas. No es magia: cada pieza tiene un rol.
