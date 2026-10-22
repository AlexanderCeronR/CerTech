import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import { catalogs, languages, isLanguage, readLanguage, saveLanguage, translate, whatsappHref, STORAGE_KEY } from './core.js';
import { contactFeedback, invalidFields } from './contactFeedback.js';
import { services, budgets, projects } from '../data/site.js';

const keys = Object.keys(catalogs.es).sort();
for (const { code } of languages) {
  test(`${code}: same nonempty semantic keys, interpolation variables and no embedded HTML`, () => {
    assert.deepEqual(Object.keys(catalogs[code]).sort(), keys);
    for (const key of keys) {
      const text = catalogs[code][key];
      assert.equal(typeof text, 'string', key); assert.ok(text.trim(), key);
      assert.doesNotMatch(text, /<\/?[a-z]|undefined|\[object Object\]/i, key);
      assert.deepEqual(text.match(/\{\w+\}/g) || [], catalogs.es[key].match(/\{\w+\}/g) || [], key);
      assert.equal(translate(code, key), text);
    }
  });
  test(`${code}: catalog covers every service, project, form error and metadata`, () => {
    for (const { slug } of services) for (const part of ['title','description','benefit']) assert.ok(translate(code,`services.${slug}.${part}`));
    for (const p of projects) {
      for (const part of ['category','description','preview']) assert.ok(translate(code,`projects.${p.previewType}.${part}`));
      for (const feature of p.features) assert.ok(translate(code,`projects.${p.previewType}.features.${feature}`));
    }
    for (const field of ['nombre','correo','empresa','servicio','presupuesto','descripcion','aceptaPrivacidad']) assert.ok(translate(code,`form.validation.${field}`));
    for (const key of ['title','description','socialDescription']) assert.ok(translate(code,`meta.${key}`));
  });
  test(`${code}: WhatsApp exact number, encoded localized message and safe scheme`, () => {
    const url = new URL(whatsappHref(code));
    assert.equal(url.origin,'https://wa.me');assert.equal(url.pathname,'/573143608576');
    assert.equal(url.searchParams.get('text'),catalogs[code]['whatsapp.message']);
    assert.equal(whatsappHref(code), 'https://wa.me/573143608576?text='+encodeURIComponent(catalogs[code]['whatsapp.message']));
  });
}
test('all statically used translation keys exist', () => {
  const paths=['../App.jsx','../components/ContactForm.jsx','../components/ProjectCard.jsx','../components/LanguageSelector.jsx','../components/WhatsAppButton.jsx','./I18nProvider.jsx'];
  for (const path of paths) {
    const source=fs.readFileSync(new URL(path,import.meta.url),'utf8');
    for(const match of source.matchAll(/\bt\('([^']+)'/g))assert.ok(keys.includes(match[1]),path+': '+match[1]);
  }
});
test('Spanish default, valid saved preference and hostile/unsupported values', () => {
  assert.equal(readLanguage(()=>({getItem:()=>null})),'es');
  for(const code of ['es','en','fr'])assert.equal(readLanguage(()=>({getItem:()=>code})),code);
  for(const value of ['de','__proto__','constructor','<script>','EN','en-US',''])assert.equal(readLanguage(()=>({getItem:()=>value})),'es');
  assert.equal(isLanguage('constructor'),false);
});
test('blocked or missing storage never breaks reading or changing language', () => {
  const denied=()=>{throw new Error('SecurityError');};
  assert.equal(readLanguage(denied),'es');assert.equal(saveLanguage('fr',denied),false);
  assert.equal(readLanguage(()=>undefined),'es');assert.equal(saveLanguage('en',()=>undefined),false);
  const data=new Map();const storage=()=>({getItem:k=>data.get(k),setItem:(k,v)=>data.set(k,v)});
  assert.equal(saveLanguage('fr',storage),true);assert.equal(data.get(STORAGE_KEY),'fr');assert.equal(readLanguage(storage),'fr');
  assert.equal(saveLanguage('unsupported',storage),false);assert.equal(readLanguage(storage),'fr');
});
test('interpolation stays plain text and unknown keys are detected', () => {
  const hostile='<img src=x onerror=alert(1)>';
  assert.equal(translate('en','projects.featuresLabel',{name:hostile}),'Public features of '+hostile);
  assert.throws(()=>translate('en','unknown.key'),/Missing translation/);
});
test('service and budget API values remain the original contract in every language', () => {
  assert.deepEqual(services.map(s=>s.value),['Desarrollo Web','Ciberseguridad','Desarrollo de Software','Soporte TI','Shopify Development','Administración de Bases de Datos','Inteligencia Artificial y Automatización']);
  assert.deepEqual(budgets.map(b=>b.value),['','Menos de $1.000.000 COP','$1.000.000 – $5.000.000 COP','$5.000.000 – $15.000.000 COP','Más de $15.000.000 COP']);
  for (const {code} of languages) for(const b of budgets) assert.ok(translate(code,`form.budget.${b.key}`));
});
test('server prose and unknown fields are never used as displayed feedback', () => {
  assert.deepEqual(contactFeedback({code:'validation',fields:['correo','__proto__','password'],message:'Spanish internal prose'}),{key:'form.status.validation',fields:['correo']});
  assert.deepEqual(contactFeedback(new Error('untrusted')),{key:'form.status.submit',fields:[]});
  for(const code of ['network','validation','tooLarge','rateLimit','submit','unconfirmed'])for(const {code:lang}of languages)assert.ok(translate(lang,contactFeedback({code}).key));
});
test('localized validation keeps required, length, email and consent checks', () => {
  const controls={nombre:{value:'a',required:true,minLength:2,maxLength:120,validity:{}},correo:{value:'bad',required:true,maxLength:180,validity:{typeMismatch:true}},descripcion:{value:'short',minLength:10,maxLength:3000,validity:{}},aceptaPrivacidad:{value:'on',type:'checkbox',required:true,checked:false,validity:{}}};
  const form={elements:{namedItem:name=>controls[name]}};
  assert.deepEqual(invalidFields(form),['nombre','correo','descripcion','aceptaPrivacidad']);
  controls.nombre.value='Synthetic Client';controls.correo.value='qa@example.invalid';controls.correo.validity.typeMismatch=false;controls.descripcion.value='A synthetic project description.';controls.aceptaPrivacidad.checked=true;
  assert.deepEqual(invalidFields(form),[]);
});
