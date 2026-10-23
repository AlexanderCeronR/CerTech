import fs from 'node:fs';
import { loadEnv } from 'vite';
import { normalizeApiOrigin, validateBuildApi } from '../security/api-policy.mjs';
const env = loadEnv('production', process.cwd(), 'VITE_');
const config = JSON.parse(fs.readFileSync(new URL('../vercel.json', import.meta.url)));
validateBuildApi(env.VITE_API_URL || '', config, true);
if (!process.env.CERTECH_SITE_ORIGIN) throw new Error('Define the reviewed HTTPS frontend origin before a Vercel build.');
normalizeApiOrigin(process.env.CERTECH_SITE_ORIGIN);
console.log('Exact HTTPS API/CSP and frontend origin configured. This does not approve CORS, DNS or hosting.');
