// Offline build checks. Run only in an isolated source copy.
import fs from 'node:fs';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
const configPath = new URL('../vercel.json', import.meta.url);
const before = fs.readFileSync(configPath);
const vite = 'node_modules/vite/bin/vite.js';
const build = env => spawnSync(process.execPath, [vite, 'build'], {
  cwd: new URL('../', import.meta.url), encoding:'utf8', env:{...process.env, VITE_API_URL:'', ...env},
});
try {
  let result = build({VERCEL:'1'});
  assert.notEqual(result.status, 0);
  assert.match(result.stderr, /approved HTTPS API origin/);
  result = build({VERCEL:'1', VITE_API_URL:'http://api.example.invalid'});
  assert.notEqual(result.status, 0);
  result = build({VERCEL:'1', VITE_API_URL:'https://unapproved.example.invalid'});
  assert.notEqual(result.status, 0);
  const config = JSON.parse(before);
  config.headers[0].headers[0].value = config.headers[0].headers[0].value.replace("connect-src 'self'", "connect-src 'self' https://api.example.invalid");
  fs.writeFileSync(configPath, JSON.stringify(config));
  const ready = site => spawnSync(process.execPath, ['scripts/check-vercel-readiness.mjs'], {
    cwd: new URL('../', import.meta.url), encoding:'utf8',
    env:{...process.env,VITE_API_URL:'https://api.example.invalid',CERTECH_SITE_ORIGIN:site},
  });
  assert.notEqual(ready('').status,0);
  assert.equal(ready('https://frontend.example.invalid').status,0);
  result = build({VERCEL:'1', VITE_API_URL:'https://api.example.invalid',
    VITE_PHASE2_SECRET_CANARY:'PUBLIC_LEAK_SENTINEL_PHASE2', DB_PASSWORD:'PRIVATE_LEAK_SENTINEL_PHASE2'});
  assert.equal(result.status, 0, result.stderr);
  const assets = fs.readdirSync(new URL('../dist/assets/', import.meta.url)).filter(n=>n.endsWith('.js'));
  const js = assets.map(n=>fs.readFileSync(new URL('../dist/assets/'+n, import.meta.url),'utf8')).join('');
  assert.ok(js.includes('https://api.example.invalid'), 'Configured API missing from bundle');
  assert.doesNotMatch(js, /PUBLIC_LEAK_SENTINEL_PHASE2|PRIVATE_LEAK_SENTINEL_PHASE2/);
  console.log('Hosted build guards: missing/HTTP/unapproved API rejected; exact HTTPS API compiled; canaries excluded. No network requests to the API.');
} finally {
  fs.writeFileSync(configPath, before);
  const result=build({VERCEL:'0',VITE_PHASE2_SECRET_CANARY:'PUBLIC_LEAK_SENTINEL_PHASE2',DB_PASSWORD:'PRIVATE_LEAK_SENTINEL_PHASE2'});
  assert.equal(result.status,0,result.stderr);
}
