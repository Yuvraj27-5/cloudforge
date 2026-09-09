# Design system update

Extract over `C:\Users\ASUS\Projects\cloudforge`. Frontend only — no backend,
database or config changes, so nothing you fixed is touched.

```powershell
cd C:\Users\ASUS\Projects\cloudforge
Expand-Archive -Path "$HOME\Downloads\cloudforge-design.zip" -DestinationPath . -Force
cd frontend
npm run dev
```

No `npm install` needed; no new dependencies.

## Files

| File | Change |
|---|---|
| `src/index.css` | Replaced. Token system, shell, controls, tables, states |
| `src/components/AppShell.tsx` | New. Sidebar + topbar layout |
| `src/components/Badge.tsx` | New |
| `src/components/TableSkeleton.tsx` | New |
| `src/components/StatusMessage.tsx` | Deleted, replaced by `.alert` styles |
| `src/components/Field.tsx` | Restyled |
| `src/pages/ProjectsPage.tsx` | Rebuilt on the shell |
| `src/pages/ProjectDetailPage.tsx` | Rebuilt on the shell |
| `src/App.tsx` | Routes only; layout moved into AppShell |

## Decisions

**Neutrals carry a navy cast.** The greys are mixed toward blue so the accent
belongs to the palette instead of sitting on top of a neutral one.

**IBM Plex Sans and Plex Mono**, loaded from Google Fonts. Mono is reserved for
machine data — repository paths, branches, UUIDs — never for decorative labels.

**The sidebar is the pipeline.** Projects, Deployments, Risk, Monitoring, in the
order a deployment moves through them. Unbuilt stages are dimmed and carry the
phase that will build them, so the roadmap is readable from inside the product.

**Risk colours are defined now** (`--risk-low`, `--risk-medium`, `--risk-high`)
even though nothing uses them until Phase 7, so the score bands are consistent
the first time they appear.

**Borders, not shadows.** Instrumentation should read as flat and precise. Two
radii only: 10px for panels, 6px for controls.

**The create form moved into a panel** opened from the topbar. The empty state
now invites the action instead of sitting under a permanently open form.

## Quality floor

Responsive to a single column under 800px, visible keyboard focus, `aria-invalid`
on rejected fields, `role="alert"` on errors, `prefers-reduced-motion` respected
by the loading shimmer, light and dark both driven by the system setting.
