import { useState } from 'react';
import { ArrowDownRight, ArrowRight, Menu, X, Workflow, Bot, Cable, FileSearch, Check, Sparkles, Code2, ShieldCheck, Layers3, HeartHandshake, ChevronRight } from 'lucide-react';
import { services, projects } from './data/site.js';
import ContactForm from './components/ContactForm.jsx';
import FounderSection from './components/FounderSection.jsx';
import ProjectCard from './components/ProjectCard.jsx';
import WhatsAppButton from './components/WhatsAppButton.jsx';
import LanguageSelector from './components/LanguageSelector.jsx';
import { useI18n } from './i18n/I18nProvider.jsx';

const nav = [['home', '#inicio'], ['services', '#servicios'], ['about', '#nosotros'], ['projects', '#proyectos'], ['contact', '#contacto']];
const approach = ['understand', 'define', 'build', 'deliver'];
const reasons = [[Layers3, 'tailored'], [Cable, 'connected'], [Code2, 'maintainable'], [HeartHandshake, 'support']];
const methods = ['discovery', 'proposal', 'development', 'delivery'];

function Logo() {
  const { t } = useI18n();
  return <a className="brand" href="#inicio" aria-label={t('brand.home')}><span className="brand-mark">C<span>.</span></span><span className="brand-name">Cer<span>Tech</span></span></a>;
}

