import { Code2, ShieldCheck, Blocks, Headset, ShoppingBag, Database, Bot } from 'lucide-react';

// These values are the existing API contract, not translated display labels.
export const services = [
  { value: 'Desarrollo Web', icon: Code2, slug: 'desarrollo-web' },
  { value: 'Ciberseguridad', icon: ShieldCheck, slug: 'ciberseguridad' },
  { value: 'Desarrollo de Software', icon: Blocks, slug: 'desarrollo-software' },
  { value: 'Soporte TI', icon: Headset, slug: 'soporte-ti' },
  { value: 'Shopify Development', icon: ShoppingBag, slug: 'shopify' },
  { value: 'Administración de Bases de Datos', icon: Database, slug: 'bases-de-datos' },
  { value: 'Inteligencia Artificial y Automatización', icon: Bot, slug: 'ia-automatizacion', featured: true },
];
export const budgets = [
  { key: 'discuss', value: '' },
  { key: 'under', value: 'Menos de $1.000.000 COP' },
  { key: 'small', value: '$1.000.000 – $5.000.000 COP' },
  { key: 'medium', value: '$5.000.000 – $15.000.000 COP' },
  { key: 'large', value: 'Más de $15.000.000 COP' },
];
export const projects = [
  { name: 'CerTech', url: '#inicio', external: false, previewType: 'certech', features: ['services', 'method', 'contact'] },
  { name: 'MORA', url: 'https://mora-frontend-two.vercel.app/', external: true, previewType: 'mora', features: ['tables', 'payments', 'analytics', 'roles'] },
  { name: 'Infinity Apparel', url: 'https://infinityapparel.co/', external: true, previewType: 'infinity', features: ['shopify', 'soon', 'signup'] },
];
export const contactConfig = { email: '', phone: '', socialLinks: [] };
