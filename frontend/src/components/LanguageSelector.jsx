import { Globe2 } from 'lucide-react';
import { useI18n } from '../i18n/I18nProvider.jsx';
import { languages } from '../i18n/core.js';

export default function LanguageSelector() {
  const { language, changeLanguage, t } = useI18n();
  return <div className="language-selector">
    <Globe2 size={16} aria-hidden="true" />
    <label className="visually-hidden" htmlFor="language-picker">{t('language.label')}</label>
    <select id="language-picker" value={language} onChange={event => changeLanguage(event.target.value)}>
      {languages.map(({ code, name }) => <option key={code} value={code} lang={code}>{name}</option>)}
    </select>
    <span className="visually-hidden" role="status" aria-live="polite">{t('language.changed')}</span>
  </div>;
}
