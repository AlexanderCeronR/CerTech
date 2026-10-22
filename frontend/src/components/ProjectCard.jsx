import { ArrowRight, ArrowUpRight, Bot, Check, Code2, PanelsTopLeft, ShoppingBag, Utensils } from 'lucide-react';
import { useI18n } from '../i18n/I18nProvider.jsx';

function ProjectArtwork({ project }) {
  const { t } = useI18n();
  if (project.previewType === 'certech') {
    return <div className="project-art project-art-certech" role="img" aria-label={t('projects.certech.preview')}>
      <div className="project-art-top"><span className="project-art-brand"><i>C<span>.</span></i> CERTECH</span><span className="project-art-context">{t('art.certech.context')}</span></div>
      <div className="certech-art-content"><span className="certech-art-kicker">{t('art.certech.kicker')}</span><b>{t('art.certech.title')}</b>
        <div className="certech-art-services"><span><Code2 />{t('hero.web')}</span><span><PanelsTopLeft />{t('art.software')}</span><span><Bot />{t('art.automation')}</span></div>
      </div><span className="project-art-caption">{t('art.caption')}</span>
    </div>;
  }
  if (project.previewType === 'mora') {
    return <div className="project-art project-art-mora" role="img" aria-label={t('projects.mora.preview')}>
      <div className="project-art-top"><span className="project-art-brand"><i>M</i> MORA</span><span className="project-art-context">{t('art.mora.context')}</span></div>
      <div className="mora-art-content"><div className="mora-art-heading"><span>{t('art.mora.kicker')}</span><b>{t('art.mora.title')}</b></div>
        <div className="mora-art-panels"><div><Utensils /><b>{t('art.mora.tables')}</b><small>{t('art.mora.reservations')}</small></div><div><PanelsTopLeft /><b>{t('art.mora.orders')}</b><small>{t('art.mora.kitchen')}</small></div><div><ArrowUpRight /><b>{t('art.mora.analytics')}</b><small>{t('art.mora.sales')}</small></div></div>
      </div><span className="project-art-caption">{t('art.caption')}</span>
    </div>;
  }
  return <div className="project-art project-art-infinity" role="img" aria-label={t('projects.infinity.preview')}>
    <span className="infinity-art-mark" aria-hidden="true">∞</span><div className="infinity-art-wordmark">INFINITY <span>APPAREL</span></div>
    <div className="infinity-art-status"><ShoppingBag size={17} /><span>{t('art.infinity.status')}</span><b>{t('art.infinity.soon')}</b><small>{t('art.infinity.signup')}</small></div>
    <span className="project-art-caption">{t('art.observedCaption')}</span>
  </div>;
}
export default function ProjectCard({ project }) {
  const { t } = useI18n();
  const prefix = `projects.${project.previewType}`;
  return <article className={`project-card project-card-${project.previewType}`}>
    <ProjectArtwork project={project} />
    <div className="project-card-body">
      <div className="project-card-meta"><span>{t(`${prefix}.category`)}</span><span className="project-card-mark" aria-hidden="true">{project.previewType === 'mora' ? 'M' : project.previewType === 'infinity' ? '∞' : 'C.'}</span></div>
      <h3>{project.name}</h3><p>{t(`${prefix}.description`)}</p>
      <ul className="project-features" aria-label={t('projects.featuresLabel', { name: project.name })}>
        {project.features.map(key => <li key={key}><Check size={13} aria-hidden="true" />{t(`${prefix}.features.${key}`)}</li>)}
      </ul>
      <a className="button button-primary project-visit" href={project.url} target={project.external ? '_blank' : undefined} rel={project.external ? 'noopener noreferrer' : undefined} aria-label={project.external ? t('projects.visitLabel', { name: project.name }) : t('projects.ownLabel')}>
        {t(project.external ? 'projects.visit' : 'projects.ownVisit')} {project.external ? <ArrowUpRight size={16} aria-hidden="true" /> : <ArrowRight size={16} aria-hidden="true" />}
      </a>
    </div>
  </article>;
}
