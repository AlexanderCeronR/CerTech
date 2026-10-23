import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import { normalizeApiOrigin, validateBuildApi } from './api-policy.mjs';
const config = JSON.parse(fs.readFileSync(new URL('../vercel.json', import.meta.url)));
test('hosted build requires an approved exact HTTPS API origin and matching CSP', () => {
  assert.throws(() => validateBuildApi('', config, true));
  for (const value of ['http://api.example.invalid', 'https://user:password@api.example.invalid',
    'https://api.example.invalid/path', 'https://api.example.invalid?token=test', 'https://api.example.invalid#fragment',
    'https://*.example.invalid', 'https://api.example.invalid;connect-src']) {
    assert.throws(() => normalizeApiOrigin(value));
  }
  assert.throws(() => validateBuildApi('https://unapproved.example.invalid', config, true));
  const approved = structuredClone(config);
  approved.headers[0].headers[0].value = approved.headers[0].headers[0].value.replace("connect-src 'self'", "connect-src 'self' https://api.example.invalid");
  assert.equal(validateBuildApi('https://api.example.invalid/', approved, true), 'https://api.example.invalid');
});
test('security headers restrict execution, framing and resource origins', () => {
  const headers = Object.fromEntries(config.headers[0].headers.map(h => [h.key, h.value]));
  assert.match(headers['Content-Security-Policy'], /script-src 'self';/);
  assert.match(headers['Content-Security-Policy'], /frame-ancestors 'none'/);
  assert.doesNotMatch(headers['Content-Security-Policy'], /unsafe-inline|unsafe-eval|\*/);
  assert.equal(headers['X-Content-Type-Options'], 'nosniff');
  assert.equal(headers['X-Frame-Options'], 'DENY');
  assert.ok(headers['Permissions-Policy']);
  assert.ok(headers['Referrer-Policy']);
  assert.equal(config.framework, 'vite');
  assert.equal(config.outputDirectory, 'dist');
  assert.equal(config.rewrites, undefined); // Current navigation uses fragments, not history routing.
});
