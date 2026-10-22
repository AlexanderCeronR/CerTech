const API_URL = (import.meta.env?.VITE_API_URL ?? '').replace(/\/$/, '');

export async function submitContactRequest(payload) {
  let response;
  try {
    response = await fetch(`${API_URL}/api/contacto`, {
      method: 'POST', headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error('No pudimos conectar con el servidor. Inténtalo de nuevo más tarde.');
  }
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 400 && data.errors) throw new Error(Object.values(data.errors).join(' '));
    throw new Error(data.message || 'No fue posible enviar la solicitud. Inténtalo nuevamente.');
  }
  return data;
}
