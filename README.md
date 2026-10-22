# CerTech

Sitio corporativo React/Vite y API REST Java Spring Boot con PostgreSQL. La portada muestra los siete servicios, una sección destacada de automatización, la metodología y un formulario conectado a `POST /api/contacto`.

## Estructura

- `frontend/`: React 18, Vite 6, JavaScript ES2022 y CSS convencional.
- `backend/`: Spring Boot 3.4.5, Java 21, Spring Web, JPA, Bean Validation y Actuator.
- `backend/src/main/resources/db/migration/`: migraciones Flyway versionadas.
- `docker-compose.yml`: PostgreSQL 16 para desarrollo local.

`backend/mvnw.cmd` es el Maven Wrapper oficial (distribución script-only). Descarga Maven 3.9.16 en el perfil del usuario en su primer uso; no instala Maven globalmente. Se requiere Java 21.

## Requisitos

- Node.js 20.19+ o 22.12+.
- Java 21 (recomendado; el código y Maven usan `java.version=21`).
- Docker Desktop con el motor Linux iniciado, o PostgreSQL 14+ local.

Versiones fijadas: React 18.3.1, Vite 6.x (lockfile), Spring Boot 3.4.5, Java 21 y PostgreSQL 16.

## Configuración local en Windows / PowerShell

Desde la raíz del repositorio (la carpeta que contiene este README):

```powershell
Copy-Item .env.example .env
```

`.env.example` trae valores solo para un clúster PostgreSQL nuevo de desarrollo. El `.env` raíz está ignorado por Git y Compose lo lee automáticamente para sustituir variables. En un volumen ya inicializado, `DB_PASSWORD` debe coincidir con la clave almacenada cuando se creó el rol; editar `.env` no cambia esa clave. No reutilices el valor de ejemplo en producción.

Al crear un `.env` nuevo, reemplaza `DB_PASSWORD` por una clave local propia antes de iniciar Compose. El archivo de ejemplo contiene solo un marcador ilustrativo.

### 1. Iniciar PostgreSQL

```powershell
docker compose up -d postgres
docker compose ps
```

Compose inicia PostgreSQL 16, publica `localhost:5434` hacia el puerto interno 5432, persiste los datos en el volumen `certech-postgres-data` y comprueba salud con `pg_isready`. Los puertos 5432 y 5433 quedan intactos para los otros proyectos. No borres ese volumen para resolver errores; consulta `docker compose logs postgres` y corrige la configuración preservando los datos.

### 2. Ejecutar backend y migración

El archivo de configuración compartido es el `.env` de la raíz creado en el primer paso. El script requiere Java 21 en `JAVA_HOME` (o en `PATH`), fuerza el perfil local y carga ese `.env` en el proceso de Spring:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
./backend/run-local.ps1
```

El script usa Maven Wrapper (o Maven de `PATH` si el wrapper no está presente) y no instala herramientas globalmente. Flyway aplica las migraciones versionadas al inicio y Hibernate valida el esquema con `ddl-auto=validate`. La base queda en `localhost:5434`; API en `http://localhost:8080`; salud en `http://localhost:8080/actuator/health`.

Si prefieres ejecutar Maven manualmente, en la misma terminal define las variables que Spring necesita (Spring no carga archivos `.env` por sí solo). La contraseña se solicita sin eco en consola y no se guarda en el repositorio:

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5434/certech'
$env:DB_USERNAME = 'certech'
$securePassword = Read-Host 'DB_PASSWORD' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $securePassword).Password
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
cd backend
.\mvnw.cmd spring-boot:run
```

### 3. Ejecutar frontend

En otra terminal PowerShell:

```powershell
cd frontend
npm ci
Copy-Item .env.example .env
npm run dev
```

Vite abre `http://localhost:5173`. En desarrollo, las peticiones `/api` se reenvían a `http://localhost:8080`; ajusta `VITE_PROXY_TARGET` si Spring escucha en otra dirección. Para una compilación desplegada, define `VITE_API_URL` con la URL pública de la API. Vite incorpora variables `VITE_*` al cliente: no pongas secretos ahí.

