import test from 'node:test';
import assert from 'node:assert/strict';
import { submitContactRequest } from './contactService.js';

const originalFetch = globalThis.fetch;

test.afterEach(() => { globalThis.fetch = originalFetch; });

test('POST sends the request and resolves only after server confirmation', async () => {
  let request;
  globalThis.fetch = async (url, options) => {
    request = { url, options };
    return new Response(JSON.stringify({ id: 42, servicio: 'Desarrollo Web', fechaCreacion: '2026-10-21T12:00:00Z' }), { status: 201, headers: { 'Content-Type': 'application/json' } });
  };
  const payload = { nombre: 'Ada Lovelace', correo: 'ada@example.com' };
  const result = await submitContactRequest(payload);
  assert.equal(request.url, '/api/contacto');
  assert.equal(request.options.method, 'POST');
  assert.deepEqual(JSON.parse(request.options.body), payload);
  assert.deepEqual(result, { id: 42, servicio: 'Desarrollo Web', fechaCreacion: '2026-10-21T12:00:00Z' });
});

test('maps server validation errors to a readable rejection', async () => {
  globalThis.fetch = async () => new Response(JSON.stringify({ message: 'Revisa los datos.', errors: { correo: 'Correo inválido.', nombre: 'Nombre obligatorio.' } }), { status: 400 });
  await assert.rejects(submitContactRequest({}), /Correo inválido\. Nombre obligatorio\./);
});

test('does not treat server failures as success', async () => {
  globalThis.fetch = async () => new Response(JSON.stringify({ message: 'No fue posible guardar.' }), { status: 500 });
  await assert.rejects(submitContactRequest({}), /No fue posible guardar\./);
});

test('maps connection failures to a useful message', async () => {
  globalThis.fetch = async () => { throw new TypeError('network failure'); };
  await assert.rejects(submitContactRequest({}), /No pudimos conectar con el servidor/);
});

for (const [name, status, body] of [
  ['SPA HTML fallback', 200, '<html>not persisted</html>'],
  ['empty created response', 201, '{}'],
  ['null created response', 201, 'null'],
  ['wrong status', 200, JSON.stringify({ id: 1, servicio: 'Desarrollo Web', fechaCreacion: '2026-10-21T12:00:00Z' })],
  ['invalid timestamp', 201, JSON.stringify({ id: 1, servicio: 'Desarrollo Web', fechaCreacion: 'invalid' })],
]) {
  test('rejects unconfirmed success: ' + name, async () => {
    globalThis.fetch = async () => new Response(body, { status });
    await assert.rejects(submitContactRequest({}), /no confirmó el registro/);
  });
}

for (const [status, code] of [[400,'validation'],[413,'tooLarge'],[429,'rateLimit'],[503,'submit']]) {
  test(`HTTP ${status} exposes a stable localizable code without changing the API payload`, async () => {
    globalThis.fetch = async () => new Response(JSON.stringify({message:'Mensaje del servidor',errors:{correo:'Correo inválido.',unknown:'Ignore'}}),{status});
    await assert.rejects(submitContactRequest({}),error=>error.code===code && (status!==400 || JSON.stringify(error.fields)==='["correo"]'));
  });
}
