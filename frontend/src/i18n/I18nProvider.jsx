import { createContext, useCallback, useContext, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { isLanguage, readLanguage, saveLanguage, translate } from './core.js';

const I18nContext = createContext(null);
export function I18nProvider({ children }) {
  const [language, setLanguage] = useState(readLanguage);
  const scrollAnchor = useRef(null);
  const t = useCallback((key, values) => translate(language, key, values), [language]);
  const changeLanguage = useCallback(next => {
    if (!isLanguage(next) || next === language) return;
    // Keep the same section and position within it when translated text reflows above it.
    const header = document.querySelector('.site-header')?.getBoundingClientRect().bottom ?? 0;
    const sections = Array.from(document.querySelectorAll('main section, .site-footer'));
    const anchor = sections.find(section => section.getBoundingClientRect().bottom > header + 1);
    scrollAnchor.current = anchor ? { element: anchor, top: anchor.getBoundingClientRect().top } : null;
    saveLanguage(next);
    setLanguage(next);
  }, [language]);
  useLayoutEffect(() => {
    document.documentElement.lang = language;
    document.title = t('meta.title');
    for (const [selector, content] of [
      ['meta[name="description"]', t('meta.description')],
      ['meta[property="og:title"]', t('meta.title')],
      ['meta[property="og:description"]', t('meta.socialDescription')],
      ['meta[property="og:locale"]', { es: 'es_CO', en: 'en_US', fr: 'fr_FR' }[language]],
    ]) document.querySelector(selector)?.setAttribute('content', content);
    const anchor = scrollAnchor.current;
    if (anchor?.element.isConnected) {
      window.scrollTo({ top: window.scrollY + anchor.element.getBoundingClientRect().top - anchor.top, behavior: 'instant' });
    }
    scrollAnchor.current = null;
  }, [language, t]);
  const value = useMemo(() => ({ language, changeLanguage, t }), [language, changeLanguage, t]);
  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}
export function useI18n() {
  const value = useContext(I18nContext);
  if (!value) throw new Error('I18nProvider is required');
  return value;
}
