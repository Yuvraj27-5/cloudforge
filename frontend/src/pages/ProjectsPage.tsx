import { useState } from "react"
import { Link } from "react-router-dom"

import { useCreateProject, useProjects } from "../api/projects"
import { ApiError } from "../api/client"
import Field from "../components/Field"
import StatusMessage from "../components/StatusMessage"
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

  const apiError = createProject.error instanceof ApiError ? createProject.error : undefined

  function handleSubmit() {
    createProject.mutate(
      {
        name: form.name,
        repositoryUrl: form.repositoryUrl,
        defaultBranch: form.defaultBranch || undefined,
        cloudProvider: form.cloudProvider,
        environment: form.environment,
        description: form.description || undefined,
      },
      { onSuccess: () => setForm(EMPTY_FORM) },
    )
  }

  return (
    <div className="stack">
      <section>
        <h2>New project</h2>

        <div className="form-grid">
          <Field label="Name" htmlFor="name" error={apiError?.forField("name")}>
            <input
              id="name"
              value={form.name}
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
              placeholder="Optional"
            />
          </Field>
        </div>

        <button onClick={handleSubmit} disabled={createProject.isPending}>
          {createProject.isPending ? "Creating…" : "Create project"}
        </button>

        {apiError && apiError.fieldErrors.length === 0 && (
          <StatusMessage kind="error">{apiError.message}</StatusMessage>
        )}
      </section>

      <section>
        <h2>Projects</h2>

        {isPending && <StatusMessage kind="loading">Loading…</StatusMessage>}

        {isError && (
          <StatusMessage kind="error">
            Could not load projects: {(error as Error).message}
          </StatusMessage>
        )}

        {data && data.content.length === 0 && (
          <StatusMessage kind="empty">No projects yet. Create one above.</StatusMessage>
        )}

        {data && data.content.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Provider</th>
                <th>Environment</th>
                <th>Branch</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((project) => (
                <tr key={project.id}>
                  <td>
                    <Link to={`/projects/${project.id}`}>{project.name}</Link>
                  </td>
                  <td>{PROVIDER_LABELS[project.cloudProvider]}</td>
                  <td>{ENVIRONMENT_LABELS[project.environment]}</td>
                  <td>
                    <code>{project.defaultBranch}</code>
                  </td>
                  <td>{new Date(project.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {data && data.totalElements > 0 && (
          <p className="muted">
            {data.totalElements} project{data.totalElements === 1 ? "" : "s"}
          </p>
        )}
      </section>
    </div>
  )
}
