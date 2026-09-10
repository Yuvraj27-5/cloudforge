import { useState } from "react"
import { Link, useParams } from "react-router-dom"

import { ApiError } from "../api/client"
import { useDeployment, useUpdateDeploymentStatus } from "../api/deployments"
import AppShell from "../components/AppShell"
import DeploymentTimeline from "../components/DeploymentTimeline"
import MetricsPanel from "../components/MetricsPanel"
import StatusPill from "../components/StatusPill"
import { STATUS_LABELS, type DeploymentStatus } from "../types/deployment"

export default function DeploymentDetailPage() {
  const { id = "" } = useParams()
  const { data, isPending, isError, error } = useDeployment(id)
  const updateStatus = useUpdateDeploymentStatus(id)
  const [reason, setReason] = useState("")

  if (isPending) {
    return (
      <AppShell title="Deployment">
        <p>Loading…</p>
      </AppShell>
    )
  }

  if (isError) {
    return (
      <AppShell title="Deployment">
        <div className="alert error" role="alert">
          {(error as Error).message}
        </div>
        <Link to="/deployments">Back to deployments</Link>
      </AppShell>
    )
  }

  const { deployment, events, allowedTransitions, metrics } = data
  const apiError = updateStatus.error instanceof ApiError ? updateStatus.error : undefined

  function move(status: DeploymentStatus) {
    updateStatus.mutate(
      { status, reason: reason || undefined, actor: "manual" },
      { onSuccess: () => setReason("") },
    )
  }

  return (
    <AppShell
      title={deployment.shortSha}
      context={deployment.projectName}
      action={<StatusPill status={deployment.status} />}
    >
      <section>
        <p>
          <Link to="/deployments">Back to deployments</Link>
        </p>

        {apiError && (
          <div className="alert error" role="alert">
            {apiError.message}
          </div>
        )}

        <div className="panel">
          <dl className="detail-grid">
            <dt>Commit</dt>
            <dd className="mono">{deployment.commitSha}</dd>

            {deployment.commitMessage && (
              <>
                <dt>Message</dt>
                <dd>{deployment.commitMessage}</dd>
              </>
            )}

            <dt>Project</dt>
            <dd>
              <Link to={`/projects/${deployment.projectId}`}>{deployment.projectName}</Link>
            </dd>

            <dt>Triggered by</dt>
            <dd>{deployment.triggeredBy ?? "Not recorded"}</dd>

            <dt>Correlation ID</dt>
            <dd className="mono">{deployment.correlationId}</dd>

            <dt>Started</dt>
            <dd>
              {deployment.startedAt
                ? new Date(deployment.startedAt).toLocaleString()
                : "Not started"}
            </dd>

            <dt>Completed</dt>
            <dd>
              {deployment.completedAt
                ? new Date(deployment.completedAt).toLocaleString()
                : "Still open"}
            </dd>
          </dl>
        </div>
      </section>

      {allowedTransitions.length > 0 && (
        <section>
          <h2>Move this deployment</h2>
          <p className="muted">
            Phase 4 hands this to the pipeline. Until then, drive it by hand to exercise the
            lifecycle.
          </p>

          <div className="transition-row">
            <input
              value={reason}
              onChange={(event) => setReason(event.target.value)}
              placeholder="Reason, recorded on the event"
              aria-label="Reason"
            />
            <div className="button-row">
              {allowedTransitions.map((status) => (
                <button
                  key={status}
                  className="secondary"
                  disabled={updateStatus.isPending}
                  onClick={() => move(status)}
                >
                  {STATUS_LABELS[status]}
                </button>
              ))}
            </div>
          </div>
        </section>
      )}

      <section>
        <h2>Pipeline metrics</h2>
        <MetricsPanel metrics={metrics} />
      </section>

      <section>
        <h2>Timeline</h2>
        <DeploymentTimeline events={events} />
      </section>
    </AppShell>
  )
}
