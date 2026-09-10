import { NavLink } from "react-router-dom"

import { useProjects } from "../api/projects"

/**
 * The sidebar is the deployment pipeline in order. Stages that don't exist yet
 * are dimmed and carry the phase that will build them, so the roadmap is legible
 * from inside the product.
 */
const PIPELINE = [
  { label: "Projects", to: "/projects", phase: null },
  { label: "Deployments", to: null, phase: "2" },
  { label: "Risk", to: null, phase: "7" },
  { label: "Monitoring", to: null, phase: "10" },
] as const

type Props = {
  title: string
  context?: string
  action?: React.ReactNode
  children: React.ReactNode
}

export default function AppShell({ title, context, action, children }: Props) {
  // Reuses the projects query already in cache; no extra request.
  const { isError, isSuccess } = useProjects()

  const apiState = isSuccess ? "up" : isError ? "down" : ""
  const apiLabel = isSuccess ? "API connected" : isError ? "API unreachable" : "Checking API"

  return (
    <div className="shell">
      <aside className="sidebar">
        <NavLink to="/projects" className="wordmark">
          CloudForge
          <span className="version">0.1.0</span>
        </NavLink>

        <nav className="pipeline">
          <div className="pipeline-heading">Pipeline</div>

          {PIPELINE.map((stage) =>
            stage.to ? (
              <NavLink
                key={stage.label}
                to={stage.to}
                className={({ isActive }) => (isActive ? "nav-item active" : "nav-item")}
              >
                {stage.label}
              </NavLink>
            ) : (
              <span key={stage.label} className="nav-item pending" aria-disabled="true">
                {stage.label}
                <span className="phase-tag">phase {stage.phase}</span>
              </span>
            ),
          )}
        </nav>

        <div className="sidebar-foot">
          Local development
          <div className="status-line">
            <span className={`dot ${apiState}`} />
            {apiLabel}
          </div>
        </div>
      </aside>

      <div>
        <header className="topbar">
          <div className="topbar-title">
            <h1>{title}</h1>
            {context && <span className="context">{context}</span>}
          </div>
          {action}
        </header>

        <main className="content">{children}</main>
      </div>
    </div>
  )
}
