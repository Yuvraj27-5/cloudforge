import { Link, useParams } from "react-router-dom"

import { useDeleteProject, useProject } from "../api/projects"
import StatusMessage from "../components/StatusMessage"
import { ENVIRONMENT_LABELS, PROVIDER_LABELS } from "../types/project"

export default function ProjectDetailPage() {
  const { id = "" } = useParams()
  const { data: project, isPending, isError, error } = useProject(id)
  const deleteProject = useDeleteProject()

  if (isPending) {
    return <StatusMessage kind="loading">Loading…</StatusMessage>
  }

  if (isError) {
    return <StatusMessage kind="error">{(error as Error).message}</StatusMessage>
  }

  return (
    <div className="stack">
      <p>
        <Link to="/projects">← All projects</Link>
      </p>

      <h2>{project.name}</h2>
      {project.description && <p className="muted">{project.description}</p>}

      <dl className="detail-grid">
        <dt>Repository</dt>
        <dd>
          <code>{project.repositoryUrl}</code>
        </dd>

        <dt>Default branch</dt>
        <dd>
          <code>{project.defaultBranch}</code>
        </dd>

        <dt>Cloud provider</dt>
        <dd>{PROVIDER_LABELS[project.cloudProvider]}</dd>

        <dt>Environment</dt>
        <dd>{ENVIRONMENT_LABELS[project.environment]}</dd>

        <dt>Project ID</dt>
        <dd>
          <code>{project.id}</code>
        </dd>

        <dt>Created</dt>
        <dd>{new Date(project.createdAt).toLocaleString()}</dd>

        <dt>Updated</dt>
        <dd>{new Date(project.updatedAt).toLocaleString()}</dd>
      </dl>

      <section>
        <h3>Deployments</h3>
        <p className="muted">Deployment history arrives in Phase 2.</p>
      </section>

      <button
        className="danger"
        onClick={() => deleteProject.mutate(project.id)}
        disabled={deleteProject.isPending}
      >
        {deleteProject.isPending ? "Deleting…" : "Delete project"}
      </button>
    </div>
  )
}
