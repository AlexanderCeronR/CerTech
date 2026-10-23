const API_URL=(import.meta.env?.VITE_API_URL??'').replace(/\/$/,'');
export async function submitQuote(payload){
 let response;
 try {response=await fetch(`${API_URL}/api/cotizaciones`,{method:'POST',headers:{'Content-Type':'application/json',Accept:'application/json'},body:JSON.stringify(payload)});}catch{throw new Error('network');}
 const data=await response.json().catch(()=>null);
 if(response.status!==201){throw new Error(({400:'validation',409:'conflict',413:'tooLarge',429:'rateLimit'})[response.status]||'submit');}
 if(!data||typeof data.id!=='string'||!/^([a-f0-9]{8}-){1}[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$/i.test(data.id)||data.servicio!==payload.servicio||typeof data.fechaCreacion!=='string'||Number.isNaN(Date.parse(data.fechaCreacion)))throw new Error('unconfirmed');
 return data;
}
