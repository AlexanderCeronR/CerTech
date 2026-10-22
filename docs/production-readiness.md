# Auditoría previa a producción — CerTech

Fecha: 2026-10-21. Esta revisión analiza el código y la configuración de este checkout. No se desplegó, publicó ni creó ningún servicio externo.

## Resultado ejecutivo

El flujo local funciona y el backend ya cuenta con perfiles separados para desarrollo y producción. El proyecto **todavía no está listo para publicarse**: faltan una política de privacidad aprobada, dominio y orígenes definitivos, límites contra abuso en el borde, cabeceras HTTP del alojamiento, una restauración de respaldo probada y una auditoría de dependencias con acceso al registro. Existe Dockerfile de backend; el Compose actual sigue siendo solo PostgreSQL local.

## Hallazgos y acciones realizadas

- La contraseña local de respaldo estaba en la configuración común y se repetía como literal en ejemplos. Se quitó el valor de respaldo de Spring: `DB_PASSWORD` ahora es obligatorio incluso en local. `.env.example` usa un marcador ilustrativo que se debe reemplazar. El `.env` existente no se modificó.
- `run-local.ps1` comprueba Java 21 y fuerza el perfil `local`, para evitar iniciar accidentalmente con un perfil de producción heredado del entorno.
- Se separaron `local` y `prod`. `local` sigue siendo el perfil predeterminado y usa `localhost:5434`; `prod` exige `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `CORS_ALLOWED_ORIGINS` sin valores de respaldo.
- CORS solo permite los métodos y encabezados que requiere el formulario y ahora rechaza una lista vacía o el comodín `*`. En producción se deben usar orígenes HTTPS exactos, por ejemplo el origen propio del sitio, sin rutas.
- Spring mantiene Actuator limitado a `health`; los detalles y componentes de salud no se muestran. Los errores genéricos de Spring Boot omiten mensaje, excepción y stack trace. El controlador registra solo el tipo de excepción, no el cuerpo de la solicitud ni credenciales.
- La solicitud valida nombre, correo, servicio de la lista permitida, longitudes, descripción y aceptación afirmativa de privacidad. No hay endpoints públicos de consulta o borrado. La persistencia usa parámetros JPA, sin SQL dinámico.
- El esquema Flyway V1 y la entidad coinciden en nombres, longitudes, nulabilidad y timestamp con zona horaria. La migración se mantiene activa al inicio y Hibernate usa `ddl-auto=validate`.
- El frontend mantiene React, Vite, JavaScript y CSS. `VITE_API_URL` es pública y solo admite una URL de API, nunca secretos. Hay proxy local, estados de carga/error/éxito, etiquetas de formulario, navegación responsive, reducción de movimiento, título, descripción, Open Graph básico y favicon. Falta imagen social/canonical porque no hay dominio aprobado.
- El registro de servicios usa las siete opciones permitidas. El hero sigue diciendo `DESARROLLO INTEGRAL` y `Soluciones Digitales Integrales.`.
- No se encontraron dependencias manifiestamente sobrantes por el uso visible del código: React, React DOM y Vite sostienen la aplicación; `lucide-react` provee los iconos; Spring Web, JPA, Validation, PostgreSQL, Flyway, Actuator y Spring Test se usan en la API. `mvn dependency:analyze` genera avisos genéricos de dependencias usadas transitivamente y starters “sin uso”; se retuvieron porque agregan APIs y funcionalidades de Spring que sí usa la aplicación.

## Secretos e historial

- `.env` está excluido por `.gitignore`, al igual que archivos `.env.*`, con excepción de los ejemplos. No se leyó ni modificó la contraseña local.
- Los archivos de ejemplo deben contener solo marcadores y valores de desarrollo que no se usen fuera de una base local nueva. Nunca copiar `.env` al repositorio o a una imagen.
- Este checkout no tiene commits ni archivos rastreados por Git (cero revisiones en `git log`); por ello no existe historial versionado disponible para una búsqueda retrospectiva de secretos. Antes del primer commit, revisar cuidadosamente el staging y ejecutar un escáner de secretos sobre los archivos que se van a incluir.
- `npm audit` no pudo consultar `registry.npmjs.org` porque el entorno Windows no resolvió el host. No se declara que las dependencias estén libres de vulnerabilidades. Repetir `npm audit` con conectividad y resolver cualquier aviso antes de publicar; no se cambió el lockfile.
- `mvn dependency:analyze` terminó correctamente y emitió advertencias de análisis estático: APIs transitivas aparecen como “used undeclared” y starters requeridos como “unused declared”; no se eliminaron dependencias necesarias. El plugin OWASP Dependency-Check no está en la caché local; el intento offline no pudo descargarlo. No hay resultado de advisories CVE Maven.

## Datos, privacidad y abuso

- El pie contiene un aviso informativo, no una política de privacidad completa. Antes de producción, el propietario debe proporcionar y aprobar el aviso/política, finalidad y base legal, datos del responsable, derechos de titulares, canal de contacto, retención y procedimiento para solicitudes de acceso/supresión. No se inventaron datos corporativos ni texto legal.
- `aceptaPrivacidad` es obligatorio. V2 guarda el valor afirmativo y una marca de tiempo del servidor para solicitudes nuevas. Los campos son anulables para conservar filas históricas sin atribuirles consentimiento que no puede demostrarse. Falta completar, revisar legalmente y aprobar la [plantilla de política de privacidad](plantilla-politica-privacidad.md); no constituye una política aprobada.
- La API aplica un límite fijo configurable de 5 solicitudes por ventana de 60 segundos por dirección remota, con `429` y `Retry-After`; la huella HMAC usa una clave aleatoria solo en memoria y no registra la dirección. Es una defensa por instancia, no distribuida. La producción debe imponer el límite distribuido en el proveedor/proxy. En perfil `prod`, Spring procesa encabezados reenviados: solo usar detrás de un proxy confiable que los sobrescriba y bloquear acceso directo al backend. Revisar el umbral según tráfico y operación.
- La API añade CSP de respuesta no ejecutable, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` y `Permissions-Policy`. HSTS se deja al borde HTTPS. El frontend estático aún requiere que el propietario configure y pruebe cabeceras en el alojamiento elegido; no se conoce el proveedor ni el dominio. Empezar CSP del frontend en `Report-Only` y ajustarla a fuentes reales antes de imponerla.
- Los nombres, correos, empresas y descripciones son datos personales. Restringir acceso directo a la base, no registrar cuerpos de contacto, usar cifrado de almacenamiento del proveedor, controlar operadores y definir retención/borrado autorizado.
- Se conservaron las filas ficticias de validación. Se distinguen de solicitudes reales por correos bajo el dominio reservado `.example.invalid` y nombres de prueba que empiezan por `CerTech`; no se borraron. La eliminación queda para una operación posterior autorizada.

