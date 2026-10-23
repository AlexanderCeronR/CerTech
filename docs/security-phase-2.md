# CerTech — Frontend Hardening & Vercel Readiness (Fase 2)

Fecha: 2026-10-21. Base Git: `1e5e81c2143e5556d3b242800e74ce765f8fc81f`.
Dictamen: **APROBADO CON OBSERVACIONES**, exclusivamente para preparación local.
No constituye aprobación de producción ni autorización para integrar.

## Aislamiento y preservación

Se mantuvo `security/phase-1-hardening`, sin cambiar de rama, crear commits,
hacer push, merge, link de Vercel ni despliegues. Antes de trabajar se guardaron
hashes de los 53 archivos y el diff de Fase 1 en `/private/tmp/certech-phase2/`.
La implementación, instalación y compilación se realizaron en una copia de
`frontend/` en ese directorio, sin `.git`, archivos de entorno ni node_modules
compartidos. Solo se transfieren los archivos explícitos de esta fase al checkout.
Los archivos de Fase 1 se verifican por hash antes y después de la transferencia.

No se modificaron la API, la base, credenciales o configuraciones remotas.
No se ejecutaron migraciones ni se accedió a solicitudes reales.

## Inventario de cambios de Fase 2

- `frontend/vercel.json`: Vite, instalación congelada, prueba/build y cabeceras.
- `frontend/.vercelignore`: excluye entornos, dependencias, dist y logs del upload.
- `frontend/.nvmrc`, `package.json`, `package-lock.json`: npm declarado y Node 24;
  el lockfile solo cambia metadatos de la raíz, sin actualizar paquetes.
- `frontend/vite.config.js`: solo publica la URL API revisada; validación de
  API/CSP al compilar; salida dist y sin sourcemaps públicos.
- `frontend/src/services/contactService.js` y su test: exige confirmación 201
  con id seguro positivo, servicio y fecha antes de limpiar el formulario.
- `frontend/security/`: política API, pruebas de configuración y builds/HTTP.
- `frontend/scripts/`: preparación local de API, puerta de readiness,
  inspección de dist y servidor de pruebas exclusivamente loopback.
- Este informe. No se modifica el JSX, CSS ni el diseño del formulario.

## Configuración propuesta de Vercel

| Ajuste | Valor |
|---|---|
| Root Directory, ajuste del proyecto | `frontend` |
| Archivo de configuración | `frontend/vercel.json`, relativo al repositorio |
| Framework | `vite` |
| Instalación | `npm ci --no-audit --no-fund` |
| Build | `node scripts/check-vercel-readiness.mjs && npm test && npm run build` |
| Output Directory | `dist`, relativo a frontend; no `frontend/dist` |
| Node | `24.x`, declarado en engines y `.nvmrc` |
| Gestor | npm; `packageManager: npm@11.6.2` |

No existe una clave `rootDirectory` en esta propuesta de vercel.json: el
propietario debe seleccionar frontend en el proyecto Vercel cuando autorice
configurarlo. No se ha verificado ningún proyecto existente.
La versión menor exacta de npm proporcionada por el builder debe comprobarse
en sus logs; localmente se utilizó npm 11.6.2 y Node 24.19.0.

La navegación actual utiliza fragmentos (`/#contacto`, `/#servicios`, etc.).
No necesita un catch-all SPA. No se convierte `/api/contacto` ni una ruta
desconocida en index.html. Si se introduce un router history en el futuro,
revisar rewrites conservando los errores de API y recursos inexistentes.

## CSP y cabeceras

Se prepara CSP en modo de aplicación, no solo Report-Only:

```text
default-src 'none'; script-src 'self';
style-src 'self' https://fonts.googleapis.com;
font-src 'self' https://fonts.gstatic.com;
img-src 'self'; connect-src 'self'; object-src 'none';
base-uri 'none'; form-action 'none'; frame-ancestors 'none'
```

React/Vite compilados utilizan JS y CSS propios; los iconos son SVG inline,
sin scripts. El CSS existente importa Google Fonts: se autorizan solamente
sus dos orígenes necesarios. No se agrega ningún proveedor ni se cambian las
fuentes. No se permiten unsafe-inline, unsafe-eval, comodines ni orígenes
arbitrarios. form-action bloquea navegación por envío HTML; el formulario
usa fetch, sujeto a connect-src.

