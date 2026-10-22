const fields = ['nombre', 'correo', 'empresa', 'servicio', 'presupuesto', 'descripcion', 'aceptaPrivacidad'];
const errorCodes = new Set(['network', 'validation', 'tooLarge', 'rateLimit', 'submit', 'unconfirmed']);
export function contactFeedback(error) {
  return {
    key: `form.status.${errorCodes.has(error?.code) ? error.code : 'submit'}`,
    fields: Array.isArray(error?.fields) ? error.fields.filter(field => fields.includes(field)) : [],
  };
}
// Keep native constraints, but localize their feedback independently of browser UI language.
export function invalidFields(form) {
  return fields.filter(name => {
    const field = form.elements.namedItem(name);
    if (!field) return false;
    const value = field.value.trim();
    if (field.type === 'checkbox') return field.required && !field.checked;
    return (field.required && !value) || field.validity.typeMismatch || field.validity.patternMismatch ||
      (value && field.minLength > 0 && value.length < field.minLength) ||
      (field.maxLength > 0 && value.length > field.maxLength);
  });
}