## PostgreSQL, Docker y continuidad

- `docker-compose.yml` es de desarrollo: solo define PostgreSQL 16, publica `5434:5432`, requiere `DB_PASSWORD`, conserva el volumen `certech-postgres-data` y tiene healthcheck `pg_isready`. No define backend/frontend, redes de aplicación, política de rotación de logs, reinicio automático ni límites de recursos.
- El puerto publicado local no debe reutilizarse en producción ni exponerse a Internet. Preferir PostgreSQL administrado o una instancia protegida en red privada, con acceso solo desde el backend, TLS según el proveedor, rol de aplicación de mínimo privilegio, almacenamiento persistente y cifrado en reposo.
- Definir copias automáticas cifradas, retención y RPO/RTO. Probar la restauración en un entorno separado antes del lanzamiento y periódicamente. No ejecutar `down -v` ni borrar el volumen local para resolver problemas.
- Flyway aplica migraciones al arranque. En producción ejecutar una sola instancia de migración durante el despliegue, revisar cada nueva migración y conservar el historial; no habilitar `clean`.
- El Maven Wrapper y el Dockerfile usan Maven 3.9.16, Java 21, imágenes con digests fijados, runtime Eclipse Temurin JRE y usuario UID 10001; `.dockerignore` excluye `.env`, artefactos y frontend. La imagen local se construyó con Maven 3.9.16, ejecutó `mvn verify` (10 pruebas) y se inspeccionó; el pipeline de release debe actualizar los digests de forma controlada y generar SBOM/provenance. No se publicó la imagen.

