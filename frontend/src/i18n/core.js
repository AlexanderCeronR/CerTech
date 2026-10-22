import es from './locales/es.json' with { type: 'json' };
import en from './locales/en.json' with { type: 'json' };
import fr from './locales/fr.json' with { type: 'json' };

export const catalogs = { es, en, fr };
export const languages = [{ code: 'es', name: 'Español' }, { code: 'en', name: 'English' }, { code: 'fr', name: 'Français' }];
export const STORAGE_KEY = 'certech.language';
export function isLanguage(value) { return Object.hasOwn(catalogs, value); }
export function readLanguage(storage = () => globalThis.localStorage) {
  try { const value = storage()?.getItem(STORAGE_KEY); return isLanguage(value) ? value : 'es'; }
  catch { return 'es'; }
}
export function saveLanguage(language, storage = () => globalThis.localStorage) {
  if (!isLanguage(language)) return false;
  try { const target = storage(); if (!target?.setItem) return false; target.setItem(STORAGE_KEY, language); return true; }
  catch { return false; }
}
export function translate(language, key, values = {}) {
  const text = (catalogs[isLanguage(language) ? language : 'es'][key] ?? es[key]);
  if (typeof text !== 'string') throw new Error(`Missing translation: ${key}`);
  return text.replace(/\{(\w+)\}/g, (match, name) => Object.hasOwn(values, name) ? String(values[name]) : match);
}
export function whatsappHref(language) {
  return `https://wa.me/573143608576?text=${encodeURIComponent(translate(language, 'whatsapp.message'))}`;
}
