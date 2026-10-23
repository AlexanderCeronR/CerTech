// Local preparation only. No network calls or Vercel project changes.
import fs from 'node:fs';
import { normalizeApiOrigin } from '../security/api-policy.mjs';
const origin = normalizeApiOrigin(process.argv[2]);
if (!origin) throw new Error('Pass the approved HTTPS API origin as the sole argument.');
const path = new URL('../vercel.json', import.meta.url);
const config = JSON.parse(fs.readFileSync(path));
const header = config.headers[0].headers.find(h => h.key === 'Content-Security-Policy');
header.value = header.value.replace(/connect-src[^;]*/, "connect-src 'self' " + origin);
fs.writeFileSync(path, JSON.stringify(config, null, 2) + '\n');
console.log('Local CSP prepared. Set the same public VITE_API_URL at build time after review.');
