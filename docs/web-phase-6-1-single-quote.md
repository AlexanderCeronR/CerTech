# CerTech — Fase 6.1: un único cotizador

Dictamen: **APROBADO por el propietario; cierre local de Fase 6.1**. Trabajo sobre la copia aislada de la Fase 6. Sin integración ni publicación.

## Auditoría e identificación

No había dos motores de cotización independientes: `QuoteWizard.jsx` era el Cotizador Inteligente de Fase 6 y `ContactForm.jsx`, dentro de `#contacto`, era el formulario anterior presentado como «Solicita una cotización», «Request a quote» y «Demandez un devis», con selector de presupuesto. El antiguo formulario utiliza `POST /api/contacto`, que también es necesario para contacto general; eliminarlo por completo incumpliría la preservación solicitada.

Se retiró la presentación del formulario anterior como cotizador y su campo de presupuesto. Se conserva como contacto general, con título «Envíanos un mensaje», botón «Enviar mensaje» y descripción/placeholder adecuados, traducidos a inglés y francés. Conserva nombre, correo, empresa opcional, servicio, mensaje, consentimiento, validaciones y registro en su endpoint existente. No representa una segunda solicitud comercial por pasos.

## Cambios exactos

- `frontend/src/components/ContactForm.jsx`: retirado selector de presupuesto e importación local de `budgets`.
- `frontend/src/i18n/locales/es.json`.
- `frontend/src/i18n/locales/en.json`.
- `frontend/src/i18n/locales/fr.json`.
- Este informe nuevo: `docs/web-phase-6-1-single-quote.md`.

En los catálogos cambian título, descripción, etiqueta del mensaje, placeholder y textos de envío del contacto. Se eliminó únicamente `form.presupuesto`, etiqueta que ya no tenía consumidores. Se conservan `form.budget.*`, usados por el Cotizador Inteligente, y la validación del contrato de contacto por compatibilidad.

**Archivos eliminados: ninguno.** No se eliminó el componente de contacto ni CSS compartido; no había estilos exclusivos del selector de presupuesto que debieran retirarse.

## Navegación y preservación

Comprobación DOM: un `#cotizacion`, un `.quote-shell` y un formulario general en `#contacto`, sin selector antiguo `#contact-presupuesto`. Los dos CTA explícitos de cotización (cabecera y banda comercial) apuntan a `#cotizacion`; no hay anclas internas rotas. Los enlaces generales de contacto/conversación conservan `#contacto`.

Se verificó por SHA-256 que todo el código backend, `QuoteWizard`, su modelo, servicio API, estilos, migración V4, idempotencia, outbox y demás archivos de Fase 6 permanecen idénticos. También se compararon las traducciones `quote.*` y los rangos compartidos: sin cambios. WhatsApp, portafolio, Alexander Ceron y firma Morel Tech permanecen intactos.

Los 112 archivos y el inventario del repositorio original se verificaron nuevamente, sin modificaciones ni archivos adicionales. El estado Git original se conserva; no se ejecutó commit, push, merge ni despliegue.

## Pruebas ejecutadas

- Frontend: **35/35 pruebas aprobadas**.
- Backend: **59/59 pruebas aprobadas**, cero errores, fallos u omitidas, contra PostgreSQL sintético local; SMTP real desactivado.
- Build: Vite 6.4.5 correcto; verificación de seis artefactos dist correcta.
- Navegador: cuatro pasos completos, selección del catálogo, edición y cambio ES→EN→FR con datos conservados, revisión y registro confirmado.
- PostgreSQL: solicitud `qa-phase61@example.invalid` exclusivamente sintética, una cotización y una notificación PENDING, cero intentos de correo.
- Contacto general: envío sintético `qa-contact-phase61@example.invalid`, registro confirmado y una notificación pendiente; la ausencia de presupuesto no rompe el contrato.
- Responsive: contacto revisado a 320 px, sin desbordamiento horizontal; estilos del cotizador sin cambios frente a la validación responsive de Fase 6.
- Consola: sin errores ni advertencias observados tras el build final.
- Composición final y anclas: un cotizador, dos CTA explícitos con destino correcto, ningún enlace interno roto.

Evidencia en `../evidence/phase6-1/` respecto a la raíz de la copia: `database.json`, `frontend-tests.txt`, `backend-results.json`, capturas de contacto general en escritorio y móvil. Las capturas previas al último ajuste de redacción se conservan como evidencia de las comprobaciones; `contacto-general-final.png` muestra la redacción final.

## Vista y pendientes

Vista local: **http://127.0.0.1:4186/**. Usar «Solicitar cotización» para el único cotizador y «Contacto» para mensajes generales. API y base de datos exclusivamente sintéticas de la Fase 6; no se añadieron migraciones ni se tocaron datos reales. No se enviaron correos reales.

Se mantienen los riesgos y pendientes de Fase 6 (hosting, revisión legal y activación futura autorizada de SMTP). Esta corrección no declara producción aprobada. Aprobación visual recibida; se requiere autorización independiente antes de integrar o publicar.

## Aprobación final del propietario

El propietario aprobó expresamente el resultado mediante el mensaje «aprobado» en esta conversación. Se registra el cierre local de la Fase 6.1 con la evidencia existente de 35 pruebas frontend y 59 backend aprobadas y build correcto. No se ha modificado la implementación desde esas validaciones; esta actualización solo documenta la aprobación. No autoriza commit, push, merge, integración ni despliegue. La copia permanece aislada y a la espera de nuevas instrucciones.