## API de contacto

`POST http://localhost:8080/api/contacto` recibe JSON:

```json
{
  "nombre": "Ada Lovelace",
  "correo": "ada@example.com",
  "empresa": "",
  "servicio": "Desarrollo Web",
  "presupuesto": "",
  "descripcion": "Necesito una aplicación web para mi negocio.",
  "aceptaPrivacidad": true
}
```

`201 Created` devuelve solo `id`, `servicio` y `fechaCreacion`; `400 Bad Request` devuelve `message` y errores por campo. Un fallo de persistencia resulta en `500` sin mensaje interno ni falso éxito. La aplicación no publica endpoints para listar, leer ni borrar solicitudes.

CORS permite solo los orígenes enumerados en `CORS_ALLOWED_ORIGINS` (separados por comas); desarrollo usa `http://localhost:5173`. El proxy de Vite mantiene la llamada del navegador en el mismo origen durante el desarrollo. Para producción, configura dominios explícitos.

La aplicación usa el perfil local por defecto. En producción activa `SPRING_PROFILES_ACTIVE=prod` y proporciona `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `CORS_ALLOWED_ORIGINS` desde el gestor de secretos del proveedor. En ese perfil no hay valores de respaldo para conexión ni CORS; si falta una variable obligatoria, Spring no debe iniciar. `CONTACT_RATE_LIMIT_MAX_REQUESTS` (5 por defecto) y `CONTACT_RATE_LIMIT_WINDOW_SECONDS` (60 por defecto) configuran un límite de respaldo por instancia. No sustituye el límite distribuido del proxy/edge. En `prod`, los encabezados reenviados se procesan: solo publicar detrás de un proxy confiable que los sobrescriba y mantenga la API privada.

### Consentimiento y política de privacidad

Las solicitudes nuevas guardan `acepta_privacidad=true` y `privacidad_aceptada_en` con hora generada por el servidor. La migración V2 deja ambos campos `NULL` para registros históricos: no existe evidencia para inferir su consentimiento. La [plantilla para revisión del propietario](docs/plantilla-politica-privacidad.md) contiene campos pendientes y no es una política aprobada.

## Pruebas y compilación

Desde la raíz:

```powershell
cd frontend
npm ci
npm test
npm run build
cd ..\backend
.\mvnw.cmd test
.\mvnw.cmd package
```

`npm test` usa el test runner integrado de Node para comprobar el POST, los errores de validación, el fallo HTTP y la indisponibilidad de red. Las pruebas Java cubren restricciones del DTO, consentimiento persistido, limitación de solicitudes, cabeceras, CORS, códigos 201/400/500 y errores sin detalles internos. No necesitan una base externa; Flyway/PostgreSQL requieren un arranque de integración aparte.

### Construir imagen de backend

Con Docker Engine disponible, desde la raíz:

```powershell
docker build -f backend/Dockerfile -t certech-api:local .
docker image inspect certech-api:local --format '{{.Config.User}} {{json .Config.ExposedPorts}}'
```

La construcción multi-stage ejecuta `mvn verify`; las imágenes base se fijan por digest, y el runtime usa Eclipse Temurin Java 21 como UID 10001. `.dockerignore` excluye `.env`, credenciales comunes, artefactos y el frontend. Para una prueba local del contenedor, selecciona un puerto libre propio (el ejemplo usa 18082) y conserva PostgreSQL en 5434:

```powershell
docker run --rm --env-file .env -e SPRING_PROFILES_ACTIVE=local `
  -e DB_URL=jdbc:postgresql://host.docker.internal:5434/certech `
  -p 18082:8080 certech-api:local
