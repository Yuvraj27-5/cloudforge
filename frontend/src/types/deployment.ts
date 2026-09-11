import type { RiskAssessment } from "./risk"

export const DEPLOYMENT_STATUSES = [
  "PENDING",
  "RUNNING",
  "SUCCEEDED",
  "FAILED",
  "BLOCKED",
  "ROLLED_BACK",
  "CANCELLED",
] as const

export type DeploymentStatus = (typeof DEPLOYMENT_STATUSES)[number]

export type Deployment = {
  id: string
  projectId: string
  projectName: string
  commitSha: string
  shortSha: string
  commitMessage: string | null
  triggeredBy: string | null
  status: DeploymentStatus
  correlationId: string
  startedAt: string | null
  completedAt: string | null
  createdAt: string
}

export type DeploymentEvent = {
  id: string
  eventType: "CREATED" | "STATUS_CHANGED"
  fromStatus: DeploymentStatus | null
  toStatus: DeploymentStatus | null
  reason: string | null
  actor: string | null
  occurredAt: string
}

export type DeploymentMetrics = {
  filesChanged: number | null
  linesAdded: number | null
  linesDeleted: number | null
  testPassRate: number | null
  testCoverage: number | null
  codeComplexity: number | null
  codeSmells: number | null
  bugs: number | null
  securityHotspots: number | null
  criticalVulnerabilities: number | null
  highVulnerabilities: number | null
  mediumVulnerabilities: number | null
  lowVulnerabilities: number | null
  hasBlockingVulnerabilities: boolean
  source: string
  recordedAt: string
}

export type DeploymentDetail = {
  deployment: Deployment
  events: DeploymentEvent[]
  /** The backend owns the state machine; the UI renders whatever it allows. */
  allowedTransitions: DeploymentStatus[]
  /** Null until the pipeline reports. Absent is not the same as all-zero. */
  metrics: DeploymentMetrics | null
  /** Null until assessed. Absence blocks the deployment from starting. */
  riskAssessment: RiskAssessment | null
}

export type CreateDeploymentRequest = {
  commitSha: string
  commitMessage?: string
  triggeredBy?: string
}

export const STATUS_LABELS: Record<DeploymentStatus, string> = {
  PENDING: "Pending",
  RUNNING: "Running",
  SUCCEEDED: "Succeeded",
  FAILED: "Failed",
  BLOCKED: "Blocked",
  ROLLED_BACK: "Rolled back",
  CANCELLED: "Cancelled",
}

/** Maps to the status-* CSS classes. */
export const STATUS_TONE: Record<DeploymentStatus, string> = {
  PENDING: "neutral",
  RUNNING: "active",
  SUCCEEDED: "good",
  FAILED: "bad",
  BLOCKED: "warn",
  ROLLED_BACK: "warn",
  CANCELLED: "neutral",
}
