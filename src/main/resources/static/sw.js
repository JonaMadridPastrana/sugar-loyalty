const CACHE_NAME = 'sugar-pop-pwa-v4';
const STATIC_ASSETS = [
  '/manifest.json',
  '/sugar-icon.svg',
  '/LogoBlanco.PNG',
  '/FondoRegistro.jpg',
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS);
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) =>
      Promise.all(
        cacheNames
          .filter((name) => name !== CACHE_NAME)
          .map((name) => caches.delete(name))
      )
    )
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  const { request } = event;
  if (request.method !== 'GET') return;

  if (request.url.includes('/api/')) {
    event.respondWith(fetch(request));
    return;
  }

  // Network-First para evitar que iOS Safari sirva un index.html viejo tras actualizar despliegues
  event.respondWith(
    fetch(request)
      .then((networkResponse) => {
        const contentType = networkResponse.headers.get('content-type') || '';
        const isAssetJsOrCss =
          request.url.endsWith('.js') || request.url.endsWith('.css');

        // Nunca guardar en caché si un archivo .js/.css devolvió HTML por error
        if (
          networkResponse &&
          networkResponse.status === 200 &&
          !(isAssetJsOrCss && contentType.includes('text/html'))
        ) {
          const responseClone = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(request, responseClone);
          });
        }
        return networkResponse;
      })
      .catch(() => caches.match(request))
  );
});
