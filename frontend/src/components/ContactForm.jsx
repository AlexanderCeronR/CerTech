import { useState } from 'react';
import { Send, CheckCircle2 } from 'lucide-react';
import { services, budgets } from '../data/site.js';
import { submitContactRequest } from '../services/contactService.js';
import { useI18n } from '../i18n/I18nProvider.jsx';
import { contactFeedback, invalidFields } from '../i18n/contactFeedback.js';

function Field({ name, optional, errors, t, children, full }) {
  return <div className={`form-field${full ? ' form-full' : ''}`}>
    <label htmlFor={`contact-${name}`}>{t(`form.${name}`)} {optional ? <span>{t('form.optional')}</span> : <span aria-hidden="true">*</span>}</label>
    {children}
    {errors.includes(name) && <p className="form-field-error" id={`error-${name}`}>{t(`form.validation.${name}`)}</p>}
  </div>;
}
export default function ContactForm({ selectedService = '' }) {
  const { t } = useI18n();
  const [status, setStatus] = useState({ type: '', key: '' });
  const [errors, setErrors] = useState([]);
  const [busy, setBusy] = useState(false);
  const [service, setService] = useState(selectedService);
  const fieldProps = name => ({ id: `contact-${name}`, name, 'aria-invalid': errors.includes(name) || undefined, 'aria-describedby': errors.includes(name) ? `error-${name}` : undefined });
  function clearError(event) {
    const name = event.target.name;
    setErrors(previous => previous.includes(name) ? previous.filter(field => field !== name) : previous);
  }
  async function handleSubmit(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (busy) return;
    const invalid = invalidFields(form);
    setErrors(invalid);
    if (invalid.length) {
      setStatus({ type: 'error', key: 'form.status.validation' });
      form.elements.namedItem(invalid[0])?.focus();
      return;
    }
    const values = Object.fromEntries(new FormData(form));
    setBusy(true); setStatus({ type: '', key: '' });
    try {
      await submitContactRequest({ ...values, aceptaPrivacidad: values.aceptaPrivacidad === 'on' });
      setStatus({ type: 'success', key: 'form.status.success' });
      form.reset(); setService('');
    } catch (error) {
      const feedback = contactFeedback(error);
      setErrors(feedback.fields);
      setStatus({ type: 'error', key: feedback.key });
    } finally { setBusy(false); }
  }
  const field = (name, optional = false) => ({ name, optional, errors, t });
  return <form className="contact-form" onSubmit={handleSubmit} onInput={clearError} onChange={clearError} noValidate>
    <div className="form-grid">
      <Field {...field('nombre')}><input {...fieldProps('nombre')} autoComplete="name" minLength="2" maxLength="120" required placeholder={t('form.namePlaceholder')} /></Field>
      <Field {...field('correo')}><input {...fieldProps('correo')} type="email" autoComplete="email" maxLength="180" required placeholder={t('form.emailPlaceholder')} /></Field>
      <Field {...field('empresa', true)}><input {...fieldProps('empresa')} autoComplete="organization" maxLength="140" placeholder={t('form.companyPlaceholder')} /></Field>
      <Field {...field('servicio')}><select {...fieldProps('servicio')} value={service} onChange={event => setService(event.target.value)} required><option value="" disabled>{t('form.servicePlaceholder')}</option>{services.map(({ value, slug }) => <option key={slug} value={value}>{t(`services.${slug}.title`)}</option>)}</select></Field>
      <Field {...field('presupuesto', true)}><select {...fieldProps('presupuesto')} defaultValue="">{budgets.map(({ key, value }) => <option key={key} value={value}>{t(`form.budget.${key}`)}</option>)}</select></Field>
      <Field {...field('descripcion')} full><textarea {...fieldProps('descripcion')} rows="5" minLength="10" maxLength="3000" required placeholder={t('form.descriptionPlaceholder')} /></Field>
    </div>
    <label className="privacy-check"><input {...fieldProps('aceptaPrivacidad')} type="checkbox" required /><span>{t('form.privacy')}</span></label>
    {errors.includes('aceptaPrivacidad') && <p className="form-field-error" id="error-aceptaPrivacidad">{t('form.validation.aceptaPrivacidad')}</p>}
    <button className="button button-primary submit-button" type="submit" disabled={busy}>{t(busy ? 'form.sending' : 'form.submit')} <Send size={17} aria-hidden="true" /></button>
    <p className={`form-status ${status.type}`} role="status" aria-live="polite">{status.type === 'success' && <CheckCircle2 size={18} aria-hidden="true" />}{status.key && t(status.key)}</p>
  </form>;
}
