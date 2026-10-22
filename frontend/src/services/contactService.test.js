import test from 'node:test';
import assert from 'node:assert/strict';
import { submitContactRequest } from './contactService.js';

const originalFetch = globalThis.fetch;

test.afterEach(() => { globalThis.fetch = originalFetch; });

test('POST sends the request and resolves only after server confirmation', async () => {
  let request;
  globalThis.fetch = async (url, options) => {
    request = { url, options };
    return new Response(JSON.stringify({ id: 42 }), { status: 201, headers: { 'Content-Type': 'application/json' } });
  };
  const payload = { nombre: 'Ada Lovelace', correo: 'ada@example.com' };
  const result = await submitContactRequest(payload);
  assert.equal(request.url, '/api/contacto');
  assert.equal(request.options.method, 'POST');
  assert.deepEqual(JSON.parse(request.options.body), payload);
  assert.deepEqual(result, { id: 42 });
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
