import { ArrowRight } from 'lucide-react';
import { useI18n } from '../i18n/I18nProvider.jsx';
import portrait from '../assets/alexander-ceron-960.webp';
import portraitSmall from '../assets/alexander-ceron-480.webp';
import './FounderSection.css';

export default function FounderSection() {
  const { t } = useI18n();
  return <section className="founder-section" id="fundador" aria-labelledby="founder-heading">
    <div className="container founder-layout">
      <figure className="founder-portrait">
        <img src={portrait} srcSet={`${portraitSmall} 480w, ${portrait} 960w`} sizes="(max-width: 680px) 82vw, 340px" width="960" height="1280" loading="lazy" decoding="async" alt={t('founder.alt')} />
      </figure>
      <div className="founder-copy">
        <p className="eyebrow">{t('founder.eyebrow')}</p>
        <h2 id="founder-heading">{t('founder.heading')}</h2>
        <div className="founder-identity"><h3>{t('founder.name')}</h3><p>{t('founder.role')}</p></div>
        <p className="founder-introduction">{t('founder.introduction')}</p>
        <a className="button button-primary" href="#contacto">{t('actions.talkProject')} <ArrowRight size={17} aria-hidden="true" /></a>
      </div>
    </div>
  </section>;
}
