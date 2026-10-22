# Publicación de mejoras 4 y 5 en GitHub

El propietario aprobó visualmente la sección del fundador y la firma Morel Tech, y autorizó posteriormente subir los cambios a GitHub. Esta autorización permite el commit y push de esta rama independiente; no autoriza merge ni despliegue.

Base: codex/i18n-es-en-fr, commit 7ed0eccb0a1be172de0a07c74f9b8717f8f3594e. Cambios: sección del fundador con fotografías WebP locales, traducciones ES/EN/FR y firma Morel Tech con enlace seguro. Se preservan WhatsApp, formulario y créditos institucionales.

Validación del clon exacto de publicación: npm ci offline, 30/30 pruebas aprobadas y npm run build correcto (Vite 6.4.5). Los informes de revisión describen 32 pruebas en la copia local completa; dos controles de configuración de seguridad siguen exclusivamente en el trabajo local no integrado y no forman parte de esta rama. El servidor local de revisión y su ajuste MIME tampoco se incluyen: no son necesarios para servir estos recursos con Vite o un hosting estático correctamente configurado. Los informes previos conservan el registro histórico de aislamiento y aprobación; esta nota documenta la autorización posterior de publicación.

No se incluyen modificaciones de backend, base de datos, credenciales, configuraciones de seguridad pendientes ni dependencias nuevas. No se modificó main ni el repositorio original; la preparación se realizó en un clon temporal. No se solicitó ni ejecutó despliegue.
