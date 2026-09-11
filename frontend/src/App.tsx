import { Navigate, Route, Routes } from "react-router-dom"

import DeploymentDetailPage from "./pages/DeploymentDetailPage"
import DeploymentsPage from "./pages/DeploymentsPage"
import ProjectDetailPage from "./pages/ProjectDetailPage"
import RiskPage from "./pages/RiskPage"
import ProjectsPage from "./pages/ProjectsPage"
import AppShell from "./components/AppShell"

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/projects" replace />} />
      <Route path="/projects" element={<ProjectsPage />} />
      <Route path="/projects/:id" element={<ProjectDetailPage />} />
      <Route path="/deployments" element={<DeploymentsPage />} />
      <Route path="/deployments/:id" element={<DeploymentDetailPage />} />
      <Route path="/risk" element={<RiskPage />} />
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
