# CerTech — Sección del fundador: propuesta aislada

Estado: APROBADO por el propietario. Mejora 4 cerrada localmente; implementación aislada y lista para integración posterior. Sin integración, commit, push ni despliegue.

## Preservación
Repositorio original: /Users/pedroveras/Documents/CerTech, rama security/phase-1-hardening. Los 112 archivos del inventario inicial conservan su SHA-256; no se modificaron archivos del repositorio original. Copia de trabajo: /private/tmp/certech-founder-phase4/work. No se cambió de rama.

## Implementación
Componente FounderSection reutilizable después de Nosotros. Foto completa a la izquierda en escritorio y encima del texto en móvil; fondo azul oscuro, borde sutil, acentos cian y CTA al formulario. Nombre y cargo confirmados; presentación centrada en el propósito empresarial, sin inventar trayectoria ni atribuir citas. Textos y alternativa accesible en español, inglés y francés.

Fotografía original 960×1280, sin EXIF: se mantuvo intacta fuera del repositorio. Derivados WebP con calidad 92, sin recorte, retoque, cambio de rasgos ni ampliación: 480×640 (32.780 bytes), 960×1280 (73.178 bytes). Compresión con pérdida de formato; no se afirma identidad píxel a píxel. srcset, dimensiones explícitas y carga diferida. Sharp existente en el entorno, sin añadir dependencias al proyecto. Se añadió MIME image/webp al servidor local de pruebas. CSP self existente permite estas imágenes sin relajar la política.

## Evidencia
- npm ci --offline --no-audit --no-fund: 67 paquetes, instalación aislada con lockfile existente.
- npm test: 32/32 aprobadas; catálogos, auditoría JSX, WhatsApp, contrato del formulario y configuración de seguridad.
- npm run build: Vite 6.4.5, compilación correcta.
- node scripts/verify-dist.mjs: seis artefactos, sin firmas de secretos, canarios ni sourcemaps (verificación por patrones, no garantía absoluta).
- security/http.integration.mjs copiado fuera de fuentes y apuntado exclusivamente a 4184: cabeceras, recursos, rutas rechazadas y formulario sintético 201/400/503 correctos.
- Ambos WebP: HTTP 200 y Content-Type image/webp.
- Navegador: imagen cargada; ES/EN/FR comprobados; 320, 390, 768 y 1280 px sin desbordamiento horizontal. Una columna en móvil y dos en tablet/escritorio. Consola sin errores/advertencias.
- WhatsApp y componentes del formulario sin modificaciones; pruebas de contacto usan simulador local, sin base de datos ni correos reales.

## Reproducción
Desde frontend/ en esta copia, con Node 24 y npm 11.6.2: npm ci; npm test; npm run build; LOCAL_SECURITY_PORT=4184 node scripts/preview-security.mjs --test-fixtures. Si el puerto ya está ocupado por esta vista previa, reutilizar el servidor existente.
Vista: http://127.0.0.1:4184/ (sección posterior a Nosotros). El fragmento inicial #fundador puede necesitar desplazamiento tras el montaje de React; el CTA mantiene #contacto existente.

## Archivos nuevos o modificados en la copia
- frontend/scripts/preview-security.mjs
- frontend/src/App.jsx
- frontend/src/assets/alexander-ceron-480.webp
- frontend/src/assets/alexander-ceron-960.webp
- frontend/src/components/FounderSection.css
- frontend/src/components/FounderSection.jsx
- frontend/src/i18n/locales/en.json
- frontend/src/i18n/locales/es.json
- frontend/src/i18n/locales/fr.json

## Pendientes
Autorización expresa pendiente para trasladar esta propuesta al repositorio original o realizar commit, push, merge o despliegue. La aprobación de cierre no autoriza estas acciones. Ningún backend, base de datos, servicio remoto ni configuración de producción fue modificado.

## Cierre de la Mejora 4 — aprobación final

El propietario confirmó expresamente la revisión visual y el dictamen APROBADO en esta conversación. Se registra la aprobación visual y técnica de la preparación local, sin aprobación de producción.

Revalidación de cierre: npm test, 32 pruebas aprobadas, cero fallos; npm run build, compilación Vite 6.4.5 correcta. Se conservan las dos fotografías WebP y los catálogos español, inglés y francés. Los artefactos generados mantienen sus nombres con hash respecto a la compilación revisada.

Preservación comprobada nuevamente mediante SHA-256 de los 112 archivos originales y comparación del inventario Git: ningún archivo original alterado, añadido o eliminado por esta fase. Todas las mejoras anteriores permanecen preservadas. Únicamente se actualizó este informe en la copia aislada durante el cierre, además de regenerar dist para verificar el build.

Alcance de la autorización: cierre de la Mejora 4. No se realizó commit, push, merge, despliegue, cambio de rama ni modificación de producción o del repositorio original. La copia permanece en /private/tmp/certech-founder-phase4/work; al estar en una carpeta temporal, deberá conservarse o trasladarse a una ubicación persistente autorizada antes de limpiar temporales. Se esperan nuevas instrucciones.
