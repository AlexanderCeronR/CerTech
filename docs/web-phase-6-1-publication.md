# Publicación autorizada de Fases 6 y 6.1

El propietario aprobó visualmente la Fase 6.1 y posteriormente autorizó subir los cambios a GitHub. Esta autorización permite commit y push en una rama nueva; no autoriza merge ni despliegue. Los informes anteriores conservan su registro histórico de aislamiento.

Base: codex/founder-morel-tech, 79386c5d18a18d5e0fa40dabdc2b21f36643e08d. Se incluye el cotizador de cuatro pasos, su API PostgreSQL/outbox, migración V4 y la separación del contacto general sin presupuesto. Se conservan fundador, firma, WhatsApp, portafolio e idiomas.

Se incorporan también las dependencias locales que aún no estaban publicadas: notificaciones persistentes de contacto (V3), transporte SMTP compartido, límites HTTP y configuración de seguridad frontend/backend. No se añaden CI ni servicios externos. SMTP real permanece desactivado por defecto. .env.example solo contiene valores de ejemplo; no se incluyen bases de datos, credenciales reales, node_modules, target, dist ni capturas de pruebas.

Verificación del clon de publicación: 35/35 pruebas frontend, build Vite correcto y seis artefactos verificados sin firmas de secretos/sourcemaps. Se reutilizaron las dependencias locales existentes con el mismo lockfile; no se afirma una nueva instalación npm ci. Las 59/59 pruebas backend corresponden a la ejecución sintética aprobada de Fase 6.1: su código fuente se comprobó idéntico a lo preparado para publicar; no se repitieron aquí porque los runtimes temporales de Java/PostgreSQL ya no están disponibles. Se adjunta resumen de esa evidencia, sin datos de visitantes ni credenciales.

Los archivos preparados se compararon byte a byte con la copia aprobada. El repositorio original y main se conservan intactos. No se ejecutaron migraciones reales, envío SMTP real, merge ni comandos de despliegue. Persisten los pendientes de hosting, privacidad, secretos backend y activación futura autorizada del correo descritos en los informes.
