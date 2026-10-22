import { useState } from 'react';
import { Send, CheckCircle2 } from 'lucide-react';
import { services } from '../data/site.js';
import { submitContactRequest } from '../services/contactService.js';

export default function ContactForm({ selectedService = '' }) {
  const [status, setStatus] = useState({ type: '', message: '' });
  const [busy, setBusy] = useState(false);
  const [service, setService] = useState(selectedService);
  async function handleSubmit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity() || busy) return;
    const values = Object.fromEntries(new FormData(form));
    setBusy(true); setStatus({ type: '', message: '' });
    try {
      await submitContactRequest({ ...values, aceptaPrivacidad: values.aceptaPrivacidad === 'on' });
      setStatus({ type: 'success', message: 'Tu solicitud quedó registrada. Gracias por escribirnos.' });
      form.reset();
      setService('');
    } catch (error) {
      setStatus({ type: 'error', message: error.message });
    } finally { setBusy(false); }
  }
  return <form className="contact-form" onSubmit={handleSubmit}>
    <div className="form-grid">
      <label>Nombre completo<input name="nombre" autoComplete="name" minLength="2" maxLength="120" required placeholder="Tu nombre" /></label>
      <label>Correo electrónico<input name="correo" type="email" autoComplete="email" maxLength="180" required placeholder="nombre@empresa.com" /></label>
      <label>Empresa <span>(opcional)</span><input name="empresa" autoComplete="organization" maxLength="140" placeholder="Nombre de tu empresa" /></label>
      <label>Servicio de interés<select name="servicio" value={service} onChange={event => setService(event.target.value)} required><option value="" disabled>Selecciona un servicio</option>{services.map(({ title }) => <option key={title}>{title}</option>)}</select></label>
      <label>Presupuesto aproximado <span>(opcional)</span><select name="presupuesto" defaultValue=""><option value="">Prefiero conversarlo</option><option>Menos de $1.000.000 COP</option><option>$1.000.000 – $5.000.000 COP</option><option>$5.000.000 – $15.000.000 COP</option><option>Más de $15.000.000 COP</option></select></label>
      <label className="form-full">Cuéntanos sobre tu proyecto<textarea name="descripcion" rows="5" minLength="10" maxLength="3000" required placeholder="¿Qué te gustaría resolver o construir?" /></label>
    </div>
    <label className="privacy-check"><input name="aceptaPrivacidad" type="checkbox" required /><span>Acepto que CerTech utilice estos datos para responder a mi solicitud.</span></label>
    <button className="button button-primary submit-button" type="submit" disabled={busy}>{busy ? 'Enviando solicitud…' : 'Enviar solicitud'} <Send size={17} aria-hidden="true" /></button>
    <p className={`form-status ${status.type}`} role="status" aria-live="polite">{status.type === 'success' && <CheckCircle2 size={18} aria-hidden="true" />}{status.message}</p>
  </form>;
}
