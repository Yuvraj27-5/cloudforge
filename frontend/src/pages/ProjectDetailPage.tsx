import { useState } from "react"
import { Link, useParams } from "react-router-dom"

import { ApiError } from "../api/client"
import { useCreateDeployment, useDeployments } from "../api/deployments"
import { useDeleteProject, useProject } from "../api/projects"
import AppShell from "../components/AppShell"
import Badge from "../components/Badge"
import Field from "../components/Field"
import StatusPill from "../components/StatusPill"
import { ENVIRONMENT_LABELS, PROVIDER_LABELS } from "../types/project"

export default function ProjectDetailPage() {
  const { id = "" } = useParams()
  const { data: project, isPending, isError, error } = useProject(id)
  const { data: deploymentPage } = useDeployments(id)
  const deleteProject = useDeleteProject()
  const createDeployment = useCreateDeployment(id)

  const [formOpen, setFormOpen] = useState(false)
  const [commitSha, setCommitSha] = useState("")
  const [commitMessage, setCommitMessage] = useState("")

  const apiError = createDeployment.error instanceof ApiError ? createDeployment.error : undefined
  const deployments = deploymentPage?.content ?? []

  if (isPending) {
    return (
      <AppShell title="Project">
        <p>Loading…</p>
      </AppShell>
    )
  }

  if (isError) {
    return (
      <AppShell title="Project">
        <div className="alert error" role="alert">
          {(error as Error).message}
        </div>
        <Link to="/projects">Back to projects</Link>
      </AppShell>
    )
  }

  function recordDeployment() {
    createDeployment.mutate(
      {
        commitSha,
        commitMessage: commitMessage || undefined,
        triggeredBy: "manual",
      },
      {
        onSuccess: () => {
          setCommitSha("")
          setCommitMessage("")
          setFormOpen(false)
        },
      },
    )
  }

  return (
    <AppShell
      title={project.name}
      context={`${PROVIDER_LABELS[project.cloudProvider]} · ${ENVIRONMENT_LABELS[project.environment]}`}
      action={!formOpen && <button onClick={() => setFormOpen(true)}>Record deployment</button>}
    >
      <section>
        <p>
          <Link to="/projects">Back to projects</Link>
        </p>

        {project.description && <p>{project.description}</p>}

        <div className="panel">
          <dl className="detail-grid">
            <dt>Repository</dt>
            <dd className="mono">{project.repositoryUrl}</dd>

            <dt>Default branch</dt>
            <dd className="mono">{project.defaultBranch}</dd>

            <dt>Deployment target</dt>
            <dd>{PROVIDER_LABELS[project.cloudProvider]}</dd>

            <dt>Environment</dt>
            <dd>
              <Badge emphasis={project.environment === "PRODUCTION" ? "production" : undefined}>
                {ENVIRONMENT_LABELS[project.environment]}
              </Badge>
            </dd>

            <dt>Project ID</dt>
            <dd className="mono">{project.id}</dd>

            <dt>Added</dt>
            <dd>{new Date(project.createdAt).toLocaleString()}</dd>
          </dl>
        </div>
      </section>

      {formOpen && (
        <section>
          <div className="panel">
            <div className="panel-head">
              <div>
                <h2>Record a deployment</h2>
                <p>Phase 4 does this automatically from the pipeline.</p>
              </div>
            </div>

            {apiError && apiError.fieldErrors.length === 0 && (
              <div className="alert error" role="alert">
                {apiError.message}
              </div>
            )}

            <div className="form-grid">
              <Field
                label="Commit SHA"
                htmlFor="commitSha"
                error={apiError?.forField("commitSha")}
              >
                <input
                  id="commitSha"
                  value={commitSha}
                  aria-invalid={Boolean(apiError?.forField("commitSha"))}
                  onChange={(event) => setCommitSha(event.target.value)}
                  placeholder="a1b2c3d4e5f6"
                />
              </Field>

              <Field label="Commit message" htmlFor="commitMessage">
                <input
                  id="commitMessage"
                  value={commitMessage}
                  onChange={(event) => setCommitMessage(event.target.value)}
                  placeholder="What changed"
                />
              </Field>
            </div>

            <div className="button-row">
              <button onClick={recordDeployment} disabled={createDeployment.isPending}>
                {createDeployment.isPending ? "Recording…" : "Record deployment"}
              </button>
              <button className="secondary" onClick={() => setFormOpen(false)}>
                Cancel
              </button>
            </div>
          </div>
        </section>
      )}

      <section>
        <h2>Deployment history</h2>

        {deployments.length === 0 ? (
          <div className="empty">
            <h3>Nothing deployed yet</h3>
            <p>Record a deployment to start building this project&rsquo;s history.</p>
          </div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Commit</th>
                <th>Status</th>
                <th>Triggered by</th>
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
                    <StatusPill status={deployment.status} />
                  </td>
                  <td className="subtle">{deployment.triggeredBy ?? "—"}</td>
                  <td className="subtle">
                    {new Date(deployment.createdAt).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section>
        <div className="button-row">
          <button
            className="destructive"
            onClick={() => deleteProject.mutate(project.id)}
            disabled={deleteProject.isPending}
          >
            {deleteProject.isPending ? "Removing…" : "Remove project"}
          </button>
        </div>
      </section>
    </AppShell>
  )
}