## Copias de seguridad y recuperación

Procedimiento preparado, no ejecutado contra la base de CerTech:

1. Definir operador, ventana, RPO/RTO y retención aprobada (por ejemplo, copias diarias y retención definida por el responsable; no asumir que el ejemplo es el requisito legal).
2. Crear un directorio local separado del contenedor y del volumen, por ejemplo `%USERPROFILE%\CerTech-Backups`; restringirlo al usuario/operador con ACL de Windows y cifrar antes de cualquier copia fuera del equipo. En PowerShell, crear la carpeta y revisar el ACL aplicado antes de guardar datos:

   ```powershell
   $backupDir = Join-Path $env:USERPROFILE 'CerTech-Backups'
   New-Item -ItemType Directory -Force $backupDir
   icacls $backupDir /inheritance:r /grant:r "$($env:USERDOMAIN)\$($env:USERNAME):(OI)(CI)F"
   ```

3. Desde la raíz del proyecto, define un nombre único por ejecución y genera el dump lógico comprimido en un temporal dentro del contenedor; cópialo al directorio protegido:

   ```powershell
   $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
   $tempDump = "/tmp/certech-backup-$stamp.dump"
   $localDump = Join-Path $backupDir "certech-$stamp.dump"
   docker exec certech-postgres pg_dump -U certech -d certech -Fc -f $tempDump
   if ($LASTEXITCODE -ne 0) { throw 'pg_dump failed; backup not verified.' }
   docker cp "certech-postgres:$tempDump" $localDump
   if ($LASTEXITCODE -ne 0 -or !(Test-Path -LiteralPath $localDump)) { throw 'Copy failed; backup not verified.' }
   Get-FileHash -Algorithm SHA256 -LiteralPath $localDump
   ```

   El archivo contiene datos personales: restringir acceso y no adjuntarlo a tickets ni repositorios. Verificar tamaño y hash y registrar fecha/resultado.

4. Solo después de confirmar copia íntegra, retirar exclusivamente el temporal de esta ejecución con `docker exec certech-postgres rm -f $tempDump`. Si `docker cp` o la verificación falla, conservar/revisar el temporal y no declarar copia exitosa.
5. Para probar recuperación, crear una instancia de PostgreSQL aislada con nombre y volumen nuevos, sin publicar puertos y con red deshabilitada (`--network none`). Con un identificador único que el operador compruebe primero, ejecutar `docker run -d --name certech-restore-test-<ID> --network none -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_USER=certech -e POSTGRES_DB=certech_restore -v certech-restore-test-<ID>:/var/lib/postgresql/data postgres:16-alpine`. Esta base solo de prueba no publica puertos y `trust` solo es aceptable mientras siga completamente aislada. Copiar el dump con `docker cp $localDump certech-restore-test-<ID>:/tmp/restore.dump` y restaurar con `docker exec certech-restore-test-<ID> pg_restore -U certech -d certech_restore --no-owner --no-acl /tmp/restore.dump`. No reutilizar `certech-postgres`, `certech-postgres-data`, ni sus puertos. No usar `docker compose down -v`.
6. Verificar en la instancia de prueba `pg_restore` sin errores, `SELECT to_regclass('public.solicitudes_contacto')`, conteos agregados esperados y que Flyway conserva su historial. No mostrar valores personales. Registrar duración y comparar con RPO/RTO. Retirar el entorno de prueba solo mediante procedimiento aprobado que identifique inequívocamente su volumen temporal; esta auditoría no borra volúmenes.

La recuperación no se probó en esta fase: requiere que el operador ejecute la prueba aislada y confirme el dump, su ACL/cifrado, retención y resultado de consultas sin tocar la base actual.

## Propuesta de despliegue