También: `X-Content-Type-Options: nosniff`,
`Referrer-Policy: strict-origin-when-cross-origin`,
`Permissions-Policy: camera=(), geolocation=(), microphone=(), payment=(), usb=()`
y `X-Frame-Options: DENY`. La regla se propone para todas las rutas.
El harness aplica las cabeceras literalmente, pero no reproduce el edge de Vercel.

### HSTS condicionado

El propietario confirmó que aún no hay dominios definitivos. No se añade una
política HSTS global inventada ni se configura preload/includeSubDomains.
Tras verificar HTTPS, redirección HTTP y dominio controlado, proponer una regla
de headers condicionada por `has` de tipo `host` al dominio definitivo, con
`Strict-Transport-Security: max-age=300` inicialmente. Aumentar su duración
solo tras observar el comportamiento y aprobarlo. No emitir HSTS en HTTP local.
Inspeccionar antes la política que Vercel ya entregue por defecto para evitar
confundir defaults del proveedor con configuración explícita del proyecto.

## Contrato frontend / API

El contrato propuesto es comunicación directa **navegador → API HTTPS**.
Vercel aloja archivos estáticos; no se introduce una Function o proxy Java.
`VITE_API_URL` es el origen HTTPS de Spring, sin path, usuario, contraseña,
query o fragmento. El cliente añade `/api/contacto`.

Antes de una futura compilación hospedada:

1. Aprobar los orígenes frontend y API. No usar dominios ficticios en despliegues.
2. En una rama/copia revisada, ejecutar desde frontend:
   `node scripts/configure-api.mjs ORIGEN_HTTPS_API_APROBADO`.
   Solo modifica la CSP local; no realiza peticiones ni escribe variables remotas.
3. Configurar en el entorno de build autorizado `VITE_API_URL` con ese mismo
   origen y `CERTECH_SITE_ORIGIN` con el origen HTTPS del frontend.
4. La puerta de readiness falla si faltan esos valores o API y connect-src
   no coinciden. `CERTECH_SITE_ORIGIN` es un dato de control de build, no
   configura DNS/CORS ni demuestra propiedad del dominio.

Actualmente la propuesta mantiene connect-src self y los dominios vacíos;
un build Vercel deliberadamente **no está listo para pasar**.
La compilación local normal funciona con `/api` y permite usar el proxy Vite
existente o las respuestas sintéticas del harness.

Request: POST JSON con nombre, correo, empresa/presupuesto opcionales, servicio,
descripcion y aceptaPrivacidad. No se agregan Authorization ni credenciales
cross-origin. Respuesta válida: 201 JSON con id, servicio y fechaCreacion.
400: validación; 413: tamaño; 429: cuota; 5xx: fallo. Ninguno debe borrar el formulario.
Una respuesta 200 HTML o JSON sin confirmación ya no produce éxito.

En Spring, preparar posteriormente CORS con el origen frontend exacto, POST y
OPTIONS, Content-Type y Accept, sin wildcard ni credenciales. No se modifica
Spring en esta fase. CORS no autentica bots ni reemplaza límites de abuso.

Previews: separar API de staging y datos sintéticos; no dar acceso automático a
producción. Preferir un alias estable de staging explícitamente autorizado o una
lista exacta y temporal de previews revisadas. Nunca permitir `*.vercel.app`,
reflejar Origin arbitrariamente ni facilitar secretos a PR no confiables.
La CSP puede bloquear scripts de toolbar/analytics de previews no autorizados;
no relajarla automáticamente para esas herramientas.

## Variables públicas y secretos

`envPrefix: []` desactiva la exposición automática de VITE_*. Se define
explícitamente solo import.meta.env.VITE_API_URL. Los valores internos estándar
de Vite siguen siendo metadatos públicos normales de compilación.
VITE_PROXY_TARGET se usa únicamente en el proceso del servidor de desarrollo.
No poner secretos en ninguna variable VITE_, incluso con este control.
No usar define con process.env completo, ni copiar archivos .env a public/dist.
Las variables del proveedor y código no revisado aún requieren controles de CI.

Se compilaron valores centinela ficticios en VITE_PHASE2_SECRET_CANARY y
DB_PASSWORD: ninguno apareció en dist. Se buscaron firmas comunes de claves
y tokens en artefactos; no se encontraron. Esto no sustituye un escáner completo.

