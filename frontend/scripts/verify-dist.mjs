import fs from 'node:fs';
import path from 'node:path';
import assert from 'node:assert/strict';
const root = new URL('../dist/', import.meta.url);
const files = fs.readdirSync(root, {recursive:true}).filter(name => fs.statSync(new URL(name, root)).isFile());
for (const name of files) {
  assert.ok(!/(\.map$|(^|\/)\.env|\.pem$|\.key$)/.test(name), 'Unexpected public artifact: '+name);
  const data = fs.readFileSync(new URL(name, root), 'utf8');
  assert.doesNotMatch(data, /PUBLIC_LEAK_SENTINEL_PHASE2|PRIVATE_LEAK_SENTINEL_PHASE2|-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----|\b(?:ghp_[A-Za-z0-9]{30,}|AKIA[A-Z0-9]{16})\b/);
}
const html = fs.readFileSync(new URL('index.html', root), 'utf8');
assert.doesNotMatch(html, /<style\b|\sstyle=|<script(?![^>]*\bsrc=)/i);
for (const match of html.matchAll(/(?:src|href)="(\/assets\/[^"?#]+)"/g)) {
  assert.ok(fs.existsSync(new URL('.'+match[1], root)), 'Missing production asset');
}
console.log(`Production artifacts checked: ${files.length}; no secret signatures/canaries or sourcemaps; HTML uses external self-hosted scripts/styles.`);
