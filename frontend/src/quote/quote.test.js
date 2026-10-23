import test from 'node:test';
import assert from 'node:assert/strict';
import {validateStep,newDraft} from './model.js';
import {submitQuote} from '../services/quoteService.js';
import {services} from '../data/site.js';
import {translate} from '../i18n/core.js';
test('wizard validates each boundary, optional fields and consent without mutating data',()=>{
 const d=newDraft();assert.deepEqual(validateStep(d,0,services),['servicio']);d.servicio=services[0].value;assert.deepEqual(validateStep(d,0,services),[]);
 assert.deepEqual(validateStep(d,1,services),['titulo','descripcion','objetivo','requisitos']);Object.assign(d,{titulo:'Project',descripcion:'Synthetic project description',objetivo:'Receive enquiries',requisitos:'Contact form',nombre:'Synthetic Client',correo:'qa@example.invalid',aceptaPrivacidad:true});
 assert.deepEqual(validateStep(d,1,services),[]);assert.deepEqual(validateStep(d,2,services),[]);
 for(const [key,value] of Object.entries({titulo:'x'.repeat(161),descripcion:'x'.repeat(3001),objetivo:' ',requisitos:'x'.repeat(1501),correo:'bad\r\nBcc:x',telefono:'<script>',pais:'x'.repeat(81),aceptaPrivacidad:false})){
 const copy={...d,[key]:value};assert.ok(validateStep(copy,['titulo','descripcion','objetivo','requisitos'].includes(key)?1:2,services).includes(key));}
 const before=JSON.stringify(d);for(const lang of ['es','en','fr']){assert.ok(translate(lang,'quote.title'));assert.ok(translate(lang,'quote.success'));}assert.equal(JSON.stringify(d),before);
});
test('quote API requires a matching persisted 201 receipt and never trusts arbitrary server prose',async()=>{
 const original=globalThis.fetch;const payload={requestId:'875a0fc4-f5a8-4d85-8077-c1f24aeec005',servicio:services[0].value};const receipt={id:'981c7484-126c-48c6-ae7b-dd4b6f14e588',servicio:payload.servicio,fechaCreacion:'2026-10-22T12:00:00Z'};
 try{globalThis.fetch=async(url,opts)=>{assert.equal(url,'/api/cotizaciones');assert.equal(JSON.parse(opts.body).requestId,payload.requestId);return new Response(JSON.stringify(receipt),{status:201});};assert.deepEqual(await submitQuote(payload),receipt);
 for(const [status,body,code] of [[200,receipt,'submit'],[201,{},'unconfirmed'],[201,{...receipt,servicio:'Other'},'unconfirmed'],[400,{message:'private'},'validation'],[409,{},'conflict'],[413,{},'tooLarge'],[429,{},'rateLimit'],[503,{},'submit']]){globalThis.fetch=async()=>new Response(JSON.stringify(body),{status});await assert.rejects(submitQuote(payload),{message:code});}
 globalThis.fetch=async()=>{throw new Error('private');};await assert.rejects(submitQuote(payload),{message:'network'});
 }finally{globalThis.fetch=original;}
});

test('all quote states, fields, deadlines and errors are translated in three languages',()=>{
 const keys=['title','subtitle','progress','success','successTitle','reference','consent','dataHelp','budgetHelp','reviewHelp','noPrice','next','previous','send','sending',...[0,1,2,3].map(n=>`step.${n}`),...['flexible','month','quarter','later'].map(v=>`deadline.${v}`),...['servicio','titulo','descripcion','objetivo','requisitos','plazo','presupuesto','nombre','correo','empresa','telefono','pais'].map(v=>`field.${v}`),...['network','validation','conflict','tooLarge','rateLimit','submit','unconfirmed'].map(v=>`error.${v}`)];
 for(const lang of ['es','en','fr'])for(const key of keys)assert.ok(translate(lang,`quote.${key}`).length>0);
});