```

No publiques la imagen en esta preparación. En cada release, actualiza los digests de forma controlada y usa el registro/proceso aprobado por el propietario.

## Errores frecuentes

- **Docker no conecta:** inicia Docker Desktop y espera a que su motor indique que está listo; verifica `docker info` y `docker compose ps`.
- **El wrapper no puede descargar Maven:** verifica la conexión a `repo.maven.apache.org`; el wrapper usa Maven 3.9.16 y mantiene la descarga en el perfil del usuario.
- **La API no conecta a PostgreSQL:** confirma que el contenedor está `healthy`, que `DB_URL` usa `localhost:5434`, y que usuario/clave coinciden con el volumen ya inicializado. Cambiar contraseña en `.env` no cambia la clave almacenada en un volumen existente.
- **El navegador bloquea la petición:** en desarrollo deja `VITE_API_URL` vacío para usar el proxy de Vite, o añade la URL exacta del frontend a `CORS_ALLOWED_ORIGINS` cuando llames a la API directamente; reinicia Spring después de cambiar CORS.
- **Vite no instala paquetes:** revisa acceso a npm y vuelve a ejecutar `npm ci` desde `frontend`.

## Preparación para producción

Consulta [docs/production-readiness.md](docs/production-readiness.md) para la auditoría, los bloqueos previos a publicar y la propuesta de arquitectura. Esta preparación no despliega ni crea recursos externos.

## Estado de validación local — 2026-10-21

- `npm ci --no-audit --no-fund` completó; `npm test` pasó 4/4 y `npm run build` generó el bundle de producción con Node 24.12.0, npm 11.6.2 y Vite 6.4.5.
- PostgreSQL de CerTech está saludable en `localhost:5434`; los proyectos en 5432 y 5433 no se modificaron.
- Con Java 21.0.12.1, `mvn test package` terminó con `BUILD SUCCESS` y 8 pruebas pasaron. `npm test` pasó 4/4 y `npm run build` compiló el frontend.
- Flyway aplicó V1 en la base persistente. Una solicitud ficticia enviada por API y otra enviada desde el navegador se encontraron con SELECT de solo lectura en `solicitudes_contacto`; no se borraron filas.
- La validación E2E usó temporalmente Vite 5174 y API 18081 porque el puerto 8080 ya lo ocupa un contenedor. El modo normal conserva Vite 5173 y Spring 8080.
- `.env` está excluido de Git. No se imprimió ni cambió la contraseña local.

Para usar el volumen PostgreSQL existente con Spring, el propietario debe mantener localmente el valor correcto de `DB_PASSWORD` en `.env`. La publicación local de CerTech quedó en `5434:5432`, porque 5432 y 5433 ya están ocupados por otros proyectos; no se modificó ninguno de ellos, ninguna contraseña ni se borraron datos.

Antes de publicar, el propietario debe proporcionar una política de privacidad válida y datos de contacto oficiales. El aviso del pie es informativo y no sustituye esa política. No hay clientes, proyectos, testimonios, certificaciones o datos comerciales inventados; n8n e IA quedan como preparación futura, sin integraciones activas.

## Validación Fase 5 — 2026-10-21

- Backend: `mvn test package` pasó 10/10 pruebas con Java 21.0.12.1. Consentimiento, validación de datos, límite `429`, cabeceras y errores seguros están cubiertos.
- PostgreSQL: estado `healthy` en 5434. Flyway V2 se aplicó con éxito. Un POST ficticio recibió `201` y un `SELECT` comprobó el contenido y consentimiento con timestamp; un POST sin consentimiento recibió `400` sin persistirse. Las filas históricas siguen con consentimiento `NULL`.
- API: prueba HTTP de abuso llegó a `429` con `Retry-After`; la instancia de verificación fue temporal y usó 18082. No se tocaron los servicios de 5432, 5433 ni 8080.
- Frontend: `npm test` pasó 4/4 y `npm run build` terminó correctamente. `npm audit` no pudo acceder a `registry.npmjs.org` por DNS; no se alteró el lockfile.
- Docker: imagen local Java 21, no root, multi-stage, digest fijado y sin publicación. La verificación de imagen ejecuta `mvn verify`. Véase [el informe de preparación](docs/production-readiness.md) para estado final, hallazgos y pendientes del propietario.
