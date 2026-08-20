# Flujo de desarrollo — cómo añadir una pantalla nueva a InventarioSIBE

Esta guía describe, **paso a paso**, el flujo completo para añadir una pantalla nueva al sistema, aplicado al caso real de la **pantalla de movimientos** (bitácora de un lote). Es la receta que debes seguir para cualquier nueva vista.

## Índice

1. [Visión general del flujo](#1-visión-general-del-flujo)
2. [Fase 1 — Backend](#2-fase-1--backend)
3. [Fase 2 — Frontend](#3-fase-2--frontend)
4. [Fase 3 — Configuración (rutas limpias y seguridad)](#4-fase-3--configuración-rutas-limpias-y-seguridad)
5. [Fase 4 — Tests y build](#5-fase-4--tests-y-build)
6. [Fase 5 — Commit y push](#6-fase-5--commit-y-push)
7. [Caso práctico: pantalla de movimientos](#7-caso-práctico-pantalla-de-movimientos)
8. [Checklist final](#8-checklist-final)

---

## 1. Visión general del flujo

Cada pantalla del sistema se construye en **5 fases secuenciales**. No te saltes ninguna: cada una deja el reposorio en estado compilable y con tests verdes.

```
┌─────────────────────────────────────────────────────────────┐
│ FASE 1 — BACKEND                                             │
│   Entidad JPA  →  DTO  →  Service  →  Controller            │
│   (si toca esquema: migration Flyway V_n__descripcion.sql)  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ FASE 2 — FRONTEND                                            │
│   static/mipagina.html  +  static/js/mipagina.js            │
│   Cargar SIEMPRE: <script src="js/common.js"></script>       │
│   Usar authFetch() para todas las llamadas HTTP             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ FASE 3 — CONFIGURACIÓN                                       │
│   WebConfig.java      → forward de la ruta limpia           │
│   SecurityConfig.java → permitAll de la ruta                │
│   common.js (renderNavbar) → entrada del sidebar (si aplica)│
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ FASE 4 — TESTS Y BUILD                                       │
│   ./mvnw test  →  debe terminar BUILD SUCCESS               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ FASE 5 — COMMIT Y PUSH                                       │
│   git add ...  →  git commit -m "feat(scope): ..."          │
│   git push origin develop                                    │
└─────────────────────────────────────────────────────────────┘
```

**Principios de orden**:
- **Si la pantalla consume un endpoint que ya existe**, empieza en la Fase 2.
- **Si necesitas un endpoint nuevo**, empieza en la Fase 1.
- Cada fase debe dejar el repo en estado `mvnw test` verde.

---

## 2. Fase 1 — Backend

### 2.1 ¿Necesitas tocar el backend?

Antes de escribir nada, revisa si el endpoint que necesitas ya existe en los controllers:

```
src/main/java/.../auth/AuthController.java         → POST /api/auth/login
src/main/java/.../insumo/InsumoController.java     → CRUD /api/insumos
src/main/java/.../lote/LoteController.java         → /api/lotes
src/main/java/.../movimiento/MovimientoController  → /api/movimientos
src/main/java/.../usuario/UsuarioController.java   → /api/usuarios
```

- **Si existe** → salta a la Fase 2.
- **Si no existe** → sigue leyendo.

### 2.2 Entidad JPA (solo si toca el modelo de datos)

Si necesitas un campo nuevo o una tabla nueva:

1. **Campo nuevo en entidad existente**: añade el `@Column` + getter/setter en la clase `@Entity`.
2. **Tabla nueva**: crea la clase `@Entity` con sus campos.
3. **Migration Flyway**: crea `src/main/resources/db/migration/V_n__descripcion.sql` con el `ALTER TABLE` o `CREATE TABLE`.

> **Importante**: el proyecto usa `spring.jpa.hibernate.ddl-auto: validate` y Flyway. Hibernate **no** crea ni modifica tablas: solo valida que el esquema de la DB coincida con las entidades. **Toda** modificación del esquema debe ir en una migration Flyway, **antes** de tocar la entidad.

Convención de numeración: `V1__init_schema.sql`, `V2__lote_activo.sql`, `V3__...`. El número indica el orden de aplicación.

### 2.3 DTO (Data Transfer Object)

Los controllers **nunca** devuelven entidades JPA directamente: devuelven DTOs. Esto desacopla la API del modelo de datos y permite añadir campos calculados.

- **`XxxRequestDTO`**: lo que recibe el endpoint (entrada del POST/PUT). Lleva validaciones `@NotNull`, `@NotBlank`, `@Positive`.
- **`XxxResponseDTO`**: lo que devuelve. Se construye desde la entidad en el service.

Patrón del `LoteResponseDTO` (referencia):

```java
public class LoteResponseDTO {
    private UUID id;
    // ... campos planos ...
    private EstadoSemaforo estado;        // ← campo CALCULADO por el service
    private Long diasRestantes;

    public LoteResponseDTO() {}

    // El constructor recibe la entidad + los valores calculados
    public LoteResponseDTO(Lote lote, EstadoSemaforo estado, Long diasRestantes) {
        this.id = lote.getId();
        // ... copia campos ...
        this.estado = estado;
        this.diasRestantes = diasRestantes;
    }

    // getters/setters de cada campo
}
```

### 2.4 Service

El service contiene la **lógica de negocio**. Inyecta los repositories necesarios y expone métodos al controller.

Reglas:
- Anota con `@Service`.
- Métodos de lectura → `@Transactional(readOnly = true)`.
- Métodos de escritura → `@Transactional`.
- **Nunca** devolver la entidad: construir el DTO (idealmente con un método helper `aResponse(entidad)`).

Patrón del `LoteService.aResponse` (referencia para campos calculados):

```java
private final ConfiguracionSemaforoRepository configuracionSemaforoRepository;

private LoteResponseDTO aResponse(Lote lote) {
    LocalDate hoy = LocalDate.now();
    ConfiguracionSemaforo conf = configuracionSemaforoRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No existe configuración de semáforo"));
    EstadoSemaforo estado = lote.calcularEstadoSemaforo(conf, hoy);  // lógica de la entidad
    Long diasRestantes = estado == EstadoSemaforo.AGOTADO
            ? null
            : ChronoUnit.DAYS.between(hoy, lote.getFechaVencimiento());
    return new LoteResponseDTO(lote, estado, diasRestantes);
}
```

### 2.5 Controller

El controller es **fino**: solo enruta HTTP al service.

Reglas:
- `@RestController` + `@RequestMapping("/api/xxx")`.
- Cada método: `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping`.
- `@PreAuthorize("isAuthenticated()")` o `@PreAuthorize("hasRole('SUPERVISOR')")` para control de acceso.
- `@ResponseStatus(HttpStatus.CREATED)` en los POST que crean recursos.

Patrón de un endpoint GET (referencia):

```java
@GetMapping("/por-lote/{loteId}")
@PreAuthorize("isAuthenticated()")
public List<MovimientoResponseDTO> listarPorLote(@PathVariable UUID loteId) {
    return movimientoService.listarPorLote(loteId);
}
```

### 2.6 Errores del backend

El `GlobalExceptionHandler` ya traduce excepciones a respuestas JSON con `{mensaje, campos}`. **Lanza** las excepciones estándar, no las captures:

| Excepción | HTTP | Cuándo |
|---|---|---|
| `NoSuchElementException` | 404 | no se encontró el recurso por id |
| `IllegalArgumentException` | 400 | argumento inválido |
| `IllegalStateException` | 409 | regla de negocio violada (p. ej. stock insuficiente) |
| `MethodArgumentNotValidException` | 400 | validación de campos del DTO (lo maneja Spring solo) |

---

## 3. Fase 2 — Frontend

### 3.1 Estructura del HTML

Cada página interna usa el **layout Flexbox** con sidebar + contenido:

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Titulo · InventarioSIBE</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet" />
  <link href="css/custom.css" rel="stylesheet" />
</head>
<body>
  <!-- Wrapper Flexbox: sidebar + contenido -->
  <div class="app-layout">
    <div id="navbar"></div>            <!-- aquí inyecta el sidebar common.js -->

    <main class="app-main">
      <div class="container">
        <!-- ... contenido de la página ... -->
      </div>
    </main>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script src="js/common.js"></script>   <!-- SIEMPRE primero -->
  <script src="js/mipagina.js"></script> <!-- después el de la página -->
</body>
</html>
```

**Reglas del layout**:
- `#navbar` reserva 250px con fondo teal-dark **antes** de que JS inyecte el menú → mitiga el FOUC.
- `.app-main` lleva `flex: 1 1 0; min-width: 0` → evita overflow horizontal.
- El `.container` de Bootstrap va **dentro** de `.app-main`, nunca combinado (`container app-main` rompía el layout).

### 3.2 Estructura del JS

Cada página `js/mipagina.js` sigue **este patrón obligatorio**:

```js
/* mipagina.js — mipagina.html */

document.addEventListener('DOMContentLoaded', async function () {
  // ① Portero: si no hay sesión, redirige a login
  const usuario = requireAuth();
  if (!usuario) return;

  // ② Inyecta el sidebar (si la página es solo SUPERVISOR, usa requerirRol)
  renderNavbar(usuario);

  // ③ Toast opcional dejado por otra página
  const toast = consumirToast('toast_mipagina');
  if (toast) { /* pintar alert success */ }

  // ④ Cargar datos con authFetch (NUNCA fetch directo)
  const resp = await authFetch(API_BASE + '/recurso');
  if (!resp || !resp.ok) {
    mostrarErrorGlobal('No se pudo cargar la información.');
    return;
  }
  const datos = await resp.json();

  // ⑤ Listeners de filtros/botones
  document.getElementById('filtro').addEventListener('input', render);

  // ⑥ Función render() que pinta la tabla
  function render() { ... }

  render();
});
```

### 3.3 Reglas de oro del JS

| Regla | Por qué |
|---|---|
| `authFetch()` siempre, nunca `fetch()` directo | añade el `Authorization: Bearer` automáticamente y maneja 401 |
| `if (!usuario) return;` tras `requireAuth()` | la redirección a login no detiene el JS; sin el return el código sigue con `usuario === null` |
| `if (!resp \|\| !resp.ok) return;` tras `authFetch` | `authFetch` devuelve `null` si falló la red o no había token |
| `escapeHtml(...)` al inyectar texto del backend | evita XSS |
| `async function` en listeners que usan `await` | `await` solo existe dentro de funciones `async` |
| `try/catch/finally` en submits | el `finally` rehabilita el botón y oculta el spinner siempre |
| `parsearErrorApi(resp)` para errores del backend | extrae `{mensaje, campos}` del formato del `GlobalExceptionHandler` |
| `marcarToast(clave, msg)` antes de redirigir | el contexto JS se pierde al navegar; `sessionStorage` pasa el mensaje |

### 3.4 Patrón de pintar tabla

```js
const tbody = document.getElementById('tablaX');
tbody.innerHTML = items.map(function (item) {
  return '<tr>' +
    '<td>' + escapeHtml(item.nombre) + '</td>' +          // ← escapeHtml SIEMPRE
    '<td>' + formatFecha(item.fecha) + '</td>' +
    '<td><span class="badge badge-sem ' + item.clase + '">' + escapeHtml(item.estado) + '</span></td>' +
    '<td class="text-end">' +
      '<button class="btn btn-sm btn-outline-teal" data-id="' + item.id + '">Acción</button>' +
    '</td>' +
    '</tr>';
}).join('');

// Registrar listeners DESPUÉS de inyectar el HTML
tbody.querySelectorAll('button[data-id]').forEach(function (btn) {
  btn.addEventListener('click', async function () {
    const id = btn.getAttribute('data-id');
    // ... acción
  });
});
```

### 3.5 Funciones de `common.js` que debes reutilizar

| Función | Uso |
|---|---|
| `requireAuth()` / `requerirRol(rol)` | portero al inicio de cada página |
| `authFetch(url, options)` | todas las llamadas HTTP autenticadas |
| `renderNavbar(usuario)` | inyecta el sidebar |
| `calcularEstadoSemaforo(lote)` | mapea el enum `EstadoSemaforo` del backend a `{estado, clase, diasRestantes}` |
| `badgeSemaforoHtml(lote)` | HTML del badge de semáforo ya armado |
| `formatFecha(fecha)` | `YYYY-MM-DD` → `dd/mm/yyyy` |
| `escapeHtml(s)` | escapado anti-XSS |
| `parsearErrorApi(resp)` | extrae error del backend |
| `mostrarErrorForm(msg, campos)` | pinta error en `#formError` |
| `mostrarErrorGlobal(msg)` | pinta error en `#errorGlobal` |
| `marcarToast(clave, msg)` / `consumirToast(clave)` | mensajes entre páginas |

(Referencia completa en `docs/frontend-js-reference.md`.)

---

## 4. Fase 3 — Configuración (rutas limpias y seguridad)

Para que la URL del navegador sea `/mipagina` (sin `.html`), registra **dos** piezas:

### 4.1 `WebConfig.java`

```java
@Override
public void addViewControllers(ViewControllerRegistry registry) {
    // ... rutas existentes ...
    forward(registry, "/mipagina", "forward:/mipagina.html");
}
```

El `forward` es **interno**: Spring sirve el `.html` sin cambiar la URL visible.

### 4.2 `SecurityConfig.java`

En el `.requestMatchers(...).permitAll()` añade la ruta limpia y la con extensión:

```java
.requestMatchers(
    // ... rutas existentes ...
    "/mipagina", "/mipagina.html",
    "/css/**", "/js/**", "/img/**",
    // ...
).permitAll()
```

Sin esto, el filtro JWT bloquearía la página con 401 antes de que se sirva el HTML.

### 4.3 `common.js` (sidebar)

Si la página debe aparecer en el menú lateral, añádelo al array `items` de `renderNavbar`:

```js
const items = [
    { href: 'dashboard', label: 'Dashboard', icon: 'bi-speedometer2' },
    // ... demás ...
    { href: 'mipagina',  label: 'Mi página', icon: 'bi-xxx' }   // ← nuevo
];
```

El icono es de Bootstrap Icons (`bi-*`). El "ruta activa" se marca automáticamente comparando el path actual.

---

## 5. Fase 4 — Tests y build

Antes de commitear, verifica que todo compila y los tests pasan:

```powershell
.\mvnw.cmd test
```

Debe terminar con:

```
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Reglas**:
- Si tocaste el backend, los tests unitarios (`LoteServiceTest`, `MovimientoServiceTest`, etc.) deben seguir pasando. Si añadiste una dependencia nueva al service, actualiza los mocks del test.
- Si añadiste campo a una entidad, asegúrate de que la migration Flyway esté creada → si no, `ddl-auto: validate` falla al arrancar.
- El frontend (HTML/CSS/JS) no se compila, pero si rompes `common.js` las páginas dejan de cargar el sidebar.

### Cómo probar la UI

```powershell
.\mvnw.cmd spring-boot:run
```

Abre `http://localhost:8080/login` y prueba la pantalla nueva.

---

## 6. Fase 5 — Commit y push

### 6.1 Convención de mensajes

Formato: `tipo(scope): descripción`

| tipo | cuándo |
|---|---|
| `feat` | nueva funcionalidad |
| `fix` | corrección de bug |
| `refactor` | reestructuración sin cambio de comportamiento |
| `docs` | solo documentación |
| `test` | solo tests |
| `chore` | tareas de mantenimiento |

`scope` = dominio afectado (`lote`, `movimiento`, `auth`, `frontend`, `semaforo`, `layout`...).

### 6.2 Flujo de commit

```powershell
# Ver qué cambió
git status
git diff --stat

# Stagear por grupos lógicos (un commit por feature, no un commit gigante)
git add src/main/java/.../movimiento/MovimientoController.java `
        src/main/java/.../movimiento/MovimientoService.java `
        src/main/java/.../movimiento/MovimientoRepository.java

git commit -m "feat(movimiento): listar movimientos por usuario"
```

### 6.3 Push

```powershell
git push origin develop
```

> **Nota de seguridad**: nunca hagas `git push --force` a `main` o `develop` sin confirmación explícita.

---

## 7. Caso práctico: pantalla de movimientos

Vamos a aplicar el flujo completo a la **bitácora de movimientos de un lote** (la pantalla que falta).

### 7.1 Estado actual

**Endpoint disponible**:

```
GET /api/movimientos/por-lote/{loteId}   →  [MovimientoResponseDTO]
```

Ya existe en `MovimientoController:36` y devuelve una lista de `MovimientoResponseDTO` con estos campos:

| campo | tipo | nota |
|---|---|---|
| `id` | UUID | |
| `loteId` | UUID | |
| `usuarioId` | UUID | hay que resolver el nombre del usuario |
| `tipo` | enum `ENTRADA`/`SALIDA` | |
| `cantidad` | int | |
| `fecha` | LocalDateTime | ISO string |
| `observacion` | string \| null | |

**No existe**: una pantalla `movimientos.html` que la consuma.

### 7.2 Decisión de alcance

Como el endpoint **ya existe**, **no hace falta tocar el backend** (Fase 1). Empiezas directo en la Fase 2.

> Si más adelante quieres ver "todos los movimientos" (no solo de un lote), ahí sí necesitarías añadir `GET /api/movimientos` en `MovimientoController` + `MovimientoService.listar()`. Pero para la bitácora por lote, el endpoint actual basta.

### 7.3 Fase 2 — Frontend

#### `static/movimientos.html`

Sigue la plantilla del §3.1. Tabla con columnas: **Fecha, Tipo, Cantidad, Usuario, Observación**.

Como el `MovimientoResponseDTO` solo trae `usuarioId` (no el nombre), necesitas resolver el nombre. Dos opciones:

- **Opción A (recomendada)**: cargar la lista de usuarios con `GET /api/usuarios` (solo SUPERVISOR puede) y armar un mapa `usuarioId → nombre`. **Problema**: si el usuario logueado es ENFERMERIA, no podrá ver los nombres (el endpoint `/usuarios` es solo SUPERVISOR).
- **Opción B**: añadir `nombreUsuario` al `MovimientoResponseDTO` en el backend (join en el service). Es más limpio pero requiere Fase 1.

Para una primera versión sin tocar backend, **muestra el `usuarioId` recortado** (los primeros 8 caracteres) como etiqueta. Es feo pero funcional. Luego mejoras con la Opción B.

Estructura del HTML:

```html
<div class="app-layout">
  <div id="navbar"></div>
  <main class="app-main">
    <div class="container">
      <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
          <h2 class="card-title-section mb-1">Bitácora del lote</h2>
          <p class="text-muted small mb-0" id="loteInfo">Cargando…</p>
        </div>
        <a href="lotes" class="btn btn-link text-teal px-0">
          <i class="bi bi-arrow-left"></i> Volver a lotes
        </a>
      </div>

      <div id="errorGlobal"></div>

      <div class="card app-card">
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Tipo</th>
                  <th>Cantidad</th>
                  <th>Usuario</th>
                  <th>Observación</th>
                </tr>
              </thead>
              <tbody id="tablaMovimientos">
                <tr><td colspan="5" class="text-center text-muted py-4">Cargando…</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </main>
</div>
```

#### `static/js/movimientos.js`

```js
/* movimientos.js — movimientos.html: bitácora de movimientos de un lote */

document.addEventListener('DOMContentLoaded', async function () {
  const usuario = requireAuth();
  if (!usuario) return;
  renderNavbar(usuario);

  const tbody = document.getElementById('tablaMovimientos');
  const loteInfo = document.getElementById('loteInfo');

  // El loteId llega por query: /movimientos?loteId=abc-123
  const params = parametrosUrl();
  const loteId = params.loteId;

  if (!loteId) {
    mostrarErrorGlobal('Falta el id del lote en la URL (?loteId=...).');
    return;
  }

  // Cargar info del lote (para el encabezado) + sus movimientos en paralelo
  const [respLote, respMov] = await Promise.all([
    authFetch(API_BASE + '/lotes/' + loteId),
    authFetch(API_BASE + '/movimientos/por-lote/' + loteId)
  ]);

  if (!respLote || !respLote.ok) {
    mostrarErrorGlobal('No se pudo cargar el lote.');
    return;
  }
  if (!respMov || !respMov.ok) {
    mostrarErrorGlobal('No se pudo cargar la bitácora de movimientos.');
    return;
  }

  const lote = await respLote.json();
  const movimientos = await respMov.json();

  // Encabezado con info del lote + badge semáforo
  loteInfo.innerHTML = '<strong>' + escapeHtml(lote.numeroLote) + '</strong> · ' +
    'Stock actual: ' + lote.cantidadActual + ' · ' + badgeSemaforoHtml(lote);

  if (!movimientos.length) {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">Sin movimientos registrados.</td></tr>';
    return;
  }

  // Ordenar por fecha descendente (más reciente primero)
  movimientos.sort(function (a, b) {
    return (b.fecha || '').localeCompare(a.fecha || '');
  });

  tbody.innerHTML = movimientos.map(function (m) {
    const esEntrada = m.tipo === 'ENTRADA';
    const tipoBadge = esEntrada
      ? '<span class="badge badge-sem badge-activo">Entrada</span>'
      : '<span class="badge badge-sem badge-critico">Salida</span>';
    const cantidadTxt = (esEntrada ? '+' : '−') + m.cantidad;
    const usuarioTxt = m.usuarioId ? m.usuarioId.substring(0, 8) : '–';
    const obs = m.observacion ? escapeHtml(m.observacion) : '<span class="text-muted">–</span>';

    return '<tr>' +
      '<td>' + escapeHtml(m.fecha || '') + '</td>' +   // mejorar con formatFechaHora
      '<td>' + tipoBadge + '</td>' +
      '<td class="fw-semibold ' + (esEntrada ? 'text-success' : 'text-danger') + '">' + cantidadTxt + '</td>' +
      '<td><code>' + escapeHtml(usuarioTxt) + '</code></td>' +
      '<td>' + obs + '</td>' +
      '</tr>';
  }).join('');
});
```

**Mejoras pendientes que puedes aplicar después**:
- `formatFechaHora` en `common.js` para mostrar `dd/mm/yyyy HH:MM` (la `fecha` viene con hora).
- Resolver el nombre del usuario: opción B (campo `nombreUsuario` en el DTO del backend).
- En `lotes.js`, añadir un botón "Ver bitácora" por fila que enlace a `movimientos?loteId=...`.

### 7.4 Fase 3 — Configuración

Añade la ruta limpia en **dos** sitios:

**`WebConfig.java`**:
```java
forward(registry, "/movimientos", "forward:/movimientos.html");
```

**`SecurityConfig.java`** (en `permitAll`):
```java
"/movimientos", "/movimientos.html",
```

**`common.js`** (sidebar) — opcional, porque la bitácora se llega desde `lotes` (con `?loteId=`), no es una página suelta del menú. Si la quieres en el menú:
```js
{ href: 'movimientos', label: 'Movimientos', icon: 'bi-clock-history' }
```
Pero como requiere `?loteId=`, mejor déjala fuera del sidebar y enlázala desde `lotes.js`.

### 7.5 Fase 4 — Test

```powershell
.\mvnw.cmd test
```

Como no tocaste backend, los tests siguen verdes. Verifica la UI con `.\mvnw.cmd spring-boot:run`.

### 7.6 Fase 5 — Commit

```powershell
git add src/main/resources/static/movimientos.html `
        src/main/resources/static/js/movimientos.js `
        src/main/java/co/edu/uceva/inventariosibe/config/WebConfig.java `
        src/main/java/co/edu/uceva/inventariosibe/config/SecurityConfig.java

git commit -m "feat(movimientos): pantalla de bitacora por lote"
git push origin develop
```

---

## 8. Checklist final

Antes de dar por terminada una pantalla nueva, verifica:

- [ ] **Backend**: el endpoint existe (o lo añadiste siguiendo entidad → DTO → service → controller → migration).
- [ ] **HTML**: usa el layout `app-layout` + `#navbar` + `main.app-main` + `container`.
- [ ] **JS**: empieza con `requireAuth()` + `if (!usuario) return` + `renderNavbar(usuario)`.
- [ ] **JS**: todas las llamadas HTTP por `authFetch`, nunca `fetch` directo.
- [ ] **JS**: todo texto del backend pasa por `escapeHtml` al inyectar HTML.
- [ ] **JS**: errores del backend con `parsearErrorApi` + `mostrarErrorForm`/`mostrarErrorGlobal`.
- [ ] **Config**: ruta limpia registrada en `WebConfig` Y `SecurityConfig`.
- [ ] **Sidebar**: entrada añadida en `common.js` si procede.
- [ ] **Build**: `.\mvnw.cmd test` termina `BUILD SUCCESS`.
- [ ] **Commit**: mensaje con formato `tipo(scope): descripción`.
- [ ] **Push**: `git push origin develop`.

---

## Apéndice — endpoints disponibles y consumidos

| Endpoint | Consumido por | Estado |
|---|---|---|
| `POST /api/auth/login` | login.js | ✅ |
| `GET /api/insumos` | dashboard, inventario, lote-form, movimiento-wizard, lotes | ✅ |
| `GET /api/insumos/{id}` | insumo-form | ✅ |
| `POST /api/insumos` | insumo-form | ✅ |
| `PUT /api/insumos/{id}` | insumo-form | ✅ |
| `DELETE /api/insumos/{id}` | inventario | ✅ |
| `GET /api/lotes` | dashboard, inventario, lotes | ✅ |
| `GET /api/lotes/{id}` | (futuro: movimientos.js usa `lotes/{id}`) | 🔶 |
| `GET /api/lotes/por-insumo/{id}` | movimiento-wizard | ✅ |
| `POST /api/lotes` | lote-form | ✅ |
| `PATCH /api/lotes/{id}/activo` | lotes | ✅ |
| `POST /api/movimientos` | movimiento-wizard | ✅ |
| `GET /api/movimientos/por-lote/{loteId}` | (futuro: movimientos.js) | 🔶 |
| `GET /api/movimientos/por-usuario/{usuarioId}` | (sin consumidor) | 🔶 |
| `GET /api/usuarios` | usuarios, asegurarUsuarioId | ✅ |
| `POST /api/usuarios` | usuarios | ✅ |
| `GET /api/usuarios/{id}` | (sin consumidor) | 🔶 |

✅ = consumido · 🔶 = disponible pero pendiente de pantalla
