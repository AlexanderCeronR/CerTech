# CerTech — Security Hardening Fase 1

Fecha: 2026-10-21. Base: `1e5e81c2143e5556d3b242800e74ce765f8fc81f`.
Rama: `security/phase-1-hardening`. Referencia: Security Assessment V1.0.

## Alcance y aislamiento

Correcciones locales para H01–H04 y pruebas de regresión. No se han realizado
push, merge, despliegues, migraciones ni operaciones sobre datos o credenciales.
El formulario y el contrato público de la API se conservan. No se añade autenticación.
No hay nuevas dependencias de aplicación ni cambios en los lockfiles.

Las pruebas HTTP utilizan un servidor efímero enlazado solo a `127.0.0.1`,
un controlador exclusivo de test y exclusiones explícitas de DataSource,
Hibernate y Flyway. No escanean la aplicación completa. Las pruebas MVC utilizan
un servicio ficticio; las pruebas de servicio usan un repositorio en memoria.
Cargar propiedades del perfil `prod` en esta aplicación de test no accede a producción.

## Controles implementados

### PostgreSQL de desarrollo

Compose declara `127.0.0.1:5434:5432`, sin cambiar usuario, contraseña o volumen.
No se ejecutó Compose ni se recrearon contenedores: un contenedor preexistente
conservará su publicación anterior hasta una recreación posteriormente autorizada.
No se acredita aislamiento de ninguna base que esté funcionando actualmente.

### Cabeceras reenviadas

`server.forward-headers-strategy=none` en configuración común y perfil `prod`.
El cliente no puede elegir su identidad mediante `Forwarded`, `X-Forwarded-For`
o `X-Forwarded-Proto`. El limitador utiliza la dirección de la conexión directa.
No se ha definido una lista inventada de proxies confiables.

Detrás de un proxy varios visitantes podrían compartir cuota. Antes de producción,
el propietario debe verificar topología, bloqueo de acceso directo, sobrescritura
de cabeceras, lista de proxies y límite distribuido en el borde. No activar
`framework` o `native` sin una revisión expresa de ese límite de confianza.
Las propiedades todavía pueden ser sobrescritas por configuración del despliegue;
este repositorio no controla las variables efectivas del proveedor.

### Límites HTTP configurables

| Variable | Predeterminado | Alcance |
|---|---|---|
| `HTTP_MAX_BODY_BYTES` | `16384` | Bytes del cuerpo antes de deserializar, todas las rutas |
| `HTTP_MAX_HEADER_SIZE` | `16KB` | Límite de cabeceras del conector Tomcat |
| `HTTP_CONNECTION_TIMEOUT` | `10s` | Timeout de lectura/conexión según semántica Tomcat |
| `HTTP_KEEP_ALIVE_TIMEOUT` | `15s` | Espera entre solicitudes en conexión persistente |
| `HTTP_MAX_SWALLOW_SIZE` | `16KB` | Máximo drenaje de cuerpo rechazado por Tomcat |
| `CONTACT_RATE_LIMIT_MAX_REQUESTS` | `5` | POST de contacto por dirección directa y proceso |
| `CONTACT_RATE_LIMIT_WINDOW_SECONDS` | `60` | Ventana fija del limitador existente |

El filtro lee como máximo el límite más un byte. Si Content-Length supera el
límite rechaza antes de leer; también cuenta cuerpos de longitud desconocida,
incluido chunked. Devuelve 413 JSON sin eco del contenido y no llega al servicio.
El tamaño configurado debe estar entre 1 y 1048576 bytes o el arranque falla.
La medición corresponde a bytes serializados, no al número de caracteres;
JSON con escapes puede ocupar más que el mismo texto enviado como UTF-8 directo.

El filtro está diseñado para las rutas síncronas actuales; no habilita lectura
Servlet asíncrona, uploads o streaming. Esos cambios requieren revisar este control.
El timeout no es un plazo total absoluto de petición ni sustituye protección contra
clientes lentos, concurrencia o DDoS en el hosting. El límite por proceso permanece
en memoria y no se comparte entre réplicas.

## Cabeceras y controles pendientes del hosting

Se conservan las cabeceras del interceptor MVC para `/api/**`. El rechazo temprano
413 añade JSON, `nosniff` y `Cache-Control: no-store`; no atraviesa el interceptor
MVC ni la lógica CORS, por lo que no se promete el mismo conjunto de cabeceras en
ese camino. Rechazos del conector, como cabeceras grandes, tampoco pasan por MVC.
El proxy debe normalizar cabeceras y CORS de respuestas de error cuando corresponda.

No se han activado HTTPS, HSTS, CSP del frontend, WAF ni protección distribuida.
Siguen pendientes hosting/dominio, TLS a base de datos, mínimo privilegio,
secret manager, auditoría de dependencias, backups/restauración, alertas y política
de privacidad aprobada. No se considera producción aprobada.

## Validación reproducible

Con JDK 21 y Maven Wrapper, desde `backend/`:

```sh
sh ./mvnw -B -ntp test
```

Usar `-o` si las dependencias ya están en caché. Desde `frontend/`, con Node disponible:

```sh
node --test
```

No utilizar `spring-boot:run`, Compose ni migraciones para estas pruebas.
Para evitar escrituras globales durante esta fase se usaron JDK, Maven y repositorio
de dependencias en `/private/tmp/certech-phase1-*`. El JDK Temurin 21.0.12.1 se
descargó del proveedor oficial y se comprobó contra su SHA-256 publicado.

Las pruebas cubren tamaño exacto, exceso con longitud conocida/desconocida,
UTF-8, configuración inválida, límites HTTP reales en ambos perfiles, cabeceras
reenviadas falsas, timeouts efectivos, cuota por conexión directa, CORS permitido
y no permitido, JSON inválido, validación de formulario y persistencia ficticia.
Los errores sintéticos de cabeceras excesivas y persistencia fallida en logs son
resultados esperados de pruebas negativas, no errores del entorno productivo.

### Resultados de esta ejecución

- Backend: `BUILD SUCCESS`, 26 pruebas, 0 fallos, 0 errores, 0 omitidas.
  Incluye cuatro pruebas HTTP en perfil local y las mismas cuatro cargando `prod`.
- Frontend: 4 pruebas, 4 aprobadas. No se modificó su código ni se reinstalaron paquetes.
- Compose: YAML parseado localmente y publicación verificada exactamente como
  `127.0.0.1:5434:5432`. No validado con Docker ni aplicado a un contenedor.
- `git diff --check`: sin errores. Rama `main` y HEAD conservan el commit base;
  los cambios permanecen sin commit únicamente en el árbol de trabajo de la rama solicitada.
- Revisión de firmas comunes de tokens y claves privadas en 53 archivos fuente:
  cero coincidencias. Es una comprobación limitada, no una garantía de ausencia
  de secretos. No cambiaron `.env`, ejemplos, manifiestos, lockfiles ni migraciones.
- Evidencia local de Java: `backend/target/surefire-reports/TEST-*.xml` y
  `backend/target/surefire-reports/*.txt` (artefactos ignorados por Git).

Dictamen: **APROBADO CON OBSERVACIONES**, exclusivamente para el alcance local
probado. No acredita despliegue ni controles activos en producción.

## Riesgos residuales y puerta de integración

La instalación frontend anterior conserva Vite 6.4.4 frente a 6.4.5 del lockfile;
no se altera en esta fase. La recreación de contenedores, controles del hosting,
validación integral contra base de datos y remediación de otras fases requieren
autorización independiente. Ninguna prueba de esta fase verifica datos reales.
Se requiere autorización expresa antes de integrar esta rama.