export default function App() {
  const { t } = useI18n();
  const [menuOpen, setMenuOpen] = useState(false);
  const [serviceChoice, setServiceChoice] = useState('');
  function chooseService(value) { setServiceChoice(value); }
  return <>
    <header className="site-header"><div className="container nav-wrap">
      <Logo /><LanguageSelector />
      <button className="menu-toggle" aria-label={t(menuOpen ? 'nav.close' : 'nav.open')} aria-expanded={menuOpen} aria-controls="main-nav" onClick={() => setMenuOpen(!menuOpen)}>{menuOpen ? <X /> : <Menu />}</button>
      <nav id="main-nav" className={menuOpen ? 'main-nav open' : 'main-nav'} aria-label={t('nav.label')}>
        {nav.map(([key, href]) => <a key={href} href={href} onClick={() => setMenuOpen(false)}>{t(`nav.${key}`)}</a>)}
        <a className="nav-cta" href="#contacto" onClick={() => setMenuOpen(false)}>{t('actions.quote')} <ArrowRight size={15} /></a>
      </nav>
    </div></header>
    <main>
      <section className="hero section" id="inicio"><div className="container hero-layout"><div className="hero-copy">
        <p className="eyebrow"><span /> {t('hero.eyebrow')}</p><h1>{t('hero.title')} <span>{t('hero.titleAccent')}</span></h1>
        <p className="hero-description">{t('hero.description')}</p>
        <div className="hero-actions"><a className="button button-primary" href="#servicios">{t('actions.explore')} <ArrowDownRight size={18} /></a><a className="button button-quiet" href="#contacto">{t('actions.talkProject')} <ArrowRight size={17} /></a></div>
        <div className="hero-note"><span className="note-line" /> {t('hero.note')}</div></div>
        <div className="hero-visual" aria-label={t('hero.artLabel')} role="img"><div className="visual-orbit orbit-one"/><div className="visual-orbit orbit-two"/>
          <div className="visual-core"><span className="core-symbol">C<span>.</span></span><span className="core-caption">CERTECH SYSTEMS</span></div>
          <div className="visual-node node-web"><Code2 /><span>{t('hero.web')}</span></div><div className="visual-node node-ai"><Bot /><span>{t('hero.ai')}</span></div>
          <div className="visual-node node-data"><Layers3 /><span>{t('hero.data')}</span></div><div className="visual-node node-secure"><ShieldCheck /><span>{t('hero.security')}</span></div><span className="visual-dot dot-a"/><span className="visual-dot dot-b"/>
        </div></div><div className="container hero-bottom"><span>{t('hero.bottomTitle')}</span><span className="hero-bottom-line"/><span>{t('hero.bottomStages')}</span></div>
      </section>
      <section className="services-section section" id="servicios"><div className="container"><div className="section-heading"><div><p className="eyebrow">{t('services.eyebrow')}</p><h2>{t('services.title')}<br /><span>{t('services.titleAccent')}</span></h2></div><p className="section-intro">{t('services.intro')}</p></div>
        <div className="service-grid">{services.map(({ value, icon: Icon, slug, featured }, index) => <article className={`service-card ${featured ? 'featured' : ''}`} id={`servicio-${slug}`} key={slug}>
          <div className="service-top"><span className="service-icon"><Icon size={21} /></span><span className="service-index">0{index + 1}</span></div><h3>{t(`services.${slug}.title`)}</h3><p>{t(`services.${slug}.description`)}</p>
          <div className="service-benefit"><Check size={15} />{t(`services.${slug}.benefit`)}</div><a href="#contacto" className="text-link" onClick={() => chooseService(value)}>{t('actions.learnService')} <ArrowRight size={15} /></a>
        </article>)}</div>
      </div></section>
      <section className="automation-section section" id="automatizacion"><div className="container automation-layout"><div className="automation-copy">
        <p className="eyebrow"><span /> {t('automation.eyebrow')}</p><h2>{t('automation.title')}<br /><span>{t('automation.titleAccent')}</span></h2><p>{t('automation.description')}</p>
        <div className="automation-points">{['processes', 'tools', 'evolve'].map(key => <span key={key}><Check /> {t(`automation.${key}`)}</span>)}</div>
        <a className="button button-primary" href="#contacto" onClick={() => chooseService('Inteligencia Artificial y Automatización')}>{t('automation.action')} <ArrowRight size={17} /></a>
      </div><div className="workflow-panel"><div className="workflow-header"><span className="workflow-live"><i /> {t('workflow.title')}</span><span className="workflow-menu" aria-hidden="true">•••</span></div><div className="workflow-canvas"><div className="workflow-label">{t('workflow.subtitle')}</div>
        <div className="workflow-row"><div className="workflow-card"><span className="flow-icon"><FileSearch /></span><b>{t('workflow.receive')}</b><small>{t('workflow.incoming')}</small></div><span className="flow-connector"><i /></span><div className="workflow-card ai-card"><span className="flow-icon"><Sparkles /></span><b>{t('workflow.process')}</b><small>{t('workflow.classify')}</small></div><span className="flow-connector"><i /></span><div className="workflow-card"><span className="flow-icon"><Workflow /></span><b>{t('workflow.connect')}</b><small>{t('workflow.tool')}</small></div></div>
        <div className="workflow-footer"><span><span className="workflow-check"><Check size={12}/></span> {t('workflow.footer')}</span><span className="flow-tag">{t('workflow.tag')}</span></div>
      </div></div></div></section>
      <section className="about-section section" id="nosotros"><div className="container about-layout"><div className="about-title"><p className="eyebrow">{t('about.eyebrow')}</p><h2>{t('about.title')} <span>{t('about.titleAccent')}</span></h2><p>{t('about.description')}</p><a href="#contacto" className="text-link">{t('actions.talk')} <ArrowRight size={15} /></a></div>
        <div className="approach-list">{approach.map((key, i) => <div className="approach-item" key={key}><span>0{i + 1}</span><p>{t(`about.steps.${key}`)}</p><ChevronRight size={17}/></div>)}</div>
      </div></section>
      <FounderSection />
      <section className="why-section section"><div className="container"><div className="compact-heading"><p className="eyebrow">{t('why.eyebrow')}</p><h2>{t('why.title')}<br /><span>{t('why.titleAccent')}</span></h2></div>
        <div className="reason-grid">{reasons.map(([Icon, key]) => <article className="reason-card" key={key}><span><Icon size={20}/></span><h3>{t(`why.${key}.title`)}</h3><p>{t(`why.${key}.body`)}</p></article>)}</div>
        <div className="methodology"><div className="method-title"><p className="eyebrow">{t('method.eyebrow')}</p><h3>{t('method.title')}</h3></div>{methods.map((key, i) => <article className="method-step" key={key}><span>0{i + 1}</span><h4>{t(`method.${key}.title`)}</h4><p>{t(`method.${key}.body`)}</p></article>)}</div>
      </div></section>
      <section className="projects-section section" id="proyectos"><div className="container"><div className="projects-heading"><div><p className="eyebrow">{t('projects.eyebrow')}</p><h2>{t('projects.title')}</h2></div><p>{t('projects.intro')}</p></div><div className="project-grid">{projects.map(project => <ProjectCard project={project} key={project.name} />)}</div></div></section>
      <section className="cta-band"><div className="container cta-content"><div><p className="eyebrow">{t('cta.eyebrow')}</p><h2>{t('cta.title')}<br />{t('cta.titleEnd')}</h2><p>{t('cta.description')}</p></div><a href="#contacto" className="button button-light">{t('actions.quote')} <ArrowRight size={17}/></a><div className="cta-orbit" aria-hidden="true"/></div></section>
      <section className="contact-section section" id="contacto"><div className="container contact-layout"><div className="contact-copy"><p className="eyebrow">{t('contact.eyebrow')}</p><h2>{t('contact.title')}<br />{t('contact.titleMiddle')} <span>{t('contact.titleAccent')}</span></h2><p>{t('contact.description')}</p><div className="contact-aside"><span className="aside-icon"><HeartHandshake /></span><div><b>{t('contact.asideTitle')}</b><p>{t('contact.asideBody')}</p></div></div></div>
        <div className="contact-form-wrap"><h3>{t('contact.formTitle')}</h3><p>{t('contact.required')}</p><ContactForm key={serviceChoice} selectedService={serviceChoice}/></div>
      </div></section>
    </main>
    <footer className="site-footer"><div className="container"><div className="footer-main"><div className="footer-brand"><Logo /><p>{t('footer.description')}</p></div><div className="footer-col"><b>{t('footer.explore')}</b>{nav.map(([key, href]) => <a href={href} key={href}>{t(`nav.${key}`)}</a>)}</div><div className="footer-col footer-services"><b>{t('nav.services')}</b>{services.map(({ slug }) => <a href={`#servicio-${slug}`} key={slug}>{t(`services.${slug}.title`)}</a>)}</div></div>
      <details className="privacy-note" id="privacidad"><summary>{t('privacy.summary')}</summary><p>{t('privacy.body')}</p></details><div className="footer-bottom"><span>© {new Date().getFullYear()} CerTech. {t('footer.rights')}</span><a href="#privacidad">{t('privacy.link')}</a><span className="footer-location">{t('footer.location')}</span></div>
      <p className="development-credit">{t('footer.development')} <a href="https://moreltech.dev" target="_blank" rel="noopener noreferrer">{t('footer.developmentBrand')}</a></p>
    </div></footer>
    <WhatsAppButton />
  </>;
}
