import { ArrowRight, ArrowUpRight, Bot, Check, Code2, PanelsTopLeft, ShoppingBag, Utensils } from 'lucide-react';

function ProjectArtwork({ project }) {
  if (project.previewType === 'certech') {
    return <div className="project-art project-art-certech" role="img" aria-label={project.previewLabel}>
      <div className="project-art-top"><span className="project-art-brand"><i>C<span>.</span></i> CERTECH</span><span className="project-art-context">DESARROLLO INTEGRAL</span></div>
      <div className="certech-art-content"><span className="certech-art-kicker">TECNOLOGÍA CON PROPÓSITO</span><b>Soluciones digitales<br />integrales.</b>
        <div className="certech-art-services"><span><Code2 />Web</span><span><PanelsTopLeft />Software</span><span><Bot />Automatización</span></div>
      </div>
      <span className="project-art-caption">Ilustración informativa · no es captura</span>
    </div>;
  }

  if (project.previewType === 'mora') {
    return <div className="project-art project-art-mora" role="img" aria-label={project.previewLabel}>
      <div className="project-art-top"><span className="project-art-brand"><i>M</i> MORA</span><span className="project-art-context">GESTIÓN DE RESTAURANTES</span></div>
      <div className="mora-art-content"><div className="mora-art-heading"><span>Una operación conectada</span><b>Restaurante, en un solo lugar.</b></div>
        <div className="mora-art-panels"><div><Utensils /><b>Mesas</b><small>Estado y reservas</small></div><div><PanelsTopLeft /><b>Pedidos</b><small>Seguimiento de cocina</small></div><div><ArrowUpRight /><b>Analítica</b><small>Ventas e insights</small></div></div>
      </div>
      <span className="project-art-caption">Ilustración informativa · no es captura</span>
    </div>;
  }

  return <div className="project-art project-art-infinity" role="img" aria-label={project.previewLabel}>
    <span className="infinity-art-mark" aria-hidden="true">∞</span>
    <div className="infinity-art-wordmark">INFINITY <span>APPAREL</span></div>
    <div className="infinity-art-status"><ShoppingBag size={17} /><span>ESTADO PÚBLICO DEL SITIO</span><b>Opening soon</b><small>Registro para novedades · Shopify</small></div>
    <span className="project-art-caption">Ilustración del estado observado · no es captura</span>
  </div>;
}

export default function ProjectCard({ project }) {
  return <article className={`project-card project-card-${project.previewType}`}>
    <ProjectArtwork project={project} />
    <div className="project-card-body">
      <div className="project-card-meta"><span>{project.category}</span><span className="project-card-mark" aria-hidden="true">{project.previewType === 'mora' ? 'M' : project.previewType === 'infinity' ? '∞' : 'C.'}</span></div>
      <h3>{project.name}</h3>
      <p>{project.description}</p>
      <ul className="project-features" aria-label={`Características públicas de ${project.name}`}>
        {project.features.map(feature => <li key={feature}><Check size={13} aria-hidden="true" />{feature}</li>)}
      </ul>
      <a className="button button-primary project-visit" href={project.url} target={project.external ? '_blank' : undefined} rel={project.external ? 'noopener noreferrer' : undefined} aria-label={project.external ? `Visitar proyecto ${project.name} en una pestaña nueva` : 'Ir al inicio del sitio CerTech'}>
        {project.external ? 'Visitar proyecto' : 'Ver sitio CerTech'} {project.external ? <ArrowUpRight size={16} aria-hidden="true" /> : <ArrowRight size={16} aria-hidden="true" />}
      </a>
    </div>
  </article>;
}
