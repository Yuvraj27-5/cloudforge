import { Link, Navigate, Route, Routes } from "react-router-dom"

import ProjectDetailPage from "./pages/ProjectDetailPage"
import ProjectsPage from "./pages/ProjectsPage"

export default function App() {
  return (
    <div className="app">
      <header>
        <Link to="/projects" className="brand">
          CloudForge
        </Link>
        <span className="muted">Phase 1</span>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/projects" replace />} />
          <Route path="/projects" element={<ProjectsPage />} />
          <Route path="/projects/:id" element={<ProjectDetailPage />} />
          <Route path="*" element={<p>Page not found.</p>} />
        </Routes>
      </main>
    </div>
  )
}
