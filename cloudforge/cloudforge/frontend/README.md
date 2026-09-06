# frontend

React 19 + TypeScript + Vite dashboard for CloudForge.

```powershell
Copy-Item .env.example .env
npm install
npm run dev     # http://localhost:5173
```

`vite.config.ts` proxies `/api` to `http://localhost:8080`, so the browser stays on
one origin during development and CORS never applies locally.

Phase 0 renders a backend connectivity probe only. Routing, TanStack Query, and the
real dashboard arrive in Phase 1.

Lint with `npm run lint` (oxlint). Type-check and build with `npm run build`.