1. Publicar `frontend/dist` en hosting estático/CDN con HTTPS. Configurar `VITE_API_URL` en tiempo de compilación con la URL pública de la API; esa variable será visible en el bundle y no debe contener secretos. Si frontend y API comparten dominio, preferir proxy `/api` del edge.
2. Ejecutar el backend Java 21 en un servicio administrado con HTTPS en el proxy, red privada hacia PostgreSQL, `SPRING_PROFILES_ACTIVE=prod`, y secretos inyectados desde el gestor del proveedor. Configurar `CORS_ALLOWED_ORIGINS` con el origen HTTPS exacto del frontend.
3. Mantener PostgreSQL sin IP/puerto público, con acceso solo del backend y del canal administrativo seguro. Usar copias, restauración probada, retención, alertas y rotación controlada de credenciales por el propietario.
4. En el edge, habilitar límite distribuido para `POST /api/contacto`, protección contra tráfico automatizado según el proveedor, TLS, y cabeceras `Strict-Transport-Security` (solo después de HTTPS), CSP, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy` y `frame-ancestors`. Ajustar CSP del frontend a recursos reales y probarla en modo report-only antes de imponerla. El backend privado debe recibir encabezados de cliente sobrescritos por el proxy confiable.
5. Definir dominio y DNS, política legal, responsable de soporte, monitoreo de errores/health, procedimiento de rollback de aplicación y migraciones, y política de tratamiento de solicitudes antes de cambiar DNS o abrir tráfico.

No se fijan proveedor, dominio, DNS ni recursos de pago en esta fase. No se considera activa ninguna de estas protecciones de alojamiento hasta que el proveedor las configure y se comprueben en el entorno publicado.

## Verificación del código

- Validación de esta auditoría: backend 8/8 pruebas; frontend 4/4; builds de backend y frontend exitosos; Flyway V1 validada; `201` para solicitud válida, `400` para inválida y persistencia verificada con SELECT.
- En esta auditoría no se reinició PostgreSQL ni se cambió el puerto local 5434. Los puertos 5432, 5433 y el servicio existente de 8080 permanecieron intactos.

## Validación Fase 5 — 2026-10-21

- Con Java 21.0.12.1, `mvn test package` terminó `BUILD SUCCESS`: 10 pruebas, sin fallos. El build de la imagen también ejecutó `mvn verify`: 10/10.
- `npm test`: 4/4; `npm run build`: correcto. `npm audit` se reintentó y no alcanzó el endpoint del registro (`ENOTFOUND`); no se modificó `package-lock.json`.
- La imagen final se construyó localmente con digests fijados, `USER 10001:10001`, y `EXPOSE 8080`; su JRE respondió Java 21.0.12.1. No se publicó.
- PostgreSQL CerTech se inspeccionó como `healthy` en 5434. Una instancia de API aislada y temporal en 18082 conectó por `certech_default` al servicio `postgres:5432`. Flyway registró V2 exitosa. Se obtuvo `201` con solicitud sintética; un `SELECT` confirmó sus campos y consentimiento verdadero con timestamp. Consentimiento falso obtuvo `400` y no fue insertado. Otras solicitudes inválidas alcanzaron `429` con `Retry-After`. Los errores no incluyeron trazas, excepciones ni secretos.
- Un `SELECT` confirmó que las filas anteriores siguen con ambos campos de consentimiento `NULL`. No se modificaron ni borraron filas preexistentes. El contenedor temporal de API fue detenido y eliminado; PostgreSQL quedó saludable, y los contenedores/puertos existentes 5432, 5433, 5434, 8080 y 18081 quedaron intactos.
- El chequeo HTTP de salud devolvió `UP`; las pruebas de controlador confirmaron cabeceras CSP, `nosniff`, framing, referrer y permisos. HSTS y cabeceras del frontend estático aún dependen del proxy/hosting real.
- La prueba de restauración permanece pendiente; los procedimientos están documentados arriba. El repositorio continúa con cero commits y cero archivos rastreados; no se preparó ni creó un commit.
