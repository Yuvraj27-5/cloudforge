const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "/api/v1"

/** One field-level validation failure from the backend. */
export type FieldError = {
  field: string
  message: string
}

/** RFC 9457 Problem Details, as produced by GlobalExceptionHandler. */
export type ProblemDetail = {
  type?: string
  title?: string
  status: number
  detail?: string
  instance?: string
  errors?: FieldError[]
}

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: FieldError[]

  constructor(problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Request failed (${problem.status})`)
    this.name = "ApiError"
    this.status = problem.status
    this.fieldErrors = problem.errors ?? []
  }

  /** Message for a specific field, if the backend rejected it. */
  forField(field: string): string | undefined {
    return this.fieldErrors.find((error) => error.field === field)?.message
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  })

  if (!response.ok) {
    // Every error path in the backend returns Problem Details, but a proxy or
    // network failure may not, so fall back to a synthetic one.
    let problem: ProblemDetail
    try {
      problem = (await response.json()) as ProblemDetail
    } catch {
      problem = { status: response.status, detail: response.statusText }
    }
    throw new ApiError({ ...problem, status: problem.status ?? response.status })
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: "PUT", body: JSON.stringify(body) }),
  delete: (path: string) => request<void>(path, { method: "DELETE" }),
}
