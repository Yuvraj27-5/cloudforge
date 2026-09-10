import { Link } from "react-router-dom"

import { useDeployments } from "../api/deployments"
import AppShell from "../components/AppShell"
import StatusPill from "../components/StatusPill"
import TableSkeleton from "../components/TableSkeleton"

export default function DeploymentsPage() {
  const { data, isPending, isError, error } = useDeployments()
  const deployments = data?.content ?? []

  return (
    <AppShell
      title="Deployments"
      context={data && data.totalElements > 0 ? `${data.totalElements} recorded` : undefined}
    >
      <section>
        {isPending && <TableSkeleton rows={4} />}

        {isError && (
          <div className="alert error" role="alert">
            Could not load deployments. {(error as Error).message}
          </div>
        )}

        {data && deployments.length === 0 && (
          <div className="empty">
            <h3>No deployments yet</h3>
            <p>Open a project and record a deployment to see it here.</p>
            <Link to="/projects">
              <button>Go to projects</button>
            </Link>
          </div>
        )}

        {deployments.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Commit</th>
                <th>Project</th>
                <th>Status</th>
                <th>Triggered by</th>
                <th>Started</th>
              </tr>
            </thead>
            <tbody>
              {deployments.map((deployment) => (
                <tr key={deployment.id}>
                  <td className="name">
                    <Link to={`/deployments/${deployment.id}`} className="mono">
                      {deployment.shortSha}
                    </Link>
                    {deployment.commitMessage && (
                      <div className="subtle">{deployment.commitMessage}</div>
                    )}
                  </td>
                  <td>
                    <Link to={`/projects/${deployment.projectId}`}>{deployment.projectName}</Link>
                  </td>
                  <td>
                    <StatusPill status={deployment.status} />
                  </td>
                  <td className="subtle">{deployment.triggeredBy ?? "—"}</td>
                  <td className="subtle">
                    {deployment.startedAt
                      ? new Date(deployment.startedAt).toLocaleString()
                      : "Not started"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </AppShell>
  )
}
