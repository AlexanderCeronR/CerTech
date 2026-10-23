export const limits = {titulo:[2,160],descripcion:[10,3000],objetivo:[5,1000],requisitos:[5,1500],nombre:[2,120],correo:[1,180],empresa:[0,140],telefono:[0,32],pais:[0,80]};
export const deadlines=['flexible','month','quarter','later'];
export function validateStep(data,step,services){
 const fields=step===0?['servicio']:step===1?['titulo','descripcion','objetivo','requisitos','plazo','presupuesto']:['nombre','correo','empresa','telefono','pais','aceptaPrivacidad'];
 return fields.filter(key=>{
  const value=data[key]??'';
  if(key==='servicio')return !services.some(s=>s.value===value);
  if(key==='plazo')return !deadlines.includes(value);
  if(key==='presupuesto')return !['','discuss','under','small','medium','large'].includes(value);
  if(key==='aceptaPrivacidad')return value!==true;
  if(!limits[key])return false;
  const [min,max]=limits[key];if(value.trim().length<min||value.length>max)return true;
  if(key==='correo')return !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
  if(key==='telefono')return !/^[+0-9 ()-]*$/.test(value);
  return false;
 });
}
export function newDraft(){return {servicio:'',titulo:'',descripcion:'',objetivo:'',requisitos:'',plazo:'flexible',presupuesto:'',nombre:'',correo:'',empresa:'',telefono:'',pais:'',aceptaPrivacidad:false};}
