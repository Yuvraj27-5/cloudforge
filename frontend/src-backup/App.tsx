import { Navigate, Route, Routes } from "react-router-dom"

import ProjectDetailPage from "./pages/ProjectDetailPage"
import ProjectsPage from "./pages/ProjectsPage"
import AppShell from "./components/AppShell"

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/projects" replace />} />
      <Route path="/projects" element={<ProjectsPage />} />
      <Route path="/projects/:id" element={<ProjectDetailPage />} />
      <Route
        path="*"
        element={
          <AppShell title="Not found">
            <p>That page does not exist.</p>
          </AppShell>
        }
      />
    </Routes>
  )
}
