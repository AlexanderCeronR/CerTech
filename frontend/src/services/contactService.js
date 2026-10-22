import { translate } from '../i18n/core.js';
const API_URL = (import.meta.env?.VITE_API_URL ?? '').replace(/\/$/, '');
const fieldNames = new Set(['nombre', 'correo', 'empresa', 'servicio', 'presupuesto', 'descripcion', 'aceptaPrivacidad']);
export class ContactRequestError extends Error {
  constructor(code, message, fields = []) {
    super(message || translate('es', `form.status.${code}`));
    this.name = 'ContactRequestError'; this.code = code; this.fields = fields;
  }
}
export async function submitContactRequest(payload) {
  let response;
  try {
    response = await fetch(`${API_URL}/api/contacto`, {
      method: 'POST', headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(payload),
    });
  } catch { throw new ContactRequestError('network'); }
  const parsed = await response.json().catch(() => ({}));
  const data = parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {};
  if (!response.ok) {
    if (response.status === 400) {
      const errors = data.errors && typeof data.errors === 'object' && !Array.isArray(data.errors) ? data.errors : {};
      const fields = Object.keys(errors).filter(field => fieldNames.has(field));
      // Diagnostics only: the UI translates the code and known field names, never server prose.
      const message = Object.values(errors).filter(value => typeof value === 'string').join(' ');
      throw new ContactRequestError('validation', message, fields);
    }
    const code = response.status === 413 ? 'tooLarge' : response.status === 429 ? 'rateLimit' : 'submit';
    throw new ContactRequestError(code, typeof data.message === 'string' ? data.message : undefined);
  }
  if (response.status !== 201 || !Number.isSafeInteger(data.id) || data.id < 1 ||
      typeof data.servicio !== 'string' || !data.servicio ||
      typeof data.fechaCreacion !== 'string' || Number.isNaN(Date.parse(data.fechaCreacion))) {
    throw new ContactRequestError('unconfirmed');
  }
  return data;
}