## Dependencias y reproducibilidad

La discrepancia previa era consecuencia de la instalación con pnpm sin respetar
la resolución exacta de package-lock.json: Vite instalado 6.4.4 frente a 6.4.5
del lockfile. La copia aislada usa npm ci y obtiene 6.4.5.
La reinstalación offline desde caché también completó: 67 paquetes instalados.
Las 117 entradas bloqueadas de paquetes permanecen idénticas, con fuentes npm
e integridad declarada. No se actualizaron versiones ni se añadieron dependencias.
La instalación compartida anterior no se toca; para usar los cambios probar
una copia limpia con npm ci antes de integrarlos.

No se ejecutó npm audit, Dependency-Check, Snyk ni otro servicio de auditoría.
Vulnerabilidades vigentes, obsolescencia y advisories quedan pendientes de
autorización para una consulta externa específica. No declarar dependencias
libres de CVE a partir de pruebas o hashes.

## Pruebas y evidencia

En una copia aislada, desde frontend con Node 24/npm disponibles:

```sh
npm ci --no-audit --no-fund
npm test
node security/build.integration.mjs
node scripts/verify-dist.mjs
node scripts/preview-security.mjs --test-fixtures
```

En otra terminal, mientras el harness escucha en 127.0.0.1:4175:

```sh
node security/http.integration.mjs
```

El test de build cambia temporalmente vercel.json en la copia, lo restaura
en finally y recompila la configuración original. No ejecutarlo en una copia
con trabajo concurrente. Los hosts example.invalid son datos sintéticos de
configuración: ningún test les envía solicitudes.

Resultados:

- 11 pruebas Node aprobadas, sin fallos: regresión del POST y errores,
  contrato de confirmación, políticas de API y cabeceras.
- Vite 6.4.5 compiló 1581 módulos. Cuatro archivos públicos, sin sourcemaps.
- Guardas: API ausente, HTTP o fuera de CSP rechazadas; configuración HTTPS
  sintética coincidente compiló. Readiness exige también origen frontend.
- HTTP local: cabeceras correctas, JS/CSS 200, rutas desconocidas, API GET,
  archivos fuente y .env devuelven 404. Fixtures POST 201/400/503 correctas.
- Navegador: renderizado conservado; todos los anchors internos tienen destino;
  envío sintético exitoso limpia campos, errores 400/503 conservan contenido.
- Página local de prueba mantuvo su título original: el script inline no se
  ejecutó bajo CSP; el mismo navegador sí ejecutó el bundle React permitido.
- Recursos de fuentes autorizados por CSP y renderizado observado. Esto no
  garantiza disponibilidad permanente de Google Fonts ni evalúa su privacidad.
- Evidencia visual: `/private/tmp/certech-phase2/form-success.png`.

El servidor de fixtures no persiste, no registra cuerpos y no contacta Spring.
No se acredita integración contra API real ni eficacia del edge de Vercel.
Una vez finalizadas las pruebas se detiene el harness.

## Recomendaciones para Spring Boot y pendientes de infraestructura

Mantener Java 21 en un servicio JVM/contenedor adecuado, con proxy HTTPS,
API privada detrás del borde cuando la arquitectura lo permita y base en red
privada accesible solo por la aplicación y administración autorizada.
Separar credenciales runtime/migración, gestionar secretos en el proveedor,
TLS verificado a PostgreSQL, límites distribuidos, alertas y backups probados.
Conservar estrategia forwarded=none hasta revisar expresamente proxies e IP.
Estas son recomendaciones: no se crean servicios ni se cambian roles.

Antes de desplegar: dominios y DNS aprobados, CORS exacto, API staging disponible,
TLS/HSTS verificados, política de privacidad aprobada, auditoría de dependencias,
backup/restauración y validación integral contra datos sintéticos. Revisar Git
integration y protecciones para evitar que un push futuro despliegue sin aprobación.

La integración, configuración remota y publicación requieren autorización expresa.

## Referencias oficiales consultadas

- https://vercel.com/docs/frameworks/frontend/vite
- https://vercel.com/docs/project-configuration/vercel-json
- https://vercel.com/docs/functions/runtimes/node-js/node-js-versions
- https://vercel.com/docs/environment-variables
