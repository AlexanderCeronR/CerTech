// Local production-build preview; forwards only the two public POST endpoints to loopback.
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
const root=path.resolve(new URL('../../frontend/dist/',import.meta.url).pathname);
const config=JSON.parse(fs.readFileSync(new URL('../../frontend/vercel.json',import.meta.url)));
const headers=Object.fromEntries(config.headers.flatMap(group=>group.headers.map(h=>[h.key,h.value])));
const mime={'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.webp':'image/webp','.svg':'image/svg+xml'};
const server=http.createServer((req,res)=>{
 for(const [k,v] of Object.entries(headers))res.setHeader(k,v);
 const url=new URL(req.url,'http://127.0.0.1');
 if(['/api/contacto','/api/cotizaciones'].includes(url.pathname)&&['POST','OPTIONS'].includes(req.method)){
  let total=0;const chunks=[];req.on('data',chunk=>{total+=chunk.length;if(total<=16384)chunks.push(chunk);});req.on('end',()=>{
   if(total>16384){res.writeHead(413,{'Content-Type':'application/json'});res.end('{"message":"Request too large"}');return;}
   const allowed={};for(const name of ['content-type','accept','origin','access-control-request-method','access-control-request-headers'])if(req.headers[name])allowed[name]=req.headers[name];
   const proxy=http.request({hostname:'127.0.0.1',port:18086,path:url.pathname,method:req.method,headers:allowed,timeout:15000},upstream=>{res.statusCode=upstream.statusCode;for(const name of ['content-type','retry-after','access-control-allow-origin','access-control-allow-methods','access-control-allow-headers','vary'])if(upstream.headers[name])res.setHeader(name,upstream.headers[name]);upstream.pipe(res);});
   proxy.on('timeout',()=>proxy.destroy());proxy.on('error',()=>{if(!res.headersSent)res.writeHead(503,{'Content-Type':'application/json'});res.end('{"message":"Local API unavailable"}');});proxy.end(Buffer.concat(chunks));
  });return;
 }
 if(req.method!=='GET'||(url.pathname!=='/'&&!/^\/assets\/[a-zA-Z0-9._-]+$/.test(url.pathname))){res.writeHead(404);res.end();return;}
 const file=path.join(root,url.pathname==='/'?'index.html':url.pathname.slice(1));if(!fs.existsSync(file)){res.writeHead(404);res.end();return;}
 res.setHeader('Content-Type',mime[path.extname(file)]||'application/octet-stream');res.setHeader('Cache-Control','no-store');fs.createReadStream(file).pipe(res);
});
server.requestTimeout=20000;server.headersTimeout=10000;server.listen(4186,'127.0.0.1',()=>console.log('Synthetic-only integrated preview http://127.0.0.1:4186 ; API 127.0.0.1:18086'));
