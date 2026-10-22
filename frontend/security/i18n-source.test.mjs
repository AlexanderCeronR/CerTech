import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { createRequire } from 'node:module';
// Reuse Babel already required by Vite's React plugin; no extra package is installed.
const { parseSync } = createRequire(import.meta.resolve('@vitejs/plugin-react'))('@babel/core');
const brands = new Set(['C','Cer','Tech','CerTech.','CERTECH SYSTEMS','CERTECH','M','MORA','INFINITY','APPAREL']);
function files(dir) { return fs.readdirSync(dir,{withFileTypes:true}).flatMap(entry=>entry.isDirectory()?files(path.join(dir,entry.name)):[path.join(dir,entry.name)]); }
test('visible JSX text and accessible labels use catalogs, except unchanged brand marks', () => {
  const findings=[];
  for(const file of files(fileURLToPath(new URL('../src/',import.meta.url))).filter(file=>file.endsWith('.jsx'))) {
    const ast=parseSync(fs.readFileSync(file,'utf8'),{babelrc:false,configFile:false,parserOpts:{plugins:['jsx']}});
    function visit(node) {
      if(!node || typeof node!=='object')return;
      if(node.type==='JSXText') {
        const text=node.value.trim();
        if(/[A-Za-zÀ-ž]/.test(text) && !brands.has(text))findings.push({file,line:node.loc.start.line,text});
      }
      if(node.type==='JSXAttribute' && ['aria-label','alt','placeholder','title'].includes(node.name?.name) && node.value?.type==='StringLiteral')findings.push({file,line:node.loc.start.line,text:node.value.value});
      for(const value of Object.values(node))if(Array.isArray(value))value.forEach(visit);else if(value && typeof value==='object' && value.type)visit(value);
    }
    visit(ast);
  }
  assert.deepEqual(findings,[]);
});
