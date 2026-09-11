import { Link } from "react-router-dom"

import { useDeployments } from "../api/deployments"
import AppShell from "../components/AppShell"
import StatusPill from "../components/StatusPill"
import TableSkeleton from "../components/TableSkeleton"

/**
 * Fleet-wide view. Deliberately lists deployments rather than assessments: the
 * question an operator asks is "what is waiting on a decision", and an unassessed
 * deployment is the most important row on this page.
 */
export default function RiskPage() {
  const { data, isPending, isError, error } = useDeployments()
  const deployments = data?.content ?? []

  return (
    <AppShell title="Risk" context="Scored before deployment">
      <section>
        <p className="muted">
          Every deployment is scored from its pipeline metrics before it can start. Open one
          to see its score and what drove it.
        </p>

        {isPending && <TableSkeleton rows={4} />}

        {isError && (
          <div className="alert error" role="alert">
            Could not load deployments. {(error as Error).message}
          </div>
        )}

        {data && deployments.length === 0 && (
          <div className="empty">
            <h3>Nothing to score yet</h3>
            <p>Record a deployment and its pipeline metrics, then assess it.</p>
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
                <th>Recorded</th>
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
                  <td className="subtle">
                    {new Date(deployment.createdAt).toLocaleString()}
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
