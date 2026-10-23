// Run against scripts/preview-security.mjs --test-fixtures, never a public URL.
import assert from 'node:assert/strict';
const base='http://127.0.0.1:4175';
const response=await fetch(base);
assert.equal(response.status,200);
const csp=response.headers.get('Content-Security-Policy');
assert.match(csp,/frame-ancestors 'none'/);
assert.match(csp,/script-src 'self'/);
assert.doesNotMatch(csp,/unsafe-inline|unsafe-eval/);
for(const [key,value] of [['X-Content-Type-Options','nosniff'],['X-Frame-Options','DENY'],['Referrer-Policy','strict-origin-when-cross-origin']]) {
  assert.equal(response.headers.get(key),value);
}
assert.ok(response.headers.get('Permissions-Policy'));
const html=await response.text();
for(const match of html.matchAll(/(?:src|href)="(\/assets\/[^"?#]+)"/g)) {
  assert.equal((await fetch(base+match[1])).status,200);
}
for(const route of ['/not-a-route','/api/contacto','/.env','/src/App.jsx']) {
  assert.equal((await fetch(base+route)).status,404,route);
}
for(const [description,status] of [['Local synthetic request',201],['simulated-validation',400],['simulated-failure',503]]) {
  const result=await fetch(base+'/api/contacto',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({nombre:'Test',correo:'phase2@example.invalid',servicio:'Desarrollo Web',descripcion:description,aceptaPrivacidad:true})});
  assert.equal(result.status,status);
}
console.log('Local HTTP: security headers and production assets OK; unknown/API/source/secret routes 404; synthetic form 201/400/503 OK.');
