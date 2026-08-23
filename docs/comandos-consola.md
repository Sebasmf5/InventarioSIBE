# Comandos para manejar InventarioSIBE desde la consola

Guía de referencia de todos los comandos que necesitas para trabajar con el proyecto: compilar, correr, ver logs, testear, manejar git y depurar.

## Índice

1. [Requisitos previos](#1-requisitos-previos)
2. [Compilar y tests](#2-compilar-y-tests)
3. [Correr la aplicación](#3-correr-la-aplicación)
4. [Ver logs y depurar](#4-ver-logs-y-depurar)
5. [Base de datos y migrations](#5-base-de-datos-y-migrations)
6. [Git](#6-git)
7. [Variables de entorno](#7-variables-de-entorno)
8. [Solución de problemas](#8-solución-de-problemas)

---

## 1. Requisitos previos

Antes de cualquier comando, necesitas:

- **Java 22** instalado (`java -version`).
- **PowerShell** (ya lo tienes en Windows).
- El archivo `src/main/resources/application-dev.yml` con tus credenciales de Neon (copia de `application-dev.yml.example`).

> No necesitas instalar Maven: el proyecto usa `mvnw.cmd` (Maven Wrapper) que se trae su propia versión.

Verifica que Java está disponible:
```powershell
java -version
```

---

## 2. Compilar y tests

### Compilar sin tests (rápido, solo verifica que todo compile)
```powershell
.\mvnw.cmd compile
```

### Compilar y correr todos los tests
```powershell
.\mvnw.cmd test
```
Salida esperada si todo bien:
```
[INFO] Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Correr solo una clase de test
```powershell
.\mvnw.cmd test -Dtest=LoteTest
```

### Correr solo un método específico de test
```powershell
.\mvnw.cmd test -Dtest=LoteTest#deberiaCalcularCajasYUnidadesConResiduo
```

### Limpiar el target y recompilar desde cero
```powershell
.\mvnw.cmd clean test
```
Útil cuando hay comportamientos raros por clases compiladas viejas en `target/`.

### Compilar sin descargar dependencias (offline)
```powershell
.\mvnw.cmd test -o
```
Solo si ya descargaste todo antes. Falla si falta alguna dependencia.

---

## 3. Correr la aplicación

### Arrancar la app (modo desarrollo)
```powershell
.\mvnw.cmd spring-boot:run
```
La app arranca en `http://localhost:8080`. Mantén la consola abierta; la app se detiene con `Ctrl+C`.

### Arrancar con un perfil específico
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```
El perfil `dev` está activo por defecto en `application.yml`. Si quieres otro, lo cambias aquí.

### Arrancar en segundo plano (libera la consola)
```powershell
Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -NoNewWindow
```
Para detenerla, busca el proceso de Java:
```powershell
Get-Process java | Stop-Process
```

### Empaquetar en JAR (para desplegar)
```powershell
.\mvnw.cmd package -DskipTests
```
Genera `target/inventariosibe-0.0.1-SNAPSHOT.jar`. Para correrlo:
```powershell
java -jar target\inventariosibe-0.0.1-SNAPSHOT.jar
```

### Ver en qué puerto está corriendo
```powershell
netstat -ano | findstr :8080
```

### Detener la app si perdiste la consola
```powershell
# Busca el PID del proceso que usa el puerto 8080
netstat -ano | findstr :8080
# Mata ese PID (reemplaza XXXX por el número)
taskkill /PID XXXX /F
```

---

## 4. Ver logs y depurar

### Ver logs en tiempo real (modo normal)
Simplemente corre la app y mira la consola:
```powershell
.\mvnw.cmd spring-boot:run
```
Todos los logs aparecen en la misma ventana. Spring Boot los imprime con colores.

### Guardar logs en un archivo
```powershell
.\mvnw.cmd spring-boot:run 2>&1 | Tee-Object -FilePath logs.txt
```
Los logs aparecen en la consola **y** se guardan en `logs.txt`.

### Solo guardar logs (sin verlos en consola)
```powershell
.\mvnw.cmd spring-boot:run > logs.txt 2>&1
```

### Ver logs de una app que ya está corriendo en segundo plano
Si arrancaste con `Start-Process`, los logs no aparecen en tu consola. Mejor arranca con `Tee-Object` (ver arriba) si quieres logs.

### Activar logs de debug (SQL, Hibernate, Spring Security)
Arranca con propiedades de logging extra:
```powershell
.\mvnw.cmd spring-boot:run `
  -Dspring-boot.run.arguments="--logging.level.org.hibernate.SQL=DEBUG --logging.level.org.hibernate.type.descriptor.sql=TRACE --logging.level.org.springframework.security=DEBUG"
```
Esto muestra:
- **SQL** que Hibernate ejecuta.
- **Parámetros** de cada query (`TRACE`).
- **Filtro de seguridad** JWT (qué peticiones pasa/rechaza).

### Ver qué queries se ejecutan al cargar una página
Con los logs de SQL activos (ver arriba), abre una página en el navegador y mira la consola. Verás las `SELECT` que Hibernate hace.

### Depurar con breakpoints (VS Code / IntelliJ)
1. Arranca la app en modo debug:
   ```powershell
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
   ```
2. En tu IDE, crea una configuración "Remote Java Application" que se conecte a `localhost:5005`.
3. Pon breakpoints en el código y el IDE se detendrá ahí.

---

## 5. Base de datos y migrations

### Ver estado de las migrations (qué se aplicó)
Spring Boot ejecuta Flyway automáticamente al arrancar. Para ver el estado:
```powershell
.\mvnw.cmd flyway:info
```
Muestra qué migrations se aplicaron y cuáles pendientes.

### Aplicar migrations manualmente (sin arrancar la app)
```powershell
.\mvnw.cmd flyway:migrate
```

### Ver qué migrations faltarían por aplicar
```powershell
.\mvnw.cmd flyway:pending
```

### Reparar una migration que falló
Si una migration quedó marcada como "failed" en la tabla `flyway_schema_history`:
```powershell
.\mvnw.cmd flyway:repair
```

### Conectarse a la base de datos (PostgreSQL en Neon)
No hay un comando Maven para esto. Necesitas `psql` o un cliente gráfico (DBeaver, pgAdmin):
```powershell
psql "jdbc:postgresql://ep-XXXXX.aws-region.aws.neon.tech/inventariosibe?sslmode=require" -U TU_USER
```

### Ver el esquema de la DB (qué tablas existen)
Conectado con `psql`:
```sql
\dt
```

---

## 6. Git

### Ver qué cambió
```powershell
git status
```

### Ver diferencias detalladas
```powershell
git diff                       # cambios sin stagear
git diff --staged              # cambios ya stageados
git diff --stat                # resumen (qué archivos, cuántas líneas)
```

### Stagear y commitear
```powershell
git add src/main/java/.../Lote.java   # un archivo
git add src/main/resources/static/    # una carpeta
git add .                             # todo

git commit -m "feat(lote): descripción corta"
```

### Commitear con mensaje largo (multilínea)
```powershell
git commit -F C:\Users\moral\AppData\Local\Temp\opencode\commit-msg.txt
```
Escribe el mensaje en ese archivo y luego lo commiteas.

### Ver historial
```powershell
git log --oneline -10              # últimos 10 commits, compacto
git log --oneline --graph --all    # con ramas visualizadas
git log -p -2                      # últimos 2 con diff completo
```

### Ramas
```powershell
git branch                         # listar
git branch mi-feature              # crear
git checkout mi-feature            # cambiar a ella
git checkout -b mi-feature         # crear y cambiar en un paso
git branch -vv                     # ver cuáles tienen tracking remoto
```

### Push y pull
```powershell
git push origin mi-rama            # subir rama
git push -u origin mi-rama         # subir y crear tracking
git push origin develop            # subir a develop
git pull origin develop            # traer cambios de develop
```

### Merge (integrar una rama a otra)
```powershell
git checkout develop               # vas a la rama destino
git merge feature-control-de-unidades   # integras la feature
git push origin develop            # subes develop actualizado
```

---

## 7. Variables de entorno

La app necesita dos variables que están en `application-dev.yml`:

| Variable | Dónde | Para qué |
|---|---|---|
| URL de Neon | `spring.datasource.url` | conexión a PostgreSQL |
| `JWT_SECRET` | `jwt.secret` | firmar tokens JWT |

### Si te falta `application-dev.yml`
```powershell
Copy-Item src\main\resources\application-dev.yml.example src\main\resources\application-dev.yml
```
Luego edita el archivo con tus credenciales reales de Neon.

### Si te falta `JWT_SECRET` como variable de entorno
```powershell
$env:JWT_SECRET = "una-clave-aleatoria-de-mas-de-32-caracteres-1234567890"
.\mvnw.cmd spring-boot:run
```

---

## 8. Solución de problemas

### "Port 8080 was already in use"
Otra app está usando el puerto. Búscala y mátala:
```powershell
netstat -ano | findstr :8080
taskkill /PID <el-numero> /F
```

### "No existe configuración de semáforo en la base de datos"
La tabla `configuracion_semaforo` está vacía. La migration V1 debería insertar la fila inicial. Verifica:
```sql
SELECT * FROM configuracion_semaforo;
```
Si está vacía, inserta manualmente:
```sql
INSERT INTO configuracion_semaforo (id, dias_verde, dias_amarillo, dias_rojo)
VALUES ('00000000-0000-0000-0000-000000000001', 90, 30, 0);
```

### "SchemaManagementException: wrong column type"
La entidad no coincide con la DB. Seguramente creaste un campo en la entidad sin su migration. Verifica que el archivo `V_n__xxx.sql` exista en `src/main/resources/db/migration/`.

### "Flyway validation failed"
Una migration ya aplicada cambió. Flyway no permite modificar migrations aplicadas. Si necesitas cambiar el esquema, crea una **nueva** migration (V4, V5...) en vez de editar una vieja.

### Los tests fallan pero la app corre bien
Los tests usan H2 en memoria (ver `src/test/resources/application.yml`), no tu PostgreSQL. Si un test falla por el esquema, revisa que las migrations sean compatibles con H2 (modo PostgreSQL).

### La app arranca pero el frontend no carga
Verifica que los archivos están en `src/main/resources/static/` y que accedes a `http://localhost:8080/login` (sin `/api/`).

### Los cambios en CSS/JS no se reflejan
El navegador cachea recursos estáticos. Fuerza recarga con `Ctrl+Shift+R` o abre en modo incógnito.

---

## Resumen: los 5 comandos que más vas a usar

| Comando | Para qué |
|---|---|
| `.\mvnw.cmd test` | verificar que todo compila y los tests pasan |
| `.\mvnw.cmd spring-boot:run` | arrancar la app y probarla en el navegador |
| `.\mvnw.cmd clean test` | limpieza + recompilación cuando hay cosas raras |
| `git status` | ver qué cambiaste |
| `git push origin <rama>` | subir tus cambios al remoto |

---

## Glosario rápido

| Término | Significado |
|---|---|
| `mvnw.cmd` | Maven Wrapper — Maven incluido en el proyecto, no necesitas instalarlo |
| `spring-boot:run` | arranca la aplicación Spring Boot |
| `clean` | borra `target/` (lo compilado) |
| `package` | genera el JAR ejecutable en `target/` |
| `flyway:migrate` | aplica migrations pendientes a la DB |
| `Ctrl+C` | detiene la app que corre en la consola |
| `Ctrl+Shift+R` | recarga el navegador sin caché |
