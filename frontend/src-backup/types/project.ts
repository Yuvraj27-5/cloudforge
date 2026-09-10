export const CLOUD_PROVIDERS = ["LOCAL_KUBERNETES", "AWS", "AZURE"] as const
export const ENVIRONMENTS = ["DEVELOPMENT", "STAGING", "PRODUCTION"] as const

export type CloudProvider = (typeof CLOUD_PROVIDERS)[number]
export type Environment = (typeof ENVIRONMENTS)[number]

export type Project = {
  id: string
  name: string
  repositoryUrl: string
  defaultBranch: string
  cloudProvider: CloudProvider
  environment: Environment
  description: string | null
  createdAt: string
  updatedAt: string
}

export type CreateProjectRequest = {
  name: string
  repositoryUrl: string
  defaultBranch?: string
  cloudProvider: CloudProvider
  environment: Environment
  description?: string
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

/** Labels for display. The wire format stays the backend enum. */
export const PROVIDER_LABELS: Record<CloudProvider, string> = {
  LOCAL_KUBERNETES: "Local Kubernetes",
  AWS: "AWS",
  AZURE: "Azure",
}

export const ENVIRONMENT_LABELS: Record<Environment, string> = {
  DEVELOPMENT: "Development",
  STAGING: "Staging",
  PRODUCTION: "Production",
}
