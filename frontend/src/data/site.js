import { Code2, ShieldCheck, Blocks, Headset, ShoppingBag, Database, Bot } from 'lucide-react';

export const services = [
  { title: 'Desarrollo Web', icon: Code2, description: 'Experiencias digitales modernas, rápidas y adaptables para fortalecer tu presencia en internet.', benefit: 'Una presencia digital lista para crecer.', slug: 'desarrollo-web' },
  { title: 'Ciberseguridad', icon: ShieldCheck, description: 'Identificamos riesgos y fortalecemos sistemas con evaluaciones autorizadas y buenas prácticas.', benefit: 'Más claridad sobre tus riesgos digitales.', slug: 'ciberseguridad' },
  { title: 'Desarrollo de Software', icon: Blocks, description: 'Aplicaciones, APIs e integraciones diseñadas alrededor de las necesidades de tu negocio.', benefit: 'Procesos digitales a tu medida.', slug: 'desarrollo-software' },
  { title: 'Soporte TI', icon: Headset, description: 'Asistencia, mantenimiento y diagnóstico para que tu tecnología siga funcionando.', benefit: 'Menos interrupciones en tu operación.', slug: 'soporte-ti' },
  { title: 'Shopify Development', icon: ShoppingBag, description: 'Tiendas Shopify personalizadas para ofrecer una experiencia de compra profesional.', benefit: 'Una tienda alineada con tu marca.', slug: 'shopify' },
  { title: 'Administración de Bases de Datos', icon: Database, description: 'Organización, mantenimiento y optimización para mantener tus datos útiles y accesibles.', benefit: 'Información más organizada y confiable.', slug: 'bases-de-datos' },
  { title: 'Inteligencia Artificial y Automatización', icon: Bot, description: 'Conectamos herramientas y automatizamos tareas para que tu equipo se enfoque en lo que importa.', benefit: 'Menos trabajo repetitivo, más capacidad para avanzar.', slug: 'ia-automatizacion', featured: true },
];

export const projects = [
  {
    name: 'CerTech',
    category: 'Sitio corporativo propio',
    url: '#inicio',
    external: false,
    description: 'Sitio corporativo que presenta los siete servicios de CerTech, su enfoque de trabajo y el canal de contacto.',
    previewType: 'certech',
    previewLabel: 'Representación ilustrativa del sitio corporativo de CerTech; no es una captura de pantalla.',
    features: ['7 servicios tecnológicos', 'Metodología de trabajo', 'Formulario de contacto'],
  },
  {
    name: 'MORA',
    category: 'Gestión para restaurantes',
    url: 'https://mora-frontend-two.vercel.app/',
    external: true,
    description: 'Plataforma para centralizar la gestión de mesas, pedidos, caja y análisis de un restaurante.',
    previewType: 'mora',
    previewLabel: 'Representación ilustrativa de MORA, basada en las funciones descritas en su sitio público; no es una captura de pantalla.',
    features: ['Mesas y pedidos', 'Caja y pagos', 'Analítica e IA', 'Roles de acceso'],
  },
  {
    name: 'Infinity Apparel',
    category: 'Moda · Shopify',
    url: 'https://infinityapparel.co/',
    external: true,
    description: 'Sitio público de una marca de moda. Actualmente muestra una página de próxima apertura con registro para novedades.',
    previewType: 'infinity',
    previewLabel: 'Representación ilustrativa del estado público actual de Infinity Apparel; no es una captura de pantalla.',
    features: ['Sitio sobre Shopify', 'Apertura próxima', 'Registro de novedades'],
  },
];
export const contactConfig = { email: '', phone: '', socialLinks: [] };
