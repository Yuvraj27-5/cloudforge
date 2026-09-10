import { useState } from "react"
import { Link } from "react-router-dom"

import { ApiError } from "../api/client"
import { useCreateProject, useProjects } from "../api/projects"
import AppShell from "../components/AppShell"
import Badge from "../components/Badge"
import Field from "../components/Field"
import TableSkeleton from "../components/TableSkeleton"
import {
  CLOUD_PROVIDERS,
  ENVIRONMENTS,
  ENVIRONMENT_LABELS,
  PROVIDER_LABELS,
  type CloudProvider,
  type Environment,
} from "../types/project"

const EMPTY_FORM = {
  name: "",
  repositoryUrl: "",
  defaultBranch: "",
  cloudProvider: "LOCAL_KUBERNETES" as CloudProvider,
  environment: "DEVELOPMENT" as Environment,
  description: "",
}

export default function ProjectsPage() {
  const { data, isPending, isError, error } = useProjects()
  const createProject = useCreateProject()
  const [form, setForm] = useState(EMPTY_FORM)
  const [formOpen, setFormOpen] = useState(false)

  const apiError = createProject.error instanceof ApiError ? createProject.error : undefined
  const projects = data?.content ?? []

  function submit() {
    createProject.mutate(
      {
        name: form.name,
        repositoryUrl: form.repositoryUrl,
        defaultBranch: form.defaultBranch || undefined,
        cloudProvider: form.cloudProvider,
        environment: form.environment,
        description: form.description || undefined,
      },
      {
        onSuccess: () => {
          setForm(EMPTY_FORM)
          setFormOpen(false)
        },
      },
    )
  }

  const count = data?.totalElements ?? 0

  return (
    <AppShell
      title="Projects"
      context={count > 0 ? `${count} tracked` : undefined}
      action={
        !formOpen && (
          <button onClick={() => setFormOpen(true)}>Add project</button>
        )
      }
    >
      {formOpen && (
        <section>
          <div className="panel">
            <div className="panel-head">
              <div>
                <h2>Add a project</h2>
                <p>CloudForge watches this repository and scores its deployments.</p>
              </div>
            </div>

            {apiError && apiError.fieldErrors.length === 0 && (
              <div className="alert error" role="alert">
                {apiError.message}
              </div>
            )}

            <div className="form-grid">
              <Field label="Name" htmlFor="name" error={apiError?.forField("name")}>
                <input
                  id="name"
                  value={form.name}
                  aria-invalid={Boolean(apiError?.forField("name"))}
                  onChange={(event) => setForm({ ...form, name: event.target.value })}
                  placeholder="payments-api"
                />
              </Field>

              <Field
                label="Repository URL"
                htmlFor="repositoryUrl"
                error={apiError?.forField("repositoryUrl")}
              >
                <input
                  id="repositoryUrl"
                  value={form.repositoryUrl}
                  aria-invalid={Boolean(apiError?.forField("repositoryUrl"))}
                  onChange={(event) => setForm({ ...form, repositoryUrl: event.target.value })}
                  placeholder="https://github.com/acme/payments-api"
                />
              </Field>

              <Field label="Default branch" htmlFor="defaultBranch">
                <input
                  id="defaultBranch"
                  value={form.defaultBranch}
                  onChange={(event) => setForm({ ...form, defaultBranch: event.target.value })}
                  placeholder="main"
                />
              </Field>

              <Field label="Cloud provider" htmlFor="cloudProvider">
                <select
                  id="cloudProvider"
                  value={form.cloudProvider}
                  onChange={(event) =>
                    setForm({ ...form, cloudProvider: event.target.value as CloudProvider })
                  }
                >
                  {CLOUD_PROVIDERS.map((provider) => (
                    <option key={provider} value={provider}>
                      {PROVIDER_LABELS[provider]}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Environment" htmlFor="environment">
                <select
                  id="environment"
                  value={form.environment}
                  onChange={(event) =>
                    setForm({ ...form, environment: event.target.value as Environment })
                  }
                >
                  {ENVIRONMENTS.map((environment) => (
                    <option key={environment} value={environment}>
                      {ENVIRONMENT_LABELS[environment]}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Description" htmlFor="description">
                <input
                  id="description"
                  value={form.description}
                  onChange={(event) => setForm({ ...form, description: event.target.value })}
                  placeholder="What this service does"
                />
              </Field>
            </div>

            <div className="button-row">
              <button onClick={submit} disabled={createProject.isPending}>
                {createProject.isPending ? "Adding…" : "Add project"}
              </button>
              <button
                className="secondary"
                onClick={() => {
                  setFormOpen(false)
                  setForm(EMPTY_FORM)
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        </section>
      )}

      <section>
        {isPending && <TableSkeleton />}

        {isError && (
          <div className="alert error" role="alert">
            Could not load projects. {(error as Error).message}
          </div>
        )}

        {data && projects.length === 0 && !formOpen && (
          <div className="empty">
            <h3>No projects yet</h3>
            <p>Add a repository and CloudForge will start scoring its deployments.</p>
            <button onClick={() => setFormOpen(true)}>Add project</button>
          </div>
        )}

        {projects.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Project</th>
                <th>Repository</th>
                <th>Target</th>
                <th>Environment</th>
                <th>Added</th>
              </tr>
            </thead>
            <tbody>
              {projects.map((project) => (
                <tr key={project.id}>
                  <td className="name">
                    <Link to={`/projects/${project.id}`}>{project.name}</Link>
                  </td>
                  <td className="repo mono">{shortRepo(project.repositoryUrl)}</td>
                  <td>{PROVIDER_LABELS[project.cloudProvider]}</td>
                  <td>
                    <Badge
                      emphasis={project.environment === "PRODUCTION" ? "production" : undefined}
                    >
                      {ENVIRONMENT_LABELS[project.environment]}
                    </Badge>
                  </td>
                  <td>{new Date(project.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </AppShell>
  )
}

/** github.com/acme/payments-api -> acme/payments-api */
function shortRepo(url: string): string {
  return url
    .replace(/^https?:\/\/[^/]+\//, "")
    .replace(/^git@[^:]+:/, "")
    .replace(/\.git$/, "")
}
