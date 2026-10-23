// Local harness for the production bundle. Never a deployment server.
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const root = fileURLToPath(new URL('../dist/', import.meta.url));
const config = JSON.parse(fs.readFileSync(new URL('../vercel.json', import.meta.url)));
const fixtures = process.argv.includes('--test-fixtures');
const port = Number(process.env.LOCAL_SECURITY_PORT || 4175);
const types = {'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.svg':'image/svg+xml'};
http.createServer(async (req, res) => {
  for (const header of config.headers[0].headers) res.setHeader(header.key, header.value);
  res.setHeader('Cache-Control', 'no-store');
  const url = new URL(req.url, 'http://127.0.0.1');
  if (fixtures && url.pathname === '/__security/csp-probe') {
    res.setHeader('Content-Type', 'text/html; charset=utf-8');
    res.end(`<title>CSP probe</title><p>CSP probe: inline script must be blocked.</p><script>document.title='UNSAFE INLINE EXECUTED'</script>`);
    return;
  }
  if (url.pathname.startsWith('/api/')) {
    res.setHeader('Content-Type', 'application/json');
    if (!fixtures || url.pathname !== '/api/contacto' || req.method !== 'POST') {
      res.writeHead(404).end('{"message":"No local API configured."}'); return;
    }
    let body = '';
    for await (const chunk of req) {
      body += chunk;
      if (Buffer.byteLength(body) > 16384) { res.writeHead(413).end('{}'); return; }
    }
    let payload;
    try { payload = JSON.parse(body); } catch { res.writeHead(400).end('{}'); return; }
    if (!payload.aceptaPrivacidad || !payload.nombre || !payload.correo || !payload.servicio) {
      res.writeHead(400).end('{"errors":{"nombre":"Datos de prueba incompletos."}}'); return;
    }
    if (payload.descripcion === 'simulated-validation') {
      res.writeHead(400).end('{"errors":{"descripcion":"Error de validación simulado."}}'); return;
    }
    if (payload.descripcion === 'simulated-failure') {
      res.writeHead(503).end('{"message":"Fallo simulado; no se guardaron datos."}'); return;
    }
    res.writeHead(201).end(JSON.stringify({id:1,servicio:payload.servicio,fechaCreacion:'2026-10-21T12:00:00Z'}));
    return; // Synthetic confirmation only. No storage or external requests.
  }
  if (req.method !== 'GET' && req.method !== 'HEAD') { res.writeHead(405).end(); return; }
  let pathname;
  try { pathname = decodeURIComponent(url.pathname); } catch { res.writeHead(400).end(); return; }
  const file = path.resolve(root, '.' + (pathname === '/' ? '/index.html' : pathname));
  if (!file.startsWith(root) || !fs.existsSync(file) || !fs.statSync(file).isFile()) {
    res.writeHead(404).end('Not found'); return;
  }
  res.setHeader('Content-Type', types[path.extname(file)] || 'application/octet-stream');
  if (req.method === 'HEAD') res.end(); else fs.createReadStream(file).pipe(res);
}).listen(port, '127.0.0.1', () => console.log(`Local security harness: http://127.0.0.1:${port}; synthetic fixtures=${fixtures}`));
