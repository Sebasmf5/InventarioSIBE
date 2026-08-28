# InventarioSIBE

Sistema de gestión de inventario para el programa de Monitoría de Bienestar de la UCEVA. Controla insumos médicos y medicamentos por lotes, con seguimiento de vencimientos (semáforo), trazabilidad de movimientos (entradas/salidas) y auditoría.

> **Nota para asistentes de IA**: este README es la puerta de entrada al proyecto. Lee primero la sección [Arquitectura](#arquitectura) y luego [Reglas de negocio](#reglas-de-negocio) antes de tocar código. La documentación detallada está en `docs/`.

---

## Tabla de contenidos

1. [Stack tecnológico](#stack-tecnológico)
2. [Arquitectura](#arquitectura)
3. [Estructura del proyecto](#estructura-del-proyecto)
4. [Modelo de datos](#modelo-de-datos)
5. [API REST](#api-rest)
6. [Frontend](#frontend)
7. [Reglas de negocio](#reglas-de-negocio)
8. [Seguridad](#seguridad)
9. [Migrations y base de datos](#migrations-y-base-de-datos)
10. [Cómo ejecutar](#cómo-ejecutar)
11. [Testing](#testing)
12. [Convenciones](#convenciones)
13. [Documentación adicional](#documentación-adicional)

---

## Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Backend | Spring Boot | 4.1.0 |
| Lenguaje | Java | 17 (pom) / 22 (runtime) |
| ORM | Hibernate (Spring Data JPA) | incluido en Spring Boot |
| Base de datos (prod) | PostgreSQL (Neon) | hospedado en la nube |
| Base de datos (tests) | H2 en memoria | modo PostgreSQL |
| Migrations | Flyway | incluido en Spring Boot |
| Autenticación | JWT (jjwt 0.12.6) | sin estado, 8h de expiración |
| Build | Maven (wrapper incluido) | 3.9.16 |
| Frontend | HTML + JavaScript vanilla + Bootstrap 5.3 | sin framework SPA |
| Iconos | Bootstrap Icons 1.11 | CDN |
| Repositorio | GitHub | `Sebasmf5/InventarioSIBE` |

---

## Arquitectura

### Visión general

```
┌─────────────────────────────────────────────────────┐
│                    NAVEGADOR                         │
│                                                      │
│  HTML estático + JS vanilla + Bootstrap 5            │
│  (servido por Spring Boot desde /static)             │
│                                                      │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐  │
│  │ login.js │  │common.js │  │  página.js (x7)   │  │
│  └────┬─────┘  └────┬─────┘  └────────┬──────────┘  │
│       │             │                 │              │
│       └─────────────┴─────────────────┘              │
│                     │                                │
│            authFetch() con JWT                       │
└─────────────────────┼───────────────────────────────┘
                      │ HTTP (JSON)
                      ▼
┌─────────────────────────────────────────────────────┐
│              SPRING BOOT (puerto 8080)               │
│                                                      │
│  SecurityConfig ── JwtAuthenticationFilter           │
│       │                                              │
│       ▼                                              │
│  @RestController (API REST, /api/*)                  │
│       │                                              │
│       ▼                                              │
│  @Service (lógica de negocio)                       │
│       │                                              │
│       ▼                                              │
│  JpaRepository (acceso a datos)                      │
│       │                                              │
│       ▼                                              │
│  Flyway ── PostgreSQL (Neon)                         │
└─────────────────────────────────────────────────────┘
```

### Backend — arquitectura en capas

El backend sigue el patrón **layered architecture** clásico de Spring Boot:

```
Controller  →  Service  →  Repository  →  Entidad JPA  →  Tabla DB
    │              │            │
    │              │            └── JpaRepository<Entidad, UUID>
    │              └── @Service, @Transactional
    └── @RestController, @PreAuthorize, DTOs de entrada/salida
```

**Reglas de las capas**:
- **Controller**: solo enruta HTTP → service. No tiene lógica de negocio. Recibe DTOs de entrada (`XxxRequestDTO`) y devuelve DTOs de salida (`XxxResponseDTO`). Nunca expone entidades JPA directamente.
- **Service**: contiene la lógica de negocio. Inyecta repositories. Métodos de lectura con `@Transactional(readOnly = true)`, escritura con `@Transactional`. Construye los DTOs de respuesta (helper `aResponse(entidad)`).
- **Repository**: interfaz que extiende `JpaRepository`. Spring Data genera la implementación. Métodos de consulta por convención de nombres (`findByEmail`, `findByInsumoId`).
- **Entidad**: clase `@Entity` que mapea a una tabla. Contiene lógica de dominio (ej. `Lote.calcularEstadoSemaforo()`, `Lote.registrarSalida()`).

### Frontend — arquitectura sin framework

No es una SPA (Single Page Application). Es un **sitio multipágina tradicional** donde cada pantalla es un `.html` independiente servido por Spring Boot desde `src/main/resources/static/`.

```
static/
├── index.html              → redirección inicial según localStorage
├── login.html + js/login.js
├── dashboard.html + js/dashboard.js
├── inventario.html + js/inventario.js
├── insumo-form.html + js/insumo-form.js
├── lote-form.html + js/lote-form.js
├── movimiento-wizard.html + js/movimiento-wizard.js
├── usuarios.html + js/usuarios.js
├── lotes.html + js/lotes.js
├── movimientos.html + js/movimientos.js
├── css/custom.css
└── js/common.js            → utilidades compartidas (auth, fetch, navbar, semáforo)
```

**Patrón de cada página**:
1. `requireAuth()` → portero: si no hay JWT en `localStorage`, redirige a `/login`.
2. `renderNavbar(usuario)` → inyecta el sidebar lateral en `<div id="navbar">`.
3. `authFetch(API_BASE + '/recurso')` → llama al backend con el JWT.
4. Pinta la tabla/formulario con `innerHTML` + `escapeHtml()`.

**Comunicación**: el navegador pide el HTML (GET) y luego el JS pide los datos (fetch con JWT). Son dos peticiones independientes.

---

## Estructura del proyecto

```
InventarioSIBE/
├── src/main/java/co/edu/uceva/inventariosibe/
│   ├── InventarioSibeApplication.java     → @SpringBootApplication (entry point)
│   ├── auth/
│   │   ├── AuthController.java            → POST /api/auth/login
│   │   ├── AuthService.java               → valida credenciales, genera JWT
│   │   └── dto/
│   │       ├── LoginRequestDTO.java       → {email, password}
│   │       └── LoginResponseDTO.java      → {id, token, email, nombre, rol}
│   ├── config/
│   │   ├── SecurityConfig.java            → filter chain, permitAll de rutas
│   │   ├── WebConfig.java                 → forward de rutas limpias (/login → /login.html)
│   │   ├── JwtService.java                → generar/validar JWT, extraer claims
│   │   ├── JwtAuthenticationFilter.java   → filtra cada petición, carga la authority
│   │   ├── GlobalExceptionHandler.java    → @ControllerAdvice, traduce excepciones a JSON
│   │   └── ErrorResponseDTO.java          → {timestamp, status, mensaje, campos}
│   ├── insumo/
│   │   ├── Insumo.java                    → @Entity (tabla insumo)
│   │   ├── InsumoController.java          → CRUD /api/insumos
│   │   ├── InsumoService.java             → lógica + cálculo de estado agregado
│   │   ├── InsumoRepository.java          → JpaRepository
│   │   ├── TipoInsumo.java                → enum {MEDICAMENTO, INSUMO_MEDICO}
│   │   └── dto/
│   │       ├── InsumoRequestDTO.java      → entrada POST/PUT
│   │       └── InsumoResponseDTO.java     → salida (incluye estadoInsumo)
│   ├── lote/
│   │   ├── Lote.java                      → @Entity + lógica de dominio
│   │   ├── LoteController.java            → /api/lotes
│   │   ├── LoteService.java               → registrarLote, cambiarEstado, aResponse
│   │   ├── LoteRepository.java            → JpaRepository + findByInsumoId
│   │   ├── EstadoSemaforo.java            → enum {VERDE, AMARILLO, ROJO, AGOTADO}
│   │   ├── ConfiguracionSemaforo.java     → @Entity (umbrales en DB)
│   │   ├── ConfiguracionSemaforoRepository.java
│   │   └── dto/
│   │       ├── RegistrarLoteRequestDTO.java
│   │       └── LoteResponseDTO.java       → incluye estado, diasRestantes, stockFormateado
│   ├── movimiento/
│   │   ├── Movimiento.java                → @Entity (tabla movimiento)
│   │   ├── MovimientoController.java      → /api/movimientos
│   │   ├── MovimientoService.java         → registrar + listar + aResponse enriquecido
│   │   ├── MovimientoRepository.java      → findByLoteId, findByUsuarioId
│   │   ├── TipoMovimiento.java            → enum {ENTRADA, SALIDA}
│   │   └── dto/
│   │       ├── MovimientoRequestDTO.java  → {loteId, usuarioId, tipo, cantidad, observacion}
│   │       └── MovimientoResponseDTO.java → enriquecido con nombreUsuario, numeroLote, etc.
│   └── usuario/
│       ├── Usuario.java                   → @Entity (tabla usuario)
│       ├── UsuarioController.java         → /api/usuarios
│       ├── UsuarioService.java            → crear + listar
│       ├── UsuarioRepository.java         → findByEmail
│       ├── Rol.java                       → enum {ENFERMERIA, SUPERVISOR}  ← pendiente eliminar SUPERVISOR
│       └── dto/
│           ├── UsuarioRequestDTO.java
│           └── UsuarioResponseDTO.java
├── src/main/resources/
│   ├── application.yml                    → config global (perfil dev activo)
│   ├── application-dev.yml                → credenciales Neon + JWT_SECRET (NO commiteado)
│   ├── application-dev.yml.example        → template para copiar
│   ├── db/migration/
│   │   ├── V1__init_schema.sql            → tablas + datos iniciales
│   │   ├── V2__lote_activo.sql            → baja lógica del lote
│   │   └── V3__insumo_unidades_por_caja.sql → factor de conversión caja→unidades
│   └── static/                            → frontend (ver sección Frontend)
├── src/test/
│   ├── java/.../                          → tests unitarios y de integración
│   └── resources/application.yml         → config de tests (H2 en memoria)
├── docs/
│   ├── frontend-js-api.md                 → tutorial async/await aplicado al proyecto
│   ├── frontend-js-reference.md           → referencia función por función del JS
│   ├── flujo-desarrollo.md                → guía paso a paso para añadir pantallas
│   ├── html-principios.md                 → principios HTML usados en el frontend
│   └── comandos-consola.md               → comandos de Maven, git, logs, debug
└── pom.xml                                → dependencias y build
```

---

## Modelo de datos

### Esquema de la base de datos

```
┌──────────────────┐       ┌──────────────────┐
│     insumo        │       │     usuario       │
├──────────────────┤       ├──────────────────┤
│ id (UUID) PK     │       │ id (UUID) PK     │
│ nombre           │       │ nombre           │
│ presentacion     │       │ email (unique)   │
│ unidad_medida    │       │ password_hash    │
│ stock_minimo (u) │       │ rol              │
│ activo           │       │ activo           │
│ marca            │       └────────┬─────────┘
│ tipo             │                │
│ registro_invima  │                │
│ unidades_por_caja│                │
└──────┬───────────┘                │
       │ 1                          │ N
       │                            │
       │ N                          │
┌──────┴───────────┐       ┌────────┴─────────┐
│      lote         │       │   movimiento      │
├──────────────────┤       ├──────────────────┤
│ id (UUID) PK     │       │ id (UUID) PK     │
│ insumo_id (FK)   │◄──┐   │ lote_id (FK)     │──► lote
│ numero_lote      │   └──►│ usuario_id (FK)  │──► usuario
│ fecha_vencimiento│       │ tipo (ENTRADA/    │
│ cantidad_inicial │       │      SALIDA)     │
│ cantidad_actual  │       │ cantidad (u)     │
│ fecha_ingreso    │       │ fecha (timestamp) │
│ ubicacion        │       │ observacion      │
│ activo           │       └──────────────────┘
└──────────────────┘

┌───────────────────────┐
│ configuracion_semaforo │  (1 fila, umbrales globales)
├───────────────────────┤
│ id (UUID) PK          │
│ dias_verde (default 90)│
│ dias_amarillo (def 30) │
│ dias_rojo (default 0)  │
└───────────────────────┘
```

### Entidades y relaciones

| Entidad | Tabla | Relaciones | Notas |
|---|---|---|---|
| `Insumo` | `insumo` | 1 → N lotes | `unidadesPorCaja` define el factor de conversión. Un insumo se identifica por nombre + marca. |
| `Lote` | `lote` | N → 1 insumo, 1 → N movimientos | `cantidadActual` en unidades. `activo` = baja lógica. No se edita stock directamente, solo vía movimientos. |
| `Movimiento` | `movimiento` | N → 1 lote, N → 1 usuario | Registro inmutable de cada entrada/salida. `cantidad` en unidades. |
| `Usuario` | `usuario` | 1 → N movimientos | Password hasheado con BCrypt. Rol define permisos. |
| `ConfiguracionSemaforo` | `configuracion_semaforo` | ninguna | Una sola fila. Umbrales de vencimiento configurables en DB. |

### UUIDs

Todas las PKs son `UUID` generados por Hibernate (`@GeneratedValue(strategy = GenerationType.UUID)`). No se usa auto-increment numérico.

---

## API REST

Todos los endpoints bajo `/api`. Los de negocio requieren cabecera `Authorization: Bearer <token>`.

### Autenticación

| Método | URL | Body | Respuesta |
|---|---|---|---|
| POST | `/api/auth/login` | `{email, password}` | 200 `{id, token, email, nombre, rol}` |

### Insumos

| Método | URL | Body | Respuesta | Notas |
|---|---|---|---|---|
| GET | `/api/insumos` | — | `[InsumoResponseDTO]` | incluye `estadoInsumo` (peor estado entre lotes) |
| GET | `/api/insumos/{id}` | — | `InsumoResponseDTO` | |
| POST | `/api/insumos` | `InsumoRequestDTO` | 201 `InsumoResponseDTO` | |
| PUT | `/api/insumos/{id}` | `InsumoRequestDTO` | `InsumoResponseDTO` | |
| DELETE | `/api/insumos/{id}` | — | 204 | baja lógica (`activo = false`) |

### Lotes

| Método | URL | Body | Respuesta | Notas |
|---|---|---|---|---|
| GET | `/api/lotes` | — | `[LoteResponseDTO]` | incluye `estado`, `diasRestantes`, `stockFormateado` |
| GET | `/api/lotes/{id}` | — | `LoteResponseDTO` | |
| GET | `/api/lotes/por-insumo/{insumoId}` | — | `[LoteResponseDTO]` | |
| POST | `/api/lotes` | `RegistrarLoteRequestDTO` | 201 `LoteResponseDTO` | genera movimiento ENTRADA automático |
| PATCH | `/api/lotes/{id}/activo` | `{activo, motivo?}` | `LoteResponseDTO` | baja lógica del lote |

### Movimientos

| Método | URL | Body | Respuesta | Notas |
|---|---|---|---|---|
| GET | `/api/movimientos` | — | `[MovimientoResponseDTO]` | panel general de auditoría |
| GET | `/api/movimientos/por-lote/{loteId}` | — | `[MovimientoResponseDTO]` | bitácora de un lote |
| GET | `/api/movimientos/por-usuario/{usuarioId}` | — | `[MovimientoResponseDTO]` | historial por usuario |
| POST | `/api/movimientos` | `MovimientoRequestDTO` | 201 `MovimientoResponseDTO` | descuenta/aumenta stock del lote |

### Usuarios

| Método | URL | Body | Respuesta | Notas |
|---|---|---|---|---|
| GET | `/api/usuarios` | — | `[UsuarioResponseDTO]` | |
| GET | `/api/usuarios/{id}` | — | `UsuarioResponseDTO` | |
| POST | `/api/usuarios` | `UsuarioRequestDTO` | 201 `UsuarioResponseDTO` | |

### Formato de errores

Todos los errores usan el formato de `GlobalExceptionHandler`:

```json
// Error de negocio (404, 409)
{ "timestamp": "...", "status": 404, "mensaje": "...", "campos": null }

// Error de validación (400)
{ "timestamp": "...", "status": 400, "mensaje": "Validación de campos fallida",
  "campos": { "nombre": "no debe estar vacío" } }
```

---

## Frontend

### Layout

Todas las páginas internas usan el layout Flexbox:

```html
<div class="app-layout">
  <div id="navbar"></div>
  <main class="app-main">
    <div class="container">
      <!-- contenido -->
    </div>
  </main>
</div>
```

- `#navbar` reserva 250px con fondo teal-dark **antes** de que JS inyecte el sidebar (mitiga FOUC).
- `.app-main` con `flex: 1 1 0; min-width: 0` evita overflow horizontal.
- En móvil, el sidebar se vuelve un overlay fijo con botón hamburger.

### `js/common.js` — utilidades compartidas

Funciones que usan **todas** las páginas:

| Función | Propósito |
|---|---|
| `requireAuth()` | portero: si no hay JWT → redirige a login |
| `requerirRol(rol)` | portero con validación de rol (pendiente eliminar) |
| `authFetch(url, opts)` | `fetch` con `Authorization: Bearer` automático; en 401 cierra sesión |
| `renderNavbar(usuario)` | inyecta el sidebar lateral |
| `calcularEstadoSemaforo(lote)` | mapea el enum `EstadoSemaforo` del backend a etiqueta + clase CSS |
| `badgeSemaforoHtml(lote)` | HTML del badge de semáforo |
| `formatFecha(fecha)` | `YYYY-MM-DD` → `dd/mm/yyyy` |
| `escapeHtml(s)` | escapado anti-XSS |
| `parsearErrorApi(resp)` | extrae `{mensaje, campos}` del error del backend |
| `mostrarErrorForm(msg, campos)` | pinta alert en `#formError` |
| `mostrarErrorGlobal(msg)` | pinta alert en `#errorGlobal` |
| `marcarToast(clave, msg)` / `consumirToast(clave)` | mensajes entre páginas vía `sessionStorage` |

### Pantallas

| Pantalla | HTML | JS | Función |
|---|---|---|---|
| Login | `login.html` | `login.js` | autenticación JWT |
| Dashboard | `dashboard.html` | `dashboard.js` | 4 tarjetas de conteo + tabla de urgentes |
| Inventario | `inventario.html` | `inventario.js` | tabla de insumos con filtros y baja lógica |
| Formulario insumo | `insumo-form.html` | `insumo-form.js` | crear/editar insumo |
| Formulario lote | `lote-form.html` | `lote-form.js` | registrar lote (cajas + unidades sueltas) |
| Wizard movimiento | `movimiento-wizard.html` | `movimiento-wizard.js` | 3 pasos: insumo → lote → tipo/cantidad |
| Gestión de lotes | `lotes.html` | `lotes.js` | tabla de lotes con toggle activar/deshabilitar |
| Movimientos | `movimientos.html` | `movimientos.js` | panel de auditoría + bitácora por lote |
| Usuarios | `usuarios.html` | `usuarios.js` | listar + crear usuarios (modal) |

### Rutas limpias

`WebConfig.java` hace `forward` de rutas sin extensión:
- `/login` → sirve `/login.html` (sin cambiar la URL visible)
- `/dashboard` → sirve `/dashboard.html`
- etc.

`SecurityConfig.java` permite todas las rutas de páginas estáticas en `permitAll()`.

---

## Reglas de negocio

### 1. Stock en unidades indivisibles

El stock base es siempre la **unidad indivisible** (tableta, ampolla, jeringa). Las cajas se **derivan** por división:

```
cajas = cantidadActual / unidadesPorCaja
unidadesSueltas = cantidadActual % unidadesPorCaja
```

`unidadesPorCaja` vive en el `Insumo` (la presentación la define el medicamento, no cada lote). Default 1 para insumos sin empaque.

### 2. Semáforo de vencimiento

El cálculo se hace en el **backend** (`Lote.calcularEstadoSemaforo`) con umbrales de `configuracion_semaforo` (configurables en DB):

| Condición | Estado | Color |
|---|---|---|
| `cantidadActual <= 0` | AGOTADO | gris |
| vence en 91+ días | VERDE | verde |
| vence en 31-90 días | AMARILLO | amarillo |
| vence en 30 días o menos (incluye vencidos) | ROJO | rojo |

El `LoteResponseDTO` ya trae `estado` (enum) y `diasRestantes` calculados. El frontend solo mapea el enum a etiqueta + clase CSS.

### 3. Estado agregado del insumo

El `InsumoResponseDTO` trae `estadoInsumo`: el **peor** estado entre sus lotes activos. Se calcula en `InsumoService.aResponse()` usando `EstadoSemaforo.getSeveridad()`:

```
ROJO (3) > AMARILLO (2) > VERDE (1) > AGOTADO (0)
```

Si el insumo no tiene lotes activos → `estadoInsumo = null` (el front muestra "Sin lotes").

### 4. Trazabilidad de movimientos

- Cada cambio de stock se registra como `Movimiento` (ENTRADA o SALIDA) con usuario, fecha y observación.
- Al registrar un lote, el backend crea automáticamente un movimiento de ENTRADA con la cantidad inicial.
- Los movimientos son **inmutables** (no se editan ni eliminan).
- El stock del lote (`cantidadActual`) **solo** cambia vía movimientos. No se edita directamente.

### 5. Baja lógica

- **Insumos**: `DELETE /api/insumos/{id}` → `activo = false`. No se borra el registro.
- **Lotes**: `PATCH /api/lotes/{id}/activo` → `activo = false` + `motivoBaja`. No se toca stock ni movimientos.
- Los lotes inactivos se excluyen del dashboard, del inventario y del wizard de salidas.

### 6. Validaciones de stock

- No se puede registrar una SALIDA mayor al `cantidadActual` del lote (`IllegalStateException` → 409).
- No se puede descontar de un lote inactivo.
- `cantidadActual` nunca puede ser negativo (CHECK en DB).
- `cantidadActual` nunca puede superar `cantidadInicial` (validación en constructor de `Lote`).

### 7. Un insumo se identifica por nombre + marca

"Acetaminofén Genfar" y "Acetaminofén MK" son insumos distintos. El formulario unificado (pendiente de implementar) debe reutilizar insumos existentes por nombre + marca.

---

## Seguridad

### Autenticación JWT

1. `POST /api/auth/login` con `{email, password}` → backend valida con BCrypt → devuelve JWT.
2. El JWT incluye claims: `sub` (email), `rol`, `iat`, `exp` (8 horas).
3. El frontend guarda `token` y `usuario` en `localStorage`.
4. Cada petición incluye `Authorization: Bearer <token>` (lo añade `authFetch`).
5. `JwtAuthenticationFilter` valida el token en cada petición y carga la authority en el `SecurityContext`.
6. Si el token expira (401), `authFetch` cierra sesión y redirige a login.

### Autorización

- **Hoy**: dos roles — `ENFERMERIA` (acceso general) y `SUPERVISOR` (acceso a `/api/usuarios`).
- **Pendiente**: eliminar `SUPERVISOR` — todos los usuarios serán `ENFERMERIA` con acceso a todo.
- `@PreAuthorize` en controllers: `isAuthenticated()` para endpoints generales, `hasRole('SUPERVISOR')` para usuarios (pendiente cambiar a `isAuthenticated()`).
- Las páginas HTML están en `permitAll()` — la defensa está en el JS (`requerirRol`) y el backend (`@PreAuthorize`).

---

## Migrations y base de datos

### Flyway

- Configurado en `application.yml` (`spring.flyway.enabled: true`).
- Las migrations viven en `src/main/resources/db/migration/`.
- Se ejecutan **automáticamente** al arrancar Spring Boot, antes de que Hibernate valide.
- `ddl-auto: validate` → Hibernate no crea ni modifica tablas, solo valida que las entidades coincidan con el esquema.

### Migrations existentes

| Archivo | Qué hace |
|---|---|
| `V1__init_schema.sql` | Crea las 5 tablas + índices + fila inicial de configuración de semáforo |
| `V2__lote_activo.sql` | Añade `activo boolean` a `lote` (baja lógica) |
| `V3__insumo_unidades_por_caja.sql` | Añade `unidades_por_caja int` a `insumo` (factor de conversión) |

### Regla de oro

**Crea la migration ANTES de tocar la entidad**. Si añades un campo a una `@Entity` sin su migration, Hibernate falla al arrancar con `SchemaManagementException`.

### Entornos

| Entorno | DB | Config |
|---|---|---|
| Desarrollo | PostgreSQL (Neon, nube) | `application-dev.yml` (no commiteado, tiene credenciales) |
| Tests | H2 en memoria (`jdbc:h2:mem:inventariosibe;MODE=PostgreSQL`) | `src/test/resources/application.yml` |

---

## Cómo ejecutar

### Prerrequisitos

- Java 17+ (pom) o Java 22 (runtime actual)
- `application-dev.yml` con credenciales de Neon y `JWT_SECRET` (copiar de `application-dev.yml.example`)
- No necesitas instalar Maven — usa `mvnw.cmd`

### Arrancar

```powershell
.\mvnw.cmd spring-boot:run
```

La app arranca en `http://localhost:8080`. Abre `http://localhost:8080/login`.

### Compilar y testear

```powershell
.\mvnw.cmd test           # compila + corre los 31 tests
.\mvnw.cmd clean test     # limpieza + recompilación
```

### Ver logs de SQL y seguridad

```powershell
.\mvnw.cmd spring-boot:run `
  -Dspring-boot.run.arguments="--logging.level.org.hibernate.SQL=DEBUG --logging.level.org.springframework.security=DEBUG"
```

### Empaquetar

```powershell
.\mvnw.cmd package -DskipTests
java -jar target/inventariosibe-0.0.1-SNAPSHOT.jar
```

---

## Testing

### Estructura

| Test | Tipo | Qué cubre |
|---|---|---|
| `LoteTest` | Unit (puro) | semáforo, descuento de stock, `calcularCajasDisponibles` |
| `LoteServiceTest` | Unit (mocks) | registrar lote con insumo válido/inexistente |
| `MovimientoServiceTest` | Unit (mocks) | entrada/salida, lote inexistente, DTO enriquecido |
| `UsuarioServiceTest` | Unit (mocks) | crear usuario, hash BCrypt |
| `GlobalExceptionHandlerTest` | Unit (mocks) | traducción de excepciones a JSON |
| `InventarioSibeIntegrationTest` | Integración (MockMvc) | login, crear usuario, 404, 403 |

### Configuración de tests

- H2 en memoria con `MODE=PostgreSQL` para compatibilidad con las migrations.
- Flyway corre las migrations reales contra H2 al arrancar el contexto de tests.
- Los tests unitarios usan Mockito para mockear repositories.

---

## Convenciones

### Nomenclatura Java

- **camelCase** en campos y métodos: `unidadesPorCaja`, `cantidadActual`.
- **PascalCase** en clases: `LoteService`, `InsumoResponseDTO`.
- `@Column(name = "snake_case")` mapea camelCase Java → snake_case SQL.

### DTOs

- **Entrada**: `XxxRequestDTO` — lo que recibe el POST/PUT. Lleva validaciones (`@NotNull`, `@NotBlank`, `@Positive`).
- **Salida**: `XxxResponseDTO` — lo que devuelve el GET/POST. Se construye desde la entidad en el service con helper `aResponse(entidad)`.
- Los controllers **nunca** devuelven entidades JPA.

### Frontend

- Cada página `X.html` tiene su `js/X.js`.
- `common.js` se carga **siempre primero** (antes del JS de la página).
- Orden de scripts: Bootstrap JS → `common.js` → `pagina.js`.
- Todas las llamadas HTTP por `authFetch()`, nunca `fetch()` directo (excepto login).
- Todo texto del backend se escapa con `escapeHtml()` al inyectar HTML.
- Los toasts entre páginas usan `sessionStorage` (`marcarToast` / `consumirToast`).

### Git

- Mensajes de commit: `tipo(scope): descripción` (ej. `feat(lote): baja logica`).
- Tipos: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`.
- Scopes: `lote`, `movimiento`, `insumo`, `auth`, `frontend`, `layout`, `semaforo`, etc.

---

## Documentación adicional

| Archivo | Contenido |
|---|---|
| `docs/frontend-js-api.md` | Tutorial de async/await aplicado al proyecto |
| `docs/frontend-js-reference.md` | Referencia función por función del JS (common.js + todas las páginas) |
| `docs/flujo-desarrollo.md` | Guía paso a paso para añadir pantallas nuevas |
| `docs/html-principios.md` | Principios HTML usados en el frontend (estructura, layout, enlaces, formularios) |
| `docs/comandos-consola.md` | Comandos de Maven, git, logs, debug, solución de problemas |

---

## Cambios pendientes

Estos cambios fueron solicitados por la enfermera y están planificados pero **no implementados**:

1. **Formulario unificado insumo+lote**: un solo formulario donde la enfermera escribe todos los datos (insumo + lote) en una sola pantalla. El backend crea insumo (si no existe por nombre+marca) y lote en una transacción.
2. **Motivo al deshabilitar lote**: modal con textarea obligatoria para el motivo de baja. Se guarda en `lote.motivoBaja` (migration V4 pendiente).
3. **Eliminar rol SUPERVISOR**: un solo rol `ENFERMERIA`. Todos los usuarios tienen acceso a todo. Migration V5 pendiente (UPDATE + CHECK constraint).

Ver el plan detallado en el historial de conversación.
