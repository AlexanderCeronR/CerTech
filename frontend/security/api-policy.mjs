export function normalizeApiOrigin(value = '') {
  if (!value) return '';
  const url = new URL(value);
  if (url.protocol !== 'https:' || url.username || url.password || url.search || url.hash || url.pathname !== '/' || /[*;\s]/.test(url.hostname)) {
    throw new Error('VITE_API_URL must be an HTTPS origin without credentials, path, query or fragment.');
  }
  return url.origin;
}

export function validateBuildApi(value, config, hosted = false) {
  const origin = normalizeApiOrigin(value);
  if (hosted && !origin) throw new Error('Define the approved HTTPS API origin before building on Vercel.');
  const policy = config.headers.flatMap(rule => rule.headers).find(h => h.key === 'Content-Security-Policy').value;
  const connect = policy.split(';').map(v => v.trim()).find(v => v.startsWith('connect-src ')).split(/\s+/).slice(1);
  if (origin && !connect.includes(origin)) throw new Error('API origin must match the exact connect-src origin in vercel.json.');
  return origin;
}
