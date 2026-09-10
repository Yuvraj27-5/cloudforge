import { Link, useParams } from "react-router-dom"

import { useDeleteProject, useProject } from "../api/projects"
import AppShell from "../components/AppShell"
import Badge from "../components/Badge"
import { ENVIRONMENT_LABELS, PROVIDER_LABELS } from "../types/project"

export default function ProjectDetailPage() {
  const { id = "" } = useParams()
  const { data: project, isPending, isError, error } = useProject(id)
  const deleteProject = useDeleteProject()

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

  return (
    <AppShell
      title={project.name}
      context={PROVIDER_LABELS[project.cloudProvider]}
      action={
        <button
          className="destructive"
          onClick={() => deleteProject.mutate(project.id)}
          disabled={deleteProject.isPending}
        >
          {deleteProject.isPending ? "Removing…" : "Remove project"}
        </button>
      }
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

            <dt>Last updated</dt>
            <dd>{new Date(project.updatedAt).toLocaleString()}</dd>
          </dl>
        </div>
      </section>

      <section>
        <div className="upcoming">
          <h3>Deployments</h3>
          <p>
            Deployment history, risk scores and rollback events appear here once the pipeline
            stages are built.
          </p>
        </div>
      </section>
    </AppShell>
  )
}
